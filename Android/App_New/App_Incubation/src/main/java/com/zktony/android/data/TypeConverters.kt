package com.zktony.android.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.data.entities.internal.Log
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.data.entities.internal.Process
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

object ProcessConverters {
    @TypeConverter
    @JvmStatic
    fun toObject(value: String): List<Process> {
        val listType = object : TypeToken<List<Process>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun toString(list: List<Process>): String {
        return Gson().toJson(list)
    }
}

object PointConverters {
    @TypeConverter
    @JvmStatic
    fun toObject(value: String): List<Point> {
        val listType = object : TypeToken<List<Point>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun toString(list: List<Point>): String {
        return Gson().toJson(list)
    }
}

object LogConverters {
    @TypeConverter
    @JvmStatic
    fun toObject(value: String): List<Log> {
        val listType = object : TypeToken<List<Log>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun toString(list: List<Log>): String {
        return Gson().toJson(list)
    }
}