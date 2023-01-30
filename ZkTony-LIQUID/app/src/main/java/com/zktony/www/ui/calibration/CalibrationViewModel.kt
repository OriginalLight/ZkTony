package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.data.repository.CalibrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

}

