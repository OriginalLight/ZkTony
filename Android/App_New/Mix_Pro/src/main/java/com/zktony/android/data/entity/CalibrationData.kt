package com.zktony.android.data.entity

import com.zktony.core.ext.nextId

/**
 * @author 刘贺贺
 * @date 2023/5/12 16:42
 */
data class CalibrationData(
    val id: Long = nextId(),
    val index: Int = 0,
    val expect: Float = 100f,
    val actual: Float = 100f,
    val percent: Float = actual / expect,
)