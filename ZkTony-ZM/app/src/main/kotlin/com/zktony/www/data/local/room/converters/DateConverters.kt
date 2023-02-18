package com.zktony.www.data.local.room.converters

import androidx.room.TypeConverter
import java.util.*

/**
 * @author 刘贺贺
 */
object DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}