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
import com.zktony.www.common.app.AppEvent
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.*
import com.zktony.www.common.network.adapter.getOrNull
import com.zktony.www.common.network.adapter.isSuccess
import com.zktony.www.common.network.download.DownloadManager
import com.zktony.www.common.network.download.DownloadState
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.common.utils.Constants
import com.zktony.www.common.utils.Constants.DEVICE_ID
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.model.Version
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.data.repository.SystemRepository
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.getSerialPortEnum
import com.zktony.www.serialport.protocol.Command
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val motorRepository: MotorRepository,
    private val calibrationRepository: CalibrationRepository,
    private val systemRepository: SystemRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _event = MutableSharedFlow<AdminEvent>()
    val event: SharedFlow<AdminEvent> get() = _event

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> get() = _uiState

    init {
        viewModelScope.launch {
            launch {
                appViewModel.event.collect {
                    when (it) {
                        is AppEvent.ReceiverSerialOne -> onReceiverSerialOne(it.command)
                        is AppEvent.ReceiverSerialTwo -> onReceiverSerialTwo(it.command)
                        is AppEvent.ReceiverSerialThree -> onReceiverSerialThree(it.command)
                        else -> {}
                    }
                }
            }
            launch {
                initAndSyncMotor()
            }
            launch {
                initCalibration()
            }
        }
    }


    /**
     * 下位机复位
     */
    fun reset() {
        Gpio.instance.setDirection("gpio156", 1)
        Gpio.instance.writeGpio("gpio156", 0)
        Gpio.instance.writeGpio("gpio156", 1)
    }

    /**
     * wifi设置
     * @param context [Context]
     */
    fun wifiSetting(context: Context) {
        viewModelScope.launch {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                //是否显示button bar
                putExtra("extra_prefs_show_button_bar", true)
                putExtra("extra_prefs_set_next_text", "完成")
                putExtra("extra_prefs_set_back_text", "返回")
            }
            context.startActivity(intent)
            _event.emit(AdminEvent.ChangeBar)
        }
    }

    /**
     * 导航栏切换
     * @param bar [Boolean]
     * @param context [Context]
     */
    fun changeBar(bar: Boolean, context: Context) {
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
    fun changeTemp(temp: Float) {
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
    fun checkUpdate(context: Context) {
        viewModelScope.launch {
            val apk = checkLocalUpdate()
            if (apk != null) {
                _event.emit(AdminEvent.CheckUpdate(apk, null))
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
    fun doUpdate(context: Context, file: File?, version: Version?) {
        file?.run {
            context.installApk(this)
        } ?: version?.run {
            _uiState.value = _uiState.value.copy(isUpdating = true)
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
                        _event.emit(AdminEvent.DownloadSuccess(it.file))
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                        context.installApk(it.file)
                    }

                    is DownloadState.Err -> {
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                        _event.emit(AdminEvent.DownloadError)
                    }

                    is DownloadState.Progress -> {
                        _event.emit(AdminEvent.DownloadProgress(it.progress))
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
                val res = systemRepository.getVersionInfo(DEVICE_ID)
                if (res.isSuccess) {
                    res.getOrNull()?.let {
                        if (it.versionCode > context.versionCode()) {
                            _event.emit(AdminEvent.CheckUpdate(null, it))
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
        hex.toCommand().run {
            if (function == "03" && parameter == "04") {
                updateMotorByCallBack(data.toMotor().copy(board = SerialPortEnum.SERIAL_ONE.index))
            }
        }
    }

    /**
     * 处理串口二返回数据
     * @param hex [String]
     */
    private fun onReceiverSerialTwo(hex: String) {
        hex.toCommand().run {
            if (function == "03" && parameter == "04") {
                updateMotorByCallBack(data.toMotor().copy(board = SerialPortEnum.SERIAL_TWO.index))
            }
        }
    }

    /**
     * 处理串口三返回数据
     * @param hex [String]
     */
    private fun onReceiverSerialThree(hex: String) {
        hex.toCommand().run {
            if (function == "03" && parameter == "04") {
                updateMotorByCallBack(
                    data.toMotor().copy(board = SerialPortEnum.SERIAL_THREE.index)
                )
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
                    appViewModel.sender(
                        getSerialPortEnum(i),
                        Command(
                            function = "03",
                            parameter = "04",
                            data = j.int8ToHex()
                        ).toHex()
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

    /**
     * 初始化校准参数
     */
    private fun initCalibration() {
        viewModelScope.launch {
            // 获取不到校准参数则初始化
            calibrationRepository.getCalibration().first().let {
                if (it.isEmpty()) {
                    calibrationRepository.insert(Calibration())
                }
            }
        }
    }
}

sealed class AdminEvent {
    data class CheckUpdate(val file: File?, val version: Version?) : AdminEvent()
    data class DownloadProgress(val progress: Int) : AdminEvent()
    data class DownloadSuccess(val file: File) : AdminEvent()
    object DownloadError : AdminEvent()
    object ChangeBar : AdminEvent()
}

data class AdminUiState(
    val isUpdating: Boolean = false,
)