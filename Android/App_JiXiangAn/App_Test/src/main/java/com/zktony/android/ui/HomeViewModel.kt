package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.History
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.IncubationStage
import com.zktony.android.data.entities.internal.Log
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.readWithTemperature
import com.zktony.android.utils.SerialPortUtils.writeRegister
import com.zktony.android.utils.SerialPortUtils.writeWithPulse
import com.zktony.android.utils.SerialPortUtils.writeWithTemperature
import com.zktony.android.utils.SerialPortUtils.writeWithValve
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())
    private val _insulation = MutableStateFlow(List(5) { 0.0 })
    private val _valve = MutableStateFlow(List(2) { 0 })
    private val _motor = MutableStateFlow(List(2) { 0 })
    private val _shakerJob = MutableStateFlow<Job?>(null)
    private val _insulationJob = MutableStateFlow<Job?>(null)
    private val _valveJob = MutableStateFlow<Job?>(null)

    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val insulation = _insulation.asStateFlow()
    val valve = _valve.asStateFlow()
    val motor = _motor.asStateFlow()
    val shakerJob = _shakerJob.asStateFlow()
    val insulationJob = _insulationJob.asStateFlow()
    val valveJob = _valveJob.asStateFlow()

    init {
        viewModelScope.launch {
            // 设置定时查询温度
            while (true) {
                delay(3000L)
                repeat(5) {
                    readWithTemperature(it) { address, temp ->
                        _insulation.value = _insulation.value.toMutableList().apply {
                            this[address] = temp
                        }
                    }
                    delay(100L)
                }
            }
        }
    }

    fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is HomeIntent.Start -> start(intent.code)
            is HomeIntent.Stop -> stop(intent.code)
        }
    }

    private fun start(code: Int) {
        when(code) {
            0 -> {
                _shakerJob.value = viewModelScope.launch {
                    delay(100L)
                    writeRegister(slaveAddr = 0, startAddr = 200, value = 1)
                    _motor.value = _motor.value.toMutableList().apply {
                        this[0] = 1
                    }
                }
            }
            1 -> {
                _insulationJob.value = viewModelScope.launch {
                    var count = 0
                    while (true) {
                        repeat(5) {
                            writeWithTemperature(it, if (count % 2 == 0) 4.0 else 37.0)
                            delay(100L)
                        }
                        count++
                        delay(30 * 60 * 1000L)
                    }
                }
            }
            2 -> {
                _valveJob.value = viewModelScope.launch {
                    while (true) {
                        repeat(2) {
                            val channel = if (_valve.value[it] == (if (it == 0) 12 else 6)) 1 else _valve.value[it] + 1
                            try {
                                writeWithValve(it, channel)
                            } catch (ex: Exception) {
                               if (ex !is CancellationException) {
                                   _uiFlags.value = UiFlags.message(ex.message ?: "Unknown")
                               }
                            }
                            _valve.value = _valve.value.toMutableList().apply {
                                this[it] = channel
                            }
                            delay(500L)
                        }
                        _motor.value = _motor.value.toMutableList().apply {
                            this[1] = 1
                        }
                        try {
                            writeWithPulse(1,50 * 6400L)
                            delay(1000L)
                            writeWithPulse(1,-(50 * 6400L))
                        } catch (ex: Exception) {
                            if (ex !is CancellationException) {
                                _uiFlags.value = UiFlags.message(ex.message ?: "Unknown")
                            }
                        }
                        _motor.value = _motor.value.toMutableList().apply {
                            this[1] = 0
                        }
                        delay( 10 * 1000L)
                    }
                }
            }
            else -> {}
        }
    }

    private fun stop(code: Int) {
        when(code) {
            0 -> {
                viewModelScope.launch {
                    writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
                    delay(300L)
                    writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
                    _shakerJob.value?.cancel()
                    _shakerJob.value = null
                    _motor.value = _motor.value.toMutableList().apply {
                        this[0] = 0
                    }
                }
            }
            1 -> {
                _insulationJob.value?.cancel()
                _insulationJob.value = null
            }
            2 -> {
                _valveJob.value?.cancel()
                _valveJob.value = null
            }
            else -> {}
        }
    }
}

sealed class HomeIntent {
    data class Flags(val uiFlags: UiFlags) : HomeIntent()
    data class Start(val code: Int) : HomeIntent()
    data class Stop(val code: Int) : HomeIntent()
}