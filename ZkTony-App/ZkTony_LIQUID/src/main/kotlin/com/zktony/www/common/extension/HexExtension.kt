package com.zktony.www.common.extension

import com.zktony.common.ext.hex2ToInt16
import com.zktony.common.ext.hexToInt8
import com.zktony.www.control.serial.protocol.V1
import com.zktony.www.data.local.room.entity.Motor

/**
 * 解析电机数据
 * @return [Motor] 电机
 */
fun String.toMotor(): Motor {
    return Motor(
        address = this.substring(0, 2).hexToInt8(),
        subdivision = this.substring(2, 4).hexToInt8(),
        speed = this.substring(4, 8).hex2ToInt16(),
        acceleration = this.substring(8, 10).hexToInt8(),
        deceleration = this.substring(10, 12).hexToInt8(),
        mode = this.substring(12, 14).hexToInt8(),
        waitTime = this.substring(14, 18).hex2ToInt16()
    )
}

/**
 * 解析十六进制字符串为Command
 * @return [V1]
 */
fun String.toCommand(): V1 {
    return V1(
        header = this.substring(0, 2),
        address = this.substring(2, 4),
        fn = this.substring(4, 6),
        pa = this.substring(6, 8),
        data = this.substring(8, this.length - 8),
        end = this.substring(this.length - 8, this.length)
    )
}


