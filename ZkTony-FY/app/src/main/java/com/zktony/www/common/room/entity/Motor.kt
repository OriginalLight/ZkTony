package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.www.common.extension.int16ToHex2
import com.zktony.www.common.extension.int8ToHex
import java.util.*
import kotlin.math.max

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
    val speed: Int = 100,
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
    val xAxis: Motor = Motor(),
    val yAxis: Motor = Motor(),
    val zAxis: Motor = Motor(),
    // y轴转一圈移动的长度
    val yLength: Float = 58f,
    // z轴转一圈移动的长度
    val zLength: Float = 3.8f,
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
    fun toMotionHex(y: Float, z: Float): String {
        val str = StringBuilder()
        str.append("0,")
        str.append(yPulseCount(y))
        str.append(",")
        str.append(zPulseCount(z))
        str.append(",")
        return str.toString()
    }

    /**
     * 运动延时
     * @param y [Float] y轴运动距离
     * @param z [Float] z轴运动距离
     * @return [Long] 延时时间
     */
    fun delayTime(y: Float, z: Float): Long {
        val yTime = (y / yLength * 60 / yAxis.speed * 1000).toLong()
        val zTime = (z / zLength * 60 / zAxis.speed * 1000).toLong()
        return yTime + zTime
    }
}

data class PumpMotor(
    val pumpOne: Motor = Motor(),
    val pumpTwo: Motor = Motor(),
    val pumpThree: Motor = Motor(),
    val pumpFour: Motor = Motor(),
    val pumpFive: Motor = Motor(),
    // 泵一转一圈的出液量
    val volumeOne: Float = 0.5f,
    // 泵二转一圈的出液量
    val volumeTwo: Float = 0.5f,
    // 泵三转一圈的出液量
    val volumeThree: Float = 0.5f,
    // 泵四转一圈的出液量
    val volumeFour: Float = 0.5f,
    // 泵五转一圈的出液量
    val volumeFive: Float = 0.5f,
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
        return (volume * pulseCount(pumpOne) / this.volumeOne).toInt().toString()
    }

    /**
     * 泵二出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpTwoVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpTwo) / this.volumeTwo).toInt().toString()
    }

    /**
     * 泵三出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpThreeVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpThree) / this.volumeThree).toInt().toString()
    }

    /**
     * 泵四出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpFourVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpFour) / this.volumeFour).toInt().toString()
    }

    /**
     * 泵五出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    private fun pumpFiveVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpFive) / this.volumeFive).toInt().toString()
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

    /**
     * B板子多点运动
     * @param oneVolume [Float] 泵一出液量
     * @param twoVolume [Float] 泵二出液量
     * @param threeVolume [Float] 泵三出液量
     * @return [String] 十六进制字符串
     */
    fun toMultiPointHexB(
        oneVolume: Float,
        twoVolume: Float,
        threeVolume: Float,
    ): String {
        val str = StringBuilder()
        str.append(pumpOneVolumePulseCount(oneVolume))
        str.append(",")
        str.append(pumpTwoVolumePulseCount(twoVolume))
        str.append(",")
        str.append(pumpThreeVolumePulseCount(threeVolume))
        str.append(",")
        return str.toString()
    }

    /**
     * C板子多点运动
     * @param fourVolume [Float] 泵一出液量
     * @param fiveVolume [Float] 泵二出液量
     * @return [String] 十六进制字符串
     */
    fun toMultiPointHexC(
        fourVolume: Float,
        fiveVolume: Float,
    ): String {
        val str = StringBuilder()
        str.append(pumpFourVolumePulseCount(fourVolume))
        str.append(",")
        str.append(pumpFiveVolumePulseCount(fiveVolume))
        str.append(",")
        str.append("0,")
        return str.toString()
    }

    fun delayTime(
        one: Float = 0f,
        two: Float = 0f,
        three: Float = 0f,
        four: Float = 0f,
        five: Float = 0f
    ): Long {
        val oneTime = (one / volumeOne * 60 / pumpOne.speed * 1000).toLong()
        val twoTime = (two / volumeTwo * 60 / pumpTwo.speed * 1000).toLong()
        val threeTime = (three / volumeThree * 60 / pumpThree.speed * 1000).toLong()
        val fourTime = (four / volumeFour * 60 / pumpFour.speed * 1000).toLong()
        val fiveTime = (five / volumeFive * 60 / pumpFive.speed * 1000).toLong()
        return max(oneTime, max(twoTime, max(threeTime, max(fourTime, fiveTime))))
    }

    /**
     * 运动延时
     * @param x [Float] x轴运动距离
     * @param y [Float] y轴运动距离
     * @param z [Float] z轴运动距离
     * @return [Long] 延时时间
     */
    fun delayTimeB(x: Float, y: Float, z: Float): Long {
        val xTime = (x / volumeOne * 60 / pumpOne.speed * 1000).toLong()
        val yTime = (y / volumeTwo * 60 / pumpTwo.speed * 1000).toLong()
        val zTime = (z / volumeThree * 60 / pumpThree.speed * 1000).toLong()
        return xTime + yTime + zTime
    }

    /**
     * 运动延时
     * @param x [Float] x轴运动距离
     * @param y [Float] y轴运动距离
     * @return [Long] 延时时间
     */
    fun delayTimeC(x: Float, y: Float): Long {
        val xTime = (x / volumeFour * 60 / pumpFour.speed * 1000).toLong()
        val yTime = (y / volumeFive * 60 / pumpFive.speed * 1000).toLong()
        return xTime + yTime
    }
}