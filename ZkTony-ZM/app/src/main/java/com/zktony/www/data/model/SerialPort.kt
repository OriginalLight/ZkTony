package com.zktony.www.data.model

/**
 * @author: 刘贺贺
 * @date: 2022-09-16 9:59
 */
enum class SerialPort(var device: String) {
    TTYS0("/dev/ttyS0"),
    TTYS1("/dev/ttyS1"),
    TTYS2("/dev/ttyS2"),
    TTYS3("/dev/ttyS3"),
    TTYS4("/dev/ttyS4"),
    TTYS5("/dev/ttyS5"),
}