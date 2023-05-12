package com.zktony.android.data.entity

import com.zktony.core.ext.nextId

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 15:54
 */

data class Point(
    val id: Long = nextId(),
    val index: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val v1: Int = 0,
    val v2: Int = 0,
    val v3: Int = 0,
    val v4: Int = 0,
    val active: Boolean = false,
)

