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
import com.zktony.gpio.Gpio
import com.zktony.proto.Application
import com.zktony.protobuf.grpc.ApplicationGrpc
import com.zktony.www.BuildConfig
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AdminViewModel constructor(
    private val DS: DataStore<Preferences>,
    private val AG: ApplicationGrpc,
    private val SM: SerialManager
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
        val cmd = SM.send.value
        if (pump) {
            cmd.motorX = 1
            cmd.motorY = 1
        } else {
            cmd.motorX = 0
            cmd.motorY = 0
        }
        SM.send(cmd)
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
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-zm")) {
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
    val loading: Boolean = false
)