package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.serialport.ext.hexToInt
import com.zktony.serialport.ext.intToHex

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:27
 */
@Entity(tableName = "motor")
data class Motor(
    @PrimaryKey
    val id: Int = 0,
    // 电机编号
    val name: String = "",
    // 地址
    val address: Int = 0,
    // 转速
    val speed: Int = 600,
    // 加速
    val acceleration: Int = 100,
    // 减速
    val deceleration: Int = 100,
    // 等待时间
    val waitTime: Int = 0,
    // 模式
    val mode: Int = 1,
    // 细分
    val subdivision: Int = 16,
) {
    fun toHex(): String {
        return address.intToHex() +
                subdivision.intToHex() +
                speed.intToHex(2) +
                acceleration.intToHex() +
                deceleration.intToHex() +
                mode.intToHex() +
                waitTime.intToHex(2)

    }

    /**
     * 一圈脉冲数
     */
    private fun pulseCount(): Int {
        return 200 * subdivision
    }

    /**
     * 距离/加液需要的脉冲数
     * @param distance 距离/加液梁
     * @param unit 单位
     */
    fun pulseCount(distance: Float, unit: Float): Int {
        return (distance * pulseCount() / unit).toInt()
    }
}

/**
 * 解析电机数据
 * @return [Motor] 电机
 */
fun String.toMotor(): Motor {
    return Motor(
        address = this.substring(0, 2).hexToInt(),
        subdivision = this.substring(2, 4).hexToInt(),
        speed = this.substring(4, 8).hexToInt(),
        acceleration = this.substring(8, 10).hexToInt(),
        deceleration = this.substring(10, 12).hexToInt(),
        mode = this.substring(12, 14).hexToInt(),
        waitTime = this.substring(14, 18).hexToInt()
    )
}