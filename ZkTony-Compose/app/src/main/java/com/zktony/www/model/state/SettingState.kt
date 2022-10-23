package com.zktony.www.model.state

import com.zktony.www.model.MotionMotor
import com.zktony.www.model.PumpMotor

/**
 * @author: 刘贺贺
 * @date: 2022-10-12 13:34
 */
data class SettingState(
    var temp: Float = 3f,
    var bar: Boolean = false,
    var motionMotor: MotionMotor = MotionMotor(),
    var pumpMotor: PumpMotor = PumpMotor(),
)
