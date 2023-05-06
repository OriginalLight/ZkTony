package com.zktony.www.proxy

import com.zktony.core.ext.*
import com.zktony.serialport.SerialHelpers
import com.zktony.serialport.config.serialConfig
import com.zktony.serialport.ext.hexToInt
import com.zktony.serialport.protocol.toV1
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SerialProxy {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helpers by lazy { SerialHelpers() }

    private val _callback = MutableStateFlow<Pair<Int, String?>>(Pair(0, null))
    private val _lock = MutableStateFlow(false)

    val callback = _callback.asStateFlow()
    val lock = _lock.asStateFlow()

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 30L

    init {
        scope.launch {
            launch {
                helpers.init(
                    serialConfig {
                        index = 0
                        device = "/dev/ttyS0"
                    },
                    serialConfig {
                        index = 3
                        device = "/dev/ttyS3"
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
     * Hex处理器
     * @param hex 命令
     */
    private fun hexHandler(index: Int, hex: String) {
        _callback.value = Pair(index, hex)
        val v1 = hex.toV1()
        if (index == 0 && v1 != null) {
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
                    if (v1.pa == "0A") {
                        _lock.value = false
                        lockTime = 0L
                    }
                }
            }
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
        }
    }

    fun initializer() {
        "SerialProxy initializer".logi()
    }

}