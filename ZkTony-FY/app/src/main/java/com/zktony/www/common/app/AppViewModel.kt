package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.www.common.Logger
import com.zktony.www.common.constant.Constants
import com.zktony.www.data.entity.Calibration
import com.zktony.www.data.entity.MotionMotor
import com.zktony.www.data.entity.Motor
import com.zktony.www.data.entity.PumpMotor
import com.zktony.www.data.repository.RoomRepository
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.SerialPortManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [Application]生命周期内的[AndroidViewModel]
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
    private val dataStore: DataStore<Preferences>,
    private val roomRepository: RoomRepository,
) : AndroidViewModel(application) {

    private val _state = MutableSharedFlow<AppState>()
    val state: SharedFlow<AppState> get() = _state
    private val intent = MutableSharedFlow<AppIntent>()
    private val _settingState = MutableStateFlow(SettingState())
    val settingState: StateFlow<SettingState> get() = _settingState

    init {
        viewModelScope.launch {
            launch {
                intent.collect {
                    when (it) {
                        is AppIntent.Sender -> sender(it.serialPort, it.command)
                        is AppIntent.SenderText -> senderText(it.serialPort, it.command)
                        is AppIntent.ReceiverSerialOne -> receiverSerialOne(it.command)
                        is AppIntent.ReceiverSerialTwo -> receiverSerialTwo(it.command)
                        is AppIntent.ReceiverSerialThree -> receiverSerialThree(it.command)
                        is AppIntent.ReceiverSerialFour -> receiverSerialFour(it.command)
                    }
                }
            }
            launch {
                dataStore.data.map {
                    it[floatPreferencesKey(Constants.TEMP)] ?: 3.0f
                }.collect {
                    _settingState.value = _settingState.value.copy(temp = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.BAR)] ?: false
                }.collect {
                    _settingState.value = _settingState.value.copy(bar = it)
                }
            }
            launch {
                roomRepository.getMotorRepository().getAll().collect {
                    loadMotors(it)
                }
            }
            launch {
                delay(1000L)
                roomRepository.getCalibrationRepository().getCalibration().collect {
                    loadCalibration(it)
                }
            }
            launch {
                SerialPortManager.instance.commandQueueActuator()
            }
        }
    }

    /**
     * Intent处理器
     * @param intent [AppIntent]
     */
    fun dispatch(intent: AppIntent) {
        try {
            viewModelScope.launch {
                this@AppViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 发送指令
     * @param serialPort [SerialPortEnum]
     * @param command  [String] 指令
     */
    private fun sender(serialPort: SerialPortEnum, command: String) {
        viewModelScope.launch {
            _state.emit(AppState.Sender(serialPort, command))
        }
    }

    /**
     * 发送指令
     * @param serialPort [SerialPortEnum]
     * @param command  [String] 指令
     */
    private fun senderText(serialPort: SerialPortEnum, command: String) {
        viewModelScope.launch {
            _state.emit(AppState.SenderText(serialPort, command))
        }
    }

    /**
     * 接收指令串口一
     * @param command [String] 指令
     */
    private fun receiverSerialOne(command: String) {
        viewModelScope.launch {
            _state.emit(AppState.ReceiverSerialOne(command))
            Logger.d(msg = "receiverSerialOne: $command")
        }
    }

    /**
     * 接收指令串口二
     * @param command [String] 指令
     */
    private fun receiverSerialTwo(command: String) {
        viewModelScope.launch {
            _state.emit(AppState.ReceiverSerialTwo(command))
            Logger.d(msg = "receiverSerialTwo: $command")
        }
    }

    /**
     * 接收指令串口三
     * @param command [String] 指令
     */
    private fun receiverSerialThree(command: String) {
        viewModelScope.launch {
            _state.emit(AppState.ReceiverSerialThree(command))
            Logger.d(msg = "receiverSerialThree: $command")
        }
    }

    /**
     * 接收指令串口四
     * @param command [String] 指令
     */
    private fun receiverSerialFour(command: String) {
        viewModelScope.launch {
            _state.emit(AppState.ReceiverSerialFour(command))
            Logger.d(msg = "receiverSerialFour: $command")
        }
    }


    private fun loadMotors(motors: List<Motor>) {
        viewModelScope.launch {
            motors.forEach { motor ->
                when (motor.board) {
                    SerialPortEnum.SERIAL_ONE.index -> {
                        when (motor.address) {
                            1 -> _settingState.value = settingState.value.copy(
                                motionMotor = settingState.value.motionMotor.copy(xAxis = motor)
                            )
                            2 -> _settingState.value = settingState.value.copy(
                                motionMotor = settingState.value.motionMotor.copy(yAxis = motor)
                            )
                            3 -> _settingState.value = settingState.value.copy(
                                motionMotor = settingState.value.motionMotor.copy(zAxis = motor)
                            )
                        }
                    }
                    SerialPortEnum.SERIAL_TWO.index -> {
                        when (motor.address) {
                            1 -> _settingState.value = settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(pumpOne = motor)
                            )
                            2 -> _settingState.value = settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(pumpTwo = motor)
                            )
                            3 -> _settingState.value = settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(pumpThree = motor)
                            )
                        }
                    }
                    SerialPortEnum.SERIAL_THREE.index -> {
                        when (motor.address) {
                            1 -> _settingState.value = settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(pumpFour = motor)
                            )
                            2 -> _settingState.value = settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(pumpFive = motor)
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 加载校准数据
     */
    private fun loadCalibration(calibrations: List<Calibration>) {
        var calibration = Calibration()
        if (calibrations.isNotEmpty()) {
            calibration = calibrations[0]
        }
        _settingState.value = settingState.value.copy(
            calibration = calibration,
            motionMotor = settingState.value.motionMotor.copy(
                yLength = calibration.yMotorDistance,
                zLength = calibration.zMotorDistance
            ),
            pumpMotor = settingState.value.pumpMotor.copy(
                volumeOne = calibration.pumpOneDistance,
                volumeTwo = calibration.pumpTwoDistance,
                volumeThree = calibration.pumpThreeDistance,
                volumeFour = calibration.pumpFourDistance,
                volumeFive = calibration.pumpFiveDistance
            )
        )
    }
}

sealed class AppState {
    data class Sender(val serialPort: SerialPortEnum, val command: String) : AppState()
    data class SenderText(val serialPort: SerialPortEnum, val command: String) : AppState()
    data class ReceiverSerialOne(val command: String) : AppState()
    data class ReceiverSerialTwo(val command: String) : AppState()
    data class ReceiverSerialThree(val command: String) : AppState()
    data class ReceiverSerialFour(val command: String) : AppState()
}

sealed class AppIntent {
    data class Sender(val serialPort: SerialPortEnum, val command: String) : AppIntent()
    data class SenderText(val serialPort: SerialPortEnum, val command: String) : AppIntent()
    data class ReceiverSerialOne(val command: String) : AppIntent()
    data class ReceiverSerialTwo(val command: String) : AppIntent()
    data class ReceiverSerialThree(val command: String) : AppIntent()
    data class ReceiverSerialFour(val command: String) : AppIntent()
}

data class SettingState(
    var temp: Float = 3f,
    var bar: Boolean = false,
    var motionMotor: MotionMotor = MotionMotor(),
    var pumpMotor: PumpMotor = PumpMotor(),
    var calibration: Calibration = Calibration()
)