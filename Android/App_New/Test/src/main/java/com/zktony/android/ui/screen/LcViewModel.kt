package com.zktony.android.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.ext.collectCallback
import com.zktony.android.logic.ext.getLock
import com.zktony.android.logic.ext.sendByteArray
import com.zktony.android.logic.ext.serialPort
import com.zktony.android.logic.ext.setLock
import com.zktony.core.ext.loge
import com.zktony.serialport.command.protocol
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author 刘贺贺
 * @date 2023/5/22 15:29
 */
class LcViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LcUiState())
    val uiState = _uiState

    init {
        viewModelScope.launch {
            launch {
                while (true) {
                    _uiState.value = _uiState.value.copy(vec = serialPort.array)
                    delay(100L)
                }
            }
            launch {
                collectCallback {
                    if (it.isNotEmpty()) {
                        val index = _uiState.value.replyIndex
                        val history = _uiState.value.replyHistory.toMutableList()
                        if (history.size > 100) {
                            history.removeAt(0)
                            history.add("${index + 1} ${it.toHexString()}")
                        } else {
                            history.add("${index + 1} ${it.toHexString()}")
                        }
                        _uiState.value =
                            _uiState.value.copy(
                                replyIndex = index + 1,
                                replyHistory = history
                            )
                    }
                }
            }
        }
    }

    fun test() {
        if (_uiState.value.start) {
            if (_uiState.value.job!!.isActive) {
                _uiState.value.job!!.cancel()
                _uiState.value = _uiState.value.copy(job = null, start = false)
            } else {
                _uiState.value = _uiState.value.copy(job = null, start = false)
            }
            _uiState.value = _uiState.value.copy(
                job = null,
                replyIndex = 0,
                replyHistory = emptyList(),
                queryIndex = 0,
                queryHistory = emptyList(),
                time = 0L,
            )
        } else {
            val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
                launch {
                    while (_uiState.value.start) {
                        _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                        delay(1000L)
                    }
                }
                launch {
                    val maxIndex = 6
                    val indexList = mutableListOf<Int>()
                    var bytes = byteArrayOf()
                    repeat(maxIndex) {
                        indexList.add(it)
                        bytes += combine(it, 1600L)
                    }
                    while (_uiState.value.start) {
                        try {
                            setLock(indexList)
                            withTimeout(10000L) {
                                val p = protocol {
                                    data = bytes
                                }
                                p.toByteArray().toHexString().loge()
                                sendByteArray(p.toByteArray())
                                history(p.toByteArray())
                                while (getLock(indexList)) {
                                    delay(10L)
                                }
                            }
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(start = false)
                        }
                    }
                }
            }
            _uiState.value = _uiState.value.copy(job = job, start = true)
            job.start()
        }
    }


    private fun combine(id: Int, step: Long): ByteArray {
        val bytes = ByteArray(11)
        return bytes.writeInt8(id, 0)
            .writeInt32LE(step, 1)
            .writeInt16LE(3200, 5)
            .writeInt16LE(3200, 7)
            .writeInt16LE(12800, 9)
    }


    private fun history(bytes: ByteArray) {
        val index = _uiState.value.queryIndex
        val history = _uiState.value.queryHistory.toMutableList()
        if (history.size > 100) {
            history.removeAt(0)
            history.add("${index + 1} ${bytes.toHexString()}")
        } else {
            history.add("${index + 1} ${bytes.toHexString()}")
        }
        _uiState.value =
            _uiState.value.copy(
                queryIndex = index + 1,
                queryHistory = history
            )
    }
}

data class LcUiState(
    val job: Job? = null,
    val start: Boolean = false,
    val queryIndex: Int = 0,
    val replyIndex: Int = 0,
    val queryHistory: List<String> = listOf(),
    val replyHistory: List<String> = listOf(),
    val vec: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList(),
    val time: Long = 0L,
    val log: String = "",
)