package com.zktony.android.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.data.entities.internal.OrificePlate
import com.zktony.android.data.entities.internal.Point
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

object OrificePlateConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): List<OrificePlate> {
        val listType = object : TypeToken<List<OrificePlate>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(list: List<OrificePlate>): String {
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