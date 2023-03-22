package com.zktony.www.ui.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.common.utils.Constants
import com.zktony.common.utils.Snowflake
import com.zktony.www.data.local.room.dao.*
import com.zktony.www.data.local.room.entity.*
import com.zktony.www.manager.MotorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * [Application] 生命周期内的 [AndroidViewModel]
 */

class AppViewModel  constructor(
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
        // 初始化电机
        val motors = motor.getAll().firstOrNull() ?: emptyList()
        if (motors.isEmpty()) {
            motor.insertAll(
                listOf(
                    Motor(id = 0, name = "X轴", address = 1),
                    Motor(id = 1, name = "Z轴", address = 3),
                    Motor(id = 2, name = "泵一", address = 1),
                    Motor(id = 3, name = "泵二", address = 2),
                    Motor(id = 4, name = "泵三", address = 3),
                )
            )
        }
        // 初始化容器
        val con = container.getAll().firstOrNull() ?: emptyList()
        if (con.isEmpty()) {
            container.insert(
                Container(
                    id = 1L,
                    name = "默认容器",
                )
            )
            plate.insert(
                Plate(
                    id = 1L,
                    subId = 1L,
                    x = 10,
                )
            )
            val holes = mutableListOf<Hole>()
            val snowflake = Snowflake(1)
            for (i in 0 until 10) {
                holes.add(
                    Hole(
                        id = snowflake.nextId(),
                        subId = 1L,
                        x = i,
                    )
                )
            }
            hole.insertAll(holes)
        }
        // 初始化标定
        val cal = calibration.getAll().firstOrNull() ?: emptyList()
        if (cal.isEmpty()) {
            calibration.insert(Calibration(enable = 1))
        }
    }
}

data class Settings(
    val bar: Boolean = false,
)