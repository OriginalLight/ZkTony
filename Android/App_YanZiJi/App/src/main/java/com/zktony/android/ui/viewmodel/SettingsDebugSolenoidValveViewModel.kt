package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsDebugSolenoidValveViewModel @Inject constructor() : ViewModel() {

    suspend fun setSolenoidValveState(channel: Int, state: Int): Boolean {
        if (!SerialPortUtils.setSolenoidValveArguments(channel, state)) {
            // 设置失败
            TipsUtils.showTips(Tips.error("切换电磁阀失败 通道：${channel + 1}"))
            return false
        } else {
            // 设置成功
            TipsUtils.showTips(Tips.info("切换电磁阀成功 通道：${channel + 1}"))
            return true
        }
    }

    suspend fun setAllSolenoidValveState(state: Int): Boolean {
        val errorList = mutableListOf<Int>()
        repeat(ProductUtils.getChannelCount()) { channel ->
            if (!SerialPortUtils.setSolenoidValveArguments(channel, state)) {
                errorList.add(channel + 1)
            }
        }
        if (errorList.isNotEmpty()) {
            // 设置失败
            TipsUtils.showTips(Tips.error("切换电磁阀失败 通道：${errorList.joinToString()}"))
            return false
        } else {
            // 设置成功
            TipsUtils.showTips(Tips.info("切换电磁阀成功"))
            return true
        }
    }
}