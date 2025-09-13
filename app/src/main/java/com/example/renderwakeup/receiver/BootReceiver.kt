package com.example.renderwakeup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.renderwakeup.worker.WorkManagerHelper

/**
 * 기기 부팅 시 자동으로 실행되는 BroadcastReceiver
 * WorkManager를 다시 예약하여 앱이 백그라운드에서 계속 실행되도록 합니다.
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, scheduling WorkManager tasks")
            
            // WorkManager 작업 예약
            WorkManagerHelper.schedulePeriodicPingWork(context)
        }
    }
}
