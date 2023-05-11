package com.zktony.www.ui.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import com.zktony.www.R
import com.zktony.www.common.ext.asyncHex
import com.zktony.www.common.ext.syncHex
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel constructor(
    private val DS: DataStore<Preferences>,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                DS.read("COLLOID_HISTORY", emptySet<String>()).collect {
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
                DS.read("COAGULANT_HISTORY", emptySet<String>()).collect {
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
                DS.read("PREVIOUS_COLLOID_HISTORY", emptySet<String>()).collect {
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
                DS.read("PREVIOUS_COAGULANT_HISTORY", emptySet<String>()).collect {
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
                syncHex {
                    pa = "0B"
                    data = "0305"
                }
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.resetting))
            } else {
                PopTip.show(Ext.ctx.getString(R.string.stop_all))
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
                slowFast = _uiState.value.slowFast,
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
                slowFast = _uiState.value.slowFast,
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
                asyncHex {
                    pa = "0B"
                    data = "0300"
                }
                delay(100L)
                syncHex {
                    pa = "0B"
                    data = "0305"
                }

            } else {
                if (_uiState.value.recaptureCoagulant) {
                    PopTip.show(Ext.ctx.getString(R.string.stop_back))
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
                        asyncHex {
                            pa = "0B"
                            data = "0301"
                        }
                        delay(8500L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex {
                            pa = "0B"
                            data = "0305"
                        }
                        delay(9000L)
                    }
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
                asyncHex {
                    pa = "0B"
                    data = "0300"
                }
                delay(100L)
                syncHex {
                    pa = "0B"
                    data = "0305"
                }
            } else {
                if (_uiState.value.fillCoagulant) {
                    PopTip.show(Ext.ctx.getString(R.string.stop_fill))
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
                        asyncHex {
                            pa = "0B"
                            data = "0303"
                        }
                        delay(8500L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex {
                            pa = "0B"
                            data = "0304"
                        }
                        delay(9000L)
                    }
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
            asyncHex {
                pa = "0B"
                data = "0401"
            }
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
            asyncHex {
                pa = "0B"
                data = "0402"
            }
        }
    }

    /**
     * 停止填充和回吸
     */
    fun stopFillAndRecapture() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(start = false)
            asyncHex {
                pa = "0B"
                data = "0400"
            }
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
                DS.save("COAGULANT_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            DS.save("COAGULANT_HISTORY", set)
        }
    }

    private fun previousCoagulantHistory(str: String) {
        viewModelScope.launch {
            val set = _uiState.value.previousCoagulantHistory.toMutableSet()
            if (set.contains(str)) {
                set.remove(str)
                DS.save("PREVIOUS_COAGULANT_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            DS.save("PREVIOUS_COAGULANT_HISTORY", set)
        }
    }

    private fun colloidHistory(str: String) {
        viewModelScope.launch {
            val set = _uiState.value.colloidHistory.toMutableSet()
            if (set.contains(str)) {
                set.remove(str)
                DS.save("COLLOID_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            DS.save("COLLOID_HISTORY", set)
        }
    }

    private fun previousColloidHistory(str: String) {
        viewModelScope.launch {
            val set = _uiState.value.previousColloidHistory.toMutableSet()
            if (set.contains(str)) {
                set.remove(str)
                DS.save("PREVIOUS_COLLOID_HISTORY", set)
                delay(100L)
                set.add(str)
            } else {
                if (set.size == 5) {
                    set.remove(set.first())
                }
                set.add(str)
            }
            DS.save("PREVIOUS_COLLOID_HISTORY", set)
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

    fun slowFast() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(slowFast = !_uiState.value.slowFast)
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
    val slowFast: Boolean = false,
)