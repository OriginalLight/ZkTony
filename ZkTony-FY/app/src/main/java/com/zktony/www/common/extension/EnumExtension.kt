package com.zktony.www.common.extension

import com.zktony.www.serialport.SerialPort
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

fun Int.toSerialPort(): SerialPort {
    return when (this) {
        0 -> SerialPort.SERIAL_ONE
        1 -> SerialPort.SERIAL_TWO
        2 -> SerialPort.SERIAL_THREE
        3 -> SerialPort.SERIAL_FOUR
        4 -> SerialPort.SERIAL_FIVE
        5 -> SerialPort.SERIAL_SIX
        else -> SerialPort.SERIAL_ONE
    }
}