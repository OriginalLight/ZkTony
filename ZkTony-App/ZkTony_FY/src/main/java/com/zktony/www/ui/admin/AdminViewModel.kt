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
import com.zktony.datastore.ext.save
import com.zktony.proto.Application
import com.zktony.protobuf.grpc.ApplicationGrpc
import com.zktony.serialport.util.toSerial
import com.zktony.www.BuildConfig
import com.zktony.www.common.ext.toMotor
import com.zktony.www.common.ext.toV1
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Motor
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
                serialManager.ttys1Flow.collect {
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
                serialManager.ttys2Flow.collect {
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
                if (!serialManager.lock.value) {
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
                _uiState.value = _uiState.value.copy(
                    loading = true
                )
                grpc.getByApplicationId(BuildConfig.APPLICATION_ID).catch {
                    PopTip.show("获取版本信息失败,请重试!${it.message}")
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
        dataStore.save(Constants.BAR, bar)
        val intent = Intent().apply {
            action = "ACTION_SHOW_NAVBAR"
            putExtra("cmd", if (bar) "show" else "hide")
        }
        Ext.ctx.sendBroadcast(intent)
    }

    fun toggleRecycle(checked: Boolean) {
        dataStore.save(Constants.RECYCLE, checked)
    }

    /**
     * 抗体保温温度设置
     * @param temp [Float]
     */
    fun setAntibodyTemp(temp: Float) {
        dataStore.save(Constants.TEMP, temp)
    }

    /**
     * 同步电机参数
     */
    private fun syncMotor() {
        viewModelScope.launch {
            for (i in 0..2) {
                for (j in 1..3) {
                    serialManager.sendHex(
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