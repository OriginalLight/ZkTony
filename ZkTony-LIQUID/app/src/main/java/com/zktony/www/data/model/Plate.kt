package com.zktony.www.data.model

import java.util.UUID

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
data class Plate(
    val id: String = UUID.randomUUID().toString(),
    val sort: Int = 0,
    val row: Int = 0,
    val column: Int = 0,
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f,
    val x3: Float = 0f,
    val y3: Float = 0f,
)
