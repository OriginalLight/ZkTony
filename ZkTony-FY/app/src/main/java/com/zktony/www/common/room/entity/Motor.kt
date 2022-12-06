package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val id: String = UUID.randomUUID().toString(),
    // 地址
    val address: Int = 0,
    // 所在板子
    val board: Int = 0,
    // 电机编号
    val name: String = "",
    // 转速
    val speed: Int = 600,
    // 加速
    val acceleration: Int = 60,
    // 减速
    val deceleration: Int = 60,
    // 等待时间
    val waitTime: Int = 0,
    // 模式
    val mode: Int = 1,
    // 细分
    val subdivision: Int = 16,
    // 电机类型
    val motorType: Int = 0,
    // 创建时间
    val createTime: Date = Date(System.currentTimeMillis()),

    ) {
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
    val x: Motor = Motor(),
    val y: Motor = Motor(),
    val z: Motor = Motor(),
    // y轴转一圈移动的长度
    val distanceY: Float = 58f,
    // z轴转一圈移动的长度
    val distanceZ: Float = 3.8f,
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
        return (distance * pulseCount(y) / distanceY).toInt().toString()
    }

    /**
     * Z轴移动任意距离所需要的脉冲数
     *
     * @param distance [Float] 移动的距离
     */
    private fun zPulseCount(distance: Float): String {
        return (distance * pulseCount(z) / distanceZ).toInt().toString()
    }

    /**
     * 多点运动
     * @param y [Float] y轴运动距离
     * @param z [Float] z轴运动距离
     */
    fun toMotionHex(y: Float, z: Float): String {
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
    val one: Motor = Motor(),
    val two: Motor = Motor(),
    val three: Motor = Motor(),
    val four: Motor = Motor(),
    val five: Motor = Motor(),
    // 泵一转一圈的出液量
    val volumeOne: Float = 180f,
    // 泵二转一圈的出液量
    val volumeTwo: Float = 180f,
    // 泵三转一圈的出液量
    val volumeThree: Float = 180f,
    // 泵四转一圈的出液量
    val volumeFour: Float = 180f,
    // 泵五转一圈的出液量
    val volumeFive: Float = 47f,
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
    private fun pumpOneVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(one) * 1000 / this.volumeOne).toInt().toString()
    }

    /**
     * 泵二出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpTwoVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(two) * 1000 / this.volumeTwo).toInt().toString()
    }

    /**
     * 泵三出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpThreeVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(three) * 1000 / this.volumeThree).toInt().toString()
    }

    /**
     * 泵四出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpFourVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(four) * 1000 / this.volumeFour).toInt().toString()
    }

    /**
     * 泵五出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpFiveVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(five) * 1000 / this.volumeFive).toInt().toString()
    }

    /**
     * 多点运动
     * @param one [Float] 泵一出液量
     * @param two [Float] 泵二出液量
     * @param three [Float] 泵三出液量
     * @param three [Float] 泵四出液量
     * @param three [Float] 泵五出液量
     */
    fun toPumpHex(
        one: Float = 0f,
        two: Float = 0f,
        three: Float = 0f,
        four: Float = 0f,
        five: Float = 0f
    ): List<String> {
        val hex = StringBuilder()
        val hex1 = StringBuilder()
        hex.append(pumpOneVolumePulseCount(one))
        hex.append(",")
        hex.append(pumpTwoVolumePulseCount(two))
        hex.append(",")
        hex.append(pumpThreeVolumePulseCount(three))
        hex.append(",")
        hex1.append(pumpFourVolumePulseCount(four))
        hex1.append(",")
        hex1.append(pumpFiveVolumePulseCount(five))
        hex1.append(",")
        hex1.append("0,")
        return listOf(hex.toString(), hex1.toString())
    }
}