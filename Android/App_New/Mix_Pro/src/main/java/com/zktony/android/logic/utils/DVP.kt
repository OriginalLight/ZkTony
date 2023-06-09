package com.zktony.android.logic.utils

import com.zktony.android.logic.data.entities.MotorEntity
import com.zktony.android.logic.ext.pwc
import com.zktony.android.logic.ext.scheduleTask

/**
 * 组合类
 *
 * @property byteList MutableList<Byte>
 * @property indexList MutableList<Int>
 */
class DVP {
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()
    fun dv(block: DM.() -> Unit) {
        val dm = DM().apply(block)

        byteList.addAll(
            pwc(
                dm.index,
                dm.dv,
                MotorEntity(speed = dm.speed, acc = dm.acc, dec = dm.dec)
            ).toList()
        )
        indexList.add(dm.index)
    }

    fun pulse(block: PM.() -> Unit) {
        val pm = PM().apply(block)
        byteList.addAll(
            pwc(
                pm.index,
                pm.pulse,
                MotorEntity(speed = pm.speed, acc = pm.acc, dec = pm.dec)
            ).toList()
        )
        indexList.add(pm.index)
    }
}

class DM {
    var index: Int = 0
    var dv: Float = 0f
    var acc: Int = scheduleTask.hpm[index]!!.acc
    var dec: Int = scheduleTask.hpm[index]!!.dec
    var speed: Int = scheduleTask.hpm[index]!!.speed
}

class PM {
    var index: Int = 0
    var pulse: Long = 0
    var acc: Int = scheduleTask.hpm[index]!!.acc
    var dec: Int = scheduleTask.hpm[index]!!.dec
    var speed: Int = scheduleTask.hpm[index]!!.speed
}