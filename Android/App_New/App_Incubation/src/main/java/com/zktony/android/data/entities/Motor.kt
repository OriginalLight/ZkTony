package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:27
 */
@Entity(
    tableName = "motors",
    indices = [
        Index(value = ["text"], unique = true)
    ]
)
@Immutable
data class Motor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "index")
    val index: Int = 0,
    @ColumnInfo(name = "text")
    val text: String = "",
    @ColumnInfo(name = "speed")
    val speed: Long = 600L,
    @ColumnInfo(name = "acc")
    val acc: Long = 120L,
    @ColumnInfo(name = "dec")
    val dec: Long = 120L,
    @ColumnInfo(name = "create_time")
    val createTime: Date = Date(System.currentTimeMillis()),
) {
    fun toAdsString(): Triple<String, String, String> {
        return Triple(acc.toString(), dec.toString(), speed.toString())
    }
}