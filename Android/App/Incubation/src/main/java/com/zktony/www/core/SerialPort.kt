package com.zktony.www.core

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelpers
import com.zktony.serialport.config.serialConfig
import com.zktony.serialport.ext.hexToAscii
import com.zktony.serialport.ext.hexToInt
import com.zktony.serialport.ext.splitString
import com.zktony.serialport.protocol.toV1
import com.zktony.www.core.ext.syncHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SerialPort {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helpers by lazy { SerialHelpers() }

    private val _callback = MutableStateFlow<Pair<Int, String?>>(Pair(0, null))

    // 下位机机构运行状态
    private val _lock = MutableStateFlow(false)

    val callback = _callback.asStateFlow()
    val lock = _lock.asStateFlow()

    val drawer = AtomicBoolean(false)

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 60L


    init {
        scope.launch {
            launch {
                helpers.init(
                    serialConfig {
                        index = 0
                        device = "/dev/ttyS0"
                    },
                    serialConfig {
                        index = 1
                        device = "/dev/ttyS1"
                    },
                    serialConfig {
                        index = 2
                        device = "/dev/ttyS2"
                    },
                    serialConfig {
                        index = 3
                        device = "/dev/ttyS3"
                        baudRate = 57600
                    }
                )
            }
            launch {
                helpers.callback = { index, hex ->
                    hexHandler(index, hex)
                }
            }
            launch { timerTask() }
        }
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

    /**
     * 发送Text
     * @param text 命令
     */
    fun sendText(text: String) {
        helpers.sendText(3, text)
    }

    /**
     * Hex处理器
     * @param hex 命令
     */
    private fun hexHandler(index: Int, hex: String) {
        if (index != 3) {
            hex.splitString("EE", "FFFCFFFF").forEach {
                _callback.value = Pair(index, it)
                val v1 = it.toV1()
                if (v1 != null && index == 0) {
                    when (v1.fn) {
                        "85" -> {
                            if (v1.pa == "01") {
                                val total = v1.data.substring(2, 4).hexToInt()
                                val current = v1.data.substring(6, 8).hexToInt()
                                _lock.value = total != current
                                lockTime = 0L
                            }
                        }

                        "86" -> {
                            if (v1.pa == "01") {
                                drawer.set(v1.data.hexToInt() == 0)
                            }
                            if (v1.pa == "0A") {
                                _lock.value = false
                                lockTime = 0L
                            }
                        }
                    }
                }
            }
        } else {
            _callback.value = Pair(index, hex.hexToAscii())
        }

    }

    /**
     * 定时任务
     *
     * 每秒钟执行一次 超过 [waitTime] 没有收到串口数据则认为机构已经停止运行
     */
    private suspend fun timerTask() {
        while (true) {
            delay(1000L)
            if (_lock.value) {
                lockTime += 1L
                if (lockTime >= waitTime) {
                    _lock.value = false
                    lockTime = 0L
                }
            }
            if (drawer.get()) {
                syncHex(0) {
                    pa = "0C"
                }
            }
        }
    }

    fun initializer() {
        "SerialPort initializer".logi()
    }
}
