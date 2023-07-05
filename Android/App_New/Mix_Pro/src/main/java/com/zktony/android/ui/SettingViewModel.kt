package com.zktony.android.ui

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.core.ext.DownloadState
import com.zktony.android.core.ext.Ext
import com.zktony.android.core.ext.download
import com.zktony.android.core.ext.installApk
import com.zktony.android.core.ext.isNetworkAvailable
import com.zktony.android.core.ext.restartApp
import com.zktony.android.core.ext.showShortToast
import com.zktony.android.ui.utils.PageType
import com.zktony.datastore.ext.saveSettings
import com.zktony.datastore.ext.settingsFlow
import com.zktony.proto.Application
import com.zktony.proto.SettingsPreferences
import com.zktony.proto.copy
import com.zktony.protobuf.grpc.ApplicationGrpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class SettingViewModel constructor(
    private val grpc: ApplicationGrpc,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    private val _application = MutableStateFlow<Application?>(null)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.LIST)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                combine(
                    _application,
                    settingsFlow,
                    _progress,
                    _page
                ) { application, settings, progress, page ->
                    SettingUiState(
                        application = application,
                        settings = settings,
                        progress = progress,
                        page = page
                    )
                }.catch { ex ->
                    ex.printStackTrace()
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                if (Ext.ctx.isNetworkAvailable()) {
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch { ex ->
                            ex.printStackTrace()
                        }.collect {
                            _application.value = it
                        }
                } else {
                    _application.value = null
                }
            }
        }
    }

    fun event(event: SettingEvent) {
        when (event) {
            is SettingEvent.NavTo -> _page.value = event.page
            is SettingEvent.Language -> language(event.language)
            is SettingEvent.Navigation -> navigation(event.navigation)
            is SettingEvent.Network -> network()
            is SettingEvent.Update -> update()
        }
    }

    /**
     * 设置语言
     *
     * @param new String
     */
    private fun language(new: String) {
        viewModelScope.launch {
            val old = _uiState.value.settings.language
            saveSettings { it.copy { language = new } }
            if (old != new) {
                Ext.ctx.restartApp()
            }
        }
    }

    /**
     * 设置导航栏
     *
     * @param nav Boolean
     */
    private fun navigation(nav: Boolean) {
        viewModelScope.launch {
            saveSettings { it.copy { navigation = nav } }
            val intent = Intent().apply {
                action = "ACTION_SHOW_NAVBAR"
                putExtra("cmd", if (nav) "show" else "hide")
            }
            Ext.ctx.sendBroadcast(intent)
        }
    }

    /**
     * 跳转到wifi设置界面
     */
    private fun network() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //是否显示button bar
            putExtra("extra_prefs_show_button_bar", true)
            putExtra(
                "extra_prefs_set_next_text",
                Ext.ctx.getString(R.string.finish)
            )
            putExtra(
                "extra_prefs_set_back_text",
                Ext.ctx.getString(R.string.cancel)
            )
        }
        Ext.ctx.startActivity(intent)
    }

    /**
     * 检查更新
     */
    private fun update() {
        viewModelScope.launch {
            if (Ext.ctx.isNetworkAvailable()) {
                val application = _application.value
                if (application != null) {
                    if (application.versionCode > BuildConfig.VERSION_CODE
                        && application.downloadUrl.isNotEmpty()
                        && _progress.value == 0
                    ) {
                        download(application.downloadUrl)
                        _progress.value = 1
                    }
                } else {
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch { ex ->
                            ex.printStackTrace()
                            Ext.ctx.getString(R.string.interface_exception).showShortToast()
                        }.collect {
                            _application.value = it
                        }
                }
            } else {
                Ext.ctx.getString(R.string.network_unavailable).showShortToast()
            }
        }
    }

    /**
     *  下载apk
     *
     * @param url String
     */
    private fun download(url: String) {
        viewModelScope.launch {
            url.download(File(Ext.ctx.getExternalFilesDir(null), "update.apk"))
                .collect {
                    when (it) {
                        is DownloadState.Success -> {
                            _progress.value = 0
                            Ext.ctx.installApk(it.file)
                        }

                        is DownloadState.Err -> {
                            _progress.value = 0
                            Ext.ctx.getString(R.string.download_failed).showShortToast()
                        }

                        is DownloadState.Progress -> {
                            _progress.value = maxOf(it.progress, 1)
                        }
                    }
                }
        }
    }
}

data class SettingUiState(
    val settings: SettingsPreferences = SettingsPreferences.getDefaultInstance(),
    val application: Application? = null,
    val progress: Int = 0,
    val page: PageType = PageType.LIST,
)

sealed class SettingEvent {
    object Network : SettingEvent()
    object Update : SettingEvent()
    data class NavTo(val page: PageType) : SettingEvent()
    data class Language(val language: String) : SettingEvent()
    data class Navigation(val navigation: Boolean) : SettingEvent()
}