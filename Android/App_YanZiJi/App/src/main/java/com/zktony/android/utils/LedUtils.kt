package com.zktony.android.utils

import com.zktony.android.data.LedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object LedUtils {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var state = LedState.YELLOW_FLASH

    fun transform(newState: LedState) {
        if (state == newState) {
            return
        } else {
            scope.launch {
                if (SerialPortUtils.setLedState(newState.id)) {
                    state = newState
                }
            }
        }
    }
}