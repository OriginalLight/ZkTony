package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.room.dao.*
import com.zktony.www.room.entity.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
    private val CD: ContainerDao,
    private val CDD: CalibrationDataDao,
    private val CLD: CalibrationDao,
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(CalibrationDataUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: Long) {
        viewModelScope.launch {
            launch {
                CLD.getById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(cali = it)
                }
            }
            launch {
                CDD.getBySubId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(caliData = it)
                }
            }
            launch {
                CD.getByType(0).collect {
                    if (it.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(container = it.first())
                    }
                }
            }
        }
    }

    fun selectPump(pumpId: Int) {
        _uiState.value = _uiState.value.copy(pumpId = pumpId)
    }

    fun delete(data: CalibrationData) {
        viewModelScope.launch {
            CDD.delete(data)
            delay(300L)
            calculateActual(data.subId)
        }
    }

    fun addLiquid() {
        val state = _uiState.value
        val con = state.container
    }

    fun save() {
        viewModelScope.launch {
            CDD.insert(
                CalibrationData(
                    pumpId = _uiState.value.pumpId,
                    subId = _uiState.value.cali?.id ?: 0L,
                    expect = _uiState.value.expect,
                    actual = _uiState.value.actual,
                )
            )
            delay(300L)
            calculateActual(_uiState.value.cali?.id ?: 0L)
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
        val cali = CLD.getById(id).firstOrNull()
        val dataList = CDD.getBySubId(id).firstOrNull()
        var v1 = 200f
        var v2 = 200f
        var v3 = 200f
        if (!dataList.isNullOrEmpty()) {
            dataList.filter { it.pumpId == 0 }.let {
                if (it.isNotEmpty()) {
                    v1 *= it.map { data -> data.percent }.average().toFloat()
                }
            }
            dataList.filter { it.pumpId == 1 }.let {
                if (it.isNotEmpty()) {
                    v2 *= it.map { data -> data.percent }.average().toFloat()
                }
            }
            dataList.filter { it.pumpId == 2 }.let {
                if (it.isNotEmpty()) {
                    v3 *= it.map { data -> data.percent }.average().toFloat()
                }
            }
        }
        CLD.update(cali!!.copy(v1 = v1, v2 = v2, v3 = v3))
    }

}

data class CalibrationDataUiState(
    val actual: Float = 0f,
    val cali: Calibration? = null,
    val caliData: List<CalibrationData> = emptyList(),
    val container: Container = Container(),
    val expect: Float = 0f,
    val lock: Boolean = false,
    val pumpId: Int = 0,
)