package com.zktony.www.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.www.common.extension.hex2ToInt16
import com.zktony.www.common.extension.hexToInt8
import com.zktony.www.common.extension.int16ToHex2
import com.zktony.www.common.extension.int8ToHex
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:27
 */
@Entity(tableName = "motor")
data class Motor(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    // 地址
    var address: Int = 0,
    // 所在板子
    var board: Int = 0,
    // 电机编号
    var name: String = "",
    // 转速
    var speed: Int = 100,
    // 加速
    var acceleration: Int = 60,
    // 减速
    var deceleration: Int = 60,
    // 等待时间
    var waitTime: Int = 0,
    // 模式
    var mode: Int = 1,
    // 细分
    var subdivision: Int = 16,
    // 电机类型
    var motorType: Int = 0,
    // 创建时间
    var createTime: Date = Date(System.currentTimeMillis()),

    ) {
    constructor(hex: String) : this() {
        address = hex.substring(0, 2).hexToInt8()
        subdivision = hex.substring(2, 4).hexToInt8()
        speed = hex.substring(4, 8).hex2ToInt16()
        acceleration = hex.substring(8, 10).hexToInt8()
        deceleration = hex.substring(10, 12).hexToInt8()
        mode = hex.substring(12, 14).hexToInt8()
        waitTime = hex.substring(14, 18).hex2ToInt16()
    }

    fun toHex(): String {
        return address.int8ToHex() +
                subdivision.int8ToHex() +
                speed.int16ToHex2() +
                acceleration.int8ToHex() +
                deceleration.int8ToHex() +
                mode.int8ToHex() +
                waitTime.int16ToHex2()

    }
}

data class MotionMotor(
    var xAxis: Motor = Motor(),
    var yAxis: Motor = Motor(),
    var zAxis: Motor = Motor(),
    // y轴转一圈移动的长度
    var yLength: Float = 58f,
    // z轴转一圈移动的长度
    var zLength: Float = 3.8f,
) {
    /**
     *  电机转一圈需要的脉冲数
     */
    private fun pulseCount(motor: Motor): Int {
        return 200 * motor.subdivision
    }

    /**
     * Y轴移动任意距离所需要的脉冲数
     *
     * @param distance [Float] 移动的距离
     */
    private fun yPulseCount(distance: Float): String {
        return (distance * pulseCount(yAxis) / yLength).toInt().toString()
    }

    /**
     * Z轴移动任意距离所需要的脉冲数
     *
     * @param distance [Float] 移动的距离
     */
    private fun zPulseCount(distance: Float): String {
        return (distance * pulseCount(zAxis) / zLength).toInt().toString()
    }

    /**
     * 多点运动
     * @param y [Float] y轴运动距离
     * @param z [Float] z轴运动距离
     */
    fun toMultiPointHex(y: Float, z: Float): String {
        val str = StringBuilder()
        str.append("0,")
        str.append(yPulseCount(y))
        str.append(",")
        str.append(zPulseCount(z))
        str.append(",")
        return str.toString()
    }

    /**
     * 单点运动
     * @param y [Float] y轴运动距离
     * @param z [Float] z轴运动距离
     */
    fun toSinglePointHex(y: Float, z: Float): String {
        val str = StringBuilder()
        str.append("0,")
        str.append(yPulseCount(y))
        str.append(",")
        str.append(zPulseCount(z))
        str.append(",")
        return str.toString()
    }
}

data class PumpMotor(
    var pumpOne: Motor = Motor(),
    var pumpTwo: Motor = Motor(),
    var pumpThree: Motor = Motor(),
    var pumpFour: Motor = Motor(),
    var pumpFive: Motor = Motor(),
    // 泵一转一圈的出液量
    var volumeOne: Float = 0.5f,
    // 泵二转一圈的出液量
    var volumeTwo: Float = 0.5f,
    // 泵三转一圈的出液量
    var volumeThree: Float = 0.5f,
    // 泵四转一圈的出液量
    var volumeFour: Float = 0.5f,
    // 泵五转一圈的出液量
    var volumeFive: Float = 0.5f,
) {

    /**
     *  电机转一圈需要的脉冲数
     */
    private fun pulseCount(motor: Motor): Int {
        return 200 * motor.subdivision
    }

    /**
     * 泵一出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpOneVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpOne) / this.volumeOne).toInt().toString()
    }

    /**
     * 泵二出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpTwoVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpTwo) / this.volumeTwo).toInt().toString()
    }

    /**
     * 泵三出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpThreeVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpThree) / this.volumeThree).toInt().toString()
    }

    /**
     * 泵四出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpFourVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpFour) / this.volumeFour).toInt().toString()
    }

    /**
     * 泵五出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpFiveVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpFive) / this.volumeFive).toInt().toString()
    }
}