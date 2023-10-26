package com.zktony.android.data.entities.internal

/**
 * @author 刘贺贺
 * @date 2023/9/1 15:35
 */
data class Process(
    val type: Int,
    val duration: Double,
    val temperature: Double,
    val dosage: Double,
    val recycle: Boolean,
    val origin: Int,
    val times: Int,
    val status: Int
) {
    companion object {
        const val BLOCKING = 0
        const val PRIMARY_ANTIBODY = 1
        const val SECONDARY_ANTIBODY = 2
        const val WASHING = 3
        const val PHOSPHATE_BUFFERED_SALINE = 4

        const val FINISHED = 0
        const val RUNNING = 1
        const val UPCOMING = 2
    }
}