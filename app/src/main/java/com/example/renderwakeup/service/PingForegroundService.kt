package com.example.renderwakeup.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.renderwakeup.R
import com.example.renderwakeup.data.db.AppDatabase
import com.example.renderwakeup.data.repository.UrlRepository
import com.example.renderwakeup.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * URL 핑을 주기적으로 보내는 포그라운드 서비스
 * WorkManager의 제약을 우회하여 더 빈번한 핑 주기를 가능하게 합니다.
 */
class PingForegroundService : Service() {

    private val TAG = "PingForegroundService"
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var pingJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    private lateinit var urlRepository: UrlRepository
    private lateinit var notificationHelper: NotificationHelper
    private var wakeLock: PowerManager.WakeLock? = null
    
    // 디버깅용 카운터
    private var pingCount = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        // 저장소 초기화
        val database = AppDatabase.getInstance(this)
        urlRepository = UrlRepository(database.urlDao())
        
        // 알림 헬퍼 초기화
        notificationHelper = NotificationHelper(this)
        
        // WakeLock 획득
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        // 포그라운드 서비스 시작
        val notification = notificationHelper.createForegroundNotification(0).build()
        startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND, notification)
        
        // 이미 실행 중이 아니면 핑 작업 시작
        if (isRunning.compareAndSet(false, true)) {
            startPingJob()
        }
        
        // 서비스가 강제 종료되면 다시 시작
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        
        // 작업 중지
        stopPingJob()
        
        // WakeLock 해제
        releaseWakeLock()
        
        super.onDestroy()
    }

    /**
     * 주기적인 핑 작업을 시작합니다.
     */
    private fun startPingJob() {
        pingJob = serviceScope.launch {
            Log.d(TAG, "Starting ping job")
            
            while (isRunning.get()) {
                try {
                    pingCount++
                    Log.d(TAG, "Ping job iteration #$pingCount")
                    
                    // 핑이 필요한 URL 목록 조회 및 핑 전송
                    val urlsNeedingPing = urlRepository.getUrlsNeedingPing()
                    Log.d(TAG, "Found ${urlsNeedingPing.size} URLs needing ping")
                    
                    // 포그라운드 알림 업데이트
                    updateNotification(urlsNeedingPing.size)
                    
                    // 각 URL에 핑 요청 보내기
                    var successCount = 0
                    for (url in urlsNeedingPing) {
                        try {
                            val isSuccess = urlRepository.pingUrl(url)
                            if (isSuccess) {
                                successCount++
                                Log.d(TAG, "Ping to ${url.url} succeeded")
                            } else {
                                Log.e(TAG, "Ping to ${url.url} failed")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error pinging ${url.url}", e)
                        }
                    }
                    
                    // 핑 결과 로그
                    Log.d(TAG, "Ping results: $successCount/${urlsNeedingPing.size} successful")
                    
                    // 만약 URL이 없으면 모든 URL 확인
                    if (urlsNeedingPing.isEmpty()) {
                        val allUrls = urlRepository.getAllUrlsSync()
                        if (allUrls.isEmpty()) {
                            Log.d(TAG, "No URLs found in database")
                        } else {
                            Log.d(TAG, "Found ${allUrls.size} URLs in database, but none need ping yet")
                            for (url in allUrls) {
                                Log.d(TAG, "URL: ${url.url}, interval: ${url.interval}, lastPing: ${url.lastPingTime}")
                            }
                        }
                    }
                    
                    // 최소 30초 대기 (배터리 소모 방지)
                    delay(30_000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in ping job", e)
                    delay(60_000) // 오류 발생 시 1분 대기 후 재시도
                }
            }
        }
    }

    /**
     * 핑 작업을 중지합니다.
     */
    private fun stopPingJob() {
        isRunning.set(false)
        pingJob?.cancel()
        pingJob = null
    }

    /**
     * 포그라운드 알림을 업데이트합니다.
     */
    private fun updateNotification(activeCount: Int) {
        val notification = notificationHelper.createForegroundNotification(activeCount).build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_ID_FOREGROUND, notification)
    }

    /**
     * WakeLock을 획득합니다.
     */
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "RenderWakeUp:PingServiceWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L) // 10분 동안 WakeLock 유지
        }
    }

    /**
     * WakeLock을 해제합니다.
     */
    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            wakeLock = null
        }
    }

    companion object {
        /**
         * 서비스를 시작합니다.
         */
        fun startService(context: Context) {
            val intent = Intent(context, PingForegroundService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * 서비스를 중지합니다.
         */
        fun stopService(context: Context) {
            val intent = Intent(context, PingForegroundService::class.java)
            context.stopService(intent)
        }
    }
}