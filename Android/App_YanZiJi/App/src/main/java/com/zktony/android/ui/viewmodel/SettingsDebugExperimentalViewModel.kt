package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.data.Experimental
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsDebugExperimentalViewModel @Inject constructor() : ViewModel() {

    suspend fun pipelineClean(channel: Int, speed: Double, time: Int): Boolean {
        if (!SerialPortUtils.pipelineClean(
                channel,
                byteArrayOf((speed * 100).toInt().toByte(), (time * 60).toByte())
            )
        ) {
            TipsUtils.showTips(Tips.error("管路清洗失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("管路清洗成功 通道：${channel + 1}"))
            return true
        }
    }

    suspend fun startExperiment(channel: Int, experimental: Experimental): Boolean {
        if (!SerialPortUtils.setExperimentalArguments(channel, experimental)) {
            TipsUtils.showTips(Tips.error("实验开始失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验开始成功 通道：${channel + 1}"))
        }
        if (!SerialPortUtils.setExperimentalState(channel, 1)) {
            TipsUtils.showTips(Tips.error("实验开始失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验开始成功 通道：${channel + 1}"))
            return true
        }
    }

    suspend fun stopExperiment(channel: Int): Boolean {
        if (!SerialPortUtils.setExperimentalState(channel, 3)) {
            TipsUtils.showTips(Tips.error("实验停止失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验停止成功 通道：${channel + 1}"))
            return true
        }
    }
}
