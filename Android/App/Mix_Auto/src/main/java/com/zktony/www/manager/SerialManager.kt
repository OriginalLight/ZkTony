package com.zktony.www.manager

import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.ext.Ext
import com.zktony.core.ext.hexFormat
import com.zktony.core.ext.hexToInt8
import com.zktony.core.ext.logd
import com.zktony.core.ext.logi
import com.zktony.core.ext.verifyHex
import com.zktony.serialport.SerialConfig
import com.zktony.serialport.SerialHelpers
import com.zktony.www.common.ext.toCommand
import com.zktony.serialport.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SerialManager {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helpers by lazy { SerialHelpers() }

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
    private val waitTime = 3 * 60L

    init {
        scope.launch {
            launch {
                helpers.init(
                    SerialConfig(
                        index = 0,
                        device = "/dev/ttyS0",
                    ), SerialConfig(
                        index = 3,
                        device = "/dev/ttyS3",
                    )

                )
            }
            launch {
                helpers.callback = { index, data ->
                    when (index) {
                        0 -> {
                            data.verifyHex().forEach {
                                _ttys0Flow.value = it
                                it.hexFormat().logd("串口一 receivedHex: ")
                            }
                        }

                        3 -> {
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
                                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.reset_success))
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
        sendHex(index = 0, hex = V1().toHex())
        sendHex(index = 3, hex = V1(pa = "0B", data = "0305").toHex())
    }

    fun pause(pause: Boolean) {
        _pause.value = pause
    }

    fun reset(reset: Boolean) {
        _reset.value = reset
    }

    fun lock(lock: Boolean) {
        _lock.value = lock
        lockTime = 0L
    }

    /**
     * 发送Hex
     * @param index 串口
     * @param hex 命令
     */
    fun sendHex(index: Int, hex: String, lock: Boolean = false) {
        helpers.sendHex(index, hex)
        if (lock) {
            _lock.value = true
            lockTime = 0L
        }
    }

    fun initializer() {
        "串口管理器初始化完成！！！".logi()
    }
}
