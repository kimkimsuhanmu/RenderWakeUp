package com.example.renderwakeup.data.repository

import androidx.lifecycle.LiveData
import com.example.renderwakeup.data.db.UrlDao
import com.example.renderwakeup.data.model.PingStatus
import com.example.renderwakeup.data.model.UrlEntity
import com.example.renderwakeup.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * URL 데이터에 접근하기 위한 Repository 클래스
 * 
 * @property urlDao URL 데이터 접근 객체
 */
class UrlRepository(private val urlDao: UrlDao) {
    
    /**
     * 모든 URL 목록을 가져옵니다.
     */
    fun getAllUrls(): LiveData<List<UrlEntity>> {
        return urlDao.getAllUrls()
    }
    
    /**
     * ID로 특정 URL을 가져옵니다.
     */
    suspend fun getUrlById(id: Long): UrlEntity? {
        return urlDao.getUrlById(id)
    }
    
    /**
     * 새 URL을 추가합니다.
     */
    suspend fun insertUrl(url: UrlEntity): Long {
        return urlDao.insertUrl(url)
    }
    
    /**
     * URL 정보를 업데이트합니다.
     */
    suspend fun updateUrl(url: UrlEntity) {
        urlDao.updateUrl(url)
    }
    
    /**
     * URL을 삭제합니다.
     */
    suspend fun deleteUrl(url: UrlEntity) {
        urlDao.deleteUrl(url)
    }
    
    /**
     * 핑이 필요한 URL 목록을 가져옵니다.
     */
    suspend fun getUrlsNeedingPing(): List<UrlEntity> {
        return urlDao.getUrlsNeedingPing()
    }
    
    /**
     * URL에 핑을 보내고 결과를 업데이트합니다.
     * 
     * @param url 핑을 보낼 URL 엔티티
     * @return 핑 성공 여부
     */
    suspend fun pingUrl(url: UrlEntity): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val normalizedUrl = normalizeUrl(url.url)
                val apiService = ApiClient.createApiService(normalizedUrl)
                val response = apiService.pingUrl(normalizedUrl)
                
                val isSuccess = response.isSuccessful
                val status = if (isSuccess) PingStatus.SUCCESS else PingStatus.ERROR
                
                // 핑 결과 업데이트
                urlDao.updateUrlStatus(url.id, status)
                
                isSuccess
            } catch (e: Exception) {
                // 예외 발생 시 오류로 처리
                urlDao.updateUrlStatus(url.id, PingStatus.ERROR)
                false
            }
        }
    }
    
    /**
     * URL 문자열이 유효한 기본 URL 형식인지 확인하고 정규화합니다.
     * 
     * @param url 정규화할 URL 문자열
     * @return 정규화된 URL 문자열
     */
    private fun normalizeUrl(url: String): String {
        var normalizedUrl = url
        
        // URL이 http:// 또는 https://로 시작하지 않으면 https://를 추가
        if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            normalizedUrl = "https://$normalizedUrl"
        }
        
        // URL이 /로 끝나지 않으면 /를 추가 (Retrofit baseUrl 요구사항)
        if (!normalizedUrl.endsWith("/")) {
            normalizedUrl = "$normalizedUrl/"
        }
        
        return normalizedUrl
    }
}
