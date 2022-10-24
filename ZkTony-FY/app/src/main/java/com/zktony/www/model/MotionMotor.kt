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
) {
    /**
     * 多点运动
     * @param x [Float] x轴运动距离
     * @param y [Float] y轴运动距离
     * @param z [Float] z轴运动距离
     */
    fun toMultiPointHex(y: Float, z: Float): String {
        val str = StringBuilder()
        str.append("0,")
        str.append(yAxis.yPulseCount(y))
        str.append(",")
        str.append(zAxis.zPulseCount(z))
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
        str.append(yAxis.yPulseCount(y))
        str.append(",")
        str.append(zAxis.zPulseCount(z))
        str.append(",")
        return str.toString()
    }
}
