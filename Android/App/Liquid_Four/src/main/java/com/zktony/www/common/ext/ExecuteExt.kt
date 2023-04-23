package com.zktony.www.common.ext

import com.zktony.core.ext.toHex
import com.zktony.www.manager.MotorManager
import com.zktony.www.manager.SerialManager
import com.zktony.serialport.protocol.v1
import org.koin.java.KoinJavaComponent.inject

private val SM: SerialManager by inject(SerialManager::class.java)
private val MM: MotorManager by inject(MotorManager::class.java)


data class Step(
    var x: Float = 0f,
    var y: Float = 0f,
    var v1: Float = 0f,
    var v2: Float = 0f,
    var v3: Float = 0f,
    var v4: Float = 0f,
)

class Execute {

    private val list = mutableListOf<Step>()

    fun step(block: Step.() -> Unit) {
        list.add(Step().apply(block))
    }

    fun list(): List<Step> {
        return list
    }
}

fun execute(block: Execute.() -> Unit) {
    val list = Execute().apply(block).list()

    val axisStr = StringBuilder()
    val volumeStr = StringBuilder()
    list.forEach {
        val l1 = MM.pulse(
            listOf(it.x, it.y, it.v1, it.v2, it.v3, it.v4),
            listOf(0, 1, 2, 3, 4, 5)
        )
        axisStr.append("${l1[0]},${l1[1]},${l1[2]},")
        volumeStr.append("${l1[3]},${l1[4]},${l1[5]},")
    }

    if (list.size > 1) {
        SM.sendHex(
            index = 0,
            hex = v1 {
                fn = "05"
                pa = "04"
                data = "0101" + axisStr.toString().toHex()
            }
        )
        SM.sendHex(
            index = 3,
            hex = v1 {
                fn = "05"
                pa = "04"
                data = "0101" + volumeStr.toString().toHex()
            },
            lock = true
        )
    } else {
        SM.sendHex(
            index = 0,
            hex = v1 {
                fn = "05"
                pa = "01"
                data = "0101" + axisStr.toString().toHex()
            }
        )
        SM.sendHex(
            index = 3,
            hex = v1 {
                fn = "05"
                pa = "01"
                data = "0101" + volumeStr.toString().toHex()
            },
            lock = true
        )
    }

}


