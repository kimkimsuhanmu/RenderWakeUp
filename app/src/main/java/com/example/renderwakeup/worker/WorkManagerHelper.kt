package com.example.renderwakeup.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * WorkManager 관련 기능을 제공하는 헬퍼 클래스
 */
object WorkManagerHelper {
    
    // 최소 작업 주기 (1분)
    private const val MIN_PERIODIC_INTERVAL_MINUTES = 1L
    
    /**
     * 주기적인 URL 핑 작업을 예약합니다.
     * 
     * @param context 애플리케이션 컨텍스트
     * @param intervalMinutes 작업 주기 (분 단위, 최소 1분)
     */
    fun schedulePeriodicPingWork(context: Context, intervalMinutes: Long = MIN_PERIODIC_INTERVAL_MINUTES) {
        // 최소 주기 확인
        val finalInterval = intervalMinutes.coerceAtLeast(MIN_PERIODIC_INTERVAL_MINUTES)
        
        // 작업 제약 조건 설정
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 네트워크 연결 필요
            .build()
        
        // 주기적 작업 요청 생성
        val workRequest = PeriodicWorkRequestBuilder<WakeUpWorker>(
            finalInterval,
            TimeUnit.MINUTES
        )
        .setConstraints(constraints)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL, // 지수 백오프 전략
            30, // 초기 백오프 시간 (초)
            TimeUnit.SECONDS
        )
        .build()
        
        // 작업 예약 (이미 있으면 교체)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WakeUpWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // REPLACE 대신 UPDATE 사용 (권장)
            workRequest
        )
    }
    
    /**
     * 즉시 URL 핑 작업을 실행합니다.
     * 
     * @param context 애플리케이션 컨텍스트
     */
    fun runImmediatePingWork(context: Context) {
        // 작업 제약 조건 설정
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 네트워크 연결 필요
            .build()
        
        // 일회성 작업 요청 생성
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<WakeUpWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL, // 지수 백오프 전략
                30, // 초기 백오프 시간 (초)
                TimeUnit.SECONDS
            )
            .build()
        
        // 작업 예약
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    /**
     * 예약된 모든 작업을 취소합니다.
     * 
     * @param context 애플리케이션 컨텍스트
     */
    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WakeUpWorker.WORK_NAME)
    }
}