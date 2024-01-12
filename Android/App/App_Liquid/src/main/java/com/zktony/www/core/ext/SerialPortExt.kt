package com.zktony.www.core.ext

import com.zktony.serialport.ext.asciiToHex
import com.zktony.serialport.protocol.V1
import com.zktony.www.core.SerialPort
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)


data class Step(
    var x: Float = 0f,
    var y: Float = 0f,
    var v1: Float = 0f,
    var v2: Float = 0f,
    var v3: Float = 0f,
    var v4: Float = 0f,
)

class YesNo {
    var yes: suspend () -> Unit = {}
    var no: suspend () -> Unit = {}

    fun yes(block: suspend () -> Unit) {
        yes = block
    }

    fun no(block: suspend () -> Unit) {
        no = block
    }
}

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
        val l1 = pulse(
            listOf(it.x, it.y, it.v1, it.v2, it.v3, it.v4),
            listOf(0, 1, 2, 3, 4, 5)
        )
        axisStr.append("${l1[0]},${l1[1]},${l1[2]},")
        volumeStr.append("${l1[3]},${l1[4]},${l1[5]},")
    }

    asyncHex(0) {
        fn = "05"
        pa = "04"
        data = "0101" + axisStr.toString().asciiToHex()
    }

    syncHex(3) {
        fn = "05"
        pa = "04"
        data = "0101" + volumeStr.toString().asciiToHex()
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
 * 判定是否有锁
 * @param block YesNo
 */
suspend fun decideLock(block: YesNo.() -> Unit) {
    val yesNo = YesNo().apply(block)
    if (serialPort.lock.value) {
        yesNo.yes()
    } else {
        yesNo.no()
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