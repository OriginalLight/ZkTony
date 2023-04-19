package com.zktony.www.ui.admin

import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.dialog.updateDialog
import com.zktony.core.ext.*
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.save
import com.zktony.proto.Application
import com.zktony.protobuf.grpc.ApplicationGrpc
import com.zktony.www.BuildConfig
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
                putExtra("extra_prefs_set_next_text", "完成")
                putExtra("extra_prefs_set_back_text", "返回")
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
                    title = "发现本地新版本",
                    message = "是否更新？",
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
            PopTip.show("开始下载")
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
                            PopTip.show("下载失败,请重试!").showLong()
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
                            title = "发现在线新版本",
                            message = application.description + "\n是否升级？",
                            block = {
                                downloadApk(application)
                            })
                    } else {
                        PopTip.show("已是最新版本")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = true
                    )
                    AG.getByApplicationId(BuildConfig.APPLICATION_ID)
                        .catch {
                            PopTip.show("获取版本信息失败,请重试!")
                            _uiState.value = _uiState.value.copy(
                                loading = false
                            )
                        }.collect {
                            if (it.versionCode > BuildConfig.VERSION_CODE) {
                                updateDialog(
                                    title = "发现在线新版本",
                                    message = it.description + "\n是否升级？",
                                    block = {
                                        downloadApk(it)
                                    })
                                _uiState.value = _uiState.value.copy(
                                    loading = false
                                )
                            } else {
                                PopTip.show("已是最新版本")
                                _uiState.value = _uiState.value.copy(
                                    loading = false
                                )
                            }
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

}

data class AdminUiState(
    val application: Application? = null,
    val progress: Int = 0,
    val loading: Boolean = false
)