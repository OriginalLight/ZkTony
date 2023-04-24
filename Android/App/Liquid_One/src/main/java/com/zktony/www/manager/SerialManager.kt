package com.zktony.www.manager

import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.ext.*
import com.zktony.serialport.SerialConfig
import com.zktony.serialport.SerialHelpers
import com.zktony.serialport.protocol.V1
import com.zktony.www.common.ext.toV1
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SerialManager {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helpers by lazy { SerialHelpers() }

    private val _callback = MutableStateFlow<String?>(null)
    private val _lock = MutableStateFlow(false)
    private val _pause = MutableStateFlow(false)

    val callback = _callback.asStateFlow()
    val lock = _lock.asStateFlow()
    val pause = _pause.asStateFlow()

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 30L

    init {
        scope.launch {
            launch {
                helpers.init(
                    SerialConfig(
                        index = 0,
                        device = "/dev/ttyS0",
                    )
                )
            }
            launch {
                helpers.callback = { index, data ->
                    when (index) {
                        0 -> {
                            data.verifyHex().forEach {
                                _callback.value = it
                                it.hexFormat().logd("串口一 receivedHex: ")
                            }
                        }

                        else -> {}
                    }
                }
            }
            launch {
                callback.collect {
                    it?.let {
                        val res = it.toV1()
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
        sendHex(hex = V1().toHex())
    }

    fun pause(pause: Boolean) {
        _pause.value = pause
    }

    /**
     * 发送Hex
     * @param hex 命令
     */
    fun sendHex(hex: String, lock: Boolean = false) {
        helpers.sendHex(0, hex)
        if (lock) {
            _lock.value = true
            lockTime = 0L
        }
    }

    fun initializer() {
        "串口管理器初始化完成！！！".logi()
    }

}
