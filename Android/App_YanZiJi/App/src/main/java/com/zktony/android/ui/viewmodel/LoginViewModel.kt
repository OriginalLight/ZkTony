package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.SnackbarUtils
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
    suspend fun login(userName: String, password: String): Boolean {
        try {
            val res = userRepository.login(userName, password)
            delay(500L)
            if (res.isSuccess) {
                AuthUtils.login(res.getOrNull()!!)
                return true
            } else {
                when (res.exceptionOrNull()?.message) {
                    "001" -> SnackbarUtils.showSnackbar("用户不存在")
                    "002" -> SnackbarUtils.showSnackbar("密码错误")
                    "003" -> SnackbarUtils.showSnackbar("用户已被禁用")
                    "004" -> SnackbarUtils.showSnackbar("更新用户信息失败")
                    else -> SnackbarUtils.showSnackbar("未知错误")
                }
            }
            return false
        } catch (e: Exception) {
            SnackbarUtils.showSnackbar(e.message ?: "未知错误")
            return false
        }
    }
}