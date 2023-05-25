package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.ext.nextId

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 9:11
 */

@Entity(tableName = "container")
data class Container(
    @PrimaryKey
    val id: Long = nextId(),
    // 废液槽位置
    val wasteY: Float = 0f,
    val wasteZ: Float = 80f,
    // 洗涤槽液位置
    val washY: Float = 43f,
    val washZ: Float = 105f,
    // 封闭液槽位置
    val blockY: Float = 82f,
    val blockZ: Float = 105f,
    // 一抗槽位置
    val oneY: Float = 168f,
    val oneZ: Float = 105f,
    val recycleOneZ: Float = 80f,
    // 二抗槽位置
    val twoY: Float = 130f,
    val twoZ: Float = 105f,
)