package com.zktony.www.ui.admin

import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.*
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import com.zktony.proto.Application
import com.zktony.protobuf.grpc.ApplicationGrpc
import com.zktony.www.BuildConfig
import com.zktony.www.MainActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AdminViewModel constructor(
    private val DS: DataStore<Preferences>,
    private val AG: ApplicationGrpc,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (Ext.ctx.isNetworkAvailable()) {
                AG.getByApplicationId(BuildConfig.APPLICATION_ID)
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
                putExtra(
                    "extra_prefs_set_next_text",
                    Ext.ctx.getString(com.zktony.core.R.string.finish)
                )
                putExtra(
                    "extra_prefs_set_back_text",
                    Ext.ctx.getString(com.zktony.core.R.string.cancel)
                )
            }
            Ext.ctx.startActivity(intent)
        }
    }

    /**
     * 检查更新
     */
    fun checkUpdate() {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                updateDialog(
                    title = Ext.ctx.getString(com.zktony.core.R.string.new_local_update),
                    message = Ext.ctx.getString(com.zktony.core.R.string.whether_to_update),
                    block = {
                        Ext.ctx.installApk(apk)
                    })
            } else {
                checkRemoteUpdate()
            }
        }
    }

    /**
     *  下载apk
     *  @param application [Application]
     */
    private fun downloadApk(application: Application) {
        viewModelScope.launch {
            PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.start_downloading))
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
                            PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.download_failed))
                                .showLong()
                        }

                        is DownloadState.Progress -> {
                            _uiState.value = _uiState.value.copy(
                                progress = it.progress
                            )
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
            if (Ext.ctx.isNetworkAvailable()) {
                val application = _uiState.value.application
                if (application != null) {
                    if (application.versionCode > BuildConfig.VERSION_CODE) {
                        updateDialog(
                            title = Ext.ctx.getString(com.zktony.core.R.string.new_remote_update),
                            message = application.description + "\n${Ext.ctx.getString(com.zktony.core.R.string.whether_to_update)}",
                            block = {
                                downloadApk(application)
                            })
                    } else {
                        PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.already_latest_version))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = true
                    )
                    AG.getByApplicationId(BuildConfig.APPLICATION_ID)
                        .catch {
                            PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.failed_get_version_information))
                            _uiState.value = _uiState.value.copy(
                                loading = false
                            )
                        }.collect {
                            if (it.versionCode > BuildConfig.VERSION_CODE) {
                                updateDialog(
                                    title = Ext.ctx.getString(com.zktony.core.R.string.new_remote_update),
                                    message = it.description + "\n${Ext.ctx.getString(com.zktony.core.R.string.whether_to_update)}",
                                    block = {
                                        downloadApk(it)
                                    })
                                _uiState.value = _uiState.value.copy(
                                    loading = false
                                )
                            } else {
                                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.already_latest_version))
                                _uiState.value = _uiState.value.copy(
                                    loading = false
                                )
                            }
                        }
                }
            } else {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.no_network_usb))
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
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-incubation")) {
                    return apk
                }
            }
        }
        return null
    }

    /**
     * 导航栏切换
     * @param bar [Boolean]
     */
    fun toggleNavigationBar(bar: Boolean) {
        DS.save(Constants.BAR, bar)
        val intent = Intent().apply {
            action = "ACTION_SHOW_NAVBAR"
            putExtra("cmd", if (bar) "show" else "hide")
        }
        Ext.ctx.sendBroadcast(intent)
    }

    fun toggleRecycle(checked: Boolean) {
        DS.save(Constants.RECYCLE, checked)
    }

    /**
     * 抗体保温温度设置
     * @param temp [Float]
     */
    fun setAntibodyTemp(temp: Float) {
        DS.save(Constants.TEMP, temp)
    }

    /**
     * 设置语言
     * @param index [Int]
     */
    fun setLanguage(index: Int) {
        viewModelScope.launch {
            val language = when (index) {
                0 -> "zh"
                1 -> "en"
                else -> "zh"
            }
            DS.save(Constants.LANGUAGE, language)
            val old = DS.read(Constants.LANGUAGE, "zh").first()
            if (old != language) {
                Ext.ctx.startActivity(Intent(Ext.ctx, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }

}

data class AdminUiState(
    val application: Application? = null,
    val progress: Int = 0,
    val loading: Boolean = false
)