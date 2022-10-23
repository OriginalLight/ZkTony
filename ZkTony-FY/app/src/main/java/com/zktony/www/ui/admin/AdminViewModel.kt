package com.zktony.www.ui.admin

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.viewModelScope
import com.zktony.gpio.Gpio
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppIntent
import com.zktony.www.common.app.AppState
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.extension.*
import com.zktony.www.common.http.adapter.getOrNull
import com.zktony.www.common.http.adapter.isSuccess
import com.zktony.www.common.http.download.DownloadManager
import com.zktony.www.common.http.download.model.DownloadState
import com.zktony.www.data.entity.Motor
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.data.services.SystemService
import com.zktony.www.data.services.model.Version
import com.zktony.www.model.enum.SerialPortEnum
import com.zktony.www.model.enum.getSerialPortEnum
import com.zktony.www.serialport.protocol.Command
import com.zktony.www.ui.admin.model.AdminIntent
import com.zktony.www.ui.admin.model.AdminState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val service: SystemService,
    private val dataStore: DataStore<Preferences>,
    private val motorRepository: MotorRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _state = MutableSharedFlow<AdminState>()
    val state: SharedFlow<AdminState> get() = _state
    private val intent = MutableSharedFlow<AdminIntent>()

    init {
        viewModelScope.launch {
            intent.collect {
                when (it) {
                    is AdminIntent.Reset -> reset()
                    is AdminIntent.WifiSetting -> wifiSetting(it.context)
                    is AdminIntent.DoUpdate -> doUpdate(it.context, it.file, it.version)
                    is AdminIntent.ChangeBar -> changeBar(it.bar, it.context)
                    is AdminIntent.ChangeTemp -> changeTemp(it.temp)
                    is AdminIntent.CheckUpdate -> checkUpdate(it.context)
                }
            }
        }
        viewModelScope.launch {
            appViewModel.state.collect {
                when (it) {
                    is AppState.ReceiverSerialOne -> onReceiverSerialOne(it.command)
                    is AppState.ReceiverSerialTwo -> onReceiverSerialTwo(it.command)
                    is AppState.ReceiverSerialThree -> onReceiverSerialThree(it.command)
                    else -> {}
                }
            }
        }
        initAndSyncMotor()
    }

    /**
     * Intent处理
     * @param intent [AdminIntent]
     */
    fun dispatch(intent: AdminIntent) {
        try {
            viewModelScope.launch {
                this@AdminViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 下位机复位
     */
    private fun reset() {
        Gpio.instance.setDirection("gpio156", 1)
        Gpio.instance.writeGpio("gpio156", 0)
        Gpio.instance.writeGpio("gpio156", 1)
    }

    /**
     * wifi设置
     * @param context [Context]
     */
    private fun wifiSetting(context: Context) {
        viewModelScope.launch {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                //是否显示button bar
                putExtra("extra_prefs_show_button_bar", true)
                putExtra("extra_prefs_set_next_text", "完成")
                putExtra("extra_prefs_set_back_text", "返回")
            }
            context.startActivity(intent)
            _state.emit(AdminState.ChangeBar)
        }
    }

    /**
     * 导航栏切换
     * @param bar [Boolean]
     * @param context [Context]
     */
    private fun changeBar(bar: Boolean, context: Context) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(Constants.BAR)] = bar
            }
        }
        val intent = Intent().apply {
            action = "ACTION_SHOW_NAVBAR"
            putExtra("cmd", if (bar) "show" else "hide")
        }
        context.sendBroadcast(intent)
    }

    /**
     * 抗体保温温度切换
     * @param temp [Float]
     */
    private fun changeTemp(temp: Float) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[floatPreferencesKey(Constants.TEMP)] = temp
            }
        }
    }

    /**
     * 检查更新
     * @param context [Context]
     */
    private fun checkUpdate(context: Context) {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                _state.emit(AdminState.CheckUpdate(apk, null))
            } else {
                checkRemoteUpdate(context)
            }
        }
    }

    /**
     * 更新 有本地文件就进行本地更新否则下载远程更新
     * @param context [Context]
     * @param file [File]
     * @param version [Version]
     */
    private fun doUpdate(context: Context, file: File?, version: Version?) {
        file?.run {
            context.installApk(this)
        } ?: version?.run {
            downloadApk(context, this)
        }
    }

    /**
     *  下载apk
     *  @param context [Context]
     *  @param version [Version]
     */
    private fun downloadApk(context: Context, version: Version) {
        viewModelScope.launch {
            "开始下载".showShortToast()
            DownloadManager.download(
                version.url,
                File(context.getExternalFilesDir(null), "update.apk")
            ).collect {
                when (it) {
                    is DownloadState.Success -> {
                        _state.emit(AdminState.DownloadSuccess(it.file))
                        context.installApk(it.file)
                    }

                    is DownloadState.Err -> {
                        _state.emit(AdminState.DownloadError)
                    }

                    is DownloadState.Progress -> {
                        _state.emit(AdminState.DownloadProgress(it.progress))
                    }
                }
            }
        }

    }

    /**
     * 获取版本信息
     * @param context [Context]
     */
    private fun checkRemoteUpdate(context: Context) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                val res = service.getVersionInfo(2)
                if (res.isSuccess) {
                    res.getOrNull()?.let {
                        if (it.versionCode > context.versionCode()) {
                            _state.emit(AdminState.CheckUpdate(null, it))
                        } else {
                            "已经是最新版本".showShortToast()
                        }
                    }
                } else {
                    "升级接口异常请联系管理员".showShortToast()
                }
            } else {
                "请连接网络或插入升级U盘".showShortToast()
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
     * 处理串口一返回数据
     * @param hex [String]
     */
    private fun onReceiverSerialOne(hex: String) {
        Command(hex).run {
            if (function == "03" && parameter == "04") {
                updateMotorByCallBack(Motor(data).copy(board = SerialPortEnum.SERIAL_ONE.index))
            }
        }
    }

    /**
     * 处理串口二返回数据
     * @param hex [String]
     */
    private fun onReceiverSerialTwo(hex: String) {
        Command(hex).run {
            if (function == "03" && parameter == "04") {
                updateMotorByCallBack(Motor(data).copy(board = SerialPortEnum.SERIAL_TWO.index))
            }
        }
    }

    /**
     * 处理串口三返回数据
     * @param hex [String]
     */
    private fun onReceiverSerialThree(hex: String) {
        Command(hex).run {
            if (function == "03" && parameter == "04") {
                updateMotorByCallBack(Motor(data).copy(board = SerialPortEnum.SERIAL_THREE.index))
            }
        }
    }

    /**
     * 初始化并同步电机
     */
    private fun initAndSyncMotor() {
        viewModelScope.launch {
            initMotor()
            delay(1000L)
            syncMotor()
        }
    }

    /**
     * 初始化电机
     */
    private fun initMotor() {
        viewModelScope.launch {
            motorRepository.getAll().first().run {
                if (this.isEmpty()) {
                    val motorList = mutableListOf<Motor>()
                    motorList.add(Motor(name = "X轴", address = 1))
                    motorList.add(Motor(name = "Y轴", address = 2))
                    motorList.add(Motor(name = "Z轴", address = 3))
                    for (i in 1..5) {
                        val motor = Motor(
                            name = "泵$i",
                            address = if (i <= 3) i else i - 3,
                            board = if (i <= 3) 1 else 2,
                            motorType = 1,
                        )
                        motorList.add(motor)
                    }
                    motorRepository.insertBatch(motorList)
                }
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
                    if (i == 2 && j == 3) {
                        break
                    }
                    appViewModel.dispatch(
                        AppIntent.Sender(
                            getSerialPortEnum(i),
                            Command(
                                function = "03",
                                parameter = "04",
                                data = j.int8ToHex()
                            ).toHex()
                        )
                    )
                    delay(100L)
                }
            }
        }
    }

    /**
     * 根据返回更新电机参数
     * @param motor [Motor]
     */
    private fun updateMotorByCallBack(motor: Motor) {
        viewModelScope.launch {
            motorRepository.getByBoardAndAddress(motor.board, motor.address).firstOrNull()?.let {
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