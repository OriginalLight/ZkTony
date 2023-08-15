package com.zktony.android.utils.model

import com.zktony.android.utils.ext.asyncHelper

class MoveParams {
    var index: Int = 0
    var dv: Double = 0.0
    var pulse: Long = 0L
    var acc: Long = asyncHelper.hpm[index]!!.acc
    var dec: Long = asyncHelper.hpm[index]!!.dec
    var speed: Long = asyncHelper.hpm[index]!!.speed
}