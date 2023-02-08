package com.zktony.www.control.serial

import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.serialport.MutableSerial
import com.zktony.serialport.util.Serial
import com.zktony.serialport.util.Serial.*
import com.zktony.www.common.extension.*
import com.zktony.www.common.utils.Logger
import com.zktony.www.control.serial.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SerialManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _ttys0Flow = MutableStateFlow<String?>(null)
    private val _ttys1Flow = MutableStateFlow<String?>(null)
    private val _ttys2Flow = MutableStateFlow<String?>(null)
    private val _ttys3Flow = MutableStateFlow<String?>(null)
    private val _lock = MutableStateFlow(false)

    val ttys0Flow = _ttys0Flow.asStateFlow()
    val ttys1Flow = _ttys1Flow.asStateFlow()
    val ttys2Flow = _ttys2Flow.asStateFlow()
    val ttys3Flow = _ttys3Flow.asStateFlow()
    val lock = _lock.asStateFlow()

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 60L

    // 机构的抽屉状态
    var drawer = false

    // 正在执行模块的个数
    var executing = 0

    init {
        scope.launch {
            launch {
                MutableSerial.instance.init(TTYS0, 115200)
                MutableSerial.instance.init(TTYS1, 115200)
                MutableSerial.instance.init(TTYS2, 115200)
                MutableSerial.instance.init(TTYS3, 57600)
            }
            launch {
                MutableSerial.instance.listener = { port, data ->
                    when (port) {
                        TTYS0 -> {
                            data.verifyHex().forEach {
                                _ttys0Flow.value = it
                                //Logger.d(msg = "串口一 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        TTYS1 -> {
                            data.verifyHex().forEach {
                                _ttys1Flow.value = it
                                //Logger.d(msg = "串口二 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        TTYS2 -> {
                            data.verifyHex().forEach {
                                _ttys2Flow.value = it
                                //Logger.d(msg = "串口三 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        TTYS3 -> {
                            _ttys3Flow.value = data.hexToAscii()
                            //Logger.d(msg = "串口四 receivedText: ${hexData.hexToAscii()}")
                        }
                        else -> {}
                    }
                }
            }
            launch {
                ttys0Flow.collect {
                    it?.let {
                        val res = it.toV1()
                        if (res.fn == "85" && res.pa == "01") {
                            val total = res.data.substring(2, 4).hexToInt8()
                            val current = res.data.substring(6, 8).hexToInt8()
                            if (total == current) {
                                _lock.value = false
                                lockTime = 0L
                                // 如果还有任务运行恢复摇床
                                if (executing > 0) {
                                    sendHex(TTYS0, V1.resumeShakeBed())
                                }
                            } else {
                                _lock.value = true
                                lockTime = 0L
                            }
                        }
                        if (res.fn == "86") {
                            if (res.pa == "01") {
                                drawer = res.data.hexToInt8() == 0
                            }
                            if (res.pa == "0A") {
                                _lock.value = false
                                lockTime = 0L
                                PopTip.show("复位成功")
                            }
                        }
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    Logger.d(msg = "lock: ${lock.value}, lockTime: $lockTime, executing: $executing, drawer: $drawer")
                    // 如果正在运行，计时
                    if (lock.value) {
                        lockTime += 1L
                    }
                    // 如果运行时间超过 60 秒，默认不运行，如果还有任务运行恢复摇床
                    if (lock.value && lockTime >= waitTime) {
                        lockTime = 0L
                        _lock.value = false
                        // 恢复摇床
                        if (executing > 0) {
                            sendHex(TTYS0, V1.resumeShakeBed())
                        }
                    }
                    if (drawer) {
                        sendHex(TTYS0, V1.queryDrawer())
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
    fun sendHex(serial: Serial, hex: String, lock: Boolean = false) {
        scope.launch {
            MutableSerial.instance.sendHex(serial, hex)
            Logger.e(msg = "${serial.value} sendHex: ${hex.hexFormat()}")
            if (lock) {
                _lock.value = true
                lockTime = 0L
            }
        }
    }

    /**
     * 发送Text
     * @param serial 串口
     * @param text 命令
     */
    fun sendText(serial: Serial, text: String) {
        scope.launch {
            MutableSerial.instance.sendText(serial, text)
            //Logger.e(msg = "${serialPort.value} sendText: $text")
        }
    }

    /**
     *  设置锁
     *  @param lock [Boolean] 是否锁定 true 锁定 false 解锁
     */
    fun lock(lock: Boolean) {
        _lock.value = lock
        if (lock) {
            sendHex(TTYS0, V1.pauseShakeBed())
        }
    }

    suspend fun reset() {
        if (lock.value) {
            delay(300L)
            reset()
        } else {
            _lock.value = true
            lockTime = 0L
            sendHex(serial = TTYS0, hex = V1().toHex())
        }

    }

    fun setTemp(temp: String, addr: String) {
        scope.launch {
            sendText(TTYS3, "TC1:TCSW=0@$addr\r")
            delay(30 * 1000L)
            sendText(TTYS3, "TC1:TCSW=1@$addr\r")
            delay(1000L)
            sendText(TTYS3, "TC1:TCADJUSTTEMP=$temp@$addr\r")
        }
    }


    companion object {
        @JvmStatic
        val instance: SerialManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SerialManager()
        }
    }
}
