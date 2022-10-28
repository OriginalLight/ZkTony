package com.zktony.www.data.entity

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
    var wasteTankPosition: Float = 6.5f,
    var wasteTankHeight: Float = 20f,
    // 洗涤槽液位置
    var washTankPosition: Float = 50.5f,
    var washTankHeight: Float = 98f,
    // 封闭液槽位置
    var blockingLiquidTankPosition: Float = 90f,
    var blockingLiquidTankHeight: Float = 98f,
    // 一抗槽位置
    var antibodyOneTankPosition: Float = 176f,
    var antibodyOneTankHeight: Float = 103f,
    var recycleAntibodyOneTankHeight: Float = 30f,
    // 二抗槽位置
    var antibodyTwoTankPosition: Float = 137.5f,
    var antibodyTwoTankHeight: Float = 103f,

    // y轴电机一圈走的距离
    var yMotorDistance: Float = 58f,
    // z轴电机一圈走的距离
    var zMotorDistance: Float = 3.8f,
    // 蠕动泵一一圈走的进液量
    var pumpOneDistance: Float = 0.5f,
    // 蠕动泵二一圈走的进液量
    var pumpTwoDistance: Float = 0.5f,
    // 蠕动泵三一圈走的进液量
    var pumpThreeDistance: Float = 0.5f,
    // 蠕动泵四一圈走的进液量
    var pumpFourDistance: Float = 0.5f,
    // 蠕动泵五转的圈数
    var pumpFiveDistance: Float = 0.5f,
    // 排液转的圈数
    var drainDistance: Float = 30f,
)
