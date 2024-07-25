package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.RuntimeLog
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.android.utils.extra.size
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SettingsRuntimeLogViewModel @Inject constructor() : ViewModel() {

    private val _list = MutableStateFlow(listOf<RuntimeLog>())
    private val _selected = MutableStateFlow(listOf<RuntimeLog>())

    val list = _list.asStateFlow()
    val selected = _selected.asStateFlow()

    init {
        viewModelScope.launch {
            val logFiles = LogUtils.getLogs()
            val logs = logFiles.map { file ->
                RuntimeLog(
                    name = file.name,
                    size = file.size(),
                    createTime = Date(file.lastModified()),
                    absolutePath = file.absolutePath
                )
            }
            _list.value = logs
        }
    }

    fun select(log: RuntimeLog) {
        _selected.value = if (_selected.value.contains(log)) {
            _selected.value - log
        } else {
            _selected.value + log
        }
    }

    suspend fun export() {
        try {
            val selected = _selected.value
            if (selected.isEmpty()) {
                return
            }

            val usbList = StorageUtils.getUsbStorageDir()
            if (usbList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到U盘"))
                return
            }

            val dstDir = usbList.first() + "/${StorageUtils.LOG_DIR}/${StorageUtils.RUNTIME_DIR}"
            if (!File(dstDir).exists()) {
                File(dstDir).mkdirs()
            }

            selected.forEach { log ->
                withContext(Dispatchers.IO) {
                    File(log.absolutePath).copyTo(File(dstDir + "/" + log.name), true)
                }
                delay(100L)
            }

            TipsUtils.showTips(Tips.info("导出成功"))
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("导出失败"))
        }
    }
}