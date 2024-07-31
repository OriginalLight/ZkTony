package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.TipsUtils
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
            AuthUtils.login(res)
            LogUtils.info("登录成功：$userName", true)
            TipsUtils.showTips(Tips.info("欢迎使用 $userName"))
            return 0
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            return when (e.message) {
                "1" -> 1
                "2" -> 2
                "3" -> 3
                "4" -> 4
                else -> -1
            }
        }
    }
}