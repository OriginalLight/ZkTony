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
class DV {
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()

    fun m0(dv: Float, config: MotorEntity = scheduleTask.hpm[0]!!) {
        byteList.addAll(pwc(0, dv, config).toList())
        indexList.add(0)
    }

    fun m0(pulse: Long, config: MotorEntity = scheduleTask.hpm[0]!!) {
        byteList.addAll(pwc(0, pulse, config).toList())
        indexList.add(0)
    }

    fun m1(dv: Float, config: MotorEntity = scheduleTask.hpm[1]!!) {
        byteList.addAll(pwc(1, dv, config).toList())
        indexList.add(1)
    }

    fun m1(pulse: Long, config: MotorEntity = scheduleTask.hpm[1]!!) {
        byteList.addAll(pwc(1, pulse, config).toList())
        indexList.add(1)
    }

    fun m2(dv: Float, config: MotorEntity = scheduleTask.hpm[2]!!) {
        byteList.addAll(pwc(2, dv, config).toList())
        indexList.add(2)
    }

    fun m2(pulse: Long, config: MotorEntity = scheduleTask.hpm[2]!!) {
        byteList.addAll(pwc(2, pulse, config).toList())
        indexList.add(2)
    }

    fun m3(dv: Float, config: MotorEntity = scheduleTask.hpm[3]!!) {
        byteList.addAll(pwc(3, dv, config).toList())
        indexList.add(3)
    }

    fun m3(pulse: Long, config: MotorEntity = scheduleTask.hpm[3]!!) {
        byteList.addAll(pwc(3, pulse, config).toList())
        indexList.add(3)
    }

    fun m4(dv: Float, config: MotorEntity = scheduleTask.hpm[4]!!) {
        byteList.addAll(pwc(4, dv, config).toList())
        indexList.add(4)
    }

    fun m4(pulse: Long, config: MotorEntity = scheduleTask.hpm[4]!!) {
        byteList.addAll(pwc(4, pulse, config).toList())
        indexList.add(4)
    }

    fun m5(dv: Float, config: MotorEntity = scheduleTask.hpm[5]!!) {
        byteList.addAll(pwc(5, dv, config).toList())
        indexList.add(5)
    }

    fun m5(pulse: Long, config: MotorEntity = scheduleTask.hpm[5]!!) {
        byteList.addAll(pwc(5, pulse, config).toList())
        indexList.add(5)
    }

    fun m6(dv: Float, config: MotorEntity = scheduleTask.hpm[6]!!) {
        byteList.addAll(pwc(6, dv, config).toList())
        indexList.add(6)
    }

    fun m6(pulse: Long, config: MotorEntity = scheduleTask.hpm[6]!!) {
        byteList.addAll(pwc(6, pulse, config).toList())
        indexList.add(6)
    }

    fun m7(dv: Float, config: MotorEntity = scheduleTask.hpm[7]!!) {
        byteList.addAll(pwc(7, dv, config).toList())
        indexList.add(7)
    }

    fun m7(pulse: Long, config: MotorEntity = scheduleTask.hpm[7]!!) {
        byteList.addAll(pwc(7, pulse, config).toList())
        indexList.add(7)
    }

    fun m8(dv: Float, config: MotorEntity = scheduleTask.hpm[8]!!) {
        byteList.addAll(pwc(8, dv, config).toList())
        indexList.add(8)
    }

    fun m8(pulse: Long, config: MotorEntity = scheduleTask.hpm[8]!!) {
        byteList.addAll(pwc(8, pulse, config).toList())
        indexList.add(8)
    }

    fun m9(dv: Float, config: MotorEntity = scheduleTask.hpm[9]!!) {
        byteList.addAll(pwc(9, dv, config).toList())
        indexList.add(9)
    }

    fun m9(pulse: Long, config: MotorEntity = scheduleTask.hpm[9]!!) {
        byteList.addAll(pwc(9, pulse, config).toList())
        indexList.add(9)
    }

    fun m10(dv: Float, config: MotorEntity = scheduleTask.hpm[10]!!) {
        byteList.addAll(pwc(10, dv, config).toList())
        indexList.add(10)
    }

    fun m10(pulse: Long, config: MotorEntity = scheduleTask.hpm[10]!!) {
        byteList.addAll(pwc(10, pulse, config).toList())
        indexList.add(10)
    }

    fun m11(dv: Float, config: MotorEntity = scheduleTask.hpm[11]!!) {
        byteList.addAll(pwc(11, dv, config).toList())
        indexList.add(11)
    }

    fun m11(pulse: Long, config: MotorEntity = scheduleTask.hpm[11]!!) {
        byteList.addAll(pwc(11, pulse, config).toList())
        indexList.add(11)
    }

    fun m12(dv: Float, config: MotorEntity = scheduleTask.hpm[12]!!) {
        byteList.addAll(pwc(12, dv, config).toList())
        indexList.add(12)
    }

    fun m12(pulse: Long, config: MotorEntity = scheduleTask.hpm[12]!!) {
        byteList.addAll(pwc(12, pulse, config).toList())
        indexList.add(12)
    }

    fun m13(dv: Float, config: MotorEntity = scheduleTask.hpm[13]!!) {
        byteList.addAll(pwc(13, dv, config).toList())
        indexList.add(13)
    }

    fun m13(pulse: Long, config: MotorEntity = scheduleTask.hpm[13]!!) {
        byteList.addAll(pwc(13, pulse, config).toList())
        indexList.add(13)
    }

    fun m14(dv: Float, config: MotorEntity = scheduleTask.hpm[14]!!) {
        byteList.addAll(pwc(14, dv, config).toList())
        indexList.add(14)
    }

    fun m14(pulse: Long, config: MotorEntity = scheduleTask.hpm[14]!!) {
        byteList.addAll(pwc(14, pulse, config).toList())
        indexList.add(14)
    }

    fun m15(dv: Float, config: MotorEntity = scheduleTask.hpm[15]!!) {
        byteList.addAll(pwc(15, dv, config).toList())
        indexList.add(15)
    }

    fun m15(pulse: Long, config: MotorEntity = scheduleTask.hpm[15]!!) {
        byteList.addAll(pwc(15, pulse, config).toList())
        indexList.add(15)
    }
}