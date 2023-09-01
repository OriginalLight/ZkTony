package com.zktony.android.data.entities.internal

/**
 * @author 刘贺贺
 * @date 2023/9/1 15:35
 */
data class Process(
    val type: ProcessType,
    val duration: Double,
    val temperature: Double,
    val dosage: Double,
    val recycle: Boolean,
    val origin: Int,
    val times: Int,
    val status: ProcessStatus
)