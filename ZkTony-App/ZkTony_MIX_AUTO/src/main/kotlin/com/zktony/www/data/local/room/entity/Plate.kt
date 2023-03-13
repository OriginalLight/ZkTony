package com.zktony.www.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.common.utils.Snowflake
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(tableName = "plate")
data class Plate(
    @PrimaryKey val id: Long = Snowflake(1).nextId(),
    val subId: Long = 0L,
    val x: Int = 0,
    val y: Int = 0,
    val index: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
)
