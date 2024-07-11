package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.Arguments
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.JsonUtils
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsViewModel @Inject constructor() : ViewModel() {

    init {
        viewModelScope.launch {
            if (!AppStateUtils.isArgumentsSync) {
                syncArguments()
            }
        }
    }

    private suspend fun syncArguments() {
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

    // 导出参数
    suspend fun exportArguments() {
        try {
            val usb = StorageUtils.getUsbStorageDir()
            if (usb.isEmpty()) {
                TipsUtils.showTips(Tips(TipsType.ERROR, "未检测到U盘"))
                return
            }
            val arguments = AppStateUtils.getArgumentList()
            val argsJson = JsonUtils.toJson(arguments)
            val savePath = "${usb.first()}/arguments/${ProductUtils.getSerialNumber()}.json"
            val file = File(savePath)
            if(!file.exists()) {
                file.parentFile?.mkdirs()
                withContext(Dispatchers.IO) {
                    file.createNewFile()
                }
            }
            withContext(Dispatchers.IO) {
                file.writeText(argsJson)
            }
            TipsUtils.showTips(Tips(TipsType.INFO, "导出参数成功"))
        } catch (e: Exception) {
            LogUtils.error("ExportArguments", e.stackTraceToString(), true)
            TipsUtils.showTips(Tips(TipsType.ERROR, "导出参数失败"))
        }
    }

    // 导入参数
    fun importArguments() {

    }

    // 清除参数
    fun clearArguments() {
        viewModelScope.launch {
            val args = Arguments()
            val fail = mutableListOf<Int>()
            repeat(ProductUtils.getChannelCount()) {
                if (!SerialPortUtils.setArguments(it, "SetArguments", 0x12.toByte(), args.toByteArray())) {
                    fail.add(it + 1)
                }
            }
            if (fail.isNotEmpty()) {
                TipsUtils.showTips(Tips(TipsType.ERROR, "清除参数失败: ${fail.joinToString()}"))
            } else {
                AppStateUtils.setArgumentsList(List(ProductUtils.MAX_CHANNEL_COUNT) { Arguments() })
                TipsUtils.showTips(Tips(TipsType.INFO, "清除参数成功"))
            }
        }
    }
}