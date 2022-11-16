package com.zktony.www.ui.admin

import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.gpio.Gpio
import com.zktony.www.BuildConfig
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.app.CommonApplicationProxy
import com.zktony.www.common.extension.installApk
import com.zktony.www.common.extension.isNetworkAvailable
import com.zktony.www.common.network.download.DownloadManager
import com.zktony.www.common.network.download.DownloadState
import com.zktony.www.common.result.NetworkResult
import com.zktony.www.common.utils.Constants
import com.zktony.www.common.utils.Constants.DEVICE_ID
import com.zktony.www.data.model.Version
import com.zktony.www.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val systemRepository: SystemRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _file = MutableStateFlow<File?>(null)
    private val _version = MutableStateFlow<Version?>(null)
    private val _progress = MutableStateFlow(0)
    val file = _file.asStateFlow()
    val version = _version.asStateFlow()
    val progress = _progress.asStateFlow()

    /**
     * 下位机复位
     */
    fun lowerComputerReset() {
        Gpio.instance.setMulSel('h', 13, 1)
        Gpio.instance.writeGpio('h', 13, 0)
        Gpio.instance.writeGpio('h', 13, 1)
    }

    /**
     * 泵开关
     * @param pump [Boolean] true 开 false 关
     */
    fun touchPump(pump: Boolean) {
        val cmd = appViewModel.send.value
        if (pump) {
            cmd.zmotorX = 1
            cmd.zmotorY = 1
        } else {
            cmd.zmotorX = 0
            cmd.zmotorY = 0
        }
        appViewModel.send(cmd)
    }

    /**
     * wifi设置
     */
    fun wifiSetting() {
        viewModelScope.launch {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                //是否显示button bar
                putExtra("extra_prefs_show_button_bar", true)
                putExtra("extra_prefs_set_next_text", "完成")
                putExtra("extra_prefs_set_back_text", "返回")
            }
            CommonApplicationProxy.application.startActivity(intent)
        }
    }

    /**
     * 导航栏切换
     * @param bar [Boolean]
     */
    fun toggleNavigationBar(bar: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.BAR)] = bar
            }
        }
        val intent = Intent().apply {
            action = "ACTION_SHOW_NAVBAR"
            putExtra("cmd", if (bar) "show" else "hide")
        }
        CommonApplicationProxy.application.sendBroadcast(intent)
    }

    /**
     * 音频开关
     * @param audio [Boolean] true 开 false 关
     */
    fun toggleAudio(audio: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.AUDIO)] = audio
            }
        }
    }

    /**
     * 电流检测开关
     * @param detect [Boolean] true 开 false 关
     */
    fun toggleDetect(detect: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.DETECT)] = detect
            }
        }
    }

    /**
     * 直流泵间隔
     * @param interval [Int] 间隔时间
     */
    fun toggleInterval(interval: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(Constants.INTERVAL)] = interval
            }
        }
    }

    /**
     * 直流泵持续时间
     * @param duration [Int] 持续时间
     */
    fun toggleDuration(duration: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(Constants.DURATION)] = duration
            }
        }
    }

    /**
     * 检查更新
     */
    fun checkUpdate() {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                _file.value = apk
            } else {
                checkRemoteUpdate()
            }
        }
    }

    /**
     *  下载apk
     *  @param version [Version]
     */
    fun doRemoteUpdate(version: Version) {
        viewModelScope.launch {
            PopTip.show("开始下载")
            DownloadManager.download(
                version.url,
                File(CommonApplicationProxy.application.getExternalFilesDir(null), "update.apk")
            ).collect {
                when (it) {
                    is DownloadState.Success -> {
                        _progress.value = 0
                        CommonApplicationProxy.application.installApk(it.file)
                    }

                    is DownloadState.Err -> {
                        _progress.value = 0
                        PopTip.show("下载失败,请重试!").showLong()
                    }

                    is DownloadState.Progress -> {
                        _progress.value = it.progress
                    }
                }
            }
        }

    }

    /**
     * 获取版本信息
     */
    private fun checkRemoteUpdate() {
        viewModelScope.launch {
            if (CommonApplicationProxy.application.isNetworkAvailable()) {
                systemRepository.getVersionInfo(DEVICE_ID).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            if (it.data.versionCode > BuildConfig.VERSION_CODE) {
                                _version.value = it.data
                            } else {
                                PopTip.show("已经是最新版本")
                            }
                        }
                        is NetworkResult.Error -> {
                            PopTip.show("升级接口异常请联系管理员")
                        }
                        else -> {}
                    }
                }
            } else {
                PopTip.show("请连接网络或插入升级U盘")
            }
        }
    }

    /**
     * 查找目录下apk文件并安装
     * @return File? [File]
     */
    private fun checkLocalUpdate(): File? {
        File("/mnt/usbhost").listFiles()?.forEach {
            it.listFiles()?.forEach { apk ->
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-zm")) {
                    return apk
                }
            }
        }
        return null
    }
}