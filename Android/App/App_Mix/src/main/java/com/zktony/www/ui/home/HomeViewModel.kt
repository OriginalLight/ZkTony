package com.zktony.www.ui.home

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.serialport.ext.asciiToHex
import com.zktony.www.R
import com.zktony.www.core.ext.asyncHex
import com.zktony.www.core.ext.syncHex
import com.zktony.www.core.ext.waitLock
import com.zktony.www.core.ext.waitTime
import com.zktony.www.data.dao.CacheDao
import com.zktony.www.data.entities.Cache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel constructor(
    private val dao: CacheDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dao.getAll().collect {
                    _uiState.value = _uiState.value.copy(cacheList = it)
                    updateValue()
                }
            }
            launch {
                _uiState.value = _uiState.value.copy(
                    lock = true
                )
                delay(5000L)
                _uiState.value = _uiState.value.copy(
                    lock = false
                )
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
                updateCache()
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
                mode = _uiState.value.mode,
                scope = this,
            )
            executor.finish = {
                _uiState.value = _uiState.value.copy(
                    time = 0L,
                    previous = !_uiState.value.previous
                )
                updateValue()
                _uiState.value.job?.cancel()
                _uiState.value = _uiState.value.copy(job = null)
            }
            if (_uiState.value.previous) {
                executor.executePrevious()
            } else {
                executor.execute()
            }
        }
        _uiState.value = _uiState.value.copy(job = job)
    }

    fun stop() {
        viewModelScope.launch {
            asyncHex {
                pa = "0B"
                data = "0400"
            }
            _uiState.value = _uiState.value.copy(
                time = 0L,
                previous = !_uiState.value.previous
            )
            updateValue()
            _uiState.value.job?.cancel()
            _uiState.value = _uiState.value.copy(job = null)
            delay(200L)
            syncHex {
                pa = "0B"
                data = "0305"
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
                syncHex {
                    pa = "10"
                }
                delay(200L)
                syncHex {
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
                    previous = true
                )
                updateValue()
                delay(100L)
                while (_uiState.value.fillCoagulant) {
                    if (_uiState.value.upOrDown) {
                        _uiState.value = _uiState.value.copy(upOrDown = false)
                        asyncHex {
                            pa = "0B"
                            data = "0301"
                        }
                        delay(13500L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex {
                            pa = "0B"
                            data = "0302"
                        }
                        delay(14500L)
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
                syncHex {
                    pa = "10"
                }
                delay(200L)
                syncHex {
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
                    previous = true
                )
                updateValue()
                delay(100L)
                while (_uiState.value.recaptureCoagulant) {
                    if (_uiState.value.upOrDown) {
                        _uiState.value = _uiState.value.copy(upOrDown = false)
                        asyncHex {
                            pa = "0B"
                            data = "0303"
                        }
                        delay(13500L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex {
                            pa = "0B"
                            data = "0304"
                        }
                        delay(14500L)
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
                _uiState.value = _uiState.value.copy(previous = true)
                updateValue()
                asyncHex {
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
                _uiState.value = _uiState.value.copy(previous = true)
                updateValue()
                asyncHex {
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
                asyncHex {
                    pa = "0B"
                    data = "0400"
                }
            }
        }
    }

    fun selectCoagulant(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                coagulant = str.toFloatOrNull() ?: 0f,
            )
        }
    }

    fun selectColloid(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                colloid = str.toFloatOrNull() ?: 0f,
            )
        }
    }

    fun colloidEdit(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(colloid = str.toFloatOrNull() ?: 0f)
        }
    }

    fun coagulantEdit(str: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(coagulant = str.toFloatOrNull() ?: 0f)
        }
    }

    fun mode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                mode = !_uiState.value.mode,
                previous = true
            )
            updateValue()
            delay(100L)
            waitTime(
                if (_uiState.value.mode) {
                    30 * 60L
                } else {
                    4 * 60L
                }
            )
        }
    }

    fun getType(): Int {
        return if (_uiState.value.mode) {
            if (_uiState.value.previous) {
                2
            } else {
                3
            }
        } else {
            if (_uiState.value.previous) {
                0
            } else {
                1
            }
        }
    }

    private fun updateCache() {
        viewModelScope.launch {
            delay(100L)
            val cacheList = _uiState.value.cacheList.filter { it.type == getType() }
            if (cacheList.isEmpty()) {
                val cache = Cache(
                    type = getType(),
                    colloid = listOf(_uiState.value.colloid),
                    coagulant = listOf(_uiState.value.coagulant),
                )
                dao.insert(cache)
            } else {
                val cache = cacheList[0]
                val colloid = cache.colloid.toMutableList()
                val coagulant = cache.coagulant.toMutableList()
                if (colloid.contains(_uiState.value.colloid)) {
                    colloid.remove(_uiState.value.colloid)
                    colloid.add(_uiState.value.colloid)
                } else {
                    if (colloid.size >= 5) {
                        colloid.removeAt(0)
                        colloid.add(_uiState.value.colloid)
                    } else {
                        colloid.add(_uiState.value.colloid)
                    }
                }
                if (coagulant.contains(_uiState.value.coagulant)) {
                    coagulant.remove(_uiState.value.coagulant)
                    coagulant.add(_uiState.value.coagulant)
                } else {
                    if (coagulant.size >= 5) {
                        coagulant.removeAt(0)
                        coagulant.add(_uiState.value.coagulant)
                    } else {
                        coagulant.add(_uiState.value.coagulant)
                    }
                }
                dao.update(
                    cache.copy(
                        colloid = colloid,
                        coagulant = coagulant,
                    )
                )
            }
        }
    }

    private fun updateValue() {
        val cacheList = _uiState.value.cacheList
        if (cacheList.isNotEmpty()) {
            val list = cacheList.filter { c -> c.type == getType() }
            if (list.isNotEmpty()) {
                val colloid = list.first().colloid.lastOrNull() ?: 0f
                val coagulant = list.first().coagulant.lastOrNull() ?: 0f
                _uiState.value = _uiState.value.copy(
                    colloid = colloid,
                    coagulant = coagulant,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    colloid = 0f,
                    coagulant = 0f,
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                colloid = 0f,
                coagulant = 0f,
            )
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
    val colloid: Float = 0f,
    val coagulant: Float = 0f,
    val cacheList: List<Cache> = emptyList(),
    val mode: Boolean = false,
    val previous: Boolean = true,
)