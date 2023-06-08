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
    fun dv(index: Int, dv: Float, config: MotorEntity = scheduleTask.hpm[index]!!) {
        byteList.addAll(pwc(index, dv, config).toList())
        indexList.add(index)
    }

    fun pulse(index: Int, pulse: Long, config: MotorEntity = scheduleTask.hpm[index]!!) {
        byteList.addAll(pwc(index, pulse, config).toList())
        indexList.add(index)
    }
}