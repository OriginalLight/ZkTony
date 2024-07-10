package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.data.ArgumentsCurrent
import com.zktony.android.data.ArgumentsTemperature
import com.zktony.android.data.ArgumentsVoltage
import com.zktony.android.data.VoltageControl
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsVoltageViewModel @Inject constructor() : ViewModel() {
    suspend fun startVoltage(channel: Int, control: VoltageControl) {
        if (!SerialPortUtils.startVoltage(channel, control)) {
            TipsUtils.showTips(Tips.error("启动电极失败 通道：${channel + 1}"))
        } else {
            TipsUtils.showTips(Tips.info("启动电极成功 通道：${channel + 1}"))
        }
    }

    suspend fun stopVoltage(channel: Int) {
        if (!SerialPortUtils.stopVoltage(channel)) {
            TipsUtils.showTips(Tips.error("停止电极失败 通道：${channel + 1}"))
        } else {
            TipsUtils.showTips(Tips.info("停止电极成功 通道：${channel + 1}"))
        }
    }

    suspend fun setVoltageArguments(channel: Int, args: ArgumentsVoltage) {
        if (!SerialPortUtils.setVoltageArguments(channel, args)) {
            TipsUtils.showTips(Tips.error("设置电压参数失败 通道：${channel + 1}"))
            return
        }
        // 同步参数
        if (!SerialPortUtils.queryArguments(channel)) {
            TipsUtils.showTips(Tips.error("同步参数失败 通道：${channel + 1}"))
        }
        TipsUtils.showTips(Tips.info("设置参数成功 通道：${channel + 1}"))
    }

    suspend fun setCurrentArguments(channel: Int, args: ArgumentsCurrent) {
        if (!SerialPortUtils.setCurrentArguments(channel, args)) {
            TipsUtils.showTips(Tips.error("设置电流参数失败 通道：${channel + 1}"))
            return
        }
        // 同步参数
        if (!SerialPortUtils.queryArguments(channel)) {
            TipsUtils.showTips(Tips.error("同步参数失败 通道：${channel + 1}"))
        }
        TipsUtils.showTips(Tips.info("设置参数成功 通道：${channel + 1}"))
    }

    suspend fun setTemperatureArguments(channel: Int, args: ArgumentsTemperature) {
        if (!SerialPortUtils.setTemperatureArguments(channel, args)) {
            TipsUtils.showTips(Tips.error("设置温度参数失败 通道：${channel + 1}"))
            return
        }
        // 同步参数
        if (!SerialPortUtils.queryArguments(channel)) {
            TipsUtils.showTips(Tips.error("同步参数失败 通道：${channel + 1}"))
        }
        TipsUtils.showTips(Tips.info("设置参数成功 通道：${channel + 1}"))
    }
}