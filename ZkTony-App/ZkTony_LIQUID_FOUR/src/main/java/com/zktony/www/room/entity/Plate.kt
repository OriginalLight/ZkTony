package com.zktony.www.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.utils.Snowflake

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(tableName = "plate")
data class Plate(
    @PrimaryKey
    val id: Long = Snowflake(1).nextId(),
    val subId: Long = 0L,
    val index: Int = 0,
    val x: Int = 8,
    val y: Int = 12,
    val custom: Int = 0,
)
