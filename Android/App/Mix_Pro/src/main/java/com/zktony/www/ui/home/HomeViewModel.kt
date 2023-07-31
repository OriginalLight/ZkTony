package com.zktony.www.ui.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import com.zktony.www.R
import com.zktony.www.core.ext.asyncHex
import com.zktony.www.core.ext.syncHex
import com.zktony.www.core.ext.tx
import com.zktony.www.core.ext.waitLock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dataStore: DataStore<Preferences>
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                _uiState.value = _uiState.value.copy(
                    lock = true
                )
                delay(5000L)
                _uiState.value = _uiState.value.copy(
                    lock = false
                )
            }
            launch {
                dataStore.read("CACHE", "[0.0,0.0,0.0,0.0,0.0,0.0]").collect {
                    if (it.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            cache = Gson().fromJson(it, Array<Float>::class.java).toList()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            cache = listOf(0f, 0f, 0f, 0f, 0f, 0f)
                        )
                    }
                }

            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            if (_uiState.value.job == null) {
                syncHex(0) {}
                syncHex(1) {
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
            val executor = ProgramExecutor(
                params = uiState.value.cache,
                scope = this,
            )
            executor.execute()
        }
        val job1 = viewModelScope.launch {
            launch {
                while (true) {
                    delay(1000L)
                    _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                }
            }
        }
        job.invokeOnCompletion {
            job1.cancel()
            _uiState.value = _uiState.value.copy(time = 0L, job = null)
        }
        _uiState.value = _uiState.value.copy(job = job)
    }

    fun wash(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                asyncHex(0) {
                    pa = "0B"
                    data = "0301"
                }
            } else {
                asyncHex(0) {
                    pa = "0B"
                    data = "0300"
                }
            }
        }
    }

    /**
     * 填充促凝剂
     */
    fun fillCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.fillCoagulant) {
                if (_uiState.value.lock) {
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.resetting))
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    fillCoagulant = false,
                    lock = true
                )
                syncHex(1) {
                    pa = "10"
                }
                delay(200L)
                syncHex(1) {
                    pa = "0B"
                    data = "0305"
                }
                delay(100L)
                waitLock {
                    delay(2000L)
                    _uiState.value = _uiState.value.copy(
                        lock = false,
                    )
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.reset_success))
                }

            } else {
                if (_uiState.value.recaptureCoagulant) {
                    PopTip.show(Ext.ctx.getString(R.string.stop_back))
                    return@launch
                }
                if (_uiState.value.lock) {
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.resetting))
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    fillCoagulant = true,
                )
                delay(100L)
                while (_uiState.value.fillCoagulant) {
                    if (_uiState.value.upOrDown) {
                        _uiState.value = _uiState.value.copy(upOrDown = false)
                        asyncHex(1) {
                            pa = "0B"
                            data = "0301"
                        }
                        delay(13000L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex(1) {
                            pa = "0B"
                            data = "0302"
                        }
                        delay(13500L)
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
                if (_uiState.value.lock) {
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.resetting))
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    recaptureCoagulant = false,
                    lock = true
                )
                syncHex(1) {
                    pa = "10"
                }
                delay(200L)
                syncHex(1) {
                    pa = "0B"
                    data = "0305"
                }
                delay(100L)
                waitLock {
                    delay(2000L)
                    _uiState.value = _uiState.value.copy(
                        lock = false,
                    )
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.reset_success))
                }
            } else {
                if (_uiState.value.fillCoagulant) {
                    PopTip.show(Ext.ctx.getString(R.string.stop_fill))
                    return@launch
                }
                if (_uiState.value.lock) {
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.resetting))
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    recaptureCoagulant = true,
                )
                delay(100L)
                while (_uiState.value.recaptureCoagulant) {
                    if (_uiState.value.upOrDown) {
                        _uiState.value = _uiState.value.copy(upOrDown = false)
                        asyncHex(1) {
                            pa = "0B"
                            data = "0303"
                        }
                        delay(13000L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex(1) {
                            pa = "0B"
                            data = "0304"
                        }
                        delay(13500L)
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
            if (!uiState.value.fillCoagulant && !uiState.value.recaptureCoagulant) {
                asyncHex(1) {
                    pa = "0B"
                    data = "0401"
                }
                asyncHex(2) {
                    pa = "0B"
                    data = "0401"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0401"
                }
            }
        }
    }

    /**
     * 回吸胶体
     */
    fun recaptureColloid() {
        viewModelScope.launch {
            if (!uiState.value.fillCoagulant && !uiState.value.recaptureCoagulant) {
                asyncHex(1) {
                    pa = "0B"
                    data = "0402"
                }
                asyncHex(2) {
                    pa = "0B"
                    data = "0402"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0402"
                }
            }
        }
    }

    /**
     * 停止填充和回吸
     */
    fun stopFillAndRecapture() {
        viewModelScope.launch {
            if (!uiState.value.fillCoagulant && !uiState.value.recaptureCoagulant) {
                asyncHex(1) {
                    pa = "0B"
                    data = "0400"
                }
                asyncHex(2) {
                    pa = "0B"
                    data = "0400"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0400"
                }
            }
        }
    }

    fun setCache(value: List<Float>) {
        viewModelScope.launch {
            dataStore.save("CACHE", Gson().toJson(value))
        }
    }

    fun moveTo(y: Float, z: Float) {
        tx {
            move {
                this.y = y
            }
            move {
                this.y = y
                this.z = z
            }
        }
    }
}

data class HomeUiState(
    val job: Job? = null,
    val time: Long = 0L,
    val fillCoagulant: Boolean = false,
    val recaptureCoagulant: Boolean = false,
    val lock: Boolean = false,
    val upOrDown: Boolean = true,
    val cache: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f),
)