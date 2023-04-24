package com.zktony.www.common.ext

import com.zktony.core.ext.toHex
import com.zktony.serialport.protocol.v1
import com.zktony.www.manager.MotorManager
import com.zktony.www.manager.SerialManager
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

    val str = StringBuilder()
    list.forEach {
        val l1 = MM.pulse(
            listOf(it.x, it.y, it.v1),
            listOf(0, 1, 2)
        )
        str.append("${l1[0]},${l1[1]},${l1[2]},")
    }

    if (list.size > 1) {
        SM.sendHex(
            hex = v1 {
                fn = "05"
                pa = "04"
                data = "0101" + str.toString().toHex()
            },
            lock = true
        )
    } else {
        SM.sendHex(
            hex = v1 {
                fn = "05"
                pa = "01"
                data = "0101" + str.toString().toHex()
            },
            lock = true
        )
    }
}


