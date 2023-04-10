package com.zktony.www.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zktony.datastore.ext.read
import com.zktony.core.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                dataStore.read(Constants.BAR, false).collect {
                    _settings.value = _settings.value.copy(bar = it)
                }
            }
        }
    }

}

data class Settings(
    val bar: Boolean = false,
    // 间距
    val space: Float = 0f,
)