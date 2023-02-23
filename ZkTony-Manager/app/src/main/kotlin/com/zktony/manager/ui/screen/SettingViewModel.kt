package com.zktony.manager.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.local.model.User
import com.zktony.manager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getAll().collect {
                if (it.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(user = it.first())
                }
            }
        }
    }

    fun navigateTo(page: SettingPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun onUserChange(user: User) {
        viewModelScope.launch {
            userRepository.insert(user)
        }
    }

}

data class SettingUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val page: SettingPage = SettingPage.SETTING,
    val user: User = User()
)

enum class SettingPage {
    SETTING, USER_MODIFY
}