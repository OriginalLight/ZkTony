package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.local.dao.UserDao
import com.zktony.manager.data.local.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel constructor(
    private val dao: UserDao
) : ViewModel() {


    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                if (it.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(user = it.first())
                }
            }
        }
    }


    fun insert(user: User) {
        viewModelScope.launch {
            dao.insert(user)
        }
    }


}

data class UserUiState(
    val user: User = User()
)