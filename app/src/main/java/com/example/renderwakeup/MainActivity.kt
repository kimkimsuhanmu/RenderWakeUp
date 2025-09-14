package com.example.renderwakeup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
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
    
    // URL 목록을 표시할 RecyclerView 어댑터
    private lateinit var urlAdapter: UrlAdapter
    
    // URL 저장소
    private lateinit var urlRepository: UrlRepository
    
    // 코루틴 스코프
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
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
                
                val interval = intervalText.toIntOrNull() ?: 15
                if (interval < 15) {
                    Toast.makeText(this, R.string.error_invalid_interval, Toast.LENGTH_SHORT).show()
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
                    urlRepository.pingUrl(urlEntity)
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
                
                val interval = intervalText.toIntOrNull() ?: 15
                if (interval < 15) {
                    Toast.makeText(this, R.string.error_invalid_interval, Toast.LENGTH_SHORT).show()
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
            val success = urlRepository.pingUrl(urlEntity)
            val message = if (success) {
                getString(R.string.ping_success, urlEntity.url)
            } else {
                getString(R.string.ping_failure, urlEntity.url)
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
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