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
    // 电机编号
    val name: String = "",
    // Index
    val index: Int = 0,
    // 地址
    val address: Int = 0,
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
    // 单位的行程或出液体积
    val unit: Float = 0f,
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

    /**
     *  电机转一圈需要的脉冲数
     */
    private fun pulse(): Int {
        return 200 * subdivision
    }

    /**
     * 移动任意距离或者出任意体积液体的脉冲数
     * @param dv 距离或者体积
     * @return 脉冲数
     */
    fun pulse(dv: Float) : Int {
        return (dv / unit * pulse()).toInt()
    }
}

/**
 * 电机组
 */
data class MotorUnits(
    val x: Motor = Motor(),
    val y: Motor = Motor(),
    val z: Motor = Motor(),
    val p1: Motor = Motor(),
    val p2: Motor = Motor(),
    val p3: Motor = Motor(),
    val p4: Motor = Motor(),
    val p5: Motor = Motor(),
) {
    /**
     * 多点运动
     * @param yDv [Float] y轴运动距离
     * @param zDv [Float] z轴运动距离
     */
    fun toMotionHex(yDv: Float, zDv: Float): String {
        val str = StringBuilder()
        str.append("0,")
        str.append(y.pulse(yDv))
        str.append(",")
        str.append(z.pulse(zDv))
        str.append(",")
        return str.toString()
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
    ): Pair<String, String> {
        val hex = StringBuilder()
        val hex1 = StringBuilder()
        hex.append(p1.pulse(one * 1000))
        hex.append(",")
        hex.append(p2.pulse(two * 1000))
        hex.append(",")
        hex.append(p3.pulse(three * 1000))
        hex.append(",")
        hex1.append(p4.pulse(four * 1000))
        hex1.append(",")
        hex1.append(p5.pulse(five * 1000))
        hex1.append(",")
        hex1.append("0,")
        return Pair(hex.toString(), hex1.toString())
    }
}