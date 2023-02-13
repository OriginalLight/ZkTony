package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.repository.CalibrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val calibrationRepository: CalibrationRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<List<Calibration>?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            calibrationRepository.getAll().distinctUntilChanged().collect {
                _uiState.value = it
            }
        }
    }

    fun insert(name: String) {
        viewModelScope.launch {
            val cali = _uiState.value?.find { it.name == name }
            if (cali != null) {
                PopTip.show("校准程序名已存在")
            } else {
                val calibration = Calibration(name = name)
                calibrationRepository.insert(calibration)
            }
        }
    }

    fun delete(calibration: Calibration) {
        viewModelScope.launch {
            calibrationRepository.delete(calibration)
            if (calibration.enable == 1) {
                val cali = _uiState.value?.find { it.name == "默认" }
                cali?.let { calibrationRepository.update(it.copy(enable = 1)) }
            }
        }
    }

    fun enable(calibration: Calibration) {
        viewModelScope.launch {
            val cali = _uiState.value?.find { it.enable == 1 }
            if (cali == null) {
                calibrationRepository.update(calibration.copy(enable = 1))
            } else {
                if (cali.id != calibration.id) {
                    calibrationRepository.updateBatch(
                        listOf(
                            cali.copy(enable = 0),
                            calibration.copy(enable = 1)
                        )
                    )
                }
            }
        }
    }
}

