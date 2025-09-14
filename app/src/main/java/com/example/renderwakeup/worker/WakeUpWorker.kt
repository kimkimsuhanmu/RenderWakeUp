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
import com.example.renderwakeup.util.EmailConfigManager
import com.example.renderwakeup.util.EmailSender
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
    
    // 이메일 관련 클래스 초기화
    private val emailConfigManager = EmailConfigManager(context)
    private val emailSender = EmailSender()
    
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
                        // 알림 표시
                        notificationHelper.showPingFailureNotification(url, url.failCount + 1)
                        
                        // 이메일 알림 전송
                        sendEmailNotification(url, url.failCount + 1)
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
     * 이메일 알림을 전송합니다.
     * 
     * @param url 실패한 URL 엔티티
     * @param failCount 실패 횟수
     */
    private suspend fun sendEmailNotification(url: UrlEntity, failCount: Int) {
        // 이메일 알림이 활성화되어 있고 이메일 주소가 설정되어 있는 경우에만 전송
        if (!url.emailNotification || url.emailAddress.isNullOrEmpty()) {
            Log.d(TAG, "Email notification not enabled for ${url.url}")
            return
        }
        
        // 이메일 설정 가져오기
        val smtpConfig = emailConfigManager.getSmtpConfig()
        if (smtpConfig == null) {
            Log.w(TAG, "Email config not set up, using default Gmail config")
            
            // 기본 Gmail 설정 사용 (앱 비밀번호 필요)
            if (url.emailAddress.contains("@gmail.com")) {
                // 발신자와 수신자가 같은 Gmail 계정인 경우
                sendEmailWithDefaultConfig(url, failCount)
            } else {
                Log.e(TAG, "Cannot send email: No valid SMTP configuration")
            }
            return
        }
        
        // 이메일 제목 및 본문 생성
        val subject = "렌더웨이크 알림: ${url.url} 핑 실패"
        val body = """
            안녕하세요,
            
            ${url.url} 사이트에 대한 핑이 $failCount회 연속 실패했습니다.
            
            마지막 시도 시간: ${url.lastPingTime}
            
            사이트가 정상적으로 작동하는지 확인해주세요.
            
            감사합니다.
            렌더웨이크 앱
        """.trimIndent()
        
        // 이메일 전송
        try {
            Log.d(TAG, "Sending email notification to ${url.emailAddress}")
            val success = emailSender.sendEmail(
                to = url.emailAddress,
                subject = subject,
                body = body,
                smtpConfig = smtpConfig
            )
            
            if (success) {
                Log.d(TAG, "Email notification sent successfully")
            } else {
                Log.e(TAG, "Failed to send email notification")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending email notification", e)
        }
    }
    
    /**
     * 기본 설정으로 이메일을 전송합니다. (Gmail 앱 비밀번호 필요)
     */
    private suspend fun sendEmailWithDefaultConfig(url: UrlEntity, failCount: Int) {
        // 이메일 주소가 Gmail이 아니면 전송하지 않음
        if (!url.emailAddress!!.endsWith("@gmail.com")) {
            Log.e(TAG, "Cannot send email with default config: Recipient is not Gmail")
            return
        }
        
        // 이메일 제목 및 본문 생성
        val subject = "렌더웨이크 알림: ${url.url} 핑 실패"
        val body = """
            안녕하세요,
            
            ${url.url} 사이트에 대한 핑이 $failCount회 연속 실패했습니다.
            
            마지막 시도 시간: ${url.lastPingTime}
            
            사이트가 정상적으로 작동하는지 확인해주세요.
            
            감사합니다.
            렌더웨이크 앱
        """.trimIndent()
        
        // 이메일 전송 (자기 자신에게 보내기)
        try {
            Log.d(TAG, "Attempting to send email using self-send method")
            val username = url.emailAddress
            // 앱 비밀번호는 사용자가 설정해야 함
            val password = "앱 비밀번호를 설정하세요"
            
            val smtpConfig = EmailSender.SmtpConfig.gmail(username, password)
            val success = emailSender.sendEmail(
                to = url.emailAddress,
                subject = subject,
                body = body,
                smtpConfig = smtpConfig
            )
            
            if (success) {
                Log.d(TAG, "Self-send email notification sent successfully")
            } else {
                Log.e(TAG, "Failed to send self-send email notification")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending self-send email notification", e)
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