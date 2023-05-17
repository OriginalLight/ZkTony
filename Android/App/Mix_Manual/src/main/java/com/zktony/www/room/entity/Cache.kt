package com.zktony.www.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.core.ext.nextId

/**
 * @author 刘贺贺
 * @date 2023/5/15 16:10
 */
@Entity(tableName = "cache")
@TypeConverters(FloatConverters::class)
data class Cache(
    @PrimaryKey
    val id: Long = nextId(),
    val colloid: List<Float> = emptyList(),
    val coagulant: List<Float> = emptyList(),
    val type: Int = 0,
)

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
        val gson = Gson()
        return gson.toJson(list)
    }
}