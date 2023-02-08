package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.CalibrationData
import com.zktony.www.control.motion.MotionManager
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.data.repository.CalibrationRepository
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
            launch {
                SerialManager.instance.work.collect {
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
            calibrationRepository.deleteData(data)
        }
    }

    fun addLiquid() {
        val manager = MotionManager.instance
        val state = _uiState.value
        val gen = when (state.pumpId) {
            0 -> manager.generator(v1 = state.expect)
            1 -> manager.generator(v2 = state.expect)
            2 -> manager.generator(v3 = state.expect)
            3 -> manager.generator(v4 = state.expect)
            else -> return
        }
        manager.executor(gen)
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
    val work: Boolean = false,
)