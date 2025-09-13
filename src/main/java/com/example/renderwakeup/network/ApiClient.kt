package com.example.renderwakeup.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Retrofit API 클라이언트
 */
object ApiClient {
    
    // OkHttpClient 설정 - 타임아웃 등
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Retrofit 인스턴스를 생성합니다.
     * 
     * @param baseUrl 기본 URL
     * @return Retrofit 인스턴스
     */
    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }
    
    /**
     * ApiService 인스턴스를 생성합니다.
     * 
     * @param baseUrl 기본 URL
     * @return ApiService 인스턴스
     */
    fun createApiService(baseUrl: String): ApiService {
        return createRetrofit(baseUrl).create(ApiService::class.java)
    }
}
