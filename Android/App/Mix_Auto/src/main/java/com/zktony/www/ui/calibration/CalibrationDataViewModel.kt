package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.CalibrationDataDao
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Calibration
import com.zktony.www.room.entity.CalibrationData
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
    private val CD: ContainerDao,
    private val CDD: CalibrationDataDao,
    private val CLD: CalibrationDao,
    private val EM: ExecutionManager,
    private val SM: SerialManager,
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
            launch {
                SM.lock.collect {
                    _uiState.value = _uiState.value.copy(lock = it)
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
        val gen = when (state.pumpId) {
            0 -> listOf(EM.builder(y = con.axis, v1 = state.expect))
            1 -> listOf(EM.builder(y = con.axis, v2 = state.expect))
            2 -> listOf(
                EM.builder(y = con.axis, v3 = state.expect),
                EM.builder(y = con.axis, v3 = -state.expect)
            )

            else -> return
        }
        EM.actuator(gen)
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