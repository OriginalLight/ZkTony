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
    var z: Float = 0f,
    var v1: Float = 0f,
    var v2: Float = 0f,
    var v3: Float = 0f,
    var v4: Float = 0f,
    var v5: Float = 0f,
    var v6: Float = 0f,
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

    val s1 = StringBuilder()
    val s2 = StringBuilder()
    val s3 = StringBuilder()
    list.forEach {
        val l1 = MM.pulse(
            listOf(it.x, it.y, it.z, it.v1, it.v2, it.v3, it.v4, it.v5, it.v6),
            listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
        )
        s1.append("${l1[0]},${l1[1]},${l1[2]},")
        s2.append("${l1[3]},${l1[4]},${l1[5]},")
        s3.append("${l1[6]},${l1[7]},${l1[8]},")
    }

    SM.sendHex(
        index = 0,
        hex = v1 {
            fn = "05"
            pa = "04"
            data = "0101" + s1.toString().toHex()
        }
    )
    SM.sendHex(
        index = 1,
        hex = v1 {
            fn = "05"
            pa = "04"
            data = "0101" + s2.toString().toHex()
        }
    )
    SM.sendHex(
        index = 2,
        hex = v1 {
            fn = "05"
            pa = "04"
            data = "0101" + s3.toString().toHex()
        },
        lock = true
    )
}


