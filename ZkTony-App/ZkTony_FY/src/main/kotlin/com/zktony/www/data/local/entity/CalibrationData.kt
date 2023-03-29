package com.zktony.www.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 13:30
 */
@Entity(tableName = "calibration_data")
data class CalibrationData(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val calibrationId: String,
    val pumpId: Int = 0,
    // 预计
    val expect: Float = 180f,
    // 实际
    val actual: Float = 180f,
    // 百分比 = 实际/预计
    val percent: Float = actual / expect,
    // 校准时间
    val createTime: Date = Date(System.currentTimeMillis()),
)
