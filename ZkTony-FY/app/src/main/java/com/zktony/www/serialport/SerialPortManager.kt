package com.zktony.www.serialport

import com.zktony.serialport.MutableSerial
import com.zktony.www.common.extension.*
import com.zktony.www.common.utils.Logger
import com.zktony.www.serialport.Serial.*
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
                MutableSerial.instance.init(TTYS0.device, 115200)
                MutableSerial.instance.init(TTYS1.device, 115200)
                MutableSerial.instance.init(TTYS2.device, 115200)
                MutableSerial.instance.init(TTYS3.device, 57600)
            }
            launch {
                MutableSerial.instance.listener = { port, data ->
                    when (port) {
                        TTYS0.device -> {
                            data.verifyHex().forEach {
                                _serialOneFlow.value = it
                                Logger.d(msg = "串口一 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        TTYS1.device -> {
                            data.verifyHex().forEach {
                                _serialTwoFlow.value = it
                                Logger.d(msg = "串口二 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        TTYS2.device -> {
                            data.verifyHex().forEach {
                                _serialThreeFlow.value = it
                                Logger.d(msg = "串口三 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        TTYS3.device -> {
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
                                    sendHex(TTYS0, Command.resumeShakeBed())
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
                            sendHex(TTYS0, Command.resumeShakeBed())
                        }
                    }
                    if (drawer) {
                        sendHex(TTYS0, Command.queryDrawer())
                    }
                }
            }
        }
    }

    /**
     * 发送Hex
     * @param serial 串口
     * @param hex 命令
     */
    fun sendHex(serial: Serial, hex: String) {
        scope.launch {
            MutableSerial.instance.sendHex(serial.device, hex)
            Logger.e(msg = "${serial.value} sendHex: ${hex.hexFormat()}")
        }
    }

    /**
     * 发送Text
     * @param serial 串口
     * @param text 命令
     */
    fun sendText(serial: Serial, text: String) {
        scope.launch {
            MutableSerial.instance.sendText(serial.device, text)
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
            sendHex(TTYS0, Command.pauseShakeBed())
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

enum class Serial(val device: String, val value: String, val index: Int) {
    TTYS0("/dev/ttyS0", "串口一", 0),
    TTYS1("/dev/ttyS1", "串口二", 1),
    TTYS2("/dev/ttyS2", "串口三", 2),
    TTYS3("/dev/ttyS3", "串口四", 3),
    TTYS4("/dev/ttyS4", "串口五", 4),
    TTYS5("/dev/ttyS5", "串口六", 5),
}
