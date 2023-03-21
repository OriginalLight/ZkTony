package com.zktony.www.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.common.utils.Snowflake
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 15:54
 */
@Entity(tableName = "hole")
data class Hole(
    @PrimaryKey(autoGenerate = true)
    val id: Long = Snowflake(1).nextId(),
    val subId: Long = 0L,
    val x: Int = 0,
    val xAxis: Float = 0f,
    val v1: Float = 0f,
    val v2: Float = 0f,
    val enable: Boolean = false,
    val createTime: Date = Date(System.currentTimeMillis()),
)