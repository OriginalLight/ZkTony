package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entity.Calibration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CalibrationViewModel constructor(
    private val dao: CalibrationDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * Navigate to
     *
     * @param page CalibrationPage
     * @return Unit
     */
    fun navigateTo(page: CalibrationPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    /**
     * Insert
     *
     * @param calibration Calibration
     * @return Unit
     */
    fun insert(calibration: Calibration) {
        viewModelScope.launch {
            dao.insert(calibration)
        }
    }

    /**
     * Delete
     *
     * @param calibration Calibration
     * @return Unit
     */
    fun delete(calibration: Calibration) {
        viewModelScope.launch {
            dao.delete(calibration)
        }
    }

    /**
     * Update
     *
     * @param calibration Calibration
     * @return Unit
     */
    fun update(calibration: Calibration) {
        viewModelScope.launch {
            dao.update(calibration)
        }
    }

}

data class CalibrationUiState(
    val list: List<Calibration> = emptyList(),
    val page: CalibrationPage = CalibrationPage.CALIBRATION,
)

enum class CalibrationPage {
    CALIBRATION,
    CALIBRATION_ADD,
    CALIBRATION_EDIT
}