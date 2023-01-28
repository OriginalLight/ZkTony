package com.zktony.www.data.model

import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 15:54
 */
data class Pore(
    val id: String = UUID.randomUUID().toString(),
    val plateId: String = "",
    val row: Int = 0,
    val column: Int = 0,
    val rowAxis: Float = 0f,
    val columnAxis: Float = 0f,
    val checked: Boolean = false,
)