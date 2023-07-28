package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.serialport.ext.asciiToHex
import com.zktony.www.core.ext.*
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.entities.Calibration
import com.zktony.www.data.entities.CalibrationData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
    private val CD: CalibrationDao,
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(CalibrationDataUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: Long) {
        viewModelScope.launch {
            launch {
                CD.getById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(cali = it)
                }
            }
            launch {
                collectLock {
                    _uiState.value = _uiState.value.copy(lock = it)
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
        viewModelScope.launch {
            val state = _uiState.value
            if (state.index == 0) {
                syncHex(1) {
                    fn = "05"
                    pa = "04"
                    data = "0101" + "0,0,32000,".asciiToHex()
                }
                delay(100L)
                waitSyncHex(1) {
                    pa = "0B"
                    data = "0305"
                }
            } else {
                val index = if (state.index < 3) 1 else if (state.index < 5) 2 else 3
                syncHex(index) {
                    fn = "05"
                    pa = "04"
                    data = "0101" + "${if (state.index % 2 != 0) "32000,0" else "0,32000"},0,".asciiToHex()
                }
            }

        }
    }

    fun save() {
        viewModelScope.launch {
            val list = _uiState.value.cali.data.toMutableList()
            list.add(
                CalibrationData(
                    index = _uiState.value.index,
                    step = if (_uiState.value.index == 0) 32000 else 32000,
                    actual = _uiState.value.actual,
                )
            )
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
    val actual: Float = 0f,
    val lock: Boolean = false,
)