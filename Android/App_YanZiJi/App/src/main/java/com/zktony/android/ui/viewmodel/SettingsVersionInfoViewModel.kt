package com.zktony.android.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.BuildConfig
import com.zktony.android.data.UpgradeState
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsVersionInfoViewModel @Inject constructor() : ViewModel() {
    private val _versionList = MutableStateFlow(listOf<String>())
    val versionList = _versionList.asStateFlow()

    init {
        queryVersion()
    }

    fun queryVersion() {
        viewModelScope.launch {
            _versionList.value = listOf(BuildConfig.VERSION_NAME)
            _versionList.value += SerialPortUtils.queryVersion(0, "B")
            repeat(4) {
                _versionList.value += SerialPortUtils.queryVersion(it)
            }
        }
    }

    fun getApks(): List<File>? {
        val usbList = StorageUtils.getUsbStorageDir()
        if (usbList.isEmpty()) {
            TipsUtils.showTips(Tips.error("未检测到U盘"))
            return null
        }

        try {
            val apkList = mutableListOf<File>()
            val file = File(usbList.first())
            if (file.exists() && file.isDirectory) {
                file.listFiles()?.forEach {
                    if (it.isFile && it.name.endsWith(".apk")) {
                        apkList.add(it)
                    }
                }
            }
            if (apkList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到APK文件"))
                return null
            }
            return apkList
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("未知错误"))
            return null
        }
    }

    fun getBins(): List<File>? {
        val usbList = StorageUtils.getUsbStorageDir()
        if (usbList.isEmpty()) {
            TipsUtils.showTips(Tips.error("未检测到U盘"))
            return null
        }

        try {
            val binList = mutableListOf<File>()
            val file = File(usbList.first())
            if (file.exists() && file.isDirectory) {
                file.listFiles()?.forEach {
                    if (it.isFile && it.name.endsWith(".bin")) {
                        binList.add(it)
                    }
                }
            }

            if (binList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到BIN文件"))
                return null
            }

            return binList
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("未知错误"))
            return null
        }
    }

    @SuppressLint("DefaultLocale")
    suspend fun upgrade(file: File, device: String = "A", channel: Int = 0) {
        SerialPortUtils.upgrade(File(file.absolutePath), device, channel).collect {
            when (it) {
                is UpgradeState.Message -> {
                    TipsUtils.showTips(Tips.info(it.message))
                }

                is UpgradeState.Err -> {
                    LogUtils.error(it.t.stackTraceToString(), true)
                    TipsUtils.showTips(Tips.error(it.t.message ?: "未知错误"))
                }

                is UpgradeState.Success -> {
                    TipsUtils.showTips(Tips.info("升级成功"))
                }

                is UpgradeState.Progress -> {
                    LogUtils.info("${(it.progress * 100).toInt()}%")
                }
            }
        }
    }
}