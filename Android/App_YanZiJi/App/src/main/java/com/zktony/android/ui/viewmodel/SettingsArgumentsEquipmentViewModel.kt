package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.R
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.ResourceUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsEquipmentViewModel @Inject constructor() : ViewModel() {

    // 设置P/N参数
    fun setProductNumber(pn: String) {
        viewModelScope.launch {
            val tipsMessage = "P/N ${ResourceUtils.stringResource(R.string.set_success)}"
            TipsUtils.showTips(Tips(TipsType.INFO, tipsMessage))
            LogUtils.info("$tipsMessage $pn")
            repeat(ProductUtils.getModuleCount()) { index ->
                SerialPortUtils.setProductNumber(pn, index + 2)
            }
        }
    }

    // 设置S/N参数
    fun setSerialNumber(sn: String) {
        viewModelScope.launch {
            val tipsMessage = "S/N ${ResourceUtils.stringResource(R.string.set_success)}"
            TipsUtils.showTips(Tips(TipsType.INFO, tipsMessage))
            LogUtils.info("$tipsMessage $sn")
            repeat(ProductUtils.getModuleCount()) { index ->
                SerialPortUtils.setSerialNumber(sn, index + 2)
            }
        }
    }

}