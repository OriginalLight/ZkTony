package com.zktony.android.utils.tx

/**
 * @author 刘贺贺
 * @date 2023/7/26 13:20
 */
class MoveScope {
    var index: Int = 0
    var dv: Float = 0f
    var pulse: Long = 0L
    var acc: Long = scheduleTask.hpm[index]!!.acceleration
    var dec: Long = scheduleTask.hpm[index]!!.deceleration
    var speed: Long = scheduleTask.hpm[index]!!.speed
}