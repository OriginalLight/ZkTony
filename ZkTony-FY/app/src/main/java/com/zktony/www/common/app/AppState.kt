package com.zktony.www.common.app

import com.zktony.www.model.enum.SerialPortEnum

sealed class AppState {
    data class Sender(val serialPort: SerialPortEnum, val command: String) : AppState()
    data class SenderText(val serialPort: SerialPortEnum, val command: String) : AppState()
    data class ReceiverSerialOne(val command: String) : AppState()
    data class ReceiverSerialTwo(val command: String) : AppState()
    data class ReceiverSerialThree(val command: String) : AppState()
    data class ReceiverSerialFour(val command: String) : AppState()
}
