package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsRuntimeLogViewModel @Inject constructor() : ViewModel() {

    private val _fileList = MutableStateFlow(listOf<File>())
    private val _selected = MutableStateFlow(listOf<File>())

    val fileList = _fileList.asStateFlow()
    val selected = _selected.asStateFlow()

    init {
        viewModelScope.launch {
            _fileList.value = LogUtils.getLogs()
        }
    }

    fun select(log: File) {
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

            runBlocking {
                awaitAll(*selected.map { log ->
                    async(Dispatchers.IO) {
                        File(log.absolutePath).copyTo(File(dstDir + "/" + log.name), true)
                    }
                }.toTypedArray())
            }

            TipsUtils.showTips(Tips.info("导出成功"))
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("导出失败"))
        }
    }
}