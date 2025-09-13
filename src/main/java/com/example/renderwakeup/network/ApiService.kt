package com.example.renderwakeup.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Retrofit을 사용한 API 인터페이스
 */
interface ApiService {
    /**
     * 지정된 URL로 GET 요청을 보냅니다.
     * 
     * @param url 핑을 보낼 대상 URL
     * @return HTTP 응답
     */
    @GET
    suspend fun pingUrl(@Url url: String): Response<ResponseBody>
}
