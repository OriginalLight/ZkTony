package com.zktony.www.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.www.data.entities.CalibrationData
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