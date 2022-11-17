package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.MotionMotor
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.common.room.entity.PumpMotor
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.repository.RoomRepository
import com.zktony.www.serialport.SerialPort
import com.zktony.www.serialport.SerialPortManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    private val _settingState = MutableStateFlow(SettingState())
    val settingState = _settingState.asStateFlow()

    init {
        viewModelScope.launch {
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
                SerialPortManager.instance.queueActuator()
            }
        }
    }


    /**
     * 加载电机参数
     * @param motors [List]<[Motor]>
     */
    private fun loadMotors(motors: List<Motor>) {
        viewModelScope.launch {
            motors.forEach { motor ->
                when (motor.board) {
                    SerialPort.SERIAL_ONE.index -> {
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
                    SerialPort.SERIAL_TWO.index -> {
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
                    SerialPort.SERIAL_THREE.index -> {
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

data class SettingState(
    var temp: Float = 3f,
    var bar: Boolean = false,
    var motionMotor: MotionMotor = MotionMotor(),
    var pumpMotor: PumpMotor = PumpMotor(),
    var calibration: Calibration = Calibration()
)