package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.room.PointConverters
import com.zktony.room.entities.internal.Point
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Entity(tableName = "calibration")
@TypeConverters(PointConverters::class)
data class Calibration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val index: Int = 1,
    val displayText: String,
    val points: List<Point> = emptyList(),
    val enable: Boolean = true,
    val createTime: Date = Date(System.currentTimeMillis())
)