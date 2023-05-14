package com.zktony.android.ui.screen.setting

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.MainActivity
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.DownloadState
import com.zktony.core.ext.Ext
import com.zktony.core.ext.download
import com.zktony.core.ext.installApk
import com.zktony.core.ext.isNetworkAvailable
import com.zktony.core.ext.showShortToast
import com.zktony.datastore.SettingsPreferences
import com.zktony.datastore.copy
import com.zktony.datastore.ext.saveSettings
import com.zktony.datastore.ext.settingsFlow
import com.zktony.proto.Application
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
    private val _page = MutableStateFlow(PageEnum.MAIN)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                combine(
                    _application,
                    settingsFlow,
                    _progress,
                    _page,
                ) { application, settings, progress, page ->
                    SettingUiState(
                        application = application,
                        settings = settings,
                        progress = progress,
                        page = page,
                    )
                }.catch { ex ->
                    _uiState.value = SettingUiState(errorMessage = ex.message ?: "Unknown error")
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                if (Ext.ctx.isNetworkAvailable()) {
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch {
                            _application.value = null
                        }.collect {
                            _application.value = it
                        }
                } else {
                    _application.value = null
                }
            }
        }
    }

    fun navigationTo(page: PageEnum) {
        _page.value = page
    }

    /**
     * 设置语言
     *
     * @param new String
     */
    fun setLanguage(new: String) {
        viewModelScope.launch {
            val old = _uiState.value.settings.language
            saveSettings { it.copy { language = new } }
            if (old != new) {
                Ext.ctx.startActivity(Intent(Ext.ctx, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }

    /**
     * 设置导航栏
     *
     * @param nav Boolean
     */
    fun setNavigation(nav: Boolean) {
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
    fun openWifi() {
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
    fun checkUpdate() {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                Ext.ctx.installApk(apk)
            } else {
                checkRemoteUpdate()
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
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-mix-pro")) {
                    return apk
                }
            }
        }
        return null
    }

    /**
     * 获取版本信息
     */
    private fun checkRemoteUpdate() {
        viewModelScope.launch {
            if (Ext.ctx.isNetworkAvailable()) {
                val application = _application.value
                if (application != null) {
                    if (application.versionCode > BuildConfig.VERSION_CODE
                        && application.downloadUrl.isNotEmpty()
                        && _progress.value == 0
                    ) {
                        downloadApk(application.downloadUrl)
                        _progress.value = 1
                    }
                } else {
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch {
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
    private fun downloadApk(url: String) {
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
    val page: PageEnum = PageEnum.MAIN,
    val errorMessage: String = "",
)
