package com.zktony.www.ui.admin

import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.ext.Ext
import com.zktony.common.ext.installApk
import com.zktony.common.ext.int8ToHex
import com.zktony.common.ext.isNetworkAvailable
import com.zktony.common.download.DownloadManager
import com.zktony.common.download.DownloadState
import com.zktony.common.utils.Constants
import com.zktony.proto.Application
import com.zktony.serialport.util.Serial
import com.zktony.www.BuildConfig
import com.zktony.www.common.ext.toCommand
import com.zktony.www.common.ext.toMotor
import com.zktony.www.data.local.dao.MotorDao
import com.zktony.www.data.local.entity.Motor
import com.zktony.www.data.remote.grpc.ApplicationGrpc
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class AdminViewModel constructor(
    private val dataStore: DataStore<Preferences>,
    private val dao: MotorDao,
    private val grpc: ApplicationGrpc,
    private val serialManager: SerialManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                serialManager.ttys0Flow.collect {
                    it?.let {
                        onSerialOneResponse(it)
                    }
                }
            }
            launch {
                serialManager.ttys3Flow.collect {
                    it?.let {
                        onSerialThreeResponse(it)
                    }
                }
            }
            launch {
                if (!serialManager.lock.value) {
                    syncMotor()
                }
            }
        }
    }

    /**
     * 处理串口一返回数据
     * @param hex [String]
     */
    private fun onSerialOneResponse(hex: String) {
        hex.toCommand().run {
            if (fn == "03" && pa == "04") {
                val motor = data.toMotor()
                updateMotor(motor.copy(id = motor.address - 1))
            }
        }
    }

    /**
     * 处理串口三返回数据
     * @param hex [String]
     */
    private fun onSerialThreeResponse(hex: String) {
        hex.toCommand().run {
            if (fn == "03" && pa == "04") {
                val motor = data.toMotor()
                updateMotor(motor.copy(id = motor.address + 2))
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
     * 取消或确认更新时清空标志
     * 防止重进进入时显示更新提示
     */
    fun cleanUpdate() {
        _uiState.value = _uiState.value.copy(
            file = null,
            application = null,
            loading = false
        )
    }

    /**
     * 检查更新
     */
    fun checkUpdate() {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                _uiState.value = uiState.value.copy(
                    file = apk,
                )
            } else {
                checkRemoteUpdate()
            }
        }
    }

    /**
     *  下载apk
     */
    fun doRemoteUpdate(application: Application) {
        viewModelScope.launch {
            PopTip.show("开始下载")
            DownloadManager.download(
                application.downloadUrl,
                File(Ext.ctx.getExternalFilesDir(null), "update.apk")
            ).collect {
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
                _uiState.value = _uiState.value.copy(
                    loading = true
                )
                grpc.getByApplicationId().catch {
                    PopTip.show("获取版本信息失败,请重试!")
                    _uiState.value = _uiState.value.copy(
                        loading = false
                    )
                }.collect {
                    if (it.versionCode > BuildConfig.VERSION_CODE) {
                        _uiState.value = _uiState.value.copy(
                            application = it,
                            loading = false
                        )
                    } else {
                        PopTip.show("已是最新版本")
                        _uiState.value = _uiState.value.copy(
                            loading = false
                        )
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
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-mix-manual")) {
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
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.BAR)] = bar
            }
        }
        val intent = Intent().apply {
            action = "ACTION_SHOW_NAVBAR"
            putExtra("cmd", if (bar) "show" else "hide")
        }
        Ext.ctx.sendBroadcast(intent)
    }


    /**
     * 同步电机参数
     */
    private fun syncMotor() {
        viewModelScope.launch {
            serialManager.sendHex(
                serial = Serial.TTYS0,
                hex = V1(fn = "03", pa = "04", data = 2.int8ToHex()).toHex()
            )
            delay(100L)
            for (i in 1..3) {
                serialManager.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(fn = "03", pa = "04", data = i.int8ToHex()).toHex()
                )
                delay(100L)
            }
        }
    }

    /**
     * 根据返回更新电机参数
     * @param motor [Motor]
     */
    private fun updateMotor(motor: Motor) {
        viewModelScope.launch {
            dao.getById(motor.id).firstOrNull()?.let {
                dao.update(
                    it.copy(
                        subdivision = motor.subdivision,
                        speed = motor.speed,
                        acceleration = motor.acceleration,
                        deceleration = motor.deceleration,
                        waitTime = motor.waitTime,
                        mode = motor.mode,
                    )
                )
            }
        }
    }
}

data class AdminUiState(
    val file: File? = null,
    val application: Application? = null,
    val progress: Int = 0,
    val loading: Boolean = false
)
