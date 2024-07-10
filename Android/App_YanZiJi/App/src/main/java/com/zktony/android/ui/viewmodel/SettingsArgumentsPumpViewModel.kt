package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.data.ArgumentsSpeed
import com.zktony.android.data.PumpControl
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsPumpViewModel @Inject constructor() : ViewModel() {

    suspend fun startPump(channel: Int, control: PumpControl) {
        if (!SerialPortUtils.startPump(channel, control)) {
            TipsUtils.showTips(Tips.error("启动泵失败 通道：${channel + 1} 泵ID：${control.control}"))
        } else {
            TipsUtils.showTips(Tips.info("启动泵成功 通道：${channel + 1} 泵ID：${control.control}"))
        }
    }

    suspend fun stopPump(channel: Int, control: Int) {
        if (!SerialPortUtils.stopPump(channel, control)) {
            TipsUtils.showTips(Tips.error("停止泵失败 通道：${channel + 1} 泵ID：${control}"))
        } else {
            TipsUtils.showTips(Tips.info("停止泵成功 通道：${channel + 1} 泵ID：${control}"))
        }
    }

    suspend fun setPumpArguments(channel: Int, args: ArgumentsSpeed) {
        if (!SerialPortUtils.setSpeedArguments(channel, args)) {
            TipsUtils.showTips(Tips.error("设置泵参数失败 通道：${channel + 1}"))
            return
        }
        delay(100L)
        // 同步参数
        if (!SerialPortUtils.queryArguments(channel)) {
            TipsUtils.showTips(Tips.error("同步参数失败 通道：${channel + 1}"))
        }
        delay(100L)
        TipsUtils.showTips(Tips.info("设置参数成功 通道：${channel + 1}"))
    }
}