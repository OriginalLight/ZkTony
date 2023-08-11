package com.zktony.android.utils.runtime

import com.zktony.android.data.entities.OrificePlate

/**
 * @author 刘贺贺
 * @date 2023/8/11 9:48
 */
data class RuntimeState(
    val status: RuntimeStatus = RuntimeStatus.STOPPED,
    val orificePlate: OrificePlate = OrificePlate(),
    val process: Float = 0f,
    val selected: List<Pair<Int, Int>> = emptyList(),
)