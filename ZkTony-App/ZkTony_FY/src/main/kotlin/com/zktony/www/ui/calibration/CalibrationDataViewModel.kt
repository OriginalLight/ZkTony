package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.dao.CalibrationDao
import com.zktony.www.data.local.room.dao.CalibrationDataDao
import com.zktony.www.data.local.room.dao.ContainerDao
import com.zktony.www.data.local.room.entity.Calibration
import com.zktony.www.data.local.room.entity.CalibrationData
import com.zktony.www.data.local.room.entity.Container
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CalibrationDataViewModel constructor(
    private val dao: CalibrationDao,
    private val dataDao: CalibrationDataDao,
    private val containerDao: ContainerDao,
    private val serialManager: SerialManager,
    private val executionManager: ExecutionManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CalibrationDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                containerDao.getAll().distinctUntilChanged().collect {
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

    fun load(id: String) {
        viewModelScope.launch {
            launch {
                dao.getById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(cali = it)
                }
            }
            launch {
                dataDao.getBySubId(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(caliData = it)
                }
            }
        }
    }

    fun selectPump(pumpId: Int) {
        _uiState.value = _uiState.value.copy(pumpId = pumpId)
    }

    fun delete(data: CalibrationData) {
        viewModelScope.launch {
            dataDao.delete(data)
            calculateActual(data.calibrationId)
        }
    }

    fun addLiquid() {
        val con = _uiState.value.container
        val liquid = _uiState.value.expect
        val motorId = _uiState.value.pumpId
        if (motorId < 4) {
            executionManager.executor(
                executionManager.generator(y = con.washY),
                executionManager.generator(
                    y = con.washY,
                    z = con.washZ,
                    v1 = if (motorId == 0) liquid else 0f,
                    v2 = if (motorId == 1) liquid else 0f,
                    v3 = if (motorId == 2) liquid else 0f,
                    v4 = if (motorId == 3) liquid else 0f,
                ),
                executionManager.generator(
                    y = con.washY,
                    v1 = if (motorId == 0) 15000f else 0f,
                    v2 = if (motorId == 1) 15000f else 0f,
                    v3 = if (motorId == 2) 15000f else 0f,
                    v4 = if (motorId == 3) 15000f else 0f,
                ),
                executionManager.generator()
            )
        }
        else {
            executionManager.executor(
                executionManager.generator(),
                executionManager.generator(
                    v4 = if (motorId == 4) liquid else 0f,
                    v5 = if (motorId == 5) liquid else 0f,
                ),
                executionManager.generator()
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            val data = CalibrationData(
                pumpId = _uiState.value.pumpId,
                calibrationId = _uiState.value.cali?.id ?: "",
                expect = _uiState.value.expect,
                actual = _uiState.value.actual,
            )
            dataDao.insert(data)
            calculateActual(data.calibrationId)
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

    private suspend fun calculateActual(id: String) {
        val cali = dao.getById(id).firstOrNull()
        val dataList = dataDao.getBySubId(id).firstOrNull()
        var v1 = 180f
        var v2 = 180f
        var v3 = 180f
        var v4 = 180f
        var v5 = 180f
        var v6 = 180f
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
            dataList.filter { it.pumpId == 4 }.let {
                if (it.isNotEmpty()) {
                    v5 *= it.map { data -> data.percent }.average().toFloat()
                }
            }
            dataList.filter { it.pumpId == 5 }.let {
                if (it.isNotEmpty()) {
                    v6 *= it.map { data -> data.percent }.average().toFloat()
                }
            }
        }
        dao.update(cali!!.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4, v5 = v5, v6 = v6))
    }

}

data class CalibrationDataUiState(
    val pumpId: Int = 0,
    val cali: Calibration? = null,
    val container: Container = Container(),
    val caliData: List<CalibrationData> = emptyList(),
    val expect: Float = 0f,
    val actual: Float = 0f,
    val lock: Boolean = false,
)