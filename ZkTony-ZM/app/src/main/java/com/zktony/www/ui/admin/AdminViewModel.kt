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
import com.zktony.gpio.Gpio
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppIntent
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.extension.installApk
import com.zktony.www.common.extension.isNetworkAvailable
import com.zktony.www.common.extension.showShortToast
import com.zktony.www.common.extension.versionCode
import com.zktony.www.common.http.adapter.getOrThrow
import com.zktony.www.common.http.adapter.isSuccess
import com.zktony.www.common.http.download.DownloadManager
import com.zktony.www.common.http.download.DownloadState
import com.zktony.www.common.model.Event
import com.zktony.www.data.repository.SystemRepository
import com.zktony.www.data.services.model.Version
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

    private val _state = MutableSharedFlow<AdminState>()
    val state: SharedFlow<AdminState> get() = _state
    private val intent = MutableSharedFlow<AdminIntent>()

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> get() = _uiState

    init {
        viewModelScope.launch {
            intent.collect {
                when (it) {
                    is AdminIntent.Rest -> rest()
                    is AdminIntent.ChangePump -> changePump(it.pump)
                    is AdminIntent.WifiSetting -> wifiSetting(it.context)
                    is AdminIntent.DoUpdate -> doUpdate(it.context, it.file, it.version)
                    is AdminIntent.ChangeBar -> changeBar(it.bar, it.context)
                    is AdminIntent.ChangeAudio -> changeAudio(it.audio)
                    is AdminIntent.ChangeDetect -> changeDetect(it.detect)
                    is AdminIntent.ChangeInterval -> changeInterval(it.interval)
                    is AdminIntent.ChangeDuration -> changeDuration(it.duration)
                    is AdminIntent.CheckUpdate -> checkUpdate(it.context)

                }
            }
        }
        viewModelScope.launch {
            dataStore.data.map {
                it[booleanPreferencesKey(Constants.AUDIO)] ?: true
            }.distinctUntilChanged().collect {
                _state.emit(AdminState.ChangeAudio(audio = it))
            }
        }
        viewModelScope.launch {
            dataStore.data.map {
                it[booleanPreferencesKey(Constants.BAR)] ?: false
            }.distinctUntilChanged().collect {
                _state.emit(AdminState.ChangeBar(bar = it))
            }
        }
        viewModelScope.launch {
            dataStore.data.map {
                it[booleanPreferencesKey(Constants.DETECT)] ?: true
            }.distinctUntilChanged().collect {
                _state.emit(AdminState.ChangeDetect(detect = it))
            }
        }
        viewModelScope.launch {
            dataStore.data.map {
                it[intPreferencesKey(Constants.INTERVAL)] ?: 1
            }.distinctUntilChanged().collect {
                _state.emit(AdminState.ChangeInterval(interval = it))
            }
        }
        viewModelScope.launch {
            dataStore.data.map {
                it[intPreferencesKey(Constants.DURATION)] ?: 10
            }.distinctUntilChanged().collect {
                _state.emit(AdminState.ChangeDuration(duration = it))
            }
        }
    }

    fun dispatch(intent: AdminIntent) {
        try {
            viewModelScope.launch {
                this@AdminViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    private fun rest() {
        Gpio.instance.setMulSel('h', 13, 1)
        Gpio.instance.writeGpio('h', 13, 0)
        Gpio.instance.writeGpio('h', 13, 1)
        EventBus.getDefault().post(Event(Constants.BLANK, Constants.RESET))
    }

    private fun changePump(pump: Boolean) {
        val cmd = appViewModel.latestSendCmd
        if (pump) {
            cmd.zmotorX = 1
            cmd.zmotorY = 1
        } else {
            cmd.zmotorX = 0
            cmd.zmotorY = 0
        }
        appViewModel.dispatch(AppIntent.SendCmd(cmd))
    }

    private fun wifiSetting(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //是否显示button bar
            putExtra("extra_prefs_show_button_bar", true)
            putExtra("extra_prefs_set_next_text", "完成")
            putExtra("extra_prefs_set_back_text", "返回")
        }
        context.startActivity(intent)
    }

    private fun changeBar(bar: Boolean, context: Context) {
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

    private fun changeAudio(audio: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.AUDIO)] = audio
            }
        }
    }

    private fun changeDetect(detect: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.DETECT)] = detect
            }
        }
    }

    private fun changeInterval(interval: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(Constants.INTERVAL)] = interval
            }
        }
    }

    private fun changeDuration(duration: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(Constants.DURATION)] = duration
            }
        }
    }

    private fun checkUpdate(context: Context) {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                _state.emit(AdminState.CheckUpdate(apk, null))
            } else {
                checkRemoteUpdate(context)
            }
        }
    }

    private fun doUpdate(context: Context, file: File?, version: Version?) {
        file?.run {
            context.installApk(this)
        } ?: version?.run {
            _uiState.update {
                uiState.value.copy(isUpdating = true)
            }
            downloadApk(context, this)
        }
    }

    private fun downloadApk(context: Context, version: Version) {
        viewModelScope.launch {
            "开始下载".showShortToast()
            DownloadManager.download(
                version.url,
                File(context.getExternalFilesDir(null), "update.apk")
            ).collect {
                when (it) {
                    is DownloadState.Success -> {
                        _state.emit(AdminState.DownloadSuccess(it.file))
                        _uiState.update {
                            uiState.value.copy(isUpdating = false)
                        }
                        context.installApk(it.file)
                    }

                    is DownloadState.Err -> {
                        _state.emit(AdminState.DownloadError)
                    }

                    is DownloadState.Progress -> {
                        _state.emit(AdminState.DownloadProgress(it.progress))
                    }
                }
            }
        }

    }


    /**
     * 获取版本信息
     */
    private fun checkRemoteUpdate(context: Context) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                val res = systemRepository.getVersionInfo(1)
                if (res.isSuccess) {
                    val ver = res.getOrThrow()
                    if (ver.versionCode > context.versionCode()) {
                        _state.emit(AdminState.CheckUpdate(null, ver))
                    } else {
                        "已经是最新版本".showShortToast()
                    }
                } else {
                    "升级接口异常请联系管理员".showShortToast()
                }
            } else {
                "请连接网络或插入升级U盘".showShortToast()
            }
        }
    }

    /**
     * 查找目录下apk文件并安装
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

sealed class AdminIntent {
    data class ChangeInterval(val interval: Int) : AdminIntent()
    data class ChangeDuration(val duration: Int) : AdminIntent()
    data class ChangeBar(val bar: Boolean, val context: Context) : AdminIntent()
    data class ChangeAudio(val audio: Boolean) : AdminIntent()
    data class ChangeDetect(val detect: Boolean) : AdminIntent()
    data class ChangePump(val pump: Boolean) : AdminIntent()
    data class CheckUpdate(val context: Context) : AdminIntent()
    data class DoUpdate(val context: Context, val file: File?, val version: Version?) :
        AdminIntent()

    data class WifiSetting(val context: Context) : AdminIntent()
    object Rest : AdminIntent()
}

sealed class AdminState {
    data class ChangeInterval(val interval: Int) : AdminState()
    data class ChangeDuration(val duration: Int) : AdminState()
    data class ChangeBar(val bar: Boolean) : AdminState()
    data class ChangeAudio(val audio: Boolean) : AdminState()
    data class ChangeDetect(val detect: Boolean) : AdminState()
    data class CheckUpdate(val file: File?, val version: Version?) : AdminState()
    data class DownloadProgress(val progress: Int) : AdminState()
    data class DownloadSuccess(val file: File) : AdminState()
    object DownloadError : AdminState()
}

data class AdminUiState(
    var isUpdating: Boolean = false,
)