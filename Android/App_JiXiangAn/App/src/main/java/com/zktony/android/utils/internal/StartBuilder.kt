package com.zktony.android.utils.internal

import android.util.Log
import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8

/**
 * @author 刘贺贺
 * @date 2023/9/15 9:17
 */
class StartBuilder {
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()

    var executeType: Int = ExecuteType.SYNC
    var exceptionPolicy: Int = ExceptionPolicy.SKIP
    var timeOut: Long = 1000L * 10

    fun <T : Number> with(
        index: Int = 0,
        pdv: T,
        ads: Triple<Long, Long, Long>? = null
    ) {
        val step = SerialPortUtils.pulse(index, pdv)
//        Log.d("", "index===$index====step===$step")
        if (step != 0L) {
            val ba1 = ByteArray(5)
            val ba2 = ByteArray(12)
            ba1.writeInt8(index, 0).writeInt32LE(step, 1)
            if (ads == null) {
                val motor = AppStateUtils.hpm[index] ?: Motor(displayText = "None")
                ba2.writeInt32LE(motor.acceleration, 0)
                    .writeInt32LE(motor.deceleration, 4)
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


//    fun <T : Number> with(
//        reset: Boolean,
//        index: Int = 0,
//        pdv: T,
//        ads: Triple<Long, Long, Long>? = null,
//        map: HashMap<String, Float>,
//    ) {
//        //校准步数
////        var step = SerialPortUtils.pulseNew(reset, index, pdv, map)
//        var  step = pdv.toLong()
//        if (step != 0L) {
//            val ba1 = ByteArray(5)
//            val ba2 = ByteArray(12)
//            ba1.writeInt8(index, 0).writeInt32LE(step, 1)
//            if (ads == null) {
//                val motor = AppStateUtils.hpm[index] ?: Motor(displayText = "None")
//                ba2.writeInt32LE(motor.acceleration * 20, 0)
//                    .writeInt32LE(motor.deceleration * 20, 4)
//                    .writeInt32LE(motor.speed * 20, 8)
//            } else {
//                ba2.writeInt32LE(ads.first * 20, 0).writeInt32LE(ads.second * 20, 4)
//                    .writeInt32LE(ads.third * 20, 8)
//            }
//            byteList.addAll(ba1.toList())
//            byteList.addAll(ba2.toList())
//            indexList.add(index)
//        }
//    }

}