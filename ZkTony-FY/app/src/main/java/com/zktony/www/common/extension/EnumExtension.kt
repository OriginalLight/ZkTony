package com.zktony.www.common.extension

import com.zktony.www.serialport.Serial
import com.zktony.www.ui.home.ModuleEnum

/**
 * @author: 刘贺贺
 * @date: 2022-11-23 10:53
 */
fun Int.toModuleEnum(): ModuleEnum {
    return when (this) {
        0 -> ModuleEnum.A
        1 -> ModuleEnum.B
        2 -> ModuleEnum.C
        3 -> ModuleEnum.D
        else -> ModuleEnum.A
    }
}

fun Int.toSerialPort(): Serial {
    return when (this) {
        0 -> Serial.TTYS0
        1 -> Serial.TTYS1
        2 -> Serial.TTYS2
        3 -> Serial.TTYS3
        4 -> Serial.TTYS4
        5 -> Serial.TTYS5
        else -> Serial.TTYS0
    }
}