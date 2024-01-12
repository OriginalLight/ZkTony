package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()


    fun navigateTo(page: SettingPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

}

data class SettingUiState(
    val page: SettingPage = SettingPage.SETTING,
)

enum class SettingPage {
    SETTING,
    USER_EDIT,
    UPGRADE,
}