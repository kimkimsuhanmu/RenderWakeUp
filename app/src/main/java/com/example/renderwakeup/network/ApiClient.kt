package com.example.renderwakeup.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Retrofit API 클라이언트를 생성하는 객체
 */
object ApiClient {
    
    // 기본 타임아웃 설정 (초)
    private const val CONNECT_TIMEOUT = 10L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    /**
     * 지정된 기본 URL에 대한 API 서비스를 생성합니다.
     * 
     * @param baseUrl 기본 URL
     * @return ApiService 인스턴스
     */
    fun createApiService(baseUrl: String): ApiService {
        val client = createOkHttpClient()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
    
    /**
     * OkHttpClient 인스턴스를 생성합니다.
     * 
     * @return OkHttpClient 인스턴스
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
}
