package com.zktony.android.ext.dsl

/**
 * @author 刘贺贺
 * @date 2023/7/26 13:20
 */
class MoveScope {
    var index: Int = 0
    var dv: Float = 0f
    var pulse: Long = 0L
    var acc: Long = scheduleTask.hpm[index]!!.acc
    var dec: Long = scheduleTask.hpm[index]!!.dec
    var speed: Long = scheduleTask.hpm[index]!!.speed
}