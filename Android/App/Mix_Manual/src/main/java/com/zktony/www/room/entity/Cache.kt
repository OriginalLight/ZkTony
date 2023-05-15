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
@TypeConverters(IntConverters::class)
data class Cache(
    @PrimaryKey
    val id: Long = nextId(),
    val colloid: List<Int> = emptyList(),
    val coagulant: List<Int> = emptyList(),
    val type: Int = 0,
)

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
        val gson = Gson()
        return gson.toJson(list)
    }
}