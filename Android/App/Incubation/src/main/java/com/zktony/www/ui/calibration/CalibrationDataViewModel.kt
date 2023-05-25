package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.core.ext.collectLock
import com.zktony.www.core.ext.execute
import com.zktony.www.core.ext.pulse
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.dao.ContainerDao
import com.zktony.www.data.entities.Calibration
import com.zktony.www.data.entities.CalibrationData
import com.zktony.www.data.entities.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
    private val CD: CalibrationDao,
    private val COND: ContainerDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CalibrationDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                COND.getAll().distinctUntilChanged().collect {
                    if (it.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(container = it.first())
                    }
                }
            }
            launch {
                collectLock {
                    _uiState.value = _uiState.value.copy(lock = it)
                }
            }
        }
    }

    fun load(id: Long) {
        viewModelScope.launch {
            launch {
                CD.getById(id).collect {
                    _uiState.value = _uiState.value.copy(cali = it)
                }
            }
        }
    }

    fun selectPump(pumpId: Int) {
        _uiState.value = _uiState.value.copy(index = pumpId)
    }

    fun delete(data: CalibrationData) {
        viewModelScope.launch {
            val list = _uiState.value.cali.data.toMutableList()
            list.remove(data)
            CD.update(_uiState.value.cali.copy(data = list))
        }
    }

    fun addLiquid() {
        val con = _uiState.value.container
        val index = _uiState.value.index
        if (index < 4) {
            execute {
                pulse {
                    y = pulse(con.washY, 1)
                }
                pulse {
                    y = pulse(con.washY, 1)
                    z = pulse(con.washZ, 2)
                    v1 = if (index == 0) 30 * 3200 else 0
                    v2 = if (index == 1) 30 * 3200 else 0
                    v3 = if (index == 2) 30 * 3200 else 0
                    v4 = if (index == 3) 30 * 3200 else 0
                }
                pulse {
                    y = pulse(con.washY, 1)
                    v1 = if (index == 0) 15000 else 0
                    v2 = if (index == 1) 15000 else 0
                    v3 = if (index == 2) 15000 else 0
                    v4 = if (index == 3) 15000 else 0
                }
                pulse {}
            }
        } else {
            execute {
                pulse {}
                pulse {
                    v5 = if (index == 4) 30 * 3200 else 0
                    v6 = if (index == 5) 30 * 3200 else 0
                }
                pulse {}
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val list = _uiState.value.cali.data.toMutableList()
            val data = CalibrationData(
                index = _uiState.value.index,
                step = 3200 * 30,
                actual = _uiState.value.actual,
            )
            list.add(data)
            CD.update(_uiState.value.cali.copy(data = list))
        }
    }

    fun actual(fl: Float) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actual = fl)
        }
    }

}

data class CalibrationDataUiState(
    val index: Int = 0,
    val cali: Calibration = Calibration(),
    val container: Container = Container(),
    val actual: Float = 0f,
    val lock: Boolean = false,
)