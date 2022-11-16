package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.serialport.COMSerial
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.www.common.utils.Constants
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.model.SerialPort
import com.zktony.www.ui.home.model.Cmd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    private val _send = MutableStateFlow(Cmd())
    val send = _send.asStateFlow()
    private val _received = MutableStateFlow(Cmd())
    val received = _received.asStateFlow()

    init {
        initSettings()
        COMSerial.instance.addCOM(SerialPort.TTYS4.device, 115200)
        COMSerial.instance.addDataListener(object : OnComDataListener {
            override fun comDataBack(com: String, hexData: String) {
                val cmd = Cmd(hexData)
                if (cmd.cmd == 2) {
                    receive(cmd)
                }
            }
        })
    }

    /**
     * 发送指令
     * @param cmd [Cmd] 指令
     */
    fun send(cmd: Cmd) {
        viewModelScope.launch {
            _send.value = cmd
            COMSerial.instance.sendHex(SerialPort.TTYS4.device, cmd.genHex())
            Logger.d(msg = "发送指令：${cmd.genHex()}")
        }
    }

    /**
     * 接收到数据
     * @param cmd [Cmd] 指令
     */
    fun receive(cmd: Cmd) {
        viewModelScope.launch {
            _received.value = cmd
            Logger.d(msg ="接收到指令：${cmd.genHex()}")
        }
    }

    private fun initSettings() {
        viewModelScope.launch {
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.AUDIO)] ?: true
                }.distinctUntilChanged().collect {
                    _setting.value = _setting.value.copy(audio = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.BAR)] ?: false
                }.distinctUntilChanged().collect {
                    _setting.value = _setting.value.copy(bar = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[booleanPreferencesKey(Constants.DETECT)] ?: true
                }.distinctUntilChanged().collect {
                    _setting.value = _setting.value.copy(detect = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[intPreferencesKey(Constants.INTERVAL)] ?: 1
                }.distinctUntilChanged().collect {
                    _setting.value = _setting.value.copy(interval = it)
                }
            }
            launch {
                dataStore.data.map {
                    it[intPreferencesKey(Constants.DURATION)] ?: 10
                }.distinctUntilChanged().collect {
                    _setting.value = _setting.value.copy(duration = it)
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
)