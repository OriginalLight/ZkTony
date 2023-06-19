package com.zktony.www.core.ext

import com.zktony.serialport.ext.asciiToHex
import com.zktony.serialport.protocol.V1
import com.zktony.www.core.SerialPort
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)


data class DV(
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

data class Pulse(
    var x: Int = 0,
    var y: Int = 0,
    var z: Int = 0,
    var v1: Int = 0,
    var v2: Int = 0,
    var v3: Int = 0,
    var v4: Int = 0,
    var v5: Int = 0,
    var v6: Int = 0,
)

class Execute {

    var pulse: Triple<String, String, String> = Triple("", "", "")

    fun dv(block: DV.() -> Unit) {
        val dv = DV().apply(block)
        pulse = Triple(
            first = pulse.first + "${pulse(dv.x, 0)},${pulse(dv.y, 1)},${pulse(dv.z, 2)},",
            second = pulse.second + "${pulse(dv.v1, 3)},${pulse(dv.v2, 4)},${pulse(dv.v3, 5)},",
            third = pulse.third + "${pulse(dv.v4, 6)},${pulse(dv.v5, 7)},${pulse(dv.v6, 8)},"
        )
    }

    fun pulse(block: Pulse.() -> Unit) {
        val p = Pulse().apply(block)
        pulse = Triple(
            first = pulse.first + "${p.x},${p.y},${p.z},",
            second = pulse.second + "${p.v1},${p.v2},${p.v3},",
            third = pulse.third + "${p.v4},${p.v5},${p.v6},"
        )
    }
}

fun execute(block: Execute.() -> Unit) {
    val pulse = Execute().apply(block).pulse

    asyncHex(0) {
        fn = "05"
        pa = "04"
        data = "0101" + pulse.first.asciiToHex()
    }

    asyncHex(1) {
        fn = "05"
        pa = "04"
        data = "0101" + pulse.second.asciiToHex()
    }

    syncHex(2) {
        fn = "05"
        pa = "04"
        data = "0101" + pulse.third.asciiToHex()
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
 * 串口发送 text
 */
fun asyncText(text: String) = serialPort.sendText(text)

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
suspend fun waitLock(interval: Long = 500L, block: suspend () -> Unit) {
    while (serialPort.lock.value) {
        delay(interval)
    }
    block()
}

suspend fun waitDrawer(timerTask: () -> Unit, block: suspend () -> Unit) {
    while (serialPort.drawer.get()) {
        timerTask()
        delay(1000L)
    }
    block()
}

fun temp(temp: String, addr: Int) {
    asyncText("TC1:TCADJUSTTEMP=$temp@$addr\r")
}