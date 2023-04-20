package com.zktony.manager.data.local.database

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 10:09
 */

import androidx.room.TypeConverter
import java.util.*

/**
 * Room [TypeConverter] functions for various `java.time.*` classes.
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