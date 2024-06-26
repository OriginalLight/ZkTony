package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.HzmctUtils
import com.zktony.android.utils.PromptSoundUtils
import com.zktony.android.utils.ResourceUtils
import com.zktony.android.utils.SnackbarUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.datastore.DataSaverDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    // 语言
    fun setLanguage(language: String) {
        ResourceUtils.setLanguage(language)
        TipsUtils.showTips(Tips(TipsType.INFO, "${ResourceUtils.stringResource(R.string.language)} ${ResourceUtils.stringResource(R.string.set_success)}"))
        SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.language)} ${ResourceUtils.stringResource(R.string.set_success)}")
    }

    // 提示音
    fun setPromptSound(promptSound: String) {
        PromptSoundUtils.setPromptSound(promptSound)
        TipsUtils.showTips(Tips(TipsType.INFO, "${ResourceUtils.stringResource(R.string.prompt_sound)} ${ResourceUtils.stringResource(R.string.set_success)}"))
        SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.prompt_sound)} ${ResourceUtils.stringResource(R.string.set_success)}")
    }

    // 导航栏
    fun setNavigationBar(status: Boolean) :Boolean  {
        val bool = HzmctUtils.setNavigationBar(status)
        if (bool) {
            TipsUtils.showTips(Tips(TipsType.INFO, "${ResourceUtils.stringResource(R.string.navigation_bar)} ${ResourceUtils.stringResource(R.string.set_success)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.navigation_bar)} ${ResourceUtils.stringResource(R.string.set_success)}")
        } else {
            TipsUtils.showTips(Tips(TipsType.ERROR, "${ResourceUtils.stringResource(R.string.navigation_bar)} ${ResourceUtils.stringResource(R.string.set_failed)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.navigation_bar)} ${ResourceUtils.stringResource(R.string.set_failed)}")
        }
        return bool
    }

    // 状态栏
    fun setStatusBar(status: Boolean): Boolean {
        val bool = HzmctUtils.setStatusBar(status)
        if (bool) {
            TipsUtils.showTips(Tips(TipsType.INFO, "${ResourceUtils.stringResource(R.string.status_bar)} ${ResourceUtils.stringResource(R.string.set_success)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.status_bar)} ${ResourceUtils.stringResource(R.string.set_success)}")
        } else {
            TipsUtils.showTips(Tips(TipsType.ERROR, "${ResourceUtils.stringResource(R.string.status_bar)} ${ResourceUtils.stringResource(R.string.set_failed)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.status_bar)} ${ResourceUtils.stringResource(R.string.set_failed)}")
        }
        return bool
    }

    // 主屏幕
    fun setHomePackage(b: Boolean): Boolean {
        val bool = HzmctUtils.setHomePackage(if (b) BuildConfig.APPLICATION_ID else "com.android.launcher3")
        if (bool) {
            TipsUtils.showTips(Tips(TipsType.INFO, "${ResourceUtils.stringResource(R.string.home_package)} ${ResourceUtils.stringResource(R.string.set_success)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.home_package)} ${ResourceUtils.stringResource(R.string.set_success)}")
        } else {
            TipsUtils.showTips(Tips(TipsType.ERROR, "${ResourceUtils.stringResource(R.string.home_package)} ${ResourceUtils.stringResource(R.string.set_failed)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.home_package)} ${ResourceUtils.stringResource(R.string.set_failed)}")
        }
        return bool
    }

    // 系统时间
    fun setSystemTime(time: Long): Boolean {
        val bool = HzmctUtils.setSystemTime(time)
        if (bool) {
            TipsUtils.showTips(Tips(TipsType.INFO, "${ResourceUtils.stringResource(R.string.system_time)} ${ResourceUtils.stringResource(R.string.set_success)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.system_time)} ${ResourceUtils.stringResource(R.string.set_success)}")
        } else {
            TipsUtils.showTips(Tips(TipsType.ERROR, "${ResourceUtils.stringResource(R.string.system_time)} ${ResourceUtils.stringResource(R.string.set_failed)}"))
            SnackbarUtils.showSnackbar("${ResourceUtils.stringResource(R.string.system_time)} ${ResourceUtils.stringResource(R.string.set_failed)}")
        }
        return bool
    }
}