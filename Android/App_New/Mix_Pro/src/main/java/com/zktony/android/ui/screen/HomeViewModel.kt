package com.zktony.android.ui.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun navigateTo(page: HomePageEnum) {
        _uiState.value = _uiState.value.copy(page = page)
    }

}

data class HomeUiState(
    val page: HomePageEnum = HomePageEnum.HOME,
)

enum class HomePageEnum {
    HOME,
}