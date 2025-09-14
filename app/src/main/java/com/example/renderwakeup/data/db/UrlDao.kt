package com.example.renderwakeup.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.renderwakeup.data.model.PingStatus
import com.example.renderwakeup.data.model.UrlEntity
import java.util.Date

/**
 * URL 데이터에 접근하기 위한 Data Access Object
 */
@Dao
interface UrlDao {
    /**
     * 모든 URL 목록을 가져옵니다.
     */
    @Query("SELECT * FROM urls ORDER BY createdAt DESC")
    fun getAllUrls(): LiveData<List<UrlEntity>>
    
    /**
     * 모든 URL 목록을 동기적으로 가져옵니다.
     */
    @Query("SELECT * FROM urls ORDER BY createdAt DESC")
    suspend fun getAllUrlsSync(): List<UrlEntity>
    
    /**
     * ID로 특정 URL을 가져옵니다.
     */
    @Query("SELECT * FROM urls WHERE id = :id")
    suspend fun getUrlById(id: Long): UrlEntity?
    
    /**
     * 새 URL을 추가합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUrl(url: UrlEntity): Long
    
    /**
     * URL 정보를 업데이트합니다.
     */
    @Update
    suspend fun updateUrl(url: UrlEntity)
    
    /**
     * URL을 삭제합니다.
     */
    @Delete
    suspend fun deleteUrl(url: UrlEntity)
    
    /**
     * 핑이 필요한 URL 목록을 가져옵니다.
     * 마지막 핑 시간이 interval 분보다 오래된 URL들을 반환합니다.
     */
    @Query("SELECT * FROM urls WHERE lastPingTime IS NULL OR " +
           "datetime(lastPingTime, '+' || interval || ' minutes') <= datetime('now')")
    suspend fun getUrlsNeedingPing(): List<UrlEntity>
    
    /**
     * URL의 핑 상태를 업데이트합니다.
     */
    @Query("UPDATE urls SET status = :status, lastPingTime = :time, " +
           "failCount = CASE WHEN :status = 'ERROR' THEN failCount + 1 ELSE 0 END, " +
           "updatedAt = :time WHERE id = :id")
    suspend fun updateUrlStatus(id: Long, status: PingStatus, time: Date = Date())
}