package com.zktony.android.ui.screen.setting

import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.MainActivity
import com.zktony.core.ext.DownloadState
import com.zktony.core.ext.Ext
import com.zktony.core.ext.download
import com.zktony.core.ext.installApk
import com.zktony.core.ext.isNetworkAvailable
import com.zktony.core.ext.showShortToast
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import com.zktony.proto.Application
import com.zktony.protobuf.grpc.ApplicationGrpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class SettingViewModel constructor(
    private val datastore: DataStore<Preferences>,
    private val grpc: ApplicationGrpc,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                if (Ext.ctx.isNetworkAvailable()) {
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch {
                            _uiState.value = _uiState.value.copy(
                                application = null
                            )
                        }.collect {
                            _uiState.value = _uiState.value.copy(
                                application = it
                            )
                        }
                }
            }
            launch {
                datastore.read(Constants.LANGUAGE, "zh").collect {
                    _uiState.value = _uiState.value.copy(language = it)
                }
            }
            launch {
                datastore.read(Constants.NAVIGATION, false).collect {
                    _uiState.value = _uiState.value.copy(navigation = it)
                }
            }
        }
    }

    /**
     * 设置语言
     *
     * @param language String
     */
    fun setLanguage(language: String) {
        datastore.save(Constants.LANGUAGE, language)
        viewModelScope.launch {
            val old = datastore.read(Constants.LANGUAGE, "zh").first()
            if (old != language) {
                Ext.ctx.startActivity(Intent(Ext.ctx, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }

    /**
     * 设置导航栏
     *
     * @param navigation Boolean
     */
    fun setNavigation(navigation: Boolean) {
        datastore.save(Constants.NAVIGATION, navigation)
        val intent = Intent().apply {
            action = "ACTION_SHOW_NAVBAR"
            putExtra("cmd", if (navigation) "show" else "hide")
        }
        Ext.ctx.sendBroadcast(intent)
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
                val application = _uiState.value.application
                if (application != null) {
                    if (application.versionCode > BuildConfig.VERSION_CODE
                        && application.downloadUrl.isNotEmpty()
                        && uiState.value.progress == 0
                    ) {
                        downloadApk(application)
                        _uiState.value = _uiState.value.copy(
                            progress = 1
                        )
                    }
                } else {
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch {
                            Ext.ctx.getString(R.string.interface_exception).showShortToast()
                        }.collect {
                            _uiState.value = _uiState.value.copy(
                                application = it
                            )
                        }
                }
            } else {
                Ext.ctx.getString(R.string.network_unavailable).showShortToast()
            }
        }
    }

    /**
     *  下载apk
     *  @param application [Application]
     */
    private fun downloadApk(application: Application) {
        viewModelScope.launch {
            application.downloadUrl.download(File(Ext.ctx.getExternalFilesDir(null), "update.apk"))
                .collect {
                    when (it) {
                        is DownloadState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                progress = 0
                            )
                            Ext.ctx.installApk(it.file)
                        }

                        is DownloadState.Err -> {
                            _uiState.value = _uiState.value.copy(
                                progress = 0
                            )
                            Ext.ctx.getString(R.string.download_failed).showShortToast()
                        }

                        is DownloadState.Progress -> {
                            _uiState.value = _uiState.value.copy(
                                progress = maxOf(it.progress, 1)
                            )
                        }
                    }
                }
        }
    }
}

data class SettingUiState(
    val application: Application? = null,
    val navigation: Boolean = false,
    val language: String = "zh",
    val progress: Int = 0,
)
