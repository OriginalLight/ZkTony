package com.zktony.www.ui.home

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.serialport.util.Serial
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel constructor(
    private val serialManager: SerialManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()


    fun reset() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            if (!serialManager.pause.value) {
                if (serialManager.lock.value) {
                    PopTip.show("运动中禁止复位")
                } else {
                    serialManager.reset()
                    PopTip.show("复位-已下发")
                }
            } else {
                PopTip.show("请中止所有运行中程序")
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            _uiState.value.job?.cancel()
            _uiState.value = _uiState.value.copy(
                job = null,
                time = 0L,
            )
            serialManager.pause(false)
            serialManager.sendHex(
                serial = Serial.TTYS0,
                hex = V1(pa = "10").toHex()
            )
            delay(300L)
            reset()
        }
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(pause = !_uiState.value.pause)
        this.serialManager.pause(_uiState.value.pause)
    }


    /**
     * 填充促凝剂
     */
    fun fillCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.fillCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    fillCoagulant = false,
                )
                serialManager.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                delay(100L)
                reset()
            } else {
                if (serialManager.reset.value) {
                    if (_uiState.value.recaptureCoagulant) {
                        PopTip.show("请先停止回吸")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        upOrDown = true,
                        fillCoagulant = true,
                    )
                    delay(100L)
                    while (_uiState.value.fillCoagulant) {
                        if (_uiState.value.upOrDown) {
                            _uiState.value = _uiState.value.copy(upOrDown = false)
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0301").toHex()
                            )
                            delay(7000L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0305").toHex()
                            )
                            delay(6500L)
                        }
                    }

                } else {
                    PopTip.show("请先复位")
                }
            }
        }
    }

    /**
     * 回吸促凝剂
     */
    fun recaptureCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.recaptureCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    recaptureCoagulant = false,
                )
                serialManager.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                delay(100L)
                reset()
            } else {
                if (serialManager.reset.value) {
                    if (_uiState.value.fillCoagulant) {
                        PopTip.show("请先停止填充")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        upOrDown = true,
                        recaptureCoagulant = true,
                    )
                    delay(100L)
                    while (_uiState.value.recaptureCoagulant) {
                        if (_uiState.value.upOrDown) {
                            _uiState.value = _uiState.value.copy(upOrDown = false)
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0303").toHex()
                            )
                            delay(6500L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0305").toHex()
                            )
                            delay(6500L)
                        }
                    }

                } else {
                    PopTip.show("请先复位")
                }
            }
        }
    }

    /**
     * 填充胶体
     */
    fun fillColloid() {
        viewModelScope.launch {
            serialManager.sendHex(
                serial = Serial.TTYS3,
                hex = V1(pa = "0B", data = "0401").toHex()
            )
        }
    }

    /**
     * 回吸胶体
     */
    fun recaptureColloid() {
        viewModelScope.launch {
            serialManager.sendHex(
                serial = Serial.TTYS3,
                hex = V1(pa = "0B", data = "0402").toHex()
            )
        }
    }

    /**
     * 停止填充和回吸
     */
    fun stopFillAndRecapture() {
        viewModelScope.launch {
            serialManager.sendHex(
                serial = Serial.TTYS3,
                hex = V1(pa = "0B", data = "0400").toHex()
            )
        }
    }
}

data class HomeUiState(
    val job: Job? = null,
    val pause: Boolean = false,
    val time: Long = 0L,
    val fillCoagulant: Boolean = false,
    val recaptureCoagulant: Boolean = false,
    val upOrDown: Boolean = true,
)