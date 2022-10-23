package com.zktony.www.model.enum

/**
 * @author: 刘贺贺
 * @date: 2022-09-16 9:59
 */
enum class SerialPortEnum(val device: String, val value: String, val index: Int) {
    SERIAL_ONE("/dev/ttyS0", "串口一", 0),
    SERIAL_TWO("/dev/ttyS1", "串口二", 1),
    SERIAL_THREE("/dev/ttyS2", "串口三", 2),
    SERIAL_FOUR("/dev/ttyS3", "串口四", 3),
    SERIAL_FIVE("/dev/ttyS4", "串口五", 4),
    SERIAL_SIX("/dev/ttyS5", "串口六", 5),
}

fun getSerialPortEnum(device: String): SerialPortEnum {
    return when (device) {
        "/dev/ttyS0" -> SerialPortEnum.SERIAL_ONE
        "/dev/ttyS1" -> SerialPortEnum.SERIAL_TWO
        "/dev/ttyS2" -> SerialPortEnum.SERIAL_THREE
        "/dev/ttyS3" -> SerialPortEnum.SERIAL_FOUR
        "/dev/ttyS4" -> SerialPortEnum.SERIAL_FIVE
        "/dev/ttyS5" -> SerialPortEnum.SERIAL_SIX
        else -> SerialPortEnum.SERIAL_ONE
    }
}

fun getSerialPortEnum(index: Int): SerialPortEnum {
    return when (index) {
        0 -> SerialPortEnum.SERIAL_ONE
        1 -> SerialPortEnum.SERIAL_TWO
        2 -> SerialPortEnum.SERIAL_THREE
        3 -> SerialPortEnum.SERIAL_FOUR
        4 -> SerialPortEnum.SERIAL_FIVE
        5 -> SerialPortEnum.SERIAL_SIX
        else -> SerialPortEnum.SERIAL_ONE
    }
}