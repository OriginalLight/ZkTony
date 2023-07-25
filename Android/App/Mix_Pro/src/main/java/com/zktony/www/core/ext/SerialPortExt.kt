package com.zktony.www.core.ext

import com.zktony.core.ext.loge
import com.zktony.serialport.ext.asciiToHex
import com.zktony.serialport.protocol.V1
import com.zktony.www.core.SerialPort
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)


data class DV(
    var y: Float = 0f,
    var z: Float = 0f,
    var v1: Float = 0f,
    var v2: Float = 0f,
    var v3: Float = 0f,
)

enum class ControlType {
    MOVE, GLUE, PRE
}

class TX {
    var controlType: ControlType = ControlType.MOVE
    var hex: String = ""
    var hex1: String = ""
    var hex2: String = ""
    var hex3: String = ""

    fun move(block: DV.() -> Unit) {
        val dv = DV().apply(block)
        controlType = ControlType.MOVE
        hex += "${pulse(dv.y, 0)},${pulse(dv.z, 1)},0,"
    }

    fun glue(block: DV.() -> Unit) {
        val dv = DV().apply(block)
        controlType = ControlType.GLUE
        val avg1 = (pulse(dv.v2, 3) + pulse(dv.v3, 4)) / 2
        val avg2 = (pulse(dv.v2, 5) + pulse(dv.v3, 6)) / 2
        val avg3 = (pulse(dv.v2, 7) + pulse(dv.v3, 8)) / 2
        hex1 = "$avg1,$avg1,${pulse(dv.v1, 2)},"
        hex2 = "$avg2,$avg2,${pulse(dv.v1, 2)},"
        hex3 = "$avg3,$avg3,${pulse(dv.v1, 2)},"
    }

    fun pre(block: DV.() -> Unit) {
        val dv = DV().apply(block)
        controlType = ControlType.PRE
        hex1 = "${pulse(dv.v2, 3)},${pulse(dv.v3, 4)},${pulse(dv.v1, 2)},"
        hex2 = "${pulse(dv.v2, 5)},${pulse(dv.v3, 6)},${pulse(dv.v1, 2)},"
        hex3 = "${pulse(dv.v2, 7)},${pulse(dv.v3, 8)},${pulse(dv.v1, 2)},"
    }
}

fun tx(block: TX.() -> Unit) {
    val tx = TX().apply(block)

    when(tx.controlType) {
        ControlType.MOVE -> {
            syncHex(0) {
                fn = "05"
                pa = "01"
                data = "0101" + tx.hex.asciiToHex()
            }
        }
        ControlType.GLUE -> {
            syncHex(1) {
                fn = "05"
                pa = "01"
                data = "0101" + tx.hex1.asciiToHex()
            }
            syncHex(2) {
                fn = "05"
                pa = "01"
                data = "0101" + tx.hex2.asciiToHex()
            }
            syncHex(3) {
                fn = "05"
                pa = "01"
                data = "0101" + tx.hex3.asciiToHex()
            }
        }

        ControlType.PRE -> {
            syncHex(1) {
                fn = "05"
                pa = "04"
                data = "0101" + tx.hex1.asciiToHex()
            }
            syncHex(2) {
                fn = "05"
                pa = "04"
                data = "0101" + tx.hex2.asciiToHex()
            }
            syncHex(3) {
                fn = "05"
                pa = "04"
                data = "0101" + tx.hex3.asciiToHex()
            }
        }
    }
}

/**
 * 收集hex回复
 * @param block (String?) -> Unit
 */
suspend fun collectHex(block: (Pair<Int, String?>) -> Unit) {
    serialPort.callback.collect {
        block(it)
    }
}


/**
 * 串口发送 lock = true
 *
 * @param index 串口号
 * @param block V1协议
 */
fun syncHex(index: Int, block: V1.() -> Unit) {
    "$index -> ${V1().apply(block).toHex()}".loge()
    serialPort.sendHex(index, V1().apply(block).toHex(), lock = true)
}

/**
 * 串口发送 lock = false
 *
 * @param index 串口号
 * @param block V1协议
 */
fun asyncHex(index: Int, block: V1.() -> Unit) {
    serialPort.sendHex(index, V1().apply(block).toHex())
}

/**
 * 串口发送 lock = true
 *
 * @param index 串口号
 * @param block V1协议
 */
suspend fun waitSyncHex(index: Int, block: V1.() -> Unit) {
    while (serialPort.lock.value) {
        delay(100L)
    }
    syncHex(index, block)
}


/**
 * 串口发送 lock = false
 *
 * @param index 串口号
 * @param block V1协议
 */
suspend fun waitAsyncHex(index: Int, block: V1.() -> Unit) {
    while (serialPort.lock.value) {
        delay(100L)
    }
    asyncHex(index, block)
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