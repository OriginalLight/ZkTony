package com.zktony.www.ui

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
import com.zktony.www.BuildConfig
import com.zktony.www.MainActivity
import com.zktony.www.R
import com.zktony.www.core.SerialPort
import com.zktony.www.core.ext.updateDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class AdminViewModel constructor(
    private val DS: DataStore<Preferences>,
    private val SM: SerialPort
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * 泵开关
     * @param pump [Boolean] true 开 false 关
     */
    fun touchPump() {
        if (uiState.value.job == null)
        {
            val job = viewModelScope.launch {
                SM.send(SM.send.value.apply { motorX = 1 })
                delay(10 * 1000L)
                SM.send(SM.send.value.apply { motorX = 0 })

            }
            job.invokeOnCompletion {
                _uiState.value = _uiState.value.copy(
                    job = null
                )
            }
            _uiState.value = _uiState.value.copy(
                job = job
            )
        } else {
            uiState.value.job?.cancel()
            SM.send(SM.send.value.apply { motorX = 0 })
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
                    Ext.ctx.getString(R.string.finish)
                )
                putExtra(
                    "extra_prefs_set_back_text",
                    Ext.ctx.getString(R.string.return_e)
                )
            }
            Ext.ctx.startActivity(intent)
        }
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

    /**
     * 音频开关
     * @param audio [Boolean] true 开 false 关
     */
    fun toggleAudio(audio: Boolean) {
        DS.save(Constants.AUDIO, audio)
    }

    /**
     * 电流检测开关
     * @param detect [Boolean] true 开 false 关
     */
    fun toggleDetect(detect: Boolean) {
        DS.save(Constants.DETECT, detect)
    }

    /**
     * 直流泵间隔
     * @param interval [Int] 间隔时间
     */
    fun toggleInterval(interval: Int) {
        DS.save(Constants.INTERVAL, minOf(interval, 10))
    }

    /**
     * 直流泵持续时间
     * @param duration [Int] 持续时间
     */
    fun toggleDuration(duration: Int) {
        DS.save(Constants.DURATION, minOf(duration, 200))
    }

    /**
     * 蠕动泵转速
     * @param speed [Int] 转速
     */
    fun toggleMotorSpeed(speed: Int) {
        DS.save(Constants.MOTOR_SPEED, minOf(speed, 250))
    }

    /**
     * 检查更新
     */
    fun checkUpdate() {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                updateDialog(
                    title = Ext.ctx.getString(R.string.new_local_update),
                    message = Ext.ctx.getString(R.string.whether_to_update),
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
            PopTip.show(Ext.ctx.getString(R.string.start_downloading))
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
                            PopTip.show(Ext.ctx.getString(R.string.download_failed))
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
                            title = Ext.ctx.getString(R.string.new_remote_update),
                            message = application.description + "\n${Ext.ctx.getString(R.string.whether_to_update)}",
                            block = {
                                downloadApk(application)
                            })
                    } else {
                        PopTip.show(Ext.ctx.getString(R.string.already_latest_version))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = true
                    )
                    httpCall(
                        failure = {
                            _uiState.value = _uiState.value.copy(
                                loading = false
                            )
                        }
                    ) {
                        val app =
                            it.find { app -> app.applicationId == BuildConfig.APPLICATION_ID }
                        if (app != null) {
                            if (app.versionCode > BuildConfig.VERSION_CODE) {
                                updateDialog(
                                    title = Ext.ctx.getString(R.string.new_remote_update),
                                    message = app.description + "\n${Ext.ctx.getString(R.string.whether_to_update)}",
                                    block = {
                                        downloadApk(app)
                                    })
                                _uiState.value = _uiState.value.copy(
                                    loading = false
                                )
                            } else {
                                PopTip.show(Ext.ctx.getString(R.string.already_latest_version))
                                _uiState.value = _uiState.value.copy(
                                    loading = false
                                )
                            }
                        } else {
                            PopTip.show(Ext.ctx.getString(R.string.already_latest_version))
                            _uiState.value = _uiState.value.copy(
                                loading = false
                            )
                        }
                    }
                }
            } else {
                PopTip.show(Ext.ctx.getString(R.string.no_network_usb))
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
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-transfer")) {
                    return apk
                }
            }
        }
        return null
    }

}

data class AdminUiState(
    val application: Application? = null,
    val progress: Int = 0,
    val loading: Boolean = false,
    val job: Job? = null
)