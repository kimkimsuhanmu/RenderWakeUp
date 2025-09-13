package com.example.renderwakeup.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.renderwakeup.data.model.UrlEntity
import com.example.renderwakeup.util.DateConverter

/**
 * 앱의 Room Database 클래스
 */
@Database(entities = [UrlEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * URL DAO를 제공합니다.
     */
    abstract fun urlDao(): UrlDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 싱글톤 패턴으로 데이터베이스 인스턴스를 제공합니다.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "render_wakeup_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
