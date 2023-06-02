package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ZktyHomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

}

data class HomeUiState(
    val page: Boolean = false,
)