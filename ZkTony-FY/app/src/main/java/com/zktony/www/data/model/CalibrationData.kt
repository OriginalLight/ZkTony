package com.zktony.www.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 13:30
 */
@Entity
data class CalibrationData(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    // 校准ID
    val calibrationId: String = "",
    // 电机ID
    val motorId: Int = 0,
    // 校准前单位加液体积
    val before: Float = 0f,
    // 校准后单位加液体积
    val after: Float = 0f,
    // 预计加液量
    val volume: Float = 0f,
    // 实际加液量
    val actualVolume: Float = 0f,
)
