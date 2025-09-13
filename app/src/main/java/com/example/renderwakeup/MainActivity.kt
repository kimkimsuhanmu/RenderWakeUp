package com.example.renderwakeup

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.renderwakeup.worker.WorkManagerHelper

/**
 * 앱의 메인 액티비티
 * 사용자 인터페이스를 제공하고 백그라운드 서비스를 시작합니다.
 */
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 배터리 최적화 확인
        checkBatteryOptimization()
        
        // WorkManager 작업 예약
        WorkManagerHelper.schedulePeriodicPingWork(this)
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
}
