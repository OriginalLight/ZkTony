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