package com.zktony.android.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.data.entity.CalibrationData
import com.zktony.android.data.entity.Point
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/5/12 16:41
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

object CalibrationDataConverters {
    @TypeConverter
    fun stringToObject(value: String): List<CalibrationData> {
        val listType = object : TypeToken<List<CalibrationData>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun objectToString(list: List<CalibrationData>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}

object PointConverters {
    @TypeConverter
    fun stringToObject(value: String): List<Point> {
        val listType = object : TypeToken<List<Point>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun objectToString(list: List<Point>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}