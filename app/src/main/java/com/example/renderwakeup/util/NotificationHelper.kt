package com.example.renderwakeup.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.renderwakeup.MainActivity
import com.example.renderwakeup.R
import com.example.renderwakeup.data.model.UrlEntity

/**
 * 알림 관련 기능을 제공하는 헬퍼 클래스
 * 
 * @property context 애플리케이션 컨텍스트
 */
class NotificationHelper(private val context: Context) {
    
    // NotificationManager 인스턴스
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        // Android Oreo 이상에서는 NotificationChannel 생성 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 포그라운드 서비스 채널 생성
            val foregroundChannel = NotificationChannel(
                CHANNEL_ID_FOREGROUND,
                context.getString(R.string.notification_channel_foreground),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_foreground_desc)
                setShowBadge(false)
            }
            
            // 알림 채널 생성
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERT,
                context.getString(R.string.notification_channel_alert),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_alert_desc)
                setShowBadge(true)
            }
            
            // 채널 등록
            notificationManager.createNotificationChannels(listOf(foregroundChannel, alertChannel))
        }
    }
    
    /**
     * 포그라운드 서비스 알림 빌더를 생성합니다.
     * 
     * @param activeCount 활성화된 URL 수
     * @return NotificationCompat.Builder 인스턴스
     */
    fun createForegroundNotification(activeCount: Int): NotificationCompat.Builder {
        // 메인 액티비티로 이동하는 PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 알림 내용 설정
        val contentText = if (activeCount > 0) {
            context.getString(R.string.notification_foreground_active, activeCount)
        } else {
            context.getString(R.string.notification_foreground_idle)
        }
        
        // 알림 빌더 생성
        return NotificationCompat.Builder(context, CHANNEL_ID_FOREGROUND)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
    }
    
    /**
     * URL 핑 실패 알림을 표시합니다.
     * 
     * @param url 실패한 URL 엔티티
     * @param failCount 실패 횟수
     */
    fun showPingFailureNotification(url: UrlEntity, failCount: Int) {
        // 메인 액티비티로 이동하는 PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            context,
            url.id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 알림 빌더 생성
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_ping_failure_title))
            .setContentText(context.getString(R.string.notification_ping_failure_text, url.url, failCount))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // 알림 표시
        notificationManager.notify(NOTIFICATION_ID_ALERT_BASE + url.id.toInt(), notification)
    }
    
    companion object {
        // 알림 채널 ID
        const val CHANNEL_ID_FOREGROUND = "com.example.renderwakeup.FOREGROUND"
        const val CHANNEL_ID_ALERT = "com.example.renderwakeup.ALERT"
        
        // 알림 ID
        const val NOTIFICATION_ID_FOREGROUND = 1
        const val NOTIFICATION_ID_ALERT_BASE = 1000
    }
}
