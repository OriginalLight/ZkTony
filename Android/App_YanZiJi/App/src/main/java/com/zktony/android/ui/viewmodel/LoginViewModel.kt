package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.SnackbarUtils
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
    suspend fun login(userName: String, password: String): Boolean {
        try {
            val res = userRepository.login(userName, password)
            delay(500L)
            AuthUtils.login(res)
            LogUtils.info("登录成功：$userName", true)
            TipsUtils.showTips(Tips.info("欢迎使用 $userName"))
            return true
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            when (e.message) {
                "1" -> SnackbarUtils.showSnackbar("用户不存在")
                "2" -> SnackbarUtils.showSnackbar("密码错误")
                "3" -> SnackbarUtils.showSnackbar("用户已被禁用")
                "4" -> SnackbarUtils.showSnackbar("用户已被删除")
                else -> SnackbarUtils.showSnackbar("登录失败")
            }
            return false
        }
    }
}