package kr.co.yogiyo.rookiephotoapp.diary.db

import android.arch.persistence.room.TypeConverter

import java.util.Date

object Converters {
    @JvmStatic
    @TypeConverter
    fun fromTimestamp(value: Long?): Date {
        return value?.let { Date(it) } ?: Date()
    }
    @JvmStatic
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long {
        return date?.time ?: Date().time
    }
}
