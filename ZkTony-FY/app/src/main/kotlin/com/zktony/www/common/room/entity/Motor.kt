package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.www.common.extension.int16ToHex2
import com.zktony.www.common.extension.int8ToHex

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
    fun pulse(): Int {
        return 200 * subdivision
    }

}