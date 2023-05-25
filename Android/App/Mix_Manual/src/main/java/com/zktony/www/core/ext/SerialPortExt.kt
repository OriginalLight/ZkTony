package com.zktony.www.core.ext

import com.zktony.serialport.ext.asciiToHex
import com.zktony.serialport.protocol.V1
import com.zktony.www.core.SerialPort
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)


data class DV(
    var v1: Float = 0f,
    var v2: Float = 0f,
    var v3: Float = 0f,
)

class Execute {

    private val list = mutableListOf<DV>()
    private var mode = false
    private var type = 1

    fun type(type: Int) {
        this.type = type
    }

    fun dv(block: DV.() -> Unit) {
        list.add(DV().apply(block))
    }

    fun mode(mode: Boolean) {
        this.mode = mode
    }

    fun list(): List<DV> {
        return list
    }

    fun type(): Int {
        return type
    }

    fun mode(): Boolean {
        return mode
    }
}

fun execute(block: Execute.() -> Unit) {
    val list = Execute().apply(block).list()
    val type = Execute().apply(block).type()
    val mode = Execute().apply(block).mode()
    val s1 = StringBuilder()
    list.forEach {
        val p1 = pulse(it.v1, 0)
        val p2 = pulse(it.v2, 1)
        val p3 = pulse(it.v3, 2)

        if (type == 0) {
            s1.append("$p1,$p2,$p3,")
        } else {
            val avg = (p1 + p2) / 2
            if (mode) {
                s1.append("${avg - 1},${avg + 1},$p3,")
            } else {
                s1.append("${avg + 1},${avg - 1},$p3,")
            }
        }
    }

    if (type == 1) {
        syncHex {
            fn = "05"
            pa = "01"
            data = "0101" + s1.toString().asciiToHex()
        }
    } else {
        syncHex {
            fn = "05"
            pa = "04"
            data = "0101" + s1.toString().asciiToHex()
        }
    }
}

/**
 * 收集hex回复
 * @param block (String?) -> Unit
 */
suspend fun collectHex(block: (String?) -> Unit) {
    serialPort.callback.collect {
        block(it)
    }
}

/**
 * 串口发送 lock = true
 *
 * @param block V1协议
 */
fun syncHex(block: V1.() -> Unit) {
    serialPort.sendHex(V1().apply(block).toHex(), lock = true)
}

/**
 * 串口发送 lock = false
 *
 * @param block V1协议
 */
fun asyncHex(block: V1.() -> Unit) {
    serialPort.sendHex(V1().apply(block).toHex())
}

/**
 * 串口发送 lock = true
 *
 * @param block V1协议
 */
suspend fun waitSyncHex(block: V1.() -> Unit) {
    while (serialPort.lock.value) {
        delay(100L)
    }
    syncHex(block)
}


/**
 * 串口发送 lock = false
 *
 * @param block V1协议
 */
suspend fun waitAsyncHex(block: V1.() -> Unit) {
    while (serialPort.lock.value) {
        delay(100L)
    }
    asyncHex(block)
}

/**
 * 收集锁
 * @param block Boolean
 */
suspend fun collectLock(block: (Boolean) -> Unit) {
    serialPort.lock.collect {
        block(it)
    }
}

/**
 * 等待解锁
 * @param block () -> Unit
 */
suspend fun waitLock(block: suspend () -> Unit) {
    while (serialPort.lock.value) {
        delay(100L)
    }
    block()
}

/**
 * waitTime
 *
 * @param time Long
 * @return Unit
 */
fun waitTime(time: Long) {
    serialPort.setWaitTime(time)
}