package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.SnackbarUtils
import com.zktony.log.LogUtils
import com.zktony.room.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            userRepository.init()
        }
    }

    // Login.
    suspend fun login(userName: String, password: String): Int {
        try {
            val res = userRepository.login(userName, password)
            delay(500L)
            if (res.isSuccess) {
                AuthUtils.login(res.getOrNull()!!)
                return 0
            } else {
                return when (res.exceptionOrNull()?.message) {
                    "001" -> 1
                    "002" -> 2
                    "003" -> 3
                    "004" -> 4
                    else -> -1
                }
            }
        } catch (e: Exception) {
            LogUtils.error("LoginViewModel", e.stackTraceToString(), true)
            return -1
        }
    }
}