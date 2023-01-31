package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.www.common.utils.Constants
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.Container
import com.zktony.www.common.room.entity.MotorUnits
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.data.repository.ContainerRepository
import com.zktony.www.data.repository.MotorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val containerRepo: ContainerRepository,
    private val calibrationRepo: CalibrationRepository
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
                    // 将list的元素放入motorUnits
                    it.forEach { motor ->
                        when (motor.id) {
                            0 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(x = motor))
                            1 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(y = motor))
                            2 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(z = motor))
                            3 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(p1 = motor))
                            4 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(p2 = motor))
                            5 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(p3 = motor))
                            6 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(p4 = motor))
                            7 -> _settings.value =
                                _settings.value.copy(motorUnits = _settings.value.motorUnits.copy(p5 = motor))
                        }
                    }
                }
            }
            launch {
                containerRepo.getAll().collect {
                    if (it.isNotEmpty()) {
                        _settings.value = _settings.value.copy(container = it.first())
                    }
                }
            }
            launch {
                calibrationRepo.getDefault().collect {
                    _settings.value =
                        _settings.value.copy(
                            motorUnits = _settings.value.motorUnits.copy(
                                cali = if (it.isEmpty()) Calibration() else it.first()
                            )
                        )
                }
            }
        }
    }
}

data class Settings(
    val temp: Float = 3f,
    val bar: Boolean = false,
    val motorUnits: MotorUnits = MotorUnits(),
    val container: Container = Container(),
)