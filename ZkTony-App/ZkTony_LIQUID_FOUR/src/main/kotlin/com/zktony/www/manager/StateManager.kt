package com.zktony.www.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import com.zktony.common.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StateManager constructor(
    private val serialManager: SerialManager,
    private val motorManager: MotorManager,
    private val executionManager: ExecutionManager,
    private val containerManager: ContainerManager,
    private val workerManager: WorkerManager,
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {


    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    fun initApp() {
        scope.launch {
            launch {
                serialManager.test()
                motorManager.test()
                containerManager.test()
                executionManager.test()
                workerManager.createWorker()
            }
            launch {
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
            }
        }
    }

}

data class Settings(
    val bar: Boolean = false,
    val needleSpace: Float = 7.3f,
)