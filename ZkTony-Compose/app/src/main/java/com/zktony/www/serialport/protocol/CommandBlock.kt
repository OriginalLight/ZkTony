package com.zktony.www.serialport.protocol

import com.zktony.www.model.enum.SerialPortEnum

/**
 * @author: 刘贺贺
 * @date: 2022-10-19 13:17
 */
sealed class CommandBlock {
    data class Hex(val serialPort: SerialPortEnum, val hex: String) : CommandBlock()
    data class Text(val serialPort: SerialPortEnum, val text: String) : CommandBlock()
    data class Delay(val delay: Long) : CommandBlock()
}