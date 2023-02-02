package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 15:54
 */
@Entity(tableName = "hole")
data class Hole(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val plateId: String = "",
    val workId: String = "",
    val x: Int = 0,
    val y: Int = 0,
    val xAxis: Float = 0f,
    val yAxis: Float = 0f,
    val checked: Boolean = false,
)