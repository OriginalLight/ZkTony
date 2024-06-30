package com.zktony.room

import androidx.room.TypeConverter
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/5/12 16:41
 */

object DateConverters {
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}