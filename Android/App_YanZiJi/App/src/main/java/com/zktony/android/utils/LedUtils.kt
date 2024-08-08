package com.zktony.android.utils

import com.zktony.android.data.LedState
import com.zktony.log.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object LedUtils {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var state = LedState.YELLOW_FLASH

    fun transform(newState: LedState) {
        try {
            if (state == newState) {
                return
            } else {
                scope.launch {
                    if (SerialPortUtils.setLedState(newState.id)) {
                        state = newState
                        LogUtils.info("LED TO ${newState.name} SUCCESS", true)
                    } else {
                        LogUtils.error("LED TO ${newState.name} FAILED", true)
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    fun transformWithoutState(newState: LedState) {
        try {
            scope.launch {
                if (SerialPortUtils.setLedState(newState.id)) {
                    LogUtils.info("LED TO ${newState.name} SUCCESS", true)
                } else {
                    LogUtils.error("LED TO ${newState.name} FAILED", true)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
        }
    }
}