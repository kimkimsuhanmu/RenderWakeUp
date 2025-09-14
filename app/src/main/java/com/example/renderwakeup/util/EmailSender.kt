package com.example.renderwakeup.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * 이메일 전송을 담당하는 유틸리티 클래스
 */
class EmailSender {
    private val TAG = "EmailSender"

    /**
     * 이메일을 전송합니다.
     *
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param body 이메일 본문
     * @param smtpConfig SMTP 설정
     * @return 이메일 전송 성공 여부
     */
    suspend fun sendEmail(
        to: String,
        subject: String,
        body: String,
        smtpConfig: SmtpConfig
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to send email to $to")

            // SMTP 서버 속성 설정
            val props = Properties().apply {
                put("mail.smtp.host", smtpConfig.host)
                put("mail.smtp.port", smtpConfig.port)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.ssl.trust", smtpConfig.host)
            }

            // 인증 객체 생성
            val auth = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(smtpConfig.username, smtpConfig.password)
                }
            }

            // 세션 생성
            val session = Session.getInstance(props, auth)

            // 메시지 생성
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(smtpConfig.username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)
                setText(body)
            }

            // 이메일 전송
            Transport.send(message)
            Log.d(TAG, "Email sent successfully to $to")
            true
        } catch (e: MessagingException) {
            Log.e(TAG, "Failed to send email", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while sending email", e)
            false
        }
    }

    /**
     * SMTP 서버 설정 데이터 클래스
     */
    data class SmtpConfig(
        val host: String,
        val port: String,
        val username: String,
        val password: String
    ) {
        companion object {
            // Gmail SMTP 서버 기본 설정
            fun gmail(username: String, password: String): SmtpConfig {
                return SmtpConfig(
                    host = "smtp.gmail.com",
                    port = "587",
                    username = username,
                    password = password
                )
            }
        }
    }
}
