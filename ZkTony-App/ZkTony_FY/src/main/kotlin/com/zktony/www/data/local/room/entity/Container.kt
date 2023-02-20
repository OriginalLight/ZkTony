package com.zktony.www.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 9:11
 */

@Entity(tableName = "container")
data class Container(
    @PrimaryKey
    val id: Int = 1,
    // 废液槽位置
    val wasteY: Float = 6.5f,
    val wasteZ: Float = 20f,
    // 洗涤槽液位置
    val washY: Float = 50f,
    val washZ: Float = 98f,
    // 封闭液槽位置
    val blockY: Float = 89f,
    val blockZ: Float = 98f,
    // 一抗槽位置
    val oneY: Float = 175.5f,
    val oneZ: Float = 103f,
    val recycleOneZ: Float = 30f,
    // 二抗槽位置
    val twoY: Float = 137f,
    val twoZ: Float = 103f,
)