package com.zktony.android.logic.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.logic.data.entities.CalibrationData
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

object CalibrationDataConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): List<CalibrationData> {
        val listType = object : TypeToken<List<CalibrationData>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(list: List<CalibrationData>): String {
        return Gson().toJson(list)
    }
}

object IntConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(list: List<Int>): String {
        return Gson().toJson(list)
    }
}

object FloatConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): List<Float> {
        val listType = object : TypeToken<List<Float>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(list: List<Float>): String {
        return Gson().toJson(list)
    }
}