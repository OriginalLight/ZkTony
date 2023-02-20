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
import com.zktony.www.data.local.room.entity.Container
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
    private val motorRepository: MotorRepository,
    private val containerRepository: ContainerRepository,
    private val calibrationRepository: CalibrationRepository
) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                motorRepository.init()
                containerRepository.init()
                calibrationRepository.init()
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
                motorRepository.getAll().collect {
                    if (it.isNotEmpty()) {
                        MotorManager.instance.initMotor(it)
                    }
                }
            }
            launch {
                containerRepository.getAll().collect {
                    if (it.isNotEmpty()) {
                        _settings.value = _settings.value.copy(container = it.first())
                    }
                }
            }
            launch {
                calibrationRepository.getAll().collect {
                    if (it.isNotEmpty()) {
                        MotorManager.instance.initCali(it)
                    }
                }
            }
        }
    }
}

data class Settings(
    val temp: Float = 3f,
    val bar: Boolean = false,
    val container: Container = Container(),
)