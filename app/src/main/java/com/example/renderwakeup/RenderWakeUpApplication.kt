package com.example.renderwakeup

import android.app.Application
import com.example.renderwakeup.data.db.AppDatabase

/**
 * 앱의 Application 클래스
 * 앱 전역에서 사용할 객체들을 초기화합니다.
 */
class RenderWakeUpApplication : Application() {
    
    // 데이터베이스 인스턴스
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    
    override fun onCreate() {
        super.onCreate()
        // 필요한 초기화 작업 수행
    }
}
