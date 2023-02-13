package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.CalibrationData
import com.zktony.www.control.motion.MotionManager
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.common.repository.CalibrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationDataViewModel @Inject constructor(
    private val calibrationRepository: CalibrationRepository
) : BaseViewModel() {


    @Inject
    lateinit var appViewModel: AppViewModel

    private val _uiState = MutableStateFlow(CalibrationDataUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: String) {
        viewModelScope.launch {
            launch {
                calibrationRepository.getById(id).distinctUntilChanged().collect {
                    _uiState.value = _uiState.value.copy(cali = it)
                }
            }
            launch {
                calibrationRepository.getDataById(id).distinctUntilChanged().collect {
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
            calibrationRepository.deleteData(data)
        }
    }

    fun addLiquid() {
        val manager = MotionManager.instance
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
            calibrationRepository.insertData(
                CalibrationData(
                    pumpId = _uiState.value.pumpId,
                    calibrationId = _uiState.value.cali?.id ?: "",
                    expect = _uiState.value.expect,
                    actual = _uiState.value.actual,
                )
            )
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

}

data class CalibrationDataUiState(
    val pumpId: Int = 0,
    val cali: Calibration? = null,
    val caliData: List<CalibrationData> = emptyList(),
    val expect: Float = 0f,
    val actual: Float = 0f,
    val lock: Boolean = false,
)