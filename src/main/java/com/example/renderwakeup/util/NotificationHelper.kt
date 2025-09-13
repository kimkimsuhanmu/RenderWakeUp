package com.example.renderwakeup.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.renderwakeup.MainActivity
import com.example.renderwakeup.R
import com.example.renderwakeup.data.model.UrlEntity

/**
 * 알림 관련 기능을 제공하는 헬퍼 클래스
 */
class NotificationHelper(private val context: Context) {

    companion object {
        // 알림 채널 ID
        const val CHANNEL_ID_FOREGROUND = "render_wakeup_foreground"
        const val CHANNEL_ID_PING_RESULT = "render_wakeup_ping_result"
        
        // 알림 ID
        const val NOTIFICATION_ID_FOREGROUND = 1001
        const val NOTIFICATION_ID_PING_RESULT = 2001
    }
    
    init {
        // 알림 채널 생성 (Android 8.0 이상)
        createNotificationChannels()
    }
    
    /**
     * 알림 채널을 생성합니다.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 포그라운드 서비스 알림 채널
            val foregroundChannel = NotificationChannel(
                CHANNEL_ID_FOREGROUND,
                "Foreground Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "앱이 백그라운드에서 실행 중임을 알리는 알림"
            }
            
            // 핑 결과 알림 채널
            val pingResultChannel = NotificationChannel(
                CHANNEL_ID_PING_RESULT,
                "Ping Results",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "URL 핑 결과 알림"
            }
            
            // 알림 채널 등록
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(foregroundChannel)
            notificationManager.createNotificationChannel(pingResultChannel)
        }
    }
    
    /**
     * 포그라운드 서비스 알림을 생성합니다.
     * 
     * @param activeCount 활성화된 URL 수
     * @return 알림 빌더
     */
    fun createForegroundNotification(activeCount: Int): NotificationCompat.Builder {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID_FOREGROUND)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("렌더웨이크 실행 중")
            .setContentText("$activeCount 개의 URL을 모니터링 중입니다")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }
    
    /**
     * 핑 실패 알림을 표시합니다.
     * 
     * @param url 핑 실패한 URL 엔티티
     * @param failCount 연속 실패 횟수
     */
    fun showPingFailureNotification(url: UrlEntity, failCount: Int) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PING_RESULT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("URL 핑 실패")
            .setContentText("${url.url} - $failCount 회 연속 실패")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_PING_RESULT + url.id.toInt(),
            notification
        )
    }
}
