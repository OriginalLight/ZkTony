package com.zktony.www.model.state

import android.view.View.VISIBLE

/**
 * @author: 刘贺贺
 * @date: 2022-10-11 17:09
 */
data class ButtonState(
    var visibility: Int = VISIBLE,
    var isClickable: Boolean = true,
    var text: String = "",
    var enable: Boolean = true,
    var background: Int = 0,
    var isRunning: Boolean = false,
    var icon: Int = 0,
    var textColor: Int = 0,
)
