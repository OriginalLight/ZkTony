package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(tableName = "plate")
data class Plate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sort: Int = 0,
    val row: Int = 8,
    val column: Int = 12,
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f,
) {
    @Ignore
    var holes: List<Hole> = emptyList()
}
