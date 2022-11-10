package com.zktony.www.ui.admin

import android.content.Context
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
import com.zktony.www.common.extension.installApk
import com.zktony.www.common.extension.isNetworkAvailable
import com.zktony.www.common.network.download.DownloadManager
import com.zktony.www.common.network.download.DownloadState
import com.zktony.www.common.result.NetworkResult
import com.zktony.www.common.utils.Constants
import com.zktony.www.common.utils.Constants.DEVICE_ID
import com.zktony.www.data.model.Event
import com.zktony.www.data.model.Version
import com.zktony.www.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val systemRepository: SystemRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _event = MutableSharedFlow<AdminEvent>()
    val event = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.AUDIO)] ?: true
                }.distinctUntilChanged().collect {
                    _event.emit(AdminEvent.ChangeAudio(audio = it))
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.BAR)] ?: false
                }.distinctUntilChanged().collect {
                    _event.emit(AdminEvent.ChangeBar(bar = it))
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.DETECT)] ?: true
                }.distinctUntilChanged().collect {
                    _event.emit(AdminEvent.ChangeDetect(detect = it))
                }
            }
            launch {
                dataStore.data.map {
                    it[intPreferencesKey(Constants.INTERVAL)] ?: 1
                }.distinctUntilChanged().collect {
                    _event.emit(AdminEvent.ChangeInterval(interval = it))
                }
            }
            launch {
                dataStore.data.map {
                    it[intPreferencesKey(Constants.DURATION)] ?: 10
                }.distinctUntilChanged().collect {
                    _event.emit(AdminEvent.ChangeDuration(duration = it))
                }
            }
        }
    }

    /**
     * 下位机复位
     */
    fun rest() {
        Gpio.instance.setMulSel('h', 13, 1)
        Gpio.instance.writeGpio('h', 13, 0)
        Gpio.instance.writeGpio('h', 13, 1)
        EventBus.getDefault().post(Event(Constants.BLANK, Constants.RESET))
    }

    /**
     * 泵开关
     * @param pump [Boolean] true 开 false 关
     */
    fun changePump(pump: Boolean) {
        val cmd = appViewModel.latestSendCmd
        if (pump) {
            cmd.zmotorX = 1
            cmd.zmotorY = 1
        } else {
            cmd.zmotorX = 0
            cmd.zmotorY = 0
        }
        appViewModel.sendCmd(cmd)
    }

    /**
     * 跳转到Wi-Fi设置界面
     * @param context [Context]
     */
    fun wifiSetting(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //是否显示button bar
            putExtra("extra_prefs_show_button_bar", true)
            putExtra("extra_prefs_set_next_text", "完成")
            putExtra("extra_prefs_set_back_text", "返回")
        }
        context.startActivity(intent)
    }

    /**
     * 导航栏开关
     * @param bar [Boolean] true 开 false 关
     * @param context [Context]
     */
    fun changeBar(bar: Boolean, context: Context) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.BAR)] = bar
            }
        }
        val intent = Intent().apply {
            action = "ACTION_SHOW_NAVBAR"
            putExtra("cmd", if (bar) "show" else "hide")
        }
        context.sendBroadcast(intent)
    }

    /**
     * 音频开关
     * @param audio [Boolean] true 开 false 关
     */
    fun changeAudio(audio: Boolean) {
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
    fun changeDetect(detect: Boolean) {
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
    fun changeInterval(interval: Int) {
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
    fun changeDuration(duration: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(Constants.DURATION)] = duration
            }
        }
    }

    /**
     * 检查更新
     * @param context [Context]
     */
    fun checkUpdate(context: Context) {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                _event.emit(AdminEvent.CheckUpdate(apk, null))
            } else {
                checkRemoteUpdate(context)
            }
        }
    }

    /**
     * 更新
     * @param context [Context]
     * @param file [File] apk文件
     * @param version [Version] 版本信息
     */
    fun doUpdate(context: Context, file: File?, version: Version?) {
        file?.run {
            context.installApk(this)
        } ?: version?.run {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            downloadApk(context, this)
        }
    }

    /**
     * 下载apk
     * @param context [Context]
     * @param version [Version] 版本信息
     */
    private fun downloadApk(context: Context, version: Version) {
        viewModelScope.launch {
            PopTip.show("正在下载更新")
            DownloadManager.download(
                version.url,
                File(context.getExternalFilesDir(null), "update.apk")
            ).collect {
                when (it) {
                    is DownloadState.Success -> {
                        _event.emit(AdminEvent.DownloadSuccess(it.file))
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                        context.installApk(it.file)
                    }

                    is DownloadState.Err -> {
                        _event.emit(AdminEvent.DownloadError)
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                    }

                    is DownloadState.Progress -> {
                        _event.emit(AdminEvent.DownloadProgress(it.progress))
                    }
                }
            }
        }

    }


    /**
     * 获取远程版本信息
     * @param context [Context]
     */
    private fun checkRemoteUpdate(context: Context) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                systemRepository.getVersionInfo(DEVICE_ID).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            if (it.data.versionCode > BuildConfig.VERSION_CODE) {
                                _event.emit(AdminEvent.CheckUpdate(null, it.data))
                            } else {
                                PopTip.show("已是最新版本")
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
     * @return [File] apk文件
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

sealed class AdminEvent {
    data class ChangeInterval(val interval: Int) : AdminEvent()
    data class ChangeDuration(val duration: Int) : AdminEvent()
    data class ChangeBar(val bar: Boolean) : AdminEvent()
    data class ChangeAudio(val audio: Boolean) : AdminEvent()
    data class ChangeDetect(val detect: Boolean) : AdminEvent()
    data class CheckUpdate(val file: File?, val version: Version?) : AdminEvent()
    data class DownloadProgress(val progress: Int) : AdminEvent()
    data class DownloadSuccess(val file: File) : AdminEvent()
    object DownloadError : AdminEvent()
}

data class AdminUiState(
    var isUpdating: Boolean = false,
)