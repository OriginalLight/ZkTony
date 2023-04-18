package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.CalibrationDataDao
import com.zktony.www.room.entity.Calibration
import com.zktony.www.room.entity.CalibrationData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
    private val CD: CalibrationDao,
    private val CDD: CalibrationDataDao,
    private val SM: SerialManager,
    private val EM: ExecutionManager
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
                SM.lock.collect {
                    _uiState.value = _uiState.value.copy(lock = it)
                }
            }
            launch {
                SM.pause.collect {
                    _uiState.value = _uiState.value.copy(work = it)
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
            calculateActual(data.subId)
        }
    }

    fun addLiquid() {
        val state = _uiState.value
        val gen = when (state.pumpId) {
            0 -> EM.builder(v1 = state.expect)
            1 -> EM.builder(v2 = state.expect)
            2 -> EM.builder(v3 = state.expect)
            3 -> EM.builder(v4 = state.expect)
            else -> return
        }
        EM.actuator(gen)
    }

    fun save() {
        viewModelScope.launch {
            val cali = CalibrationData(
                pumpId = _uiState.value.pumpId,
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
        var v2 = 200f
        var v3 = 200f
        var v4 = 200f
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
            dataList.filter { it.pumpId == 3 }.let {
                if (it.isNotEmpty()) {
                    v4 *= it.map { data -> data.percent }.average().toFloat()
                }
            }
        }
        CD.update(cali!!.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4))
    }

}

data class CalibrationDataUiState(
    val pumpId: Int = 0,
    val cali: Calibration? = null,
    val caliData: List<CalibrationData> = emptyList(),
    val expect: Float = 0f,
    val actual: Float = 0f,
    val lock: Boolean = false,
    val work: Boolean = false,
)