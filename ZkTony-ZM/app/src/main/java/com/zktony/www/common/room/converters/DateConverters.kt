package com.zktony.www.common.room.converters

import androidx.room.TypeConverter
import java.util.Date

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