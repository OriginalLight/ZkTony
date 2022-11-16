package com.zktony.www.serialport

import com.zktony.serialport.COMSerial
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.www.common.extension.hexFormat
import com.zktony.www.common.extension.hexToAscii
import com.zktony.www.common.extension.verifyHex
import com.zktony.www.common.model.Queue
import com.zktony.www.common.utils.Logger
import com.zktony.www.serialport.SerialPort.*
import com.zktony.www.serialport.protocol.Command
import com.zktony.www.serialport.protocol.CommandBlock
import com.zktony.www.ui.home.ModuleEnum
import com.zktony.www.ui.home.ModuleEnum.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SerialPortManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    val commandQueue = Queue<List<CommandBlock>>()

    private var runningOne = false
    private var runningTwo = false
    private var runningThree = false
    private var runningFour = false

    private val _responseOne = MutableStateFlow<String?>(null)
    private val _responseTwo = MutableStateFlow<String?>(null)
    private val _responseThree = MutableStateFlow<String?>(null)
    private val _responseFour = MutableStateFlow<String?>(null)

    val responseOne = _responseOne.asStateFlow()
    val responseTwo = _responseTwo.asStateFlow()
    val responseThree = _responseThree.asStateFlow()
    val responseFour = _responseFour.asStateFlow()

    init {
        scope.launch {
            launch {
                COMSerial.instance.addCOM(SERIAL_ONE.device, 115200)
                COMSerial.instance.addCOM(SERIAL_TWO.device, 115200)
                COMSerial.instance.addCOM(SERIAL_THREE.device, 115200)
                COMSerial.instance.addCOM(SERIAL_FOUR.device, 57600)
            }
            launch {
                COMSerial.instance.addDataListener(object : OnComDataListener {
                    override fun comDataBack(com: String, hexData: String) {
                        when (com) {
                            SERIAL_ONE.device -> {
                                hexData.verifyHex().forEach {
                                    _responseOne.value = it
                                    Logger.d(msg = "串口一 receivedHex: ${it.hexFormat()}")
                                }
                            }
                            SERIAL_TWO.device -> {
                                hexData.verifyHex().forEach {
                                    _responseTwo.value = it
                                    Logger.d(msg = "串口二 receivedHex: ${it.hexFormat()}")
                                }
                            }
                            SERIAL_THREE.device -> {
                                hexData.verifyHex().forEach {
                                    _responseThree.value = it
                                    Logger.d(msg = "串口三 receivedHex: ${it.hexFormat()}")
                                }
                            }
                            SERIAL_FOUR.device -> {
                                _responseFour.value = hexData.hexToAscii()
                                Logger.d(msg = "串口四 receivedText: ${hexData.hexFormat()}")
                            }
                        }
                    }
                })
            }
        }
    }

    /**
     * 发送Hex
     * @param serialPort 串口
     * @param command 命令
     */
    fun sendHex(serialPort: SerialPort, command: String) {
        scope.launch {
            COMSerial.instance.sendHex(serialPort.device, command)
            Logger.e(msg = "${serialPort.value} sendHex: ${command.hexFormat()}")
        }
    }

    /**
     * 发送Text
     * @param serialPort 串口
     * @param command 命令
     */
    fun sendText(serialPort: SerialPort, command: String) {
        scope.launch {
            COMSerial.instance.sendText(serialPort.device, command)
            Logger.e(msg = "${serialPort.value} sendText: $command")
        }
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
            if (!commandQueue.isEmpty() && commandQueue.peek() == it) {
                commandQueue.dequeue()
            }
            if (commandQueue.isEmpty() && (runningOne || runningTwo || runningThree || runningFour)) {
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
            A -> runningOne
            B -> runningTwo
            C -> runningThree
            D -> runningFour
        }
    }

    /**
     * 设置模块运行状态
     * @param module 模块
     * @param running 运行状态
     */
    fun setModuleRunning(module: ModuleEnum, running: Boolean) {
        when (module) {
            A -> runningOne = running
            B -> runningTwo = running
            C -> runningThree = running
            D -> runningFour = running
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

enum class SerialPort(val device: String, val value: String, val index: Int) {
    SERIAL_ONE("/dev/ttyS0", "串口一", 0),
    SERIAL_TWO("/dev/ttyS1", "串口二", 1),
    SERIAL_THREE("/dev/ttyS2", "串口三", 2),
    SERIAL_FOUR("/dev/ttyS3", "串口四", 3),
    SERIAL_FIVE("/dev/ttyS4", "串口五", 4),
    SERIAL_SIX("/dev/ttyS5", "串口六", 5),
}

fun getSerialPort(index: Int): SerialPort {
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
