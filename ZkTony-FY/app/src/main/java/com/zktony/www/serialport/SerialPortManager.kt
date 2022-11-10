package com.zktony.www.serialport

import com.zktony.serialport.COMSerial
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.www.common.extension.hexFormat
import com.zktony.www.common.model.Queue
import com.zktony.www.common.utils.Logger
import com.zktony.www.serialport.SerialPortEnum.*
import com.zktony.www.serialport.protocol.Command
import com.zktony.www.serialport.protocol.CommandBlock
import com.zktony.www.ui.home.ModuleEnum
import com.zktony.www.ui.home.ModuleEnum.*
import kotlinx.coroutines.delay

class SerialPortManager {

    val commandQueue = Queue<List<CommandBlock>>()
    private var A_RUNNING = false
    private var B_RUNNING = false
    private var C_RUNNING = false
    private var D_RUNNING = false

    init {
        COMSerial.instance.addCOM(SERIAL_ONE.device, 115200)
        COMSerial.instance.addCOM(SERIAL_TWO.device, 115200)
        COMSerial.instance.addCOM(SERIAL_THREE.device, 115200)
        COMSerial.instance.addCOM(SERIAL_FOUR.device, 115200)
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
        commandQueue.peek()?.let {
            sendHex(SERIAL_ONE, Command.pauseShakeBed())
            it.forEach { block ->
                when (block) {
                    is CommandBlock.Hex -> {
                        if (isModuleRunning(block.module)) {
                            sendHex(block.serialPort, block.hex)
                        }
                    }
                    is CommandBlock.Text -> {
                        if (isModuleRunning(block.module)) {
                            sendText(block.serialPort, block.text)
                        }
                    }
                    is CommandBlock.Delay -> {
                        if (isModuleRunning(block.module)) {
                            Logger.e(msg = "delay: ${block.delay} ms")
                            delay(block.delay)
                        }
                    }
                }
            }
            commandQueue.dequeue()
            if (commandQueue.isEmpty() && (A_RUNNING || B_RUNNING || C_RUNNING || D_RUNNING)) {
                sendHex(SERIAL_ONE, Command.resumeShakeBed())
            }
        }
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

    /**
     * 判断模块是否在运行
     * @param module 模块
     */
    private fun isModuleRunning(module: ModuleEnum): Boolean {
        return when (module) {
            A -> A_RUNNING
            B -> B_RUNNING
            C -> C_RUNNING
            D -> D_RUNNING
        }
    }

    /**
     * 设置模块运行状态
     * @param module 模块
     * @param running 运行状态
     */
    fun setModuleRunning(module: ModuleEnum, running: Boolean) {
        when (module) {
            A -> A_RUNNING = running
            B -> B_RUNNING = running
            C -> C_RUNNING = running
            D -> D_RUNNING = running
        }
        commandQueue.getQueue().forEach { block ->
            block.find { it is CommandBlock.Hex && it.module == module }?.let {
                if (!running) {
                    commandQueue.remove(block)
                }
            }
        }
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
        0 -> SERIAL_ONE
        1 -> SERIAL_TWO
        2 -> SERIAL_THREE
        3 -> SERIAL_FOUR
        4 -> SERIAL_FIVE
        5 -> SERIAL_SIX
        else -> SERIAL_ONE
    }
}