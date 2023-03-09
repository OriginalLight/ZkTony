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
import com.zktony.common.app.CommonApplicationProxy
import com.zktony.common.base.BaseViewModel
import com.zktony.common.ext.installApk
import com.zktony.common.ext.isNetworkAvailable
import com.zktony.common.http.download.DownloadListener
import com.zktony.common.http.download.DownloadManager
import com.zktony.common.utils.Constants
import com.zktony.gpio.Gpio
import com.zktony.www.BuildConfig
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.data.remote.model.Application
import com.zktony.www.data.repository.ApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val applicationRepository: ApplicationRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _file = MutableStateFlow<File?>(null)
    private val _application = MutableStateFlow<Application?>(null)
    private val _progress = MutableStateFlow(0)
    val file = _file.asStateFlow()
    val application = _application.asStateFlow()
    val progress = _progress.asStateFlow()

    /**
     * 下位机复位
     */
    fun lowerComputerReset() {
        Gpio.instance.setMulSel('h', 12, 1)
        Gpio.instance.writeGpio('h', 12, 0)
        Gpio.instance.writeGpio('h', 12, 1)
    }

    /**
     * 泵开关
     * @param pump [Boolean] true 开 false 关
     */
    fun touchPump(pump: Boolean) {
        val cmd = appViewModel.send.value
        if (pump) {
            cmd.motorX = 1
            cmd.motorY = 1
        } else {
            cmd.motorX = 0
            cmd.motorY = 0
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
                preferences[intPreferencesKey(Constants.INTERVAL)] = minOf(interval, 10)
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
                preferences[intPreferencesKey(Constants.DURATION)] = minOf(duration, 200)
            }
        }
    }

    /**
     * 蠕动泵转速
     * @param speed [Int] 转速
     */
    fun toggleMotorSpeed(speed: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(Constants.MOTOR_SPEED)] = minOf(speed, 250)
            }
        }
    }

    /**
     * 取消或确认更新时清空标志
     * 防止重进进入时显示更新提示
     */
    fun cleanUpdate() {
        _file.value = null
        _application.value = null
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
     *  @param application [Application]
     */
    fun doRemoteUpdate(application: Application) {
        viewModelScope.launch {
            PopTip.show("开始下载")
            DownloadManager.startDownload(
                url = application.download_url,
                listener = object : DownloadListener {
                    override fun onProgress(bytesDownloaded: Int, bytesTotal: Int) {
                        _progress.value = (bytesDownloaded * 100 / bytesTotal)
                    }

                    override fun onComplete(file: File) {
                        _progress.value = 0
                        CommonApplicationProxy.application.installApk(file)
                    }

                    override fun onError(e: Exception) {
                        _progress.value = 0
                        PopTip.show("下载失败,请重试!").showLong()
                    }
                }
            )
        }

    }

    /**
     * 获取版本信息
     */
    private fun checkRemoteUpdate() {
        viewModelScope.launch {
            if (CommonApplicationProxy.application.isNetworkAvailable()) {
                applicationRepository.getById()
                    .catch {
                        PopTip.show("升级接口异常请联系管理员")
                    }
                    .collect {
                        val data = it.body()
                        if (data != null) {
                            if (data.version_code > BuildConfig.VERSION_CODE) {
                                _application.value = data
                            } else {
                                PopTip.show("当前已是最新版本")

                            }
                        } else {
                            PopTip.show("升级接口异常请联系管理员")
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
        File("/storage").listFiles()?.forEach {
            it.listFiles()?.forEach { apk ->
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-zm")) {
                    return apk
                }
            }
        }
        return null
    }

}