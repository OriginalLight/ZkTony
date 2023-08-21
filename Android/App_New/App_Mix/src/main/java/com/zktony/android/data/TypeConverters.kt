package com.zktony.android.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.data.entities.Coordinate
import com.zktony.android.data.entities.Dosage
import com.zktony.android.data.entities.Speed
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

object TripleConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): List<Triple<Int, Double, Double>> {
        val listType = object : TypeToken<List<Triple<Int, Double, Double>>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(list: List<Triple<Int, Double, Double>>): String {
        return Gson().toJson(list)
    }
}

object CoordinateConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): Coordinate {
        val type = object : TypeToken<Coordinate>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(obj: Coordinate): String {
        return Gson().toJson(obj)
    }
}

object DosageConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): Dosage {
        val type = object : TypeToken<Dosage>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(obj: Dosage): String {
        return Gson().toJson(obj)
    }
}

object SpeedConverters {
    @TypeConverter
    @JvmStatic
    fun stringToObject(value: String): Speed {
        val type = object : TypeToken<Speed>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(obj: Speed): String {
        return Gson().toJson(obj)
    }
}