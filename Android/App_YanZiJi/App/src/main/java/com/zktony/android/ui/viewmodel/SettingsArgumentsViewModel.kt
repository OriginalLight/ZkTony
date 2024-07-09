package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsViewModel @Inject constructor() : ViewModel() {

    init {
        viewModelScope.launch {
            if (!AppStateUtils.isArgumentsSync) {
                val fail = mutableListOf<Int>()
                // 同步参数
                repeat(ProductUtils.getChannelCount()) { index ->
                    // 初始化参数
                    if (!SerialPortUtils.queryArguments(index)) {
                        fail.add(index + 1)
                    }
                    delay(10L)
                }
                AppStateUtils.isArgumentsSync = fail.isEmpty()
                if (fail.isNotEmpty()) {
                    TipsUtils.showTips(Tips(TipsType.ERROR, "同步参数失败: ${fail.joinToString()}"))
                } else {
                    TipsUtils.showTips(Tips(TipsType.INFO, "同步参数成功"))
                }
            }
        }
    }

    // 导出参数
    fun exportArguments() {

    }

    // 导入参数
    fun importArguments() {

    }

    // 清除参数
    fun clearArguments() {

    }

}