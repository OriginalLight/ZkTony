package com.zktony.android.data.entities

/**
 * @author 刘贺贺
 * @date 2023/7/27 14:22
 */
data class Orifice(
    val id: Long = 0L,
    val subscript: List<Int> = listOf(0, 0),
    val coordinate: List<Double> = listOf(0.0, 0.0),
    val volume: Double = 0.0,
    val active: Boolean = false,
)