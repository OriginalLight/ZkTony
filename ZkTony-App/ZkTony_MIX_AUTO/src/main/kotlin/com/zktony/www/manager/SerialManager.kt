package com.zktony.www.manager

import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.ext.hexFormat
import com.zktony.common.ext.hexToInt8
import com.zktony.common.ext.verifyHex
import com.zktony.common.utils.logd
import com.zktony.serialport.MutableSerial
import com.zktony.serialport.util.Serial
import com.zktony.serialport.util.Serial.TTYS0
import com.zktony.www.common.extension.toCommand
import com.zktony.www.manager.protocol.V1
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
    private val _ttys3Flow = MutableStateFlow<String?>(null)
    private val _lock = MutableStateFlow(false)
    private val _pause = MutableStateFlow(false)
    private val _reset = MutableStateFlow(true)

    val ttys0Flow = _ttys0Flow.asStateFlow()
    val ttys3Flow = _ttys3Flow.asStateFlow()
    val lock = _lock.asStateFlow()
    val pause = _pause.asStateFlow()
    val reset = _reset.asStateFlow()

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 30L

    init {
        scope.launch {
            launch {
                MutableSerial.instance.init(TTYS0, 115200)
                MutableSerial.instance.init(Serial.TTYS3, 115200)
            }
            launch {
                MutableSerial.instance.listener = { port, data ->
                    when (port) {
                        TTYS0 -> {
                            data.verifyHex().forEach {
                                _ttys0Flow.value = it
                               it.hexFormat().logd("串口一 receivedHex: ")
                            }
                        }
                        Serial.TTYS3 -> {
                            data.verifyHex().forEach {
                                _ttys3Flow.value = it
                                it.hexFormat().logd("串口三 receivedHex: ")
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
                        when (res.fn) {
                            "85" -> {
                                if (res.pa == "01") {
                                    val total = res.data.substring(2, 4).hexToInt8()
                                    val current = res.data.substring(6, 8).hexToInt8()
                                    _lock.value = total != current
                                    lockTime = 0L
                                }
                            }
                            "86" -> {
                                if (res.pa == "0A") {
                                    _lock.value = false
                                    lockTime = 0L
                                    _reset.value = true
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

    suspend fun reset() {
        while (lock.value) {
            delay(500L)
        }
        _lock.value = true
        lockTime = 0L
        sendHex(serial = TTYS0, hex = V1().toHex())
        sendHex(serial = TTYS0, hex = V1(pa = "0B", data = "0305").toHex())
    }

    fun pause(pause: Boolean) {
        _pause.value = pause
    }

    fun reset(reset: Boolean) {
        _reset.value = reset
    }

    /**
     * 发送Hex
     * @param serial 串口
     * @param hex 命令
     */
    fun sendHex(serial: Serial, hex: String, lock: Boolean = false) {
        scope.launch {
            MutableSerial.instance.sendHex(serial, hex)
            hex.hexFormat().logd("${serial.device} sendHex: ")
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
