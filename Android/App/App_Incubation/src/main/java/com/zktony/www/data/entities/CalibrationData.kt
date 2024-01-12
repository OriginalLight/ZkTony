package com.zktony.www.data.entities

import com.zktony.core.ext.nextId

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 13:30
 */
data class CalibrationData(
    val id: Long = nextId(),
    val index: Int = 0,
    val step: Int = 32000,
    val actual: Float = 100f,
    val vps: Float = actual / step,
)