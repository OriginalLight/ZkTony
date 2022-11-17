package com.zktony.www.serialport

import com.zktony.serialport.COMSerial
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.www.common.extension.*
import com.zktony.www.common.utils.Logger
import com.zktony.www.serialport.SerialPort.*
import com.zktony.www.serialport.protocol.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SerialPortManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _responseOne = MutableStateFlow<String?>(null)
    private val _responseTwo = MutableStateFlow<String?>(null)
    private val _responseThree = MutableStateFlow<String?>(null)
    private val _responseFour = MutableStateFlow<String?>(null)

    val responseOne = _responseOne.asStateFlow()
    val responseTwo = _responseTwo.asStateFlow()
    val responseThree = _responseThree.asStateFlow()
    val responseFour = _responseFour.asStateFlow()

    // 机构运行状态
    private var running = false
    private var runtime = 0L
    // 正在执行的个数
    private var executing = 0

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
                                //Logger.d(msg = "串口四 receivedText: ${hexData.hexToAscii()}")
                            }
                        }
                    }
                })
            }
            launch {
                responseOne.collect {
                    it?.let {
                        val res = it.toCommand()
                        if(res.function == "85" && res.parameter == "01") {
                            val total = res.data.substring(2, 4).hexToInt8()
                            val current = res.data.substring(6, 8).hexToInt8()
                            if (total == current) {
                                running = false
                                runtime = 0L
                                // 如果还有任务运行恢复摇床
                                if (executing > 0) {
                                    Logger.d(msg = "摇床恢复")
                                    sendHex(SERIAL_ONE, Command.resumeShakeBed())
                                }
                            } else {
                                running = true
                                runtime = 0L
                            }
                        }
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    Logger.d(msg = "running: $running, runtime: $runtime, executing: $executing")
                    // 如果正在运行，计时 否则清零
                    if (running) {
                        runtime += 1L
                    } else {
                        runtime = 0L
                    }
                    // 如果运行时间超过 60 秒，默认不运行，如果还有任务运行恢复摇床
                    if (running && runtime >= 60L) {
                        runtime = 0L
                        running = false
                        // 恢复摇床
                        if (executing > 0) {
                            Logger.d(msg = "恢复摇床")
                            sendHex(SERIAL_ONE, Command.resumeShakeBed())
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送Hex
     * @param serialPort 串口
     * @param hex 命令
     */
    fun sendHex(serialPort: SerialPort, hex: String) {
        scope.launch {
            COMSerial.instance.sendHex(serialPort.device, hex)
            Logger.e(msg = "${serialPort.value} sendHex: ${hex.hexFormat()}")
        }
    }

    /**
     * 发送Text
     * @param serialPort 串口
     * @param text 命令
     */
    fun sendText(serialPort: SerialPort, text: String) {
        scope.launch {
            COMSerial.instance.sendText(serialPort.device, text)
            //Logger.e(msg = "${serialPort.value} sendText: $text")
        }
    }

    fun isRunning() = running

    fun run(run : Boolean) {
        running = run
        if (run) {
            sendHex(SERIAL_ONE, Command.pauseShakeBed())
        }
    }

    /**
     * 设置正在执行的个数
     * @param count 个数
     */
    fun setExecuting(count: Int) {
        executing = count
    }

    fun getExecuting() = executing

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
