package com.zktony.android.utils.internal

import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.AppStateUtils
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8

/**
 * @author 刘贺贺
 * @date 2023/9/15 9:17
 */
class GlueBuilder {
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()

    var exceptionPolicy: Int = ExceptionPolicy.SKIP
    var timeOut: Long = 1000L * 10

    fun with(
        index: Int = 0,
        pdv: Long,
        ads: Triple<Long, Long, Long>? = null
    ) {
        if (pdv != 0L) {
            val ba1 = ByteArray(5)
            val ba2 = ByteArray(12)
            ba1.writeInt8(index, 0).writeInt32LE(pdv, 1)
            if (ads == null) {
                val motor = AppStateUtils.hpm[index] ?: Motor()
                ba2.writeInt32LE(motor.acceleration, 0).writeInt32LE(motor.deceleration, 4)
                    .writeInt32LE(motor.speed, 8)
            } else {
                ba2.writeInt32LE(ads.first, 0).writeInt32LE(ads.second, 4)
                    .writeInt32LE(ads.third, 8)
            }
            byteList.addAll(ba1.toList())
            byteList.addAll(ba2.toList())
            indexList.add(index)
        }
    }
}