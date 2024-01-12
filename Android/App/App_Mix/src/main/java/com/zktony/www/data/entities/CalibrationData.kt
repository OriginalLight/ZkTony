package com.zktony.www.data.entities

import com.zktony.core.ext.nextId

/**
 * @author: 刘贺贺
 * @date: 2023-01-31 13:37
 */
data class CalibrationData(
    val id: Long = nextId(),
    val index: Int = 0,
    val step: Int = 32000,
    val actual: Double = 100.0,
    val vps: Double = actual / step,
)