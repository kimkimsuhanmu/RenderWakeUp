package com.example.renderwakeup.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Retrofit API 서비스 인터페이스
 * URL에 핑을 보내기 위한 메서드를 정의합니다.
 */
interface ApiService {
    
    /**
     * 지정된 URL에 GET 요청을 보냅니다.
     * 
     * @param url 요청을 보낼 URL (기본 URL에 추가됨)
     * @return HTTP 응답
     */
    @GET
    suspend fun pingUrl(@Url url: String = ""): Response<String>
}