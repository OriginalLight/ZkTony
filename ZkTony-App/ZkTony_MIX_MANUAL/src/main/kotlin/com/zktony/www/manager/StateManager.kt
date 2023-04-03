package com.zktony.www.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zktony.common.ext.read
import com.zktony.common.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StateManager constructor(
    private val serialManager: SerialManager,
    private val motorManager: MotorManager,
    private val executionManager: ExecutionManager,
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
                executionManager.test()
            }
            launch {
                launch {
                    dataStore.read(Constants.BAR, false).collect {
                        _settings.value = _settings.value.copy(bar = it)
                    }
                }
            }
        }
    }

}

data class Settings(
    val bar: Boolean = false,
)