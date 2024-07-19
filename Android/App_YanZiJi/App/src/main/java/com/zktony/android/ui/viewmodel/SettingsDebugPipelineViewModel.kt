package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.data.PipelineControl
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsDebugPipelineViewModel @Inject constructor() : ViewModel() {

    suspend fun pipelineFill(channel: Int, value: Int): Boolean {
        if (!SerialPortUtils.pipelineFill(channel, value)) {
            TipsUtils.showTips(Tips.error("管路填充失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("管路填充成功 通道：${channel + 1}"))
            return true
        }
    }

    suspend fun pipelineDrain(channel: Int, value: Int): Boolean {
        if (!SerialPortUtils.pipelineDrain(channel, value)) {
            TipsUtils.showTips(Tips.error("管路排空失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("管路排空成功 通道：${channel + 1}"))
            return true
        }
    }

    suspend fun pipelineClean(channel: Int, control: PipelineControl): Boolean {
        if (!SerialPortUtils.pipelineClean(channel, control)
        ) {
            TipsUtils.showTips(Tips.error("管路清洗失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("管路清洗成功 通道：${channel + 1}"))
            return true
        }
    }
}
