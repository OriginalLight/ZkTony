package com.zktony.android.data.entities.internal

import androidx.annotation.Keep

/**
 * @author 刘贺贺
 * @date 2023/9/1 15:35
 */
@Keep
data class IncubationStage(
    val uuid: String,
    // 0: blocking, 1: primary antibody, 2: secondary antibody, 3: washing, 4: phosphate buffered saline
    val type: Int,
    val duration: Double,
    val temperature: Double,
    val dosage: Double,
    val recycle: Boolean,
    val origin: Int,
    val times: Int,
    // 0: finished, 1: running, 2: upcoming
    val flags: Int
)