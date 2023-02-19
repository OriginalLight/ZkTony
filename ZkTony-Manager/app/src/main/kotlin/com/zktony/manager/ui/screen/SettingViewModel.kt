package com.zktony.manager.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.local.model.User
import com.zktony.manager.data.store.UserStore
import com.zktony.manager.ui.utils.DataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    private val userStore: UserStore = DataManager.userStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userStore.getAll().collect {
                if (it.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(user = it.first())
                }
            }
        }
    }

    fun navigateTo(page: SettingPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user.copy(name = name))
    }

    fun onPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user.copy(phone = phone))
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            delay(1000)
            userStore.insert(_uiState.value.user)
            _uiState.value = _uiState.value.copy(loading = false, page = SettingPage.SETTING)
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
    SETTING, USER_INFO
}