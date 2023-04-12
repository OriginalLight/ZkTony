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
    private val containerDao: ContainerDao,
    private val calibrationDao: CalibrationDao,
    private val calibrationDataDao: CalibrationDataDao,
    private val serialManager: SerialManager,
    private val executionManager: ExecutionManager
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(CalibrationDataUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: Long) {
        viewModelScope.launch {
            launch {
                calibrationDao.getById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(cali = it)
                }
            }
            launch {
                calibrationDataDao.getBySubId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(caliData = it)
                }
            }
            launch {
                containerDao.getByType(0).collect {
                    if (it.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(container = it.first())
                    }
                }
            }
            launch {
                serialManager.lock.collect {
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
            calibrationDataDao.delete(data)
            delay(300L)
            calculateActual(data.subId)
        }
    }

    fun addLiquid() {
        val state = _uiState.value
        val con = state.container
        val gen = when (state.pumpId) {
            0 -> listOf(executionManager.generator(y = con.axis, v1 = state.expect))
            1 -> listOf(executionManager.generator(y = con.axis, v2 = state.expect))
            2 -> listOf(
                executionManager.generator(y = con.axis, v3 = state.expect),
                executionManager.generator(y = con.axis, v3 = -state.expect)
            )
            else -> return
        }
        executionManager.executor(gen)
    }

    fun save() {
        viewModelScope.launch {
            calibrationDataDao.insert(
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
        val cali = calibrationDao.getById(id).firstOrNull()
        val dataList = calibrationDataDao.getBySubId(id).firstOrNull()
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
        calibrationDao.update(cali!!.copy(v1 = v1, v2 = v2, v3 = v3))
    }

}

data class CalibrationDataUiState(
    val pumpId: Int = 0,
    val container: Container = Container(),
    val cali: Calibration? = null,
    val caliData: List<CalibrationData> = emptyList(),
    val expect: Float = 0f,
    val actual: Float = 0f,
    val lock: Boolean = false,
)