package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.common.utils.Constants
import com.zktony.www.control.motor.MotorManager
import com.zktony.www.data.local.room.dao.CalibrationDao
import com.zktony.www.data.local.room.dao.ContainerDao
import com.zktony.www.data.local.room.dao.MotorDao
import com.zktony.www.data.local.room.entity.Calibration
import com.zktony.www.data.local.room.entity.Container
import com.zktony.www.data.local.room.entity.Motor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    private val motor: MotorDao,
    private val container: ContainerDao,
    private val calibration: CalibrationDao,
) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                init()
            }
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
                motor.getAll().collect {
                    if (it.isNotEmpty()) {
                        MotorManager.instance.initMotor(it)
                    }
                }
            }
            launch {
                container.getAll().collect {
                    if (it.isNotEmpty()) {
                        _settings.value = _settings.value.copy(container = it.first())
                    }
                }
            }
            launch {
                calibration.getAll().collect {
                    if (it.isNotEmpty()) {
                        MotorManager.instance.initCali(it)
                    }
                }
            }
        }
    }

    private suspend fun init() {
        val containers = container.getAll().firstOrNull() ?: emptyList()
        if (containers.isEmpty()) {
            container.insert(Container())
        }

        val motors = motor.getAll().firstOrNull() ?: emptyList()
        if (motors.isNotEmpty()) {
            val motorList = mutableListOf<Motor>()
            motorList.add(Motor(id = 0, name = "X轴", address = 1))
            motorList.add(Motor(id = 1, name = "Y轴", address = 2))
            motorList.add(Motor(id = 2, name = "Z轴", address = 3))
            for (i in 1..6) {
                val motor = Motor(
                    id = i + 2,
                    name = "泵$i",
                    address = if (i <= 3) i else i - 3,
                )
                motorList.add(motor)
            }
            motor.insertAll(motorList)
        }

        val calibrations = calibration.getAll().firstOrNull() ?: emptyList()
        if (calibrations.isEmpty()) {
            calibration.insert(Calibration(enable = 1))
        }
    }
}

data class Settings(
    val temp: Float = 3f,
    val bar: Boolean = false,
    val container: Container = Container(),
)