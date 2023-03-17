package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.data.local.room.dao.CalibrationDao
import com.zktony.www.data.local.room.dao.CalibrationDataDao
import com.zktony.www.data.local.room.entity.Calibration
import com.zktony.www.data.local.room.entity.CalibrationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationDataViewModel @Inject constructor(
    private val dao: CalibrationDao,
    private val dataDao: CalibrationDataDao
    ) : BaseViewModel() {


    @Inject
    lateinit var appViewModel: AppViewModel

    private val _uiState = MutableStateFlow(CalibrationDataUiState())
    val uiState = _uiState.asStateFlow()

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
            launch {
                SerialManager.instance.lock.collect {
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
            dataDao.delete(data)
            calculateActual(data.calibrationId)
        }
    }

    fun addLiquid() {
        val manager = ExecutionManager.instance
        val con = appViewModel.settings.value.container
        val liquid = _uiState.value.expect
        val motorId = _uiState.value.pumpId
        manager.executor(
            manager.generator(y = con.washY),
            manager.generator(
                y = con.washY,
                z = con.washZ,
                v1 = if (motorId == 0) liquid else 0f,
                v2 = if (motorId == 1) liquid else 0f,
                v3 = if (motorId == 2) liquid else 0f,
                v4 = if (motorId == 3) liquid else 0f,
                v5 = if (motorId == 4) liquid else 0f,
                v6 = if (motorId == 5) liquid else 0f
            ),
            manager.generator(
                y = con.washY,
                v1 = if (motorId == 0) 15000f else 0f,
                v2 = if (motorId == 1) 15000f else 0f,
                v3 = if (motorId == 2) 15000f else 0f,
                v4 = if (motorId == 3) 15000f else 0f,
                v5 = if (motorId == 4) 15000f else 0f,
                v6 = if (motorId == 5) 15000f else 0f
            ),
            manager.generator()
        )
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
    val caliData: List<CalibrationData> = emptyList(),
    val expect: Float = 0f,
    val actual: Float = 0f,
    val lock: Boolean = false,
)