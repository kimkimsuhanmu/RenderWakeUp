package com.example.renderwakeup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * URL 정보를 저장하는 Room Entity 클래스
 * 
 * @property id 고유 식별자
 * @property url 핑을 보낼 대상 URL
 * @property interval 핑 간격 (분 단위)
 * @property lastPingTime 마지막 핑 시간
 * @property status 마지막 핑 상태 (SUCCESS, ERROR)
 * @property failCount 연속 실패 횟수
 * @property emailNotification 이메일 알림 활성화 여부
 * @property emailAddress 알림을 받을 이메일 주소
 * @property createdAt 생성 시간
 * @property updatedAt 수정 시간
 */
@Entity(tableName = "urls")
data class UrlEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val interval: Int, // 분 단위
    val lastPingTime: Date? = null,
    val status: PingStatus = PingStatus.PENDING,
    val failCount: Int = 0,
    val emailNotification: Boolean = false,
    val emailAddress: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * 핑 상태를 나타내는 enum 클래스
 */
enum class PingStatus {
    PENDING, // 대기 중
    SUCCESS, // 성공
    ERROR    // 실패
}
