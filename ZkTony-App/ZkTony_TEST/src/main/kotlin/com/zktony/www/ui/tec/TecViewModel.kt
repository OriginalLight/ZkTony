package com.zktony.www.ui.tec

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.extension.hexToAscii
import com.zktony.common.extension.removeZero
import com.zktony.serialport.MutableSerial
import com.zktony.serialport.util.Serial
import com.zktony.www.common.app.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TecViewModel @Inject constructor() : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _serialFlow = MutableStateFlow<String?>(null)
    private val _uiState0 = MutableStateFlow(TecUiState(title = "Tec 0"))
    private val _uiState1 = MutableStateFlow(TecUiState(title = "Tec 1"))
    private val _uiState2 = MutableStateFlow(TecUiState(title = "Tec 2"))
    private val _uiState3 = MutableStateFlow(TecUiState(title = "Tec 3"))
    private val _uiState4 = MutableStateFlow(TecUiState(title = "Tec 4"))
    val serialFlow = _serialFlow.asStateFlow()
    val uiState0 = _uiState0.asStateFlow()
    val uiState1 = _uiState1.asStateFlow()
    val uiState2 = _uiState2.asStateFlow()
    val uiState3 = _uiState3.asStateFlow()
    val uiState4 = _uiState4.asStateFlow()


    fun init() {
        viewModelScope.launch {
            launch {
                MutableSerial.instance.init(Serial.TTYS3, 57600)
                MutableSerial.instance.listener = { port, data ->
                    when (port) {
                        Serial.TTYS3 -> {
                            _serialFlow.value = data.hexToAscii()
                        }
                        else -> {}
                    }
                }
            }
            launch {
                serialFlow.collect {
                    it?.let {
                        if (it.startsWith("TC1:TCACTUALTEMP=")) {
                            // 读取温度
                            val address = it.substring(it.length - 2, it.length - 1).toInt()
                            val temp = it.replace("TC1:TCACTUALTEMP=", "").split("@")[0].removeZero()
                            val flow = flow(address)
                            flow.value = flow.value.copy(temp = temp.toFloatOrNull() ?: 0f)
                        }
                    }
                }
            }
            launch {
                // 每十秒钟查询一次温度
                while (true) {
                    for (i in 0..4) {
                        delay(200L)
                        MutableSerial.instance.sendText(Serial.TTYS3, "TC1:TCACTUALTEMP?@$i\r")
                    }
                    delay(2 * 1000L)
                }
            }
        }
    }

    private fun flow(flag: Int) = when (flag) {
        0 -> _uiState0
        1 -> _uiState1
        2 -> _uiState2
        3 -> _uiState3
        4 -> _uiState4
        else -> _uiState0
    }


    fun destroy() {
        viewModelScope.launch {
            MutableSerial.instance.close(Serial.TTYS3)
        }
    }

    fun start(flag: Int) {
        val flow = flow(flag)
        val job = viewModelScope.launch {
            tecTest(flag = flag, count = 1)
        }
        flow.value = flow.value.copy(job = job, error = null)
        job.start()
    }

    fun stop(flag: Int) {
        val flow = flow(flag)
        flow.value.job?.cancel()
        flow.value = flow.value.copy(job = null)
    }

    fun showError(flag: Int) {
        val flow = flow(flag)
        if (flow.value.error != null) {
            PopTip.show(flow.value.error)
        }
    }

    private suspend fun tecTest(
        low: Float = 4f,
        high: Float = 37f,
        offset: Float = 3f,
        time: Int = 30,
        flag: Int,
        count: Int
    ) {
        val flow = flow(flag)
        setTempDelay(flag)
        MutableSerial.instance.sendText(
            Serial.TTYS3,
            "TC1:TCADJUSTTEMP=${low.toString().removeZero()}@$flag\r"
        )
        flow.value = flow.value.copy(setTemp = low, count = count)
        delay(time * 60 * 1000L)
        if (flow.value.temp > low + offset || flow.value.temp < low - offset) {
            flow.value = flow.value.copy(error = "制冷时温度异常 ${flow.value.temp}, 期望温度 $low")
            stop(flag)
            return
        }
        setTempDelay(flag)
        MutableSerial.instance.sendText(
            Serial.TTYS3,
            "TC1:TCADJUSTTEMP=${high.toString().removeZero()}@$flag\r"
        )
        flow.value = flow.value.copy(setTemp = high)
        delay(time * 60 * 1000L)
        if (flow.value.temp > high + offset || flow.value.temp < high - offset) {
            flow.value = flow.value.copy(error = "制热时温度异常 ${flow.value.temp}, 期望温度 $high")
            stop(flag)
            return
        }
        tecTest(low, high, offset, time, flag, count + 1)
    }


    /**
     * 设置温度时先关闭输出十秒后打开输出
     */
    private suspend fun setTempDelay(flag: Int) {
        MutableSerial.instance.sendText(
            Serial.TTYS3,
            "TC1:TCSW=0@$flag\r"
        )
        delay(20 * 1000L)
        MutableSerial.instance.sendText(
            Serial.TTYS3,
            "TC1:TCSW=1@$flag\r"
        )
        delay(1000L)
    }

}

data class TecUiState(
    val title: String = "Tec",
    val job: Job? = null,
    val setTemp: Float = 0f,
    val temp: Float = 0f,
    val count: Int = 0,
    val error: String? = null,
)