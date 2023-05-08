package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ConfigViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState = _uiState.asStateFlow()

}

data class ConfigUiState(
    val xAxisTravel: Float = 0f,
    val yAxisTravel: Float = 0f,
    val zAxisTravel: Float = 0f,
)
