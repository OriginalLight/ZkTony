package com.zktony.www.manager

import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.ext.*
import com.zktony.serialport.SerialConfig
import com.zktony.serialport.SerialHelpers
import com.zktony.www.common.ext.toV1
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SerialManager {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helpers by lazy { SerialHelpers() }

    private val _ttys0Flow = MutableStateFlow<String?>(null)
    private val _ttys1Flow = MutableStateFlow<String?>(null)
    private val _ttys2Flow = MutableStateFlow<String?>(null)
    private val _ttys3Flow = MutableStateFlow<String?>(null)

    // 下位机机构运行状态
    private val _lock = MutableStateFlow(false)

    // 程序运行状态
    private val _run = MutableStateFlow(false)

    // 抽屉状态
    private val _drawer = MutableStateFlow(false)

    // 摇床状态
    private val _swing = MutableStateFlow(false)

    val ttys0Flow = _ttys0Flow.asStateFlow()
    val ttys1Flow = _ttys1Flow.asStateFlow()
    val ttys2Flow = _ttys2Flow.asStateFlow()
    val ttys3Flow = _ttys3Flow.asStateFlow()
    val lock = _lock.asStateFlow()
    val run = _run.asStateFlow()
    val drawer = _drawer.asStateFlow()
    val swing = _swing.asStateFlow()

    // 机构运行已经等待的时间
    private var lockTime = 0L

    // 机构运行小步骤等待时间
    private val waitTime = 60L


    init {
        scope.launch {
            launch {
                helpers.init(
                    SerialConfig(
                        index = 0,
                        device = "/dev/ttyS0",
                    ),
                    SerialConfig(
                        index = 1,
                        device = "/dev/ttyS1",
                    ),
                    SerialConfig(
                        index = 2,
                        device = "/dev/ttyS2",
                    ), SerialConfig(
                        index = 3,
                        device = "/dev/ttyS3",
                        baudRate = 57600,
                    )

                )
            }
            launch {
                helpers.callback = { index, data ->
                    when (index) {
                        0 -> {
                            data.verifyHex().forEach {
                                _ttys0Flow.value = it
                            }
                        }

                        1 -> {
                            data.verifyHex().forEach {
                                _ttys1Flow.value = it
                            }
                        }

                        2 -> {
                            data.verifyHex().forEach {
                                _ttys2Flow.value = it
                            }
                        }

                        3 -> {
                            _ttys3Flow.value = data.hexToAscii()
                        }

                        else -> {}
                    }
                }
            }
            launch {
                ttys0Flow.collect {
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
                                if (res.pa == "01") {
                                    _drawer.value = res.data.hexToInt8() == 0
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
            }
            launch {
                while (true) {
                    delay(1000L)
                    //Logger.d(msg = "lock: ${lock.value}, lock: ${lock.value}, run: ${run.value}, drawer: ${drawer.value}")
                    // 如果正在运行，计时
                    if (lock.value) {
                        lockTime += 1L
                    }
                    // 如果运行时间超过 60 秒，默认不运行，如果还有任务运行恢复摇床
                    if (lock.value && lockTime >= waitTime) {
                        lockTime = 0L
                        _lock.value = false
                    }
                    if (_drawer.value) {
                        sendHex(0, V1.queryDrawer())
                    }
                }
            }
            launch {
                run.collect { shakeBed(it) }
            }
        }
    }

    fun init() {
        scope.launch {
            "串口管理器初始化完成！！！".logi()
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
     * @param index 串口
     * @param text 命令
     */
    fun sendText(index: Int, text: String) {
        helpers.sendText(index, text)
    }

    suspend fun reset() {
        while (lock.value) {
            delay(500L)
        }
        _lock.value = true
        lockTime = 0L
        sendHex(index = 0, hex = V1().toHex())
    }

    fun setTemp(temp: String, addr: Int) {
        scope.launch {
            sendText(3, "TC1:TCSW=0@$addr\r")
            delay(30 * 1000L)
            sendText(3, "TC1:TCSW=1@$addr\r")
            delay(1000L)
            sendText(3, "TC1:TCADJUSTTEMP=$temp@$addr\r")
        }
    }

    fun run(flag: Boolean) {
        _run.value = flag
    }

    fun swing(flag: Boolean) {
        _swing.value = flag
    }

    private suspend fun shakeBed(flag: Boolean) {
        if (flag) {
            while (lock.value) {
                delay(500L)
            }
            sendHex(0, V1.resumeShakeBed())
            _swing.value = true
        } else {
            while (lock.value) {
                delay(500L)
            }
            sendHex(0, V1.pauseShakeBed())
            _swing.value = false
        }
    }
}
