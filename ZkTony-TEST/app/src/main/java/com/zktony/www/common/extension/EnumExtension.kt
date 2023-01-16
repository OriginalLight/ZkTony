package com.zktony.www.common.extension

import com.zktony.serialport.util.Serial

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