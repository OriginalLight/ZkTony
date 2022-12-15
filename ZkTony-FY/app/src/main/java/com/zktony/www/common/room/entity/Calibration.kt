package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
@Entity
data class Calibration(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    // 名字
    val name: String = "",
    // 状态
    val status: Int = 0,
    // x轴
    val x: Float = 0f,
    // y轴
    val y: Float = 58f,
    // z轴
    val z: Float = 3.8f,
    // 校准后泵一单位加液体积
    val p1: Float = 180f,
    // 校准后泵二单位加液体积
    val p2: Float = 180f,
    // 校准后泵三单位加液体积
    val p3: Float = 180f,
    // 校准后泵四单位加液体积
    val p4: Float = 180f,
    // 校准后泵五单位加液体积
    val p5: Float = 49f,
)
