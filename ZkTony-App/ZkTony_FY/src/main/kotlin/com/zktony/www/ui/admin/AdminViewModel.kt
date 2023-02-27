package com.zktony.www.ui.admin

import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.app.CommonApplicationProxy
import com.zktony.common.base.BaseViewModel
import com.zktony.common.extension.installApk
import com.zktony.common.extension.int8ToHex
import com.zktony.common.extension.isNetworkAvailable
import com.zktony.common.http.download.DownloadManager
import com.zktony.common.http.download.DownloadState
import com.zktony.common.utils.Constants
import com.zktony.serialport.util.toSerial
import com.zktony.www.BuildConfig
import com.zktony.www.common.extension.toMotor
import com.zktony.www.common.extension.toV1
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.control.serial.protocol.V1
import com.zktony.www.data.local.room.entity.Motor
import com.zktony.www.data.remote.model.Application
import com.zktony.www.data.repository.ApplicationRepository
import com.zktony.www.data.repository.MotorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val motorRepository: MotorRepository,
    private val applicationRepository: ApplicationRepository,
) : BaseViewModel() {

    private val _file = MutableStateFlow<File?>(null)
    private val _version = MutableStateFlow<Application?>(null)
    private val _progress = MutableStateFlow(0)
    val file = _file.asStateFlow()
    val version = _version.asStateFlow()
    val progress = _progress.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                SerialManager.instance.ttys0Flow.collect {
                    it?.let {
                        it.toV1().run {
                            if (fn == "03" && pa == "04") {
                                val motor = data.toMotor()
                                updateMotor(motor.copy(id = motor.address - 1))
                            }
                        }
                    }
                }
            }
            launch {
                SerialManager.instance.ttys1Flow.collect {
                    it?.let {
                        it.toV1().run {
                            if (fn == "03" && pa == "04") {
                                val motor = data.toMotor()
                                updateMotor(motor.copy(id = motor.address + 2))
                            }
                        }
                    }
                }
            }
            launch {
                SerialManager.instance.ttys2Flow.collect {
                    it?.let {
                        it.toV1().run {
                            if (fn == "03" && pa == "04") {
                                val motor = data.toMotor()
                                updateMotor(motor.copy(id = motor.address + 5))
                            }
                        }
                    }
                }
            }
            launch {
                if (!SerialManager.instance.lock.value) {
                    syncMotor()
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
            CommonApplicationProxy.application.startActivity(intent)
        }
    }

    /**
     * 检查更新
     */
    fun checkUpdate() {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                _file.value = apk
            } else {
                checkRemoteUpdate()
            }
        }
    }

    /**
     * 取消或确认更新时清空标志
     * 防止重进进入时显示更新提示
     */
    fun cleanUpdate() {
        _file.value = null
        _version.value = null
    }

    /**
     *  下载apk
     *  @param application [Application]
     */
    fun doRemoteUpdate(application: Application) {
        viewModelScope.launch {
            PopTip.show("开始下载")
            DownloadManager.download(
                application.download_url,
                File(CommonApplicationProxy.application.getExternalFilesDir(null), "update.apk")
            ).collect {
                when (it) {
                    is DownloadState.Success -> {
                        _progress.value = 0
                        CommonApplicationProxy.application.installApk(it.file)
                    }

                    is DownloadState.Err -> {
                        _progress.value = 0
                        PopTip.show("下载失败,请重试!").showLong()
                    }

                    is DownloadState.Progress -> {
                        _progress.value = it.progress
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
            if (CommonApplicationProxy.application.isNetworkAvailable()) {
                applicationRepository.getById()
                    .catch {
                        PopTip.show("升级接口异常请联系管理员")
                    }
                    .collect {
                        val data = it.body()
                        if (data != null) {
                            if (data.version_code > BuildConfig.VERSION_CODE) {
                                _version.value = data
                            } else {
                                PopTip.show("已经是最新版本")
                            }
                        } else {
                            PopTip.show("升级接口异常请联系管理员")
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
                if (apk.name.endsWith(".apk") && apk.name.contains("zktony-fy")) {
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
        CommonApplicationProxy.application.sendBroadcast(intent)
    }

    /**
     * 抗体保温温度设置
     * @param temp [Float]
     */
    fun setAntibodyTemp(temp: Float) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[floatPreferencesKey(Constants.TEMP)] = temp
            }
        }
    }

    /**
     * 同步电机参数
     */
    private fun syncMotor() {
        viewModelScope.launch {
            for (i in 0..2) {
                for (j in 1..3) {
                    SerialManager.instance.sendHex(
                        i.toSerial(), V1(
                            fn = "03", pa = "04", data = j.int8ToHex()
                        ).toHex()
                    )
                    delay(200L)
                }
            }
        }
    }

    /**
     * 根据返回更新电机参数
     * @param motor [Motor]
     */
    private fun updateMotor(motor: Motor) {
        viewModelScope.launch {
            motorRepository.getById(motor.id).firstOrNull()?.let {
                motorRepository.update(
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