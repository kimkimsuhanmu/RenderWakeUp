package com.example.renderwakeup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.renderwakeup.data.model.PingStatus
import com.example.renderwakeup.data.model.UrlEntity
import com.example.renderwakeup.data.repository.UrlRepository
import com.example.renderwakeup.util.EmailConfigManager
import com.example.renderwakeup.util.EmailSender
import com.example.renderwakeup.worker.WorkManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 앱의 메인 액티비티
 * 사용자 인터페이스를 제공하고 백그라운드 서비스를 시작합니다.
 */
class MainActivity : AppCompatActivity() {
    
    // 태그
    private val TAG = "MainActivity"
    
    // URL 목록을 표시할 RecyclerView 어댑터
    private lateinit var urlAdapter: UrlAdapter
    
    // URL 저장소
    private lateinit var urlRepository: UrlRepository
    
    // 이메일 설정 관리자
    private lateinit var emailConfigManager: EmailConfigManager
    
    // 이메일 전송 유틸리티
    private val emailSender = EmailSender()
    
    // 코루틴 스코프
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // 최소 및 최대 핑 간격 (분)
    private val MIN_INTERVAL = 1
    private val MAX_INTERVAL = 600
    
    // 알림 권한 요청 결과 처리
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 승인되면 WorkManager 작업 예약
            WorkManagerHelper.schedulePeriodicPingWork(this)
        } else {
            // 권한이 거부되면 사용자에게 알림
            AlertDialog.Builder(this)
                .setTitle(R.string.notification_permission_title)
                .setMessage(R.string.notification_permission_message)
                .setPositiveButton(R.string.notification_permission_settings) { _, _ ->
                    // 앱 설정 화면으로 이동
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // URL 저장소 초기화
        urlRepository = UrlRepository(
            (application as RenderWakeUpApplication).database.urlDao()
        )
        
        // 이메일 설정 관리자 초기화
        emailConfigManager = EmailConfigManager(this)
        
        // RecyclerView 설정
        setupRecyclerView()
        
        // URL 추가 버튼 설정
        findViewById<View>(R.id.btnAddUrl).setOnClickListener {
            showAddUrlDialog()
        }
        
        // 배터리 최적화 확인
        checkBatteryOptimization()
        
        // 알림 권한 확인 및 요청
        checkNotificationPermission()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_email_settings -> {
                showEmailSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        val rvUrlList = findViewById<RecyclerView>(R.id.rvUrlList)
        urlAdapter = UrlAdapter(
            onPingNow = { url ->
                pingUrl(url)
            },
            onEdit = { url ->
                showEditUrlDialog(url)
            },
            onDelete = { url ->
                showDeleteConfirmDialog(url)
            }
        )
        
        rvUrlList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = urlAdapter
        }
        
        // URL 목록 관찰
        urlRepository.getAllUrls().observe(this, Observer { urls ->
            urlAdapter.submitList(urls)
            
            // 빈 목록 메시지 표시/숨김
            findViewById<View>(R.id.tvEmptyList).visibility = 
                if (urls.isEmpty()) View.VISIBLE else View.GONE
        })
    }
    
    /**
     * URL 추가 다이얼로그 표시
     */
    private fun showAddUrlDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_url, null)
        val etUrl = dialogView.findViewById<EditText>(R.id.etUrl)
        val etInterval = dialogView.findViewById<EditText>(R.id.etInterval)
        val cbEmailNotification = dialogView.findViewById<CheckBox>(R.id.cbEmailNotification)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        
        // 힌트 업데이트
        etInterval.hint = getString(R.string.hint_interval_range, MIN_INTERVAL, MAX_INTERVAL)
        
        // 이메일 알림 체크박스 변경 시 이메일 입력란 표시/숨김
        cbEmailNotification.setOnCheckedChangeListener { _, isChecked ->
            etEmail.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_url_title)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val url = etUrl.text.toString().trim()
                val intervalText = etInterval.text.toString().trim()
                val emailEnabled = cbEmailNotification.isChecked
                val email = etEmail.text.toString().trim()
                
                if (url.isEmpty()) {
                    Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val interval = intervalText.toIntOrNull() ?: MIN_INTERVAL
                if (interval < MIN_INTERVAL || interval > MAX_INTERVAL) {
                    Toast.makeText(this, getString(R.string.error_invalid_interval_range, MIN_INTERVAL, MAX_INTERVAL), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (emailEnabled && (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
                    Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                // URL 추가
                val urlEntity = UrlEntity(
                    url = url,
                    interval = interval,
                    emailNotification = emailEnabled,
                    emailAddress = if (emailEnabled) email else null
                )
                
                coroutineScope.launch {
                    urlRepository.insertUrl(urlEntity)
                    
                    // WorkManager 작업 예약
                    WorkManagerHelper.schedulePeriodicPingWork(this@MainActivity)
                    
                    // 즉시 핑 전송
                    val pingResult = urlRepository.pingUrl(urlEntity)
                    Log.d(TAG, "Ping result for ${urlEntity.url}: $pingResult")
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    /**
     * URL 수정 다이얼로그 표시
     */
    private fun showEditUrlDialog(urlEntity: UrlEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_url, null)
        val etUrl = dialogView.findViewById<EditText>(R.id.etUrl)
        val etInterval = dialogView.findViewById<EditText>(R.id.etInterval)
        val cbEmailNotification = dialogView.findViewById<CheckBox>(R.id.cbEmailNotification)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        
        // 힌트 업데이트
        etInterval.hint = getString(R.string.hint_interval_range, MIN_INTERVAL, MAX_INTERVAL)
        
        // 기존 값 설정
        etUrl.setText(urlEntity.url)
        etInterval.setText(urlEntity.interval.toString())
        cbEmailNotification.isChecked = urlEntity.emailNotification
        etEmail.setText(urlEntity.emailAddress ?: "")
        etEmail.visibility = if (urlEntity.emailNotification) View.VISIBLE else View.GONE
        
        // 이메일 알림 체크박스 변경 시 이메일 입력란 표시/숨김
        cbEmailNotification.setOnCheckedChangeListener { _, isChecked ->
            etEmail.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_edit_url_title)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val url = etUrl.text.toString().trim()
                val intervalText = etInterval.text.toString().trim()
                val emailEnabled = cbEmailNotification.isChecked
                val email = etEmail.text.toString().trim()
                
                if (url.isEmpty()) {
                    Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val interval = intervalText.toIntOrNull() ?: MIN_INTERVAL
                if (interval < MIN_INTERVAL || interval > MAX_INTERVAL) {
                    Toast.makeText(this, getString(R.string.error_invalid_interval_range, MIN_INTERVAL, MAX_INTERVAL), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (emailEnabled && (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
                    Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                // URL 수정
                val updatedUrl = urlEntity.copy(
                    url = url,
                    interval = interval,
                    emailNotification = emailEnabled,
                    emailAddress = if (emailEnabled) email else null,
                    updatedAt = Date()
                )
                
                coroutineScope.launch {
                    urlRepository.updateUrl(updatedUrl)
                    
                    // WorkManager 작업 예약
                    WorkManagerHelper.schedulePeriodicPingWork(this@MainActivity)
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    /**
     * URL 삭제 확인 다이얼로그 표시
     */
    private fun showDeleteConfirmDialog(urlEntity: UrlEntity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_url_title)
            .setMessage(R.string.dialog_delete_url_message)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                coroutineScope.launch {
                    urlRepository.deleteUrl(urlEntity)
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    /**
     * URL에 즉시 핑 전송
     */
    private fun pingUrl(urlEntity: UrlEntity) {
        coroutineScope.launch {
            Log.d(TAG, "Sending ping to ${urlEntity.url}")
            val success = urlRepository.pingUrl(urlEntity)
            Log.d(TAG, "Ping result: $success")
            
            val message = if (success) {
                getString(R.string.ping_success, urlEntity.url)
            } else {
                getString(R.string.ping_failure, urlEntity.url)
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 이메일 설정 다이얼로그 표시
     */
    private fun showEmailSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email_settings, null)
        
        // 현재 설정 가져오기
        val currentConfig = emailConfigManager.getEmailConfig()
        
        // 뷰 참조
        val cbEnableEmail = dialogView.findViewById<CheckBox>(R.id.cbEnableEmail)
        val etSmtpHost = dialogView.findViewById<EditText>(R.id.etSmtpHost)
        val etSmtpPort = dialogView.findViewById<EditText>(R.id.etSmtpPort)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnTestEmail = dialogView.findViewById<View>(R.id.btnTestEmail)
        
        // 현재 설정 적용
        cbEnableEmail.isChecked = currentConfig.enabled
        etSmtpHost.setText(currentConfig.host)
        etSmtpPort.setText(currentConfig.port)
        etUsername.setText(currentConfig.username)
        etPassword.setText(currentConfig.password)
        
        // 테스트 이메일 버튼 클릭 리스너
        btnTestEmail.setOnClickListener {
            val config = EmailConfigManager.EmailConfig(
                host = etSmtpHost.text.toString(),
                port = etSmtpPort.text.toString(),
                username = etUsername.text.toString(),
                password = etPassword.text.toString(),
                enabled = cbEnableEmail.isChecked
            )
            
            if (!config.enabled) {
                Toast.makeText(this, "이메일 알림이 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (config.host.isEmpty() || config.port.isEmpty() || config.username.isEmpty() || config.password.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 이메일 전송 테스트
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val smtpConfig = EmailSender.SmtpConfig(
                        host = config.host,
                        port = config.port,
                        username = config.username,
                        password = config.password
                    )
                    
                    val success = emailSender.sendEmail(
                        to = config.username,
                        subject = "렌더웨이크 이메일 테스트",
                        body = "이 이메일은 렌더웨이크 앱의 이메일 설정이 올바르게 구성되었는지 확인하기 위한 테스트 메시지입니다.",
                        smtpConfig = smtpConfig
                    )
                    
                    launch(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@MainActivity, "테스트 이메일이 성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "테스트 이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending test email", e)
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "테스트 이메일 전송 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        // 다이얼로그 표시
        AlertDialog.Builder(this)
            .setTitle("이메일 설정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                // 설정 저장
                val config = EmailConfigManager.EmailConfig(
                    host = etSmtpHost.text.toString(),
                    port = etSmtpPort.text.toString(),
                    username = etUsername.text.toString(),
                    password = etPassword.text.toString(),
                    enabled = cbEnableEmail.isChecked
                )
                
                emailConfigManager.saveEmailConfig(config)
                Toast.makeText(this, "이메일 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    /**
     * 배터리 최적화 제외 여부를 확인하고, 필요하면 사용자에게 설정을 요청합니다.
     */
    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            val packageName = packageName
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                // 배터리 최적화 제외 대화상자 표시
                AlertDialog.Builder(this)
                    .setTitle(R.string.battery_optimization_title)
                    .setMessage(R.string.battery_optimization_message)
                    .setPositiveButton(R.string.battery_optimization_btn) { _, _ ->
                        // 배터리 최적화 설정 화면으로 이동
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }
    
    /**
     * Android 13 이상에서 알림 권한을 확인하고, 필요하면 사용자에게 요청합니다.
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 이미 승인됨
                    WorkManagerHelper.schedulePeriodicPingWork(this)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 권한 요청 이유 설명
                    AlertDialog.Builder(this)
                        .setTitle(R.string.notification_permission_title)
                        .setMessage(R.string.notification_permission_rationale)
                        .setPositiveButton(R.string.notification_permission_request) { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 13 미만에서는 권한 요청 없이 WorkManager 작업 예약
            WorkManagerHelper.schedulePeriodicPingWork(this)
        }
    }
}