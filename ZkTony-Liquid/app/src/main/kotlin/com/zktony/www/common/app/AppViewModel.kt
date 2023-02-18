package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.www.common.utils.Constants
import com.zktony.www.control.motor.MotorManager
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.data.repository.PlateRepository
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
    private val motorRepository: MotorRepository,
    private val calibrationRepository: CalibrationRepository,
    private val plateRepository: PlateRepository
) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                plateRepository.init()
                motorRepository.init()
                calibrationRepository.init()
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.BAR)] ?: false
                }.collect {
                    _settings.value = _settings.value.copy(bar = it)
                }
            }
            launch {
                plateRepository.load().collect {
                    _settings.value = settings.value.copy(plate = it)
                }
            }
            launch {
                motorRepository.getAll().collect {
                    MotorManager.instance.initMotor(it)
                }
            }
            launch {
                calibrationRepository.getAll().collect {
                    MotorManager.instance.initCalibration(it)
                }
            }
        }
    }
}

data class Settings(
    val bar: Boolean = false,
    val plate: List<Plate> = emptyList()
)