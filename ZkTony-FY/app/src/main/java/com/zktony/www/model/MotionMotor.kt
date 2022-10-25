package com.zktony.www.model

import com.zktony.www.data.entity.Motor

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 14:56
 */
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
    fun yPulseCount(distance: Float): String {
        return (distance * pulseCount(yAxis) / yLength).toInt().toString()
    }

    /**
     * Z轴移动任意距离所需要的脉冲数
     *
     * @param distance [Float] 移动的距离
     */
    fun zPulseCount(distance: Float): String {
        return (distance * pulseCount(zAxis) / zLength).toInt().toString()
    }

    /**
     * 多点运动
     * @param x [Float] x轴运动距离
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
     * @param x [Float] x轴运动距离
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
