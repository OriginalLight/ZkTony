package com.zktony.www.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.zktony.core.ext.logi
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StateManager constructor(
    private val serialManager: SerialManager,
    private val workerManager: WorkerManager,
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _setting = MutableStateFlow(Setting())
    val setting = _setting.asStateFlow()
    private val _send = MutableStateFlow(V1())
    val send = _send.asStateFlow()
    private val _received = MutableStateFlow(V1())
    val received = _received.asStateFlow()

    fun initApp() {
        scope.launch {
            launch {
                serialManager.test()
                workerManager.createWorker()
            }
            launch {
                serialManager.ttys4Flow.collect {
                    it?.let {
                        val v1 = V1(it)
                        if (v1.cmd == 2) {
                            _received.value = v1
                            "收到指令: ${v1.genHex()}".logi()
                        }
                    }
                }
            }
            launch {
                launch {
                    dataStore.read(Constants.AUDIO, true).collect {
                        _setting.value = _setting.value.copy(audio = it)
                    }
                }
                launch {
                    dataStore.read(Constants.BAR, false).collect {
                        _setting.value = _setting.value.copy(bar = it)
                    }
                }
                launch {
                    dataStore.read(Constants.DETECT, true).collect {
                        _setting.value = _setting.value.copy(detect = it)
                    }
                }
                launch {
                    dataStore.read(Constants.INTERVAL, 1).collect {
                        _setting.value = _setting.value.copy(interval = it)
                    }
                }
                launch {
                    dataStore.read(Constants.DURATION, 10).collect {
                        _setting.value = _setting.value.copy(duration = it)
                    }
                }
                launch {
                    dataStore.data.map {
                        it[intPreferencesKey(Constants.MOTOR_SPEED)] ?: 160
                    }.collect {
                        _setting.value = _setting.value.copy(motorSpeed = it)
                    }
                }
            }
        }
    }

    /**
     * 发送指令
     * @param v1 [V1] 指令
     */
    fun send(v1: V1) {
        scope.launch {
            _send.value = v1
            serialManager.send(v1.genHex())
            "发送指令: ${v1.genHex()}".logi()
        }
    }

}

data class Setting(
    val audio: Boolean = true,
    val bar: Boolean = false,
    val detect: Boolean = true,
    val duration: Int = 10,
    val interval: Int = 1,
    val motorSpeed: Int = 160,
)