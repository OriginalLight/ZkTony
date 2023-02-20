package com.zktony.www.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(tableName = "work_plate")
data class WorkPlate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val workId: String = "",
    val sort: Int = 0,
    val row: Int = 8,
    val column: Int = 12,
    val count: Int = 0,
    val v1: Float = 0f,
    val v2: Float = 0f,
    val v3: Float = 0f,
    val v4: Float = 0f,
)
