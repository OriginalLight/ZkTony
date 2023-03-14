package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.common.utils.Constants
import com.zktony.common.utils.Snowflake
import com.zktony.www.control.motor.MotorManager
import com.zktony.www.data.local.room.dao.*
import com.zktony.www.data.local.room.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val calibration: CalibrationDao,
    private val container: ContainerDao,
    private val plate: PlateDao,
    private val hole: HoleDao,
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
                    it[booleanPreferencesKey(Constants.BAR)] ?: false
                }.collect {
                    _settings.value = _settings.value.copy(bar = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[floatPreferencesKey(Constants.NEEDLE_SPACE)] ?: 7.3f
                }.collect {
                    _settings.value = _settings.value.copy(needleSpace = it)
                }
            }
            launch {
                motor.getAll().collect {
                    MotorManager.instance.initMotor(it)
                }
            }
            launch {
                calibration.getAll().collect {
                    MotorManager.instance.initCalibration(it)
                }
            }
        }
    }

    suspend fun init() {
        val cali = calibration.getAll().firstOrNull() ?: emptyList()
        if (cali.isEmpty()) {
            calibration.insert(Calibration(enable = 1))
        }
        val con = container.getAll().firstOrNull() ?: emptyList()
        if (con.isEmpty()) {
            container.insert(Container(id = 1L, name = "默认容器",))
            val plate1 = Plate(id = 1L, subId = 1L, index = 0)
            val plate2 = Plate(id = 2L, subId = 1L, index = 1)
            val plate3 = Plate(id = 3L, subId = 1L, index = 2)
            val plate4 = Plate(id = 4L, subId = 1L, index = 3)
            plate.insertAll(listOf(plate1, plate2, plate3, plate4))
            delay(10L)
            initHole(plate1)
            delay(10L)
            initHole(plate2)
            delay(10L)
            initHole(plate3)
            delay(10L)
            initHole(plate4)
        }
        val motors = motor.getAll().firstOrNull() ?: emptyList()
        if (motors.isEmpty()) {
            motor.insertAll(
                listOf(
                    Motor(id = 0, name = "X轴", address = 1),
                    Motor(id = 1, name = "Y轴", address = 2),
                    Motor(id = 2, name = "泵一", address = 3),
                    Motor(id = 3, name = "泵二", address = 1),
                    Motor(id = 4, name = "泵三", address = 2),
                    Motor(id = 5, name = "泵四", address = 3)
                )
            )
        }
    }

    private suspend fun initHole(plate: Plate) {
        val snowflake = Snowflake(2)
        val holes = mutableListOf<Hole>()
        for (i in 0 until  plate.x) {
            for (j in 0 until plate.y) {
                holes.add(Hole(id = snowflake.nextId(), subId = plate.id, x = i, y = j))
            }
        }
        hole.insertAll(holes)
    }
}

data class Settings(
    val bar: Boolean = false,
    val needleSpace: Float = 7.3f,
)