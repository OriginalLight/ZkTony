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

    fun test3() {
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
                    try {
                        setLock(listOf(3, 4, 5))
                        withTimeout(10000L) {
                            val p = protocol {
                                data = getRandom(3) + getRandom(4) + getRandom(5)
                            }
                            p.toByteArray().toHexString().logi()
                            sendByteArray(p.toByteArray())
                            history(p.toByteArray())
                            delay(200L)
                            while (getLock(listOf(3, 4, 5))) {
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

    fun test4() {
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
                    try {
                        setLock(listOf(6, 7, 8, 9))
                        withTimeout(10000L) {
                            val p = protocol {
                                data = getRandom(6) + getRandom(7) + getRandom(8) + getRandom(9)
                            }
                            p.toByteArray().toHexString().logi()
                            sendByteArray(p.toByteArray())
                            history(p.toByteArray())
                            delay(200L)
                            while (getLock(listOf(6, 7, 8, 9))) {
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

    fun test8() {
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
                    var bytes = byteArrayOf()
                    val indexList = mutableListOf<Int>()
                    for (i in 0..7) {
                        bytes += getRandom(i)
                        indexList.add(i)
                    }

                    try {
                        setLock(indexList)
                        withTimeout(10000L) {
                            val p = protocol {
                                data = bytes
                            }
                            p.toByteArray().toHexString().logi()
                            sendByteArray(p.toByteArray())
                            history(p.toByteArray())
                            delay(200L)
                            while (getLock(indexList)) {
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

    fun test16() {
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
                    var bytes = byteArrayOf()
                    val indexList = mutableListOf<Int>()
                    for (i in 0..15) {
                        bytes += getRandom(i)
                        indexList.add(i)
                    }

                    try {
                        setLock(indexList)
                        withTimeout(10000L) {
                            val p = protocol {
                                data = bytes
                            }
                            p.toByteArray().toHexString().logi()
                            sendByteArray(p.toByteArray())
                            history(p.toByteArray())
                            delay(200L)
                            while (getLock(indexList)) {
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


    private fun getRandom(id: Int): ByteArray {
        val bytes = ByteArray(11)
        return bytes.writeInt8(id, 0).writeInt32LE(6400L, 1)
            .writeInt16LE(6400, 5)
            .writeInt16LE(6400, 7).writeInt16LE(3200, 9)
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