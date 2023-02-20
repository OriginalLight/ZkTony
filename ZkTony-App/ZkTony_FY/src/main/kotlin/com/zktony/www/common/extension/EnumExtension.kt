package com.zktony.www.common.extension

import com.zktony.serialport.util.Serial

/**
 * @author: 刘贺贺
 * @date: 2022-11-23 10:53
 */


fun Int.toSerial(): Serial {
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