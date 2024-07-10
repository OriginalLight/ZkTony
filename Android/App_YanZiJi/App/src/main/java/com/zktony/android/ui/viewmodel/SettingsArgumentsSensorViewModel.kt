package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.data.ArgumentsBubble
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsSensorViewModel @Inject constructor() : ViewModel() {

    suspend fun setSensorArguments(channel: Int, args: ArgumentsBubble) {
        if (!SerialPortUtils.setSensorArguments(channel, args)) {
            TipsUtils.showTips(Tips.error("设置传感器参数失败 通道：${channel + 1}"))
            return
        }
        // 同步参数
        if (!SerialPortUtils.queryArguments(channel)) {
            TipsUtils.showTips(Tips.error("同步参数失败 通道：${channel + 1}"))
        }
        TipsUtils.showTips(Tips.info("设置参数成功 通道：${channel + 1}"))
    }
}