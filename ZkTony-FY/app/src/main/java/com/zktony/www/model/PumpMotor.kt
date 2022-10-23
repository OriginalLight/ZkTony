package com.zktony.www.model

import com.zktony.www.data.entity.Motor

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:01
 */
data class PumpMotor(
    var one: Motor = Motor(),
    var two: Motor = Motor(),
    var three: Motor = Motor(),
    var four: Motor = Motor(),
    var five: Motor = Motor(),
)