package com.zktony.android.logic.data.entities

import com.zktony.core.ext.nextId

/**
 * @author 刘贺贺
 * @date 2023/5/12 16:42
 */
data class CalibrationData(
    val id: Long = nextId(),
    val index: Int = 0,
    val pulse: Int = 3200,
    val volume: Double = 100.0,
    val vps: Double = volume / pulse,
)