package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.Arguments
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.JsonUtils
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.datastore.DataSaverDataStore
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsArgumentsViewModel @Inject constructor(
    private val dataStore: DataSaverDataStore
) : ViewModel() {

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
            TipsUtils.showTips(Tips.error( "同步参数失败: ${fail.joinToString()}"))
        } else {
            TipsUtils.showTips(Tips.info("同步参数成功"))
        }
    }

    // 导出参数
    suspend fun exportArguments() {
        try {
            val usbList = StorageUtils.getUsbStorageDir()
            if (usbList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到U盘"))
                return
            }
            val dir = usbList.first() + "/${StorageUtils.ARGUMENTS_DIR}"
            val sn = dataStore.readData(Constants.SN, Constants.DEFAULT_SN)
            val savePath = "$dir/$sn.json"
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
            TipsUtils.showTips(Tips.info("导出参数成功"))
        } catch (e: Exception) {
            LogUtils.error("ExportArguments", e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("导出参数失败"))
        }
    }

    // 导入参数
    suspend fun importArguments(file: File?) {
        try {
            if (file == null) {
                TipsUtils.showTips(Tips.error("参数文件不存在"))
                return
            }
            val argsJson = withContext(Dispatchers.IO) {
                file.readText()
            }
            val arguments = JsonUtils.fromJson<List<Arguments>>(argsJson)
            if (arguments.isEmpty()) {
                TipsUtils.showTips(Tips.error("参数文件格式错误"))
                return
            } else if (arguments.size != ProductUtils.getChannelCount()) {
                TipsUtils.showTips(Tips.error("参数文件通道数量错误"))
                return
            }
            val fail = mutableListOf<Int>()
            repeat(ProductUtils.getChannelCount()) { index ->
                if (!SerialPortUtils.setArguments(index, arguments[index])
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
                TipsUtils.showTips(Tips.info("导入参数成功"))
            }
        } catch (e: Exception) {
            LogUtils.error("ImportArguments", e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("导入参数失败"))
        }
    }

    // 获取参数文件
    fun getArgumentFiles(): List<File>? {
        val usbList = StorageUtils.getUsbStorageDir()
        if (usbList.isEmpty()) {
            TipsUtils.showTips(Tips.error("未检测到U盘"))
            return null
        }

        try {
            val fileList = mutableListOf<File>()
            val dir = usbList.first() + "/${StorageUtils.ARGUMENTS_DIR}"
            val file = File(dir)

            if (file.exists() && file.isDirectory) {
                file.listFiles()?.forEach {
                    if (it.isFile && it.name.endsWith(".json")) {
                        fileList.add(it)
                    }
                }
            }

            if (fileList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到参数文件"))
                return null
            }
            return fileList
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("未知错误"))
            return null
        }
    }
}