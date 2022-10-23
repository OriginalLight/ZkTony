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
import com.zktony.www.data.entity.Motor
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.model.enum.SerialPortEnum
import com.zktony.www.model.state.SettingState
import com.zktony.www.serialport.SerialPortManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val motorRepository: MotorRepository
) : AndroidViewModel(application) {

    private val _state = MutableSharedFlow<AppState>()
    val state: SharedFlow<AppState> get() = _state
    private val intent = MutableSharedFlow<AppIntent>()
    private val _settingState = MutableStateFlow(SettingState())
    val settingState: StateFlow<SettingState> get() = _settingState

    init {
        viewModelScope.launch {
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
        viewModelScope.launch {
            dataStore.data.map {
                it[floatPreferencesKey(Constants.TEMP)] ?: 3.0f
            }.collect {
                _settingState.value = _settingState.value.copy(temp = it)
            }
        }
        viewModelScope.launch {
            dataStore.data.map {
                it[booleanPreferencesKey(Constants.BAR)] ?: false
            }.collect {
                _settingState.value = _settingState.value.copy(bar = it)
            }
        }
        viewModelScope.launch {
            motorRepository.getAll().collect {
                loadMotors(it)
            }
        }
        viewModelScope.launch {
            SerialPortManager.instance.commandQueueActuator()
        }
    }

    fun dispatch(intent: AppIntent) {
        try {
            viewModelScope.launch {
                this@AppViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    private fun sender(serialPort: SerialPortEnum, command: String) {
        viewModelScope.launch {
            _state.emit(AppState.Sender(serialPort, command))
        }
    }

    private fun senderText(serialPort: SerialPortEnum, command: String) {
        viewModelScope.launch {
            _state.emit(AppState.SenderText(serialPort, command))
        }
    }

    private fun receiverSerialOne(command: String) {
        viewModelScope.launch {
            _state.emit(AppState.ReceiverSerialOne(command))
            Logger.d(msg = "receiverSerialOne: $command")
        }
    }

    private fun receiverSerialTwo(command: String) {
        viewModelScope.launch {
            _state.emit(AppState.ReceiverSerialTwo(command))
            Logger.d(msg = "receiverSerialTwo: $command")
        }
    }

    private fun receiverSerialThree(command: String) {
        viewModelScope.launch {
            _state.emit(AppState.ReceiverSerialThree(command))
            Logger.d(msg = "receiverSerialThree: $command")
        }
    }

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
                            1 -> _settingState.value = _settingState.value.copy(
                                motionMotor = settingState.value.motionMotor.copy(xAxis = motor)
                            )
                            2 -> _settingState.value = _settingState.value.copy(
                                motionMotor = settingState.value.motionMotor.copy(yAxis = motor)
                            )
                            3 -> _settingState.value = _settingState.value.copy(
                                motionMotor = settingState.value.motionMotor.copy(zAxis = motor)
                            )
                        }
                    }
                    SerialPortEnum.SERIAL_TWO.index -> {
                        when (motor.address) {
                            1 -> _settingState.value = _settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(one = motor)
                            )
                            2 -> _settingState.value = _settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(two = motor)
                            )
                            3 -> _settingState.value = _settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(three = motor)
                            )
                        }
                    }
                    SerialPortEnum.SERIAL_THREE.index -> {
                        when (motor.address) {
                            1 -> _settingState.value = _settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(four = motor)
                            )
                            2 -> _settingState.value = _settingState.value.copy(
                                pumpMotor = settingState.value.pumpMotor.copy(five = motor)
                            )
                        }
                    }
                }
            }
        }
    }
}