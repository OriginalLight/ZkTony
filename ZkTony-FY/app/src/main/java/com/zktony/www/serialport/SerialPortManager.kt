package com.zktony.www.serialport

import com.zktony.serialport.COMSerial
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.www.common.Logger
import com.zktony.www.common.extension.hexFormat
import com.zktony.www.common.model.Queue
import com.zktony.www.serialport.protocol.CommandBlock
import kotlinx.coroutines.delay

class SerialPortManager {

    val commandQueue = Queue<List<CommandBlock>>()

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

    /**
     * 命令队列执行
     */
    suspend fun commandQueueActuator() {
        commandQueue.peek()?.forEach {
            when (it) {
                is CommandBlock.Hex -> {
                    sendHex(it.serialPort, it.hex)
                }
                is CommandBlock.Text -> {
                    sendText(it.serialPort, it.text)
                }
                is CommandBlock.Delay -> {
                    Logger.e( msg = "delay: ${it.delay} ms")
                    delay(it.delay)
                }
            }
        }
        commandQueue.dequeue()
        if (commandQueue.isEmpty()) {
            delay(1000L)
            commandQueueActuator()
        } else {
            commandQueueActuator()
        }
    }

    /**
     * 等待命令块执行完成
     * @param block 命令块
     * @return Boolean
     */
    fun checkQueueExistBlock(block: List<CommandBlock>): Boolean {
        return commandQueue.contains(block)
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