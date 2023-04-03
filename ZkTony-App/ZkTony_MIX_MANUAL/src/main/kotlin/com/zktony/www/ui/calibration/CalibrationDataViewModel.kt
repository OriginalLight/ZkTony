package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.dao.CalibrationDao
import com.zktony.www.data.local.dao.CalibrationDataDao
import com.zktony.www.data.local.entity.Calibration
import com.zktony.www.data.local.entity.CalibrationData
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
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
        viewModelScope.launch {
            val state = _uiState.value
            executionManager.executor(executionManager.generator(
                v1 = if (state.pumpId == 0) state.expect else 0f,
                v2 = if (state.pumpId == 1) state.expect else 0f,
                v3 = if (state.pumpId == 2) state.expect else 0f,
            ))
            if (state.pumpId == 2) {
                delay(100L)
                while (serialManager.lock.value) {
                    delay(100L)
                }
                serialManager.reset()
            }
        }
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
    val cali: Calibration? = null,
    val caliData: List<CalibrationData> = emptyList(),
    val expect: Float = 0f,
    val actual: Float = 0f,
    val lock: Boolean = false,
)