package com.zktony.android.data.entities.internal

/**
 * @author 刘贺贺
 * @date 2023/8/21 10:38
 */


data class IncubationStage(
    val tag: IncubationTag = IncubationTag.BLOCKING,
    val duration: Double = 0.0,
    val temperature: Double = 0.0,
    val dosage: Double = 0.0,
    val recycle: Boolean = true,
    val origin: Int = 0,
    val times: Int = 1,
    val status: IncubationStageStatus = IncubationStageStatus.FINISHED
)