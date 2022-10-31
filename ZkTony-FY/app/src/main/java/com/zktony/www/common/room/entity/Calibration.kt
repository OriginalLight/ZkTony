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
    val wasteTankPosition: Float = 6.5f,
    val wasteTankHeight: Float = 20f,
    // 洗涤槽液位置
    val washTankPosition: Float = 50.5f,
    val washTankHeight: Float = 98f,
    // 封闭液槽位置
    val blockingLiquidTankPosition: Float = 90f,
    val blockingLiquidTankHeight: Float = 98f,
    // 一抗槽位置
    val antibodyOneTankPosition: Float = 176f,
    val antibodyOneTankHeight: Float = 103f,
    val recycleAntibodyOneTankHeight: Float = 30f,
    // 二抗槽位置
    val antibodyTwoTankPosition: Float = 137.5f,
    val antibodyTwoTankHeight: Float = 103f,

    // y轴电机一圈走的距离
    val yMotorDistance: Float = 58f,
    // z轴电机一圈走的距离
    val zMotorDistance: Float = 3.8f,
    // 蠕动泵一一圈走的进液量
    val pumpOneDistance: Float = 0.5f,
    // 蠕动泵二一圈走的进液量
    val pumpTwoDistance: Float = 0.5f,
    // 蠕动泵三一圈走的进液量
    val pumpThreeDistance: Float = 0.5f,
    // 蠕动泵四一圈走的进液量
    val pumpFourDistance: Float = 0.5f,
    // 蠕动泵五转的圈数
    val pumpFiveDistance: Float = 0.5f,
    // 排液转的圈数
    val drainDistance: Float = 30f,
)
