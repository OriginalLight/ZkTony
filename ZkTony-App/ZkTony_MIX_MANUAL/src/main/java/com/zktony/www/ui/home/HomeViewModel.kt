package com.zktony.www.ui.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel constructor(
    private val dataStore: DataStore<Preferences>,
    private val serialManager: SerialManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dataStore.read("COLLOID_HISTORY", emptySet<String>()).collect {
                    _uiState.value = _uiState.value.copy(
                        colloidHistory = it
                    )
                    if (_uiState.value.colloid == 0) {
                        _uiState.value = _uiState.value.copy(
                            colloid = it.lastOrNull()?.toInt() ?: 0
                        )
                    }
                }
            }
            launch {
                dataStore.read("COAGULANT_HISTORY", emptySet<String>()).collect {
                    _uiState.value = _uiState.value.copy(
                        coagulantHistory = it
                    )
                    if (_uiState.value.coagulant == 0) {
                        _uiState.value = _uiState.value.copy(
                            coagulant = it.lastOrNull()?.toInt() ?: 0
                        )
                    }
                }
            }
            launch {
                dataStore.read("PREVIOUS_COLLOID_HISTORY", emptySet<String>()).collect {
                    _uiState.value = _uiState.value.copy(
                        previousColloidHistory = it
                    )
                    if (_uiState.value.previousColloid == 0) {
                        _uiState.value = _uiState.value.copy(
                            previousColloid = it.lastOrNull()?.toInt() ?: 0
                        )
                    }
                }
            }
            launch {
                dataStore.read("PREVIOUS_COAGULANT_HISTORY", emptySet<String>()).collect {
                    _uiState.value = _uiState.value.copy(
                        previousCoagulantHistory = it
                    )
                    if (_uiState.value.previousCoagulant == 0) {
                        _uiState.value = _uiState.value.copy(
                            previousCoagulant = it.lastOrNull()?.toInt() ?: 0
                        )
                    }
                }
            }
        }
    }


    fun reset() {
        viewModelScope.launch {
            if (_uiState.value.job == null) {
                if (serialManager.lock.value) {
                    PopTip.show("运动中禁止复位")
                } else {
                    serialManager.reset()
                    PopTip.show("复位-已下发")
                }
            } else {
                PopTip.show("请中止所有运行中程序")
            }
        }
    }

    fun start() {
        val job = viewModelScope.launch {
            launch {
                coagulantHistory(_uiState.value.coagulant.toString())
                colloidHistory(_uiState.value.colloid.toString())
            }
            launch {
                while (true) {
                    delay(1000L)
                    _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                }
            }
            val executor = ProgramExecutor(
                colloid = _uiState.value.colloid,
                coagulant = _uiState.value.coagulant,
                scope = this,
            )
            executor.finish = {
                _uiState.value.job?.cancel()
                _uiState.value = _uiState.value.copy(
                    job = null,
                    time = 0L,
                    previous = true
                )
            }
            executor.execute()
        }
        _uiState.value = _uiState.value.copy(job = job)
    }

    fun previous() {
        val job = viewModelScope.launch {
            launch {
                previousColloidHistory(_uiState.value.previousColloid.toString())
                previousCoagulantHistory(_uiState.value.previousCoagulant.toString())
            }
            launch {
                while (true) {
                    delay(1000L)
                    _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                }
            }
            val executor = ProgramExecutor(
                colloid = _uiState.value.previousColloid,
                coagulant = _uiState.value.previousCoagulant,
                scope = this,
            )
            executor.finish = {
                _uiState.value.job?.cancel()
                _uiState.value = _uiState.value.copy(
                    job = null,
                    time = 0L,
                    previous = false
                )
            }
            executor.executePrevious()
        }
        _uiState.value = _uiState.value.copy(job = job)
    }


    /**
     * 填充促凝剂
     */
    fun fillCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.fillCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    fillCoagulant = false,
                    start = false,
                )
                serialManager.sendHex(hex = V1(pa = "0B", data = "0300").toHex())
                delay(100L)
                reset()
            } else {
                if (serialManager.reset.value) {
                    if (_uiState.value.recaptureCoagulant) {
                        PopTip.show("请先停止回吸")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        upOrDown = true,
                        fillCoagulant = true,
                        start = true,
                        previous = true
                    )
                    delay(100L)
                    while (_uiState.value.fillCoagulant) {
                        if (_uiState.value.upOrDown) {
                            _uiState.value = _uiState.value.copy(upOrDown = false)
                            serialManager.sendHex(hex = V1(pa = "0B", data = "0301").toHex())
                            delay(8500L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            serialManager.sendHex(hex = V1(pa = "0B", data = "0305").toHex())
                            delay(9000L)
                        }
                    }

                } else {
                    PopTip.show("请先复位")
                }
            }
        }
    }

    /**
     * 回吸促凝剂
     */
    fun recaptureCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.recaptureCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    recaptureCoagulant = false,
                    start = false
                )
                serialManager.sendHex(hex = V1(pa = "0B", data = "0300").toHex())
                delay(100L)
                reset()
            } else {
                if (serialManager.reset.value) {
                    if (_uiState.value.fillCoagulant) {
                        PopTip.show("请先停止填充")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        upOrDown = true,
                        recaptureCoagulant = true,
                        start = true,
                        previous = true
                    )
                    delay(100L)
                    while (_uiState.value.recaptureCoagulant) {
                        if (_uiState.value.upOrDown) {
                            _uiState.value = _uiState.value.copy(upOrDown = false)
                            serialManager.sendHex(hex = V1(pa = "0B", data = "0303").toHex())
                            delay(8500L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            serialManager.sendHex(hex = V1(pa = "0B", data = "0304").toHex())
                            delay(9000L)
                        }
                    }

                } else {
                    PopTip.show("请先复位")
                }
            }
        }
    }

    /**
     * 填充胶体
     */
    fun fillColloid() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                start = true,
                previous = true,
            )
            serialManager.sendHex(hex = V1(pa = "0B", data = "0401").toHex())
        }
    }

    /**
     * 回吸胶体
     */
    fun recaptureColloid() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                start = true,
                previous = true,
            )
            serialManager.sendHex(hex = V1(pa = "0B", data = "0402").toHex())
        }
    }

    /**
     * 停止填充和回吸
     */
    fun stopFillAndRecapture() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(start = false)
            serialManager.sendHex(hex = V1(pa = "0B", data = "0400").toHex())
        }
    }

    fun selectCoagulant(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                coagulant = str.toIntOrNull() ?: 0,
            )
            coagulantHistory(str)
        }
    }

    fun selectPreviousCoagulant(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                previousCoagulant = str.toIntOrNull() ?: 0,
            )
            previousCoagulantHistory(str)
        }
    }

    fun selectColloid(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                colloid = str.toIntOrNull() ?: 0,
            )
            colloidHistory(str)
        }
    }

    fun selectPreviousColloid(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                previousColloid = str.toIntOrNull() ?: 0,
            )
            previousColloidHistory(str)
        }
    }

    private fun coagulantHistory(str: String) {
        viewModelScope.launch {
            val set = _uiState.value.coagulantHistory.toMutableSet()
            if (set.contains(str)) {
                set.remove(str)
                dataStore.save("COAGULANT_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            dataStore.save("COAGULANT_HISTORY", set)
        }
    }

    private fun previousCoagulantHistory(str: String) {
        viewModelScope.launch {
            val set = _uiState.value.previousCoagulantHistory.toMutableSet()
            if (set.contains(str)) {
                set.remove(str)
                dataStore.save("PREVIOUS_COAGULANT_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            dataStore.save("PREVIOUS_COAGULANT_HISTORY", set)
        }
    }

    private fun colloidHistory(str: String) {
        viewModelScope.launch {
            val set = _uiState.value.colloidHistory.toMutableSet()
            if (set.contains(str)) {
                set.remove(str)
                dataStore.save("COLLOID_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            dataStore.save("COLLOID_HISTORY", set)
        }
    }

    private fun previousColloidHistory(str: String) {
        viewModelScope.launch {
            val set = _uiState.value.previousColloidHistory.toMutableSet()
            if (set.contains(str)) {
                set.remove(str)
                dataStore.save("PREVIOUS_COLLOID_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            dataStore.save("PREVIOUS_COLLOID_HISTORY", set)
        }
    }

    fun colloidEdit(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(colloid = str.toIntOrNull() ?: 0)
        }
    }

    fun coagulantEdit(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(coagulant = minOf(str.toIntOrNull() ?: 0, 800))
        }
    }

    fun previousColloidEdit(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(previousColloid = str.toIntOrNull() ?: 0)
        }
    }

    fun previousCoagulantEdit(str: String) {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(previousCoagulant = minOf(str.toIntOrNull() ?: 0, 800))
        }
    }
}

data class HomeUiState(
    val job: Job? = null,
    val time: Long = 0L,
    val start: Boolean = false,
    val fillCoagulant: Boolean = false,
    val recaptureCoagulant: Boolean = false,
    val upOrDown: Boolean = true,
    val colloid: Int = 0,
    val coagulant: Int = 0,
    val previousColloid: Int = 0,
    val previousCoagulant: Int = 0,
    val previous: Boolean = true,
    val colloidHistory: Set<String> = emptySet(),
    val coagulantHistory: Set<String> = emptySet(),
    val previousColloidHistory: Set<String> = emptySet(),
    val previousCoagulantHistory: Set<String> = emptySet(),
)