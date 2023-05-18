package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.core.ext.collectLock
import com.zktony.www.core.ext.execute
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.dao.CalibrationDataDao
import com.zktony.www.data.entities.Calibration
import com.zktony.www.data.entities.CalibrationData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
    private val CD: CalibrationDao,
    private val CDD: CalibrationDataDao,
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
                CDD.getBySubId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(caliData = it)
                }
            }
            launch {
                collectLock {
                    _uiState.value = _uiState.value.copy(lock = it)
                }
            }
        }
    }

    fun delete(data: CalibrationData) {
        viewModelScope.launch {
            CDD.delete(data)
            calculateActual(data.subId)
        }
    }

    fun addLiquid() {
        val state = _uiState.value
        execute {
            step {
                v1 = state.expect
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val cali = CalibrationData(
                subId = _uiState.value.cali?.id ?: 0L,
                expect = _uiState.value.expect,
                actual = _uiState.value.actual,
            )
            CDD.insert(cali)
            calculateActual(cali.subId)
        }
    }

    fun expect(fl: Float) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(expect = fl)
        }
    }

    fun actual(fl: Float) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actual = fl)
        }
    }

    // 计算实际值
    private suspend fun calculateActual(id: Long) {
        val cali = CD.getById(id).firstOrNull()
        val dataList = CDD.getBySubId(id).firstOrNull()
        var v1 = 200f
        if (!dataList.isNullOrEmpty()) {
            v1 *= dataList.map { data -> data.percent }.average().toFloat()
        }
        CD.update(cali!!.copy(v1 = v1))
    }
}

data class CalibrationDataUiState(
    val cali: Calibration? = null,
    val caliData: List<CalibrationData> = emptyList(),
    val expect: Float = 0f,
    val actual: Float = 0f,
    val lock: Boolean = false,
)