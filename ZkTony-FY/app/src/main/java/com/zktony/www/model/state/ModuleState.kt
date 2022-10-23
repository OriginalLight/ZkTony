package com.zktony.www.model.state

import android.view.View
import kotlinx.coroutines.Job

/**
 * @author: 刘贺贺
 * @date: 2022-10-12 9:53
 */
data class ModuleState(
    var index: Int = -1,
    var isRunning: Boolean = false,
    var btnStart: ButtonState = ButtonState(enable = false),
    var btnStop: ButtonState = ButtonState(visibility = View.GONE),
    var btnProgram: ButtonState = ButtonState(),
    var job: Job? = null,
)
