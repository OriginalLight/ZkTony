package com.zktony.www.common.app

import com.zktony.www.model.enum.SerialPortEnum

sealed class AppIntent {
    data class Sender(val serialPort: SerialPortEnum, val command: String) : AppIntent()
    data class SenderText(val serialPort: SerialPortEnum, val command: String) : AppIntent()
    data class ReceiverSerialOne(val command: String) : AppIntent()
    data class ReceiverSerialTwo(val command: String) : AppIntent()
    data class ReceiverSerialThree(val command: String) : AppIntent()
    data class ReceiverSerialFour(val command: String) : AppIntent()
}
