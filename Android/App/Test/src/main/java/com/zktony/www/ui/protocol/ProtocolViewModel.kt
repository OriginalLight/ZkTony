package com.zktony.www.ui.protocol

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.*
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.config.SPLIT
import com.zktony.serialport.config.serialConfig
import com.zktony.serialport.protocol.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProtocolViewModel : BaseViewModel() {

    private val helper by lazy {
        SerialHelper(serialConfig {
            device = "/dev/ttyS0"
            crc16 = true
            split = SPLIT.V2
        })
    }
    private val _uiState = MutableStateFlow(ProtocolUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                helper.openDevice()
            }
            launch {
                helper.callback = { hexHandler(it) }
            }
        }
    }

    fun sync() {
        if (_uiState.value.syncJob == null) {
            _uiState.value = ProtocolUiState()

            val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
                launch {
                    while (true) {
                        delay(1000L)
                        _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                    }
                }
                launch {
                    while (true) {
                        // 随机生成 0 - 15
                        val random = (0..15).random()
                        val step = (3200.. 64000).random()
                        val speed = (100..1000).random()
                        val acc = (10..100).random()
                        val dec = (10..100).random()
                        val dataStr = random.intToHex() + step.intToHex4() + speed.intToHex2() + acc.intToHex2() + dec.intToHex2()
                        val hex = v2 { data = dataStr }
                        _uiState.value = _uiState.value.copy(sendText = hex, sendNum = _uiState.value.sendNum + 1)
                        helper.sendHex(hex)
                        delay(5000L)
                        while (_uiState.value.map[random] != 0) {
                            delay(100L)
                        }
                    }
                }
            }
            _uiState.value = _uiState.value.copy(syncJob = job)
            job.start()
        } else {
            _uiState.value.syncJob?.cancel()
            _uiState.value = _uiState.value.copy(syncJob = null)
        }
    }

    fun async() {

    }

    private fun hexHandler(hex: String) {
        _uiState.value = _uiState.value.copy(receiveText = hex, receiveNum = _uiState.value.receiveNum + 1)
        val v2 = hex.toV2()
        if (v2 != null && v2.addr == "02" && v2.fn == "01") {
            val key = v2.data.substring(0, 2).toInt(16)
            val value = v2.data.substring(2, 4).toInt(16)
            val map = _uiState.value.map
            map[key] = value
            _uiState.value = _uiState.value.copy(map = map)
        }
    }

}

data class ProtocolUiState(
    val map: MutableMap<Int, Int> = hashMapOf(
        0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0, 6 to 0, 7 to 0, 8 to 0,
        9 to 0, 10 to 0, 11 to 0, 12 to 0, 13 to 0, 14 to 0, 15 to 0
    ),
    val syncJob: Job? = null,
    val asyncJob: Job? = null,
    val sendNum: Int = 0,
    val receiveNum: Int = 0,
    val sendText: String = "",
    val receiveText: String = "",
    val time: Long = 0L,
)