package com.zktony.www.manager

import com.zktony.core.ext.*
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.serialConfig
import com.zktony.www.common.ext.toV1
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SerialManager {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helper by lazy { SerialHelper(serialConfig { device = "/dev/ttyS1" }) }

    private val _callback = MutableStateFlow<String?>(null)
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
                helper.openDevice()
            }
            launch {
                helper.callback = { hexHandler(it) }
            }
            launch { timerTask() }
        }
    }

    /**
     * 发送Hex
     * @param hex 命令
     */
    fun sendHex(hex: String, lock: Boolean = false) {
        helper.sendHex(hex)
        if (lock) {
            _lock.value = true
            lockTime = 0L
        }
        "串口一 sendHex: $hex".logd()
    }

    /**
     * Hex处理器
     * @param hex 命令
     */
    private fun hexHandler(hex: String) {
        hex.verifyHex().forEach {
            _callback.value = it
            val v1 = it.toV1()
            when (v1.fn) {
                "85" -> {
                    if (v1.pa == "01") {
                        val total = v1.data.substring(2, 4).hexToInt8()
                        val current = v1.data.substring(6, 8).hexToInt8()
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
        "串口管理器初始化完成！！！".logi()
    }

}
