package com.zktony.www.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-31 13:37
 */
@Entity(tableName = "calibration_data")
data class CalibrationData(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val calibrationId: String,
    val pumpId: Int = 0,
    // 预计
    val expect: Float = 200f,
    // 实际
    val actual: Float = 200f,
    // 百分比 = 实际/预计
    val percent: Float = actual / expect,
    // 校准时间
    val createTime: Date = Date(System.currentTimeMillis()),
)
