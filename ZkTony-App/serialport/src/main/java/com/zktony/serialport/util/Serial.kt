package com.zktony.serialport.util

/**
 * @author: 刘贺贺
 * @date: 2022-12-08 14:51
 */
enum class Serial(val device: String, val value: String, val index: Int) {
    TTYS0("/dev/ttyS0", "串口一", 0),
    TTYS1("/dev/ttyS1", "串口二", 1),
    TTYS2("/dev/ttyS2", "串口三", 2),
    TTYS3("/dev/ttyS3", "串口四", 3),
    TTYS4("/dev/ttyS4", "串口五", 4),
    TTYS5("/dev/ttyS5", "串口六", 5),
}

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