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
    val wasteY: Float = 1f,
    val wasteZ: Float = 100f,
    // 洗涤槽液位置
    val washY: Float = 45f,
    val washZ: Float = 100f,
    // 封闭液槽位置
    val blockY: Float = 83f,
    val blockZ: Float = 100f,
    // 一抗槽位置
    val oneY: Float = 168f,
    val oneZ: Float = 100f,
    val recycleOneZ: Float = 80f,
    // 二抗槽位置
    val twoY: Float = 130f,
    val twoZ: Float = 100f,
)