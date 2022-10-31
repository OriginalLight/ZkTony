package com.zktony.www.common.room.entity

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
     * B板子多点运动
     * @param pumpOneVolume [Float] 泵一出液量
     * @param pumpTwoVolume [Float] 泵二出液量
     * @param pumpThreeVolume [Float] 泵三出液量
     * @return [String] 十六进制字符串
     */
    fun toMultiPointHexB(
        pumpOneVolume: Float,
        pumpTwoVolume: Float,
        pumpThreeVolume: Float,
    ): String {
        val str = StringBuilder()
        str.append(pumpOneVolumePulseCount(pumpOneVolume))
        str.append(",")
        str.append(pumpTwoVolumePulseCount(pumpTwoVolume))
        str.append(",")
        str.append(pumpThreeVolumePulseCount(pumpThreeVolume))
        str.append(",")
        return str.toString()
    }

    /**
     * C板子多点运动
     * @param pumpFourVolume [Float] 泵一出液量
     * @param pumpFiveVolume [Float] 泵二出液量
     * @return [String] 十六进制字符串
     */
    fun toMultiPointHexC(
        pumpFourVolume: Float,
        pumpFiveVolume: Float,
    ): String {
        val str = StringBuilder()
        str.append(pumpFourVolumePulseCount(pumpFourVolume))
        str.append(",")
        str.append(pumpFiveVolumePulseCount(pumpFiveVolume))
        str.append(",")
        str.append("0,")
        return str.toString()
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