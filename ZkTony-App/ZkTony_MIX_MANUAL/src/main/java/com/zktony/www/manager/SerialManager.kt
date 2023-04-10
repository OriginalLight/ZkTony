package com.zktony.www.manager

import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.ext.hexToInt8
import com.zktony.core.ext.logi
import com.zktony.serialport.SerialConfig
import com.zktony.serialport.SerialMap
import com.zktony.www.common.ext.toCommand
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SerialManager constructor(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val serialMap by lazy { SerialMap() }

    private val _callback = MutableStateFlow<String?>(null)
    private val _lock = MutableStateFlow(false)
    private val _reset = MutableStateFlow(true)

    val callback = _callback.asStateFlow()
    val lock = _lock.asStateFlow()
    val reset = _reset.asStateFlow()

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 2 * 60L

    init {
        scope.launch {
            launch {
                serialMap.init(SerialConfig(
                    index = 0,
                    device = "/dev/ttyS0",
                ))
            }
            launch {
                serialMap.callback = { index, data ->
                    if (index == 0) {
                        _callback.value = data
                    }
                }
            }
            launch {
                callback.collect {
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
        _reset.value = true
        _lock.value = true
        sendHex(hex = V1(pa = "0B", data = "0305").toHex())
        delay(2000L)
        _lock.value = false
    }

    /**
     * 发送Hex
     * @param hex 命令
     */
    fun sendHex(hex: String, lock: Boolean = false) {
        serialMap.sendHex(0, hex)
        if (lock) {
            _lock.value = true
            lockTime = 0L
        }
    }

    fun test() {
        scope.launch { "SerialManager test".logi() }
    }
}
