package com.zktony.www.common.ext

import com.zktony.core.ext.toHex
import com.zktony.www.manager.MotorManager
import com.zktony.www.manager.SerialManager
import com.zktony.serialport.protocol.v1
import org.koin.java.KoinJavaComponent.inject

private val SM: SerialManager by inject(SerialManager::class.java)
private val MM: MotorManager by inject(MotorManager::class.java)


data class Step(
    var v1: Float = 0f,
    var v2: Float = 0f,
    var v3: Float = 0f,
)

class Execute {

    private val list = mutableListOf<Step>()
    private var type = 1

    fun type(type: Int) {
        this.type = type
    }

    fun step(block: Step.() -> Unit) {
        list.add(Step().apply(block))
    }

    fun list(): List<Step> {
        return list
    }

    fun type(): Int {
        return type
    }
}

fun execute(block: Execute.() -> Unit) {
    val list = Execute().apply(block).list()
    val type = Execute().apply(block).type()
    val s1 = StringBuilder()
    list.forEach {
        s1.append("${MM.pulse(it.v1, 0)},${MM.pulse(it.v2, 1)},${MM.pulse(it.v3, 2)},")
    }

    if (type == 1) {
        SM.sendHex(
            hex = v1 {
                fn = "05"
                pa = "01"
                data = "0101" + s1.toString().toHex()
            },
            lock = true
        )
    } else {
        SM.sendHex(
            hex = v1 {
                fn = "05"
                pa = "04"
                data = "0101" + s1.toString().toHex()
            },
            lock = true
        )
    }
}




