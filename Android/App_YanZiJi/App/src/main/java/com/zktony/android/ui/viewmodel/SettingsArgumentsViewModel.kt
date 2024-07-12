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
            val dir = StorageUtils.getArgumentDir()
            if (dir == null) {
                TipsUtils.showTips(Tips(TipsType.ERROR, "未检测到U盘"))
                return
            }
            val savePath = "${dir}/${ProductUtils.getSerialNumber()}.json"
            val arguments = AppStateUtils.getArgumentList()
            val argsJson = JsonUtils.toJson(arguments)
            val file = File(savePath)
            if (!file.exists()) {
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
    suspend fun importArguments(file: File?) {
        try {
            if (file == null) {
                TipsUtils.showTips(Tips(TipsType.ERROR, "参数文件不存在"))
                return
            }
            val argsJson = withContext(Dispatchers.IO) {
                file.readText()
            }
            val arguments = JsonUtils.fromJson<List<Arguments>>(argsJson)
            if (arguments.isEmpty()) {
                TipsUtils.showTips(Tips(TipsType.ERROR, "参数文件格式错误"))
                return
            } else if (arguments.size != ProductUtils.getChannelCount()) {
                TipsUtils.showTips(Tips(TipsType.ERROR, "参数文件通道数量错误"))
                return
            }
            val fail = mutableListOf<Int>()
            repeat(ProductUtils.getChannelCount()) { index ->
                if (!SerialPortUtils.setArguments(
                        index,
                        "SetArguments",
                        0x12.toByte(),
                        arguments[index].toByteArray()
                    )
                ) {
                    fail.add(index + 1)
                }
            }
            if (fail.isNotEmpty()) {
                TipsUtils.showTips(
                    Tips(
                        TipsType.ERROR,
                        "导入参数失败: 通道 ${fail.joinToString()}"
                    )
                )
                return
            } else {
                AppStateUtils.setArgumentsList(arguments)
                TipsUtils.showTips(Tips(TipsType.INFO, "导入参数成功"))
            }
        } catch (e: Exception) {
            LogUtils.error("ImportArguments", e.stackTraceToString(), true)
            TipsUtils.showTips(Tips(TipsType.ERROR, "导入参数失败"))
        }
    }

    // 获取参数文件
    fun getArgumentFiles(): List<File> {
        val fileList = mutableListOf<File>()
        val dir = StorageUtils.getArgumentDir()
        if (dir == null) {
            TipsUtils.showTips(Tips(TipsType.ERROR, "未检测到U盘"))
            return fileList
        }
        val file = File(dir)
        if (file.exists() && file.isDirectory) {
            file.listFiles { f ->
                f.isFile && f.name.endsWith(".json")
            }?.let {
                fileList.addAll(it)
            }
        }
        return fileList
    }

    // 清除参数
    suspend fun clearArguments() {
        val args = Arguments()
        val fail = mutableListOf<Int>()
        repeat(ProductUtils.getChannelCount()) {
            if (!SerialPortUtils.setArguments(
                    it,
                    "SetArguments",
                    0x12.toByte(),
                    args.toByteArray()
                )
            ) {
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