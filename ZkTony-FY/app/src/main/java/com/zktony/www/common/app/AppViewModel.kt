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
import com.zktony.www.common.room.entity.PumpMotor
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.data.repository.MotorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [Application] 生命周期内的 [AndroidViewModel]
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
    private val dataStore: DataStore<Preferences>,
    private val motorRepo: MotorRepository,
    private val caliRepo: CalibrationRepository
) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dataStore.data.map {
                    it[floatPreferencesKey(Constants.TEMP)] ?: 3.0f
                }.collect {
                    _settings.value = _settings.value.copy(temp = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.BAR)] ?: false
                }.collect {
                    _settings.value = _settings.value.copy(bar = it)
                }
            }
            launch {
                motorRepo.getAll().collect {
                    it.forEach { motor ->
                        when (motor.board) {
                            0 -> {
                                when (motor.address) {
                                    1 -> _settings.value = settings.value.copy(
                                        motionMotor = settings.value.motionMotor.copy(x = motor)
                                    )
                                    2 -> _settings.value = settings.value.copy(
                                        motionMotor = settings.value.motionMotor.copy(y = motor)
                                    )
                                    3 -> _settings.value = settings.value.copy(
                                        motionMotor = settings.value.motionMotor.copy(z = motor)
                                    )
                                }
                            }
                            1 -> {
                                when (motor.address) {
                                    1 -> _settings.value = settings.value.copy(
                                        pumpMotor = settings.value.pumpMotor.copy(one = motor)
                                    )
                                    2 -> _settings.value = settings.value.copy(
                                        pumpMotor = settings.value.pumpMotor.copy(two = motor)
                                    )
                                    3 -> _settings.value = settings.value.copy(
                                        pumpMotor = settings.value.pumpMotor.copy(three = motor)
                                    )
                                }
                            }
                            2 -> {
                                when (motor.address) {
                                    1 -> _settings.value = settings.value.copy(
                                        pumpMotor = settings.value.pumpMotor.copy(four = motor)
                                    )
                                    2 -> _settings.value = settings.value.copy(
                                        pumpMotor = settings.value.pumpMotor.copy(five = motor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            launch {
                delay(1000L)
                caliRepo.getCalibration().collect {
                    it.firstOrNull()?.let { cali ->
                        _settings.value = settings.value.copy(
                            calibration = cali,
                            motionMotor = settings.value.motionMotor.copy(
                                distanceY = cali.distanceY,
                                distanceZ = cali.distanceZ
                            ),
                            pumpMotor = settings.value.pumpMotor.copy(
                                volumeOne = cali.volumeOne,
                                volumeTwo = cali.volumeTwo,
                                volumeThree = cali.volumeThree,
                                volumeFour = cali.volumeFour,
                                volumeFive = cali.volumeFive
                            )
                        )
                    }
                }
            }
        }
    }
}

data class Settings(
    var temp: Float = 3f,
    var bar: Boolean = false,
    var motionMotor: MotionMotor = MotionMotor(),
    var pumpMotor: PumpMotor = PumpMotor(),
    var calibration: Calibration = Calibration()
)