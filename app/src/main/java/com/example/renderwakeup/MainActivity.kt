package com.example.renderwakeup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.renderwakeup.worker.WorkManagerHelper

/**
 * 앱의 메인 액티비티
 * 사용자 인터페이스를 제공하고 백그라운드 서비스를 시작합니다.
 */
class MainActivity : AppCompatActivity() {
    
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
        
        // 배터리 최적화 확인
        checkBatteryOptimization()
        
        // 알림 권한 확인 및 요청
        checkNotificationPermission()
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