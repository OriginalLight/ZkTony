package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsRuntimeViewModel @Inject constructor() : ViewModel() {

    suspend fun setArguments(channel: Int, transfer: ArgumentsTransfer, clean: ArgumentsClean) {
        // 设置转膜参数
        if(!SerialPortUtils.setTransferArguments(transfer, channel + 2)) {
            // 设置失败
            TipsUtils.showTips(Tips.error("设置转膜参数失败 通道：${channel + 1}"))
            return
        }
        delay(100L)
        // 设置清洗参数
        if(!SerialPortUtils.setCleanArguments(clean, channel + 2)) {
            // 设置失败
            TipsUtils.showTips(Tips.error("设置清洗参数失败 通道：${channel + 1}"))
            return
        }
        delay(100L)
        // 同步参数
        if (!SerialPortUtils.queryArguments(channel + 2)) {
            TipsUtils.showTips(Tips.error("同步参数失败 通道：${channel + 1}"))
        }
        delay(100L)
        TipsUtils.showTips(Tips.info("设置参数成功 通道：${channel + 1}"))
    }
}