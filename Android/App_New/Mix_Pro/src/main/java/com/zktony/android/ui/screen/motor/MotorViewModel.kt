package com.zktony.android.ui.screen.motor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entity.Motor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 */
class MotorViewModel constructor(
    private val dao: MotorDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(MotorUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * Navigate to page
     *
     * @param page MotorPage
     * @return Unit
     */
    fun navigateTo(page: MotorPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    /**
     * Update motor
     *
     * @param motor Motor
     * @return Unit
     */
    fun update(motor: Motor) {
        viewModelScope.launch {
            dao.update(motor)
        }
    }
}

/**
 * Motor ui state
 */
data class MotorUiState(
    val list: List<Motor> = emptyList(),
    val page: MotorPage = MotorPage.MOTOR,
)

/**
 * Motor page
 */
enum class MotorPage {
    MOTOR,
    MOTOR_EDIT,
}