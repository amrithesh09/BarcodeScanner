package com.example.myapplication.utils

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@TypeConverters
class TimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}