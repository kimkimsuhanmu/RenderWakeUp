package com.example.renderwakeup.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 이메일 설정을 관리하는 클래스
 */
class EmailConfigManager(context: Context) {
    private val TAG = "EmailConfigManager"
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * 이메일 설정을 저장합니다.
     */
    fun saveEmailConfig(config: EmailConfig) {
        Log.d(TAG, "Saving email config for ${config.username}")
        prefs.edit().apply {
            putString(KEY_SMTP_HOST, config.host)
            putString(KEY_SMTP_PORT, config.port)
            putString(KEY_SMTP_USERNAME, config.username)
            putString(KEY_SMTP_PASSWORD, config.password)
            putBoolean(KEY_EMAIL_ENABLED, config.enabled)
            apply()
        }
    }
    
    /**
     * 저장된 이메일 설정을 가져옵니다.
     */
    fun getEmailConfig(): EmailConfig {
        return EmailConfig(
            host = prefs.getString(KEY_SMTP_HOST, DEFAULT_SMTP_HOST) ?: DEFAULT_SMTP_HOST,
            port = prefs.getString(KEY_SMTP_PORT, DEFAULT_SMTP_PORT) ?: DEFAULT_SMTP_PORT,
            username = prefs.getString(KEY_SMTP_USERNAME, "") ?: "",
            password = prefs.getString(KEY_SMTP_PASSWORD, "") ?: "",
            enabled = prefs.getBoolean(KEY_EMAIL_ENABLED, false)
        )
    }
    
    /**
     * 이메일 설정이 유효한지 확인합니다.
     */
    fun isEmailConfigValid(): Boolean {
        val config = getEmailConfig()
        return config.enabled && 
               config.host.isNotEmpty() && 
               config.port.isNotEmpty() && 
               config.username.isNotEmpty() && 
               config.password.isNotEmpty()
    }
    
    /**
     * 이메일 설정을 EmailSender.SmtpConfig 형식으로 변환합니다.
     */
    fun getSmtpConfig(): EmailSender.SmtpConfig? {
        val config = getEmailConfig()
        return if (config.enabled && 
                  config.host.isNotEmpty() && 
                  config.port.isNotEmpty() && 
                  config.username.isNotEmpty() && 
                  config.password.isNotEmpty()) {
            EmailSender.SmtpConfig(
                host = config.host,
                port = config.port,
                username = config.username,
                password = config.password
            )
        } else {
            null
        }
    }
    
    /**
     * 이메일 설정 데이터 클래스
     */
    data class EmailConfig(
        val host: String = DEFAULT_SMTP_HOST,
        val port: String = DEFAULT_SMTP_PORT,
        val username: String = "",
        val password: String = "",
        val enabled: Boolean = false
    )
    
    companion object {
        private const val PREFS_NAME = "email_config"
        
        private const val KEY_SMTP_HOST = "smtp_host"
        private const val KEY_SMTP_PORT = "smtp_port"
        private const val KEY_SMTP_USERNAME = "smtp_username"
        private const val KEY_SMTP_PASSWORD = "smtp_password"
        private const val KEY_EMAIL_ENABLED = "email_enabled"
        
        private const val DEFAULT_SMTP_HOST = "smtp.gmail.com"
        private const val DEFAULT_SMTP_PORT = "587"
    }
}
