package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ManagerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ManagerUiState())
    val uiState = _uiState.asStateFlow()

    fun navigateTo(page: ManagerPageEnum) {
        _uiState.value = _uiState.value.copy(page = page)
    }
}

data class ManagerUiState(
    val page: ManagerPageEnum = ManagerPageEnum.MANAGER,
)

enum class ManagerPageEnum {
    MANAGER,
    CUSTOMER_LIST,
    CUSTOMER_EDIT,
    INSTRUMENT_LIST,
    INSTRUMENT_EDIT,
    SOFTWARE_LIST,
    SOFTWARE_EDIT,
}