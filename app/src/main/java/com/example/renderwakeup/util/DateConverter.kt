package com.example.renderwakeup.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room에서 Date 타입을 저장하고 불러오기 위한 TypeConverter
 */
class DateConverter {
    
    /**
     * Date 객체를 Long 타입(timestamp)으로 변환합니다.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Long 타입(timestamp)을 Date 객체로 변환합니다.
     */
    @TypeConverter
    fun timestampToDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
