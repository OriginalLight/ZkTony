package com.zktony.android.data.entity

import com.zktony.core.ext.nextId

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 15:54
 */

data class Point(
    val id: Long = nextId(),
    val index: Int = 0,
    val axis: List<Float> = listOf(0f, 0f, 0f),
    val volume: List<Float> = listOf(0f, 0f, 0f, 0f),
    val active: Boolean = false,
)

