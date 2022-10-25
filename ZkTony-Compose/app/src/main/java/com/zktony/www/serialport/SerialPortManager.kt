package com.zktony.www.serialport

import com.zktony.serialport.COMSerial
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.www.common.Logger
import com.zktony.www.common.extension.hexFormat

class SerialPortManager {

    init {
        COMSerial.instance.addCOM(SerialPortEnum.SERIAL_ONE.device, 115200)
        COMSerial.instance.addCOM(SerialPortEnum.SERIAL_TWO.device, 115200)
        COMSerial.instance.addCOM(SerialPortEnum.SERIAL_THREE.device, 115200)
        COMSerial.instance.addCOM(SerialPortEnum.SERIAL_FOUR.device, 115200)
    }

    /**
     * 添加串口监听
     * @param block Unit
     */
    inline fun addDataListener(crossinline block: (String, String) -> Unit) {
        COMSerial.instance.addDataListener(object : OnComDataListener {
            override fun comDataBack(com: String, hexData: String) {
                block.invoke(com, hexData)
            }
        })
    }

    /**
     * 发送Hex
     * @param serialPort 串口
     * @param command 命令
     */
    fun sendHex(serialPort: SerialPortEnum, command: String) {
        COMSerial.instance.sendHex(serialPort.device, command)
        Logger.e(msg = "${serialPort.value} sendHex: ${command.hexFormat()}")
    }

    /**
     * 发送Text
     * @param serialPort 串口
     * @param command 命令
     */
    fun sendText(serialPort: SerialPortEnum, command: String) {
        COMSerial.instance.sendText(serialPort.device, command)
        Logger.e(msg = "${serialPort.value} sendText: $command")
    }

    companion object {
        @JvmStatic
        val instance: SerialPortManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SerialPortManager()
        }
    }
}

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
