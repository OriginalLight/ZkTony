package com.zktony.android.data.entities.internal

/**
 * @author 刘贺贺
 * @date 2023/9/4 9:19
 */
data class Orifice(
    val volume: List<Double> = List(6) { 0.0 },
    val selected: Boolean = false,
    val point: Point = Point(),
)