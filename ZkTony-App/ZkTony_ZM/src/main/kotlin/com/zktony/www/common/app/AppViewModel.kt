package com.zktony.www.common.app

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.common.utils.Constants
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [Application]生命周期内的[AndroidViewModel]
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
    private val dataStore: DataStore<Preferences>,
) : AndroidViewModel(application) {

    private val _setting = MutableStateFlow(AppSetting())
    val setting = _setting.asStateFlow()
    private val _send = MutableStateFlow(V1())
    val send = _send.asStateFlow()
    private val _received = MutableStateFlow(V1())
    val received = _received.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                initSettings()
            }
            launch {
                SerialManager.instance.ttys4Flow.collect {
                    it?.let {
                        val v1 = V1(it)
                        if (v1.cmd == 2) {
                            _received.value = v1
                            Log.d("AppViewModel", "收到指令: ${v1.genHex()}")
                        }
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
        viewModelScope.launch {
            _send.value = v1
            SerialManager.instance.send(v1.genHex())
            Log.d("AppViewModel", "发送指令: ${v1.genHex()}")
        }
    }


    private fun initSettings() {
        viewModelScope.launch {
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.AUDIO)] ?: true
                }.collect {
                    _setting.value = _setting.value.copy(audio = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.BAR)] ?: false
                }.collect {
                    _setting.value = _setting.value.copy(bar = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.DETECT)] ?: true
                }.collect {
                    _setting.value = _setting.value.copy(detect = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[intPreferencesKey(Constants.INTERVAL)] ?: 1
                }.collect {
                    _setting.value = _setting.value.copy(interval = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[intPreferencesKey(Constants.DURATION)] ?: 10
                }.collect {
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

data class AppSetting(
    val audio: Boolean = true,
    val bar: Boolean = false,
    val detect: Boolean = true,
    val duration: Int = 10,
    val interval: Int = 1,
    val motorSpeed: Int = 160,
)