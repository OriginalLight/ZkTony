package com.zktony.www.serialport

import com.zktony.serialport.MutableSerial
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
    private val _serialOneFlow = MutableStateFlow<String?>(null)
    private val _serialTwoFlow = MutableStateFlow<String?>(null)
    private val _serialThreeFlow = MutableStateFlow<String?>(null)
    private val _serialFourFlow = MutableStateFlow<String?>(null)

    val serialOneFlow = _serialOneFlow.asStateFlow()
    val serialTwoFlow = _serialTwoFlow.asStateFlow()
    val serialThreeFlow = _serialThreeFlow.asStateFlow()
    val serialFourFlow = _serialFourFlow.asStateFlow()

    // 机构运行状态
    private var lock = false

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private var waitTime = 60L * 2

    // 机构的抽屉状态
    private var drawer = false

    // 正在执行模块的个数
    private var executing = 0

    init {
        scope.launch {
            launch {
                MutableSerial.instance.init(SERIAL_ONE.device, 115200)
                MutableSerial.instance.init(SERIAL_TWO.device, 115200)
                MutableSerial.instance.init(SERIAL_THREE.device, 115200)
                MutableSerial.instance.init(SERIAL_FOUR.device, 57600)
            }
            launch {
                MutableSerial.instance.listener = { port, data ->
                    when (port) {
                        SERIAL_ONE.device -> {
                            data.verifyHex().forEach {
                                _serialOneFlow.value = it
                                Logger.d(msg = "串口一 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        SERIAL_TWO.device -> {
                            data.verifyHex().forEach {
                                _serialTwoFlow.value = it
                                Logger.d(msg = "串口二 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        SERIAL_THREE.device -> {
                            data.verifyHex().forEach {
                                _serialThreeFlow.value = it
                                Logger.d(msg = "串口三 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        SERIAL_FOUR.device -> {
                            _serialFourFlow.value = data.hexToAscii()
                            //Logger.d(msg = "串口四 receivedText: ${hexData.hexToAscii()}")
                        }
                    }
                }
            }
            launch {
                serialOneFlow.collect {
                    it?.let {
                        val res = it.toCommand()
                        if (res.function == "85" && res.parameter == "01") {
                            val total = res.data.substring(2, 4).hexToInt8()
                            val current = res.data.substring(6, 8).hexToInt8()
                            if (total == current) {
                                lock = false
                                lockTime = 0L
                                // 如果还有任务运行恢复摇床
                                if (executing > 0) {
                                    sendHex(SERIAL_ONE, Command.resumeShakeBed())
                                }
                            } else {
                                lock = true
                                lockTime = 0L
                            }
                        }
                        if (res.function == "86" && res.parameter == "01") {
                            drawer = res.data.hexToInt8() == 0
                        }
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    //Logger.d(msg = "lock: $lock, lockTime: $lockTime, executing: $executing, drawer: $drawer")
                    // 如果正在运行，计时
                    if (lock) {
                        lockTime += 1L
                    }
                    // 如果运行时间超过 60 秒，默认不运行，如果还有任务运行恢复摇床
                    if (lock && lockTime >= waitTime) {
                        lockTime = 0L
                        lock = false
                        // 恢复摇床
                        if (executing > 0) {
                            sendHex(SERIAL_ONE, Command.resumeShakeBed())
                        }
                    }
                    if (drawer) {
                        sendHex(SERIAL_ONE, Command.queryDrawer())
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
            MutableSerial.instance.sendHex(serialPort.device, hex)
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
            MutableSerial.instance.sendText(serialPort.device, text)
            //Logger.e(msg = "${serialPort.value} sendText: $text")
        }
    }

    /**
     * 获取锁
     * @return Boolean 获取到锁 true: 锁 false: 未锁
     */
    fun isLock() = lock

    /**
     *  设置锁
     *  @param lock [Boolean] 是否锁定 true 锁定 false 解锁
     */
    fun lock(lock: Boolean) {
        this.lock = lock
        if (lock) {
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

    /**
     * 获取正在执行的个数
     * @return [Int] 个数
     */
    fun getExecuting() = executing

    /**
     * 抽屉是否打开
     * @return [Boolean] true: 打开 false: 关闭
     */
    fun isDrawerOpen() = drawer


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
