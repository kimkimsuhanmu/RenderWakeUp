package com.example.renderwakeup.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.renderwakeup.data.db.AppDatabase
import com.example.renderwakeup.data.model.PingStatus
import com.example.renderwakeup.data.repository.UrlRepository
import com.example.renderwakeup.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager를 사용하여 주기적으로 URL 핑을 수행하는 Worker 클래스
 * 
 * @property context 애플리케이션 컨텍스트
 * @property params Worker 파라미터
 */
class WakeUpWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // 로그 태그
    private val TAG = "WakeUpWorker"
    
    // 데이터베이스 및 Repository 초기화
    private val database = AppDatabase.getInstance(context)
    private val urlRepository = UrlRepository(database.urlDao())
    
    // 알림 헬퍼 초기화
    private val notificationHelper = NotificationHelper(context)
    
    /**
     * 백그라운드 작업을 수행합니다.
     * 
     * @return 작업 결과
     */
    override suspend fun doWork(): Result {
        Log.d(TAG, "WakeUpWorker started")
        
        try {
            // 포그라운드 서비스로 실행
            setForeground(createForegroundInfo(0))
            
            // 핑이 필요한 URL 목록 조회
            val urlsNeedingPing = urlRepository.getUrlsNeedingPing()
            Log.d(TAG, "Found ${urlsNeedingPing.size} URLs needing ping")
            
            // 포그라운드 알림 업데이트
            setForeground(createForegroundInfo(urlsNeedingPing.size))
            
            // 각 URL에 핑 요청 보내기
            var successCount = 0
            
            for (url in urlsNeedingPing) {
                val isSuccess = urlRepository.pingUrl(url)
                
                if (isSuccess) {
                    successCount++
                    Log.d(TAG, "Ping success: ${url.url}")
                } else {
                    Log.w(TAG, "Ping failed: ${url.url}, fail count: ${url.failCount + 1}")
                    
                    // 연속 3회 이상 실패 시 알림 표시
                    if (url.failCount + 1 >= 3) {
                        notificationHelper.showPingFailureNotification(url, url.failCount + 1)
                    }
                }
            }
            
            // 작업 결과 데이터 설정
            val outputData = workDataOf(
                "total_count" to urlsNeedingPing.size,
                "success_count" to successCount
            )
            
            Log.d(TAG, "WakeUpWorker completed: $successCount/${urlsNeedingPing.size} successful")
            return Result.success(outputData)
            
        } catch (e: Exception) {
            Log.e(TAG, "WakeUpWorker failed", e)
            return Result.failure()
        }
    }
    
    /**
     * 포그라운드 서비스 정보를 생성합니다.
     * 
     * @param activeCount 활성화된 URL 수
     * @return 포그라운드 서비스 정보
     */
    private fun createForegroundInfo(activeCount: Int): ForegroundInfo {
        val notification = notificationHelper.createForegroundNotification(activeCount).build()
        
        return ForegroundInfo(
            NotificationHelper.NOTIFICATION_ID_FOREGROUND,
            notification
        )
    }
    
    companion object {
        // 작업 고유 이름
        const val WORK_NAME = "com.example.renderwakeup.WAKE_UP_WORKER"
    }
}
