package com.zktony.android.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.ext.collectCallback
import com.zktony.android.logic.ext.getLock
import com.zktony.android.logic.ext.sendByteArray
import com.zktony.android.logic.ext.serialPort
import com.zktony.android.logic.ext.setLock
import com.zktony.core.ext.logi
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

    fun test1() {
        if (_uiState.value.job != null) {
            _uiState.value.job?.cancel()
            _uiState.value = _uiState.value.copy(
                job = null,
                replyIndex = 0,
                replyHistory = emptyList(),
                queryIndex = 0,
                queryHistory = emptyList()
            )
        } else {
            val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
                while (true) {
                    val id = 3
                    val step = 6400L
                    val speed = 3200
                    val acc = 6400
                    val dec = 6400
                    val bytes = ByteArray(11)
                    bytes.writeInt8(id, 0).writeInt32LE(step, 1).writeInt16LE(speed, 5)
                        .writeInt16LE(acc, 7).writeInt16LE(dec, 9)
                    val bytes1 = ByteArray(11)
                    bytes1.writeInt8(id + 1, 0).writeInt32LE(step, 1).writeInt16LE(speed, 5)
                        .writeInt16LE(acc, 7).writeInt16LE(dec, 9)
                    val bytes2 = ByteArray(11)
                    bytes2.writeInt8(id + 2, 0).writeInt32LE(step, 1).writeInt16LE(speed, 5)
                        .writeInt16LE(acc, 7).writeInt16LE(dec, 9)
                    val bytes3 = ByteArray(11)
                    bytes3.writeInt8(id + 3, 0).writeInt32LE(step, 1).writeInt16LE(speed, 5)
                        .writeInt16LE(acc, 7).writeInt16LE(dec, 9)
                    try {
                        setLock(listOf(id, id + 1, id + 2))
                        withTimeout(10000L) {
                            val p = protocol {
                                data = bytes + bytes1 + bytes2
                            }
                            p.toByteArray().toHexString().logi()
                            sendByteArray(p.toByteArray())
                            history(p.toByteArray())
                            delay(200L)
                            while (getLock(listOf(id, id + 1, id + 2))) {
                                delay(200L)
                            }
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(job = null)
                        break
                    }
                }
            }

            job.start()
            _uiState.value = _uiState.value.copy(job = job)
        }
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
    val queryIndex: Int = 0,
    val replyIndex: Int = 0,
    val queryHistory: List<String> = listOf(),
    val replyHistory: List<String> = listOf(),
    val vec: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList(),
)