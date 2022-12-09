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
    // 废液槽位置
    val wasteY: Float = 6.5f,
    val wasteZ: Float = 20f,
    // 洗涤槽液位置
    val washingY: Float = 50f,
    val washingZ: Float = 98f,
    // 封闭液槽位置
    val blockingY: Float = 89f,
    val blockingZ: Float = 98f,
    // 一抗槽位置
    val antibodyOneY: Float = 175.5f,
    val antibodyOneZ: Float = 103f,
    val recycleAntibodyOneZ: Float = 30f,
    // 二抗槽位置
    val antibodyTwoY: Float = 137f,
    val antibodyTwoZ: Float = 103f,
    // y轴电机一圈走的距离
    val distanceY: Float = 58f,
    // z轴电机一圈走的距离
    val distanceZ: Float = 3.8f,
    // 蠕动泵一一圈走的进液量
    val volumeOne: Float = 180f,
    // 蠕动泵二一圈走的进液量
    val volumeTwo: Float = 180f,
    // 蠕动泵三一圈走的进液量
    val volumeThree: Float = 180f,
    // 蠕动泵四一圈走的进液量
    val volumeFour: Float = 180f,
    // 蠕动泵五转的圈数
    val volumeFive: Float = 49f,
    // 多转的圈数
    val extract: Float = 100f,
)
