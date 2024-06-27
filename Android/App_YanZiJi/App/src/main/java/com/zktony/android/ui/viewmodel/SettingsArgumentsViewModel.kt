package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.R
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.ResourceUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsViewModel @Inject constructor() : ViewModel() {

    // 设置P/N参数
    fun setProductNumber(pn: String) {
        val tipsMessage = "P/N ${ResourceUtils.stringResource(R.string.set_success)}"
        TipsUtils.showTips(Tips(TipsType.INFO, tipsMessage))
        LogUtils.info("$tipsMessage $pn", tipsMessage)
    }

    // 设置S/N参数
    fun setSerialNumber(sn: String) {
        val tipsMessage = "S/N ${ResourceUtils.stringResource(R.string.set_success)}"
        TipsUtils.showTips(Tips(TipsType.INFO, tipsMessage))
        LogUtils.info("$tipsMessage $sn" , tipsMessage)
    }

    // 导出参数
    fun exportArguments() {

    }

    // 导入参数
    fun importArguments() {

    }

}