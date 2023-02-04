package com.zktony.www.control.serial

import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.serialport.MutableSerial
import com.zktony.serialport.util.Serial
import com.zktony.serialport.util.Serial.TTYS0
import com.zktony.serialport.util.Serial.TTYS2
import com.zktony.www.common.extension.hexFormat
import com.zktony.www.common.extension.hexToInt8
import com.zktony.www.common.extension.toCommand
import com.zktony.www.common.extension.verifyHex
import com.zktony.www.common.utils.Logger
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
    private val _ttys2Flow = MutableStateFlow<String?>(null)
    private val _lock = MutableStateFlow(false)
    private val _work = MutableStateFlow(false)

    val ttys0Flow = _ttys0Flow.asStateFlow()
    val ttys2Flow = _ttys2Flow.asStateFlow()
    val lock = _lock.asStateFlow()
    val work = _work.asStateFlow()

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 30L

    init {
        scope.launch {
            launch {
                MutableSerial.instance.init(TTYS0, 115200)
                MutableSerial.instance.init(TTYS2, 115200)
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
                        TTYS2 -> {
                            data.verifyHex().forEach {
                                _ttys2Flow.value = it
                                //Logger.d(msg = "串口三 receivedHex: ${it.hexFormat()}")
                            }
                        }
                        else -> {}
                    }
                }
            }
            launch {
                ttys0Flow.collect {
                    it?.let {
                        val res = it.toCommand()
                        when (res.function) {
                            "85" -> {
                                if (res.parameter == "01") {
                                    val total = res.data.substring(2, 4).hexToInt8()
                                    val current = res.data.substring(6, 8).hexToInt8()
                                    _lock.value = total != current
                                    lockTime = 0L
                                }
                            }
                            "86" -> {
                                if (res.parameter == "0A") {
                                    _lock.value = false
                                    lockTime = 0L
                                    PopTip.show("复位成功")
                                }
                            }
                        }
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    if (_lock.value) {
                        lockTime += 1L
                        if (lockTime >= waitTime) {
                            _lock.value = false
                            lockTime = 0L
                        }
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


    companion object {
        @JvmStatic
        val instance: SerialManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SerialManager()
        }
    }
}