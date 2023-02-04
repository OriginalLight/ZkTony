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