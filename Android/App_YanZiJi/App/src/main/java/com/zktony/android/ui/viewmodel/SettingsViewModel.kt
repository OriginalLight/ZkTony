package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.HzmctUtils
import com.zktony.android.utils.PromptSoundUtils
import com.zktony.android.utils.ResourceUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import com.zktony.room.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // 语言
    fun setLanguage(language: String) {
        val tipsMessage =
            "${ResourceUtils.stringResource(R.string.language)} ${ResourceUtils.stringResource(R.string.set_success)}"
        TipsUtils.showTips(Tips.info(tipsMessage))
        LogUtils.info("$tipsMessage $language", true)
    }

    // 提示音
    fun setPromptSound(name: String) {
        PromptSoundUtils.with(name)
        val tipsMessage =
            "${ResourceUtils.stringResource(R.string.prompt_sound)} ${ResourceUtils.stringResource(R.string.set_success)}"
        TipsUtils.showTips(Tips.info(tipsMessage))
        LogUtils.info("$tipsMessage $name", true)
    }

    // 导航栏
    fun setNavigationBar(status: Boolean): Boolean {
        val bool = HzmctUtils.setNavigationBar(status)
        if (bool) {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.navigation_bar)} ${
                ResourceUtils.stringResource(R.string.set_success)
            }"
            TipsUtils.showTips(Tips.info(tipsMessage))
            LogUtils.info("$tipsMessage $status", true)
        } else {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.navigation_bar)} ${
                ResourceUtils.stringResource(R.string.set_failed)
            }"
            TipsUtils.showTips(Tips.error(tipsMessage))
            LogUtils.error("$tipsMessage $status", true)
        }
        return bool
    }

    // 状态栏
    fun setStatusBar(status: Boolean): Boolean {
        val bool = HzmctUtils.setStatusBar(status)
        if (bool) {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.status_bar)} ${
                ResourceUtils.stringResource(R.string.set_success)
            }"
            TipsUtils.showTips(Tips.info(tipsMessage))
            LogUtils.info("$tipsMessage $status", true)
        } else {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.status_bar)} ${
                ResourceUtils.stringResource(R.string.set_failed)
            }"
            TipsUtils.showTips(Tips.error(tipsMessage))
            LogUtils.error("$tipsMessage $status", true)
        }
        return bool
    }

    // 主屏幕
    fun setHomePackage(b: Boolean): Boolean {
        val bool =
            HzmctUtils.setHomePackage(if (b) BuildConfig.APPLICATION_ID else "com.android.launcher3")
        if (bool) {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.home_package)} ${
                ResourceUtils.stringResource(R.string.set_success)
            }"
            TipsUtils.showTips(Tips.info(tipsMessage))
            LogUtils.info("$tipsMessage $b", true)
        } else {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.home_package)} ${
                ResourceUtils.stringResource(R.string.set_failed)
            }"
            TipsUtils.showTips(Tips.error(tipsMessage))
            LogUtils.error("$tipsMessage $b", true)
        }
        return bool
    }

    // 系统时间
    fun setSystemTime(time: Long): Boolean {
        val bool = HzmctUtils.setSystemTime(time)
        if (bool) {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.system_time)} ${
                ResourceUtils.stringResource(R.string.set_success)
            }"
            TipsUtils.showTips(Tips.info(tipsMessage))
            LogUtils.info("$tipsMessage $time", true)
        } else {
            val tipsMessage = "${ResourceUtils.stringResource(R.string.system_time)} ${
                ResourceUtils.stringResource(R.string.set_failed)
            }"
            TipsUtils.showTips(Tips.error(tipsMessage))
            LogUtils.error("$tipsMessage $time", true)
        }
        return bool
    }

    // 密码验证
    suspend fun verifyPassword(oldPassword: String): Boolean {
        val res = userRepository.verifyPassword(AuthUtils.getIdentity(), oldPassword)
        delay(300L)
        if(res.isSuccess) {
            return res.getOrNull() ?: false
        }
        return false
    }

    // 修改密码
    suspend fun modifyPassword(newPassword: String): Boolean {
        val res = userRepository.modifyPassword(AuthUtils.getIdentity(), newPassword)
        delay(300L)
        if(res.isSuccess) {
            TipsUtils.showTips(Tips.info("密码修改成功"))
            LogUtils.info("密码修改成功", true)
            return res.getOrNull() != null
        }
        TipsUtils.showTips(Tips.error("密码修改失败"))
        LogUtils.error("密码修改失败", true)
        return false
    }
}