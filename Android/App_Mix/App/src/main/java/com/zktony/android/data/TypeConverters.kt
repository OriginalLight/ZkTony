package com.zktony.android.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.data.entities.internal.Dosage
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.data.entities.internal.Speed
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

object ListPointConverters {
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

object PointConverters {
    @TypeConverter
    @JvmStatic
    fun toObject(value: String): Point {
        val listType = object : TypeToken<Point>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun toString(list: Point): String {
        return Gson().toJson(list)
    }
}