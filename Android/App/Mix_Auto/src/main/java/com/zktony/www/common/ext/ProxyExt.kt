package com.zktony.www.common.ext

import com.zktony.serialport.ext.asciiToHex
import com.zktony.serialport.protocol.V1
import com.zktony.serialport.protocol.v1
import com.zktony.www.proxy.MCProxy
import com.zktony.www.proxy.SerialProxy
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

private val serialProxy: SerialProxy by inject(SerialProxy::class.java)
private val mcProxy: MCProxy by inject(MCProxy::class.java)

// region Common

/**
 * 初始化
 */
fun proxyInitializer() {
    serialProxy.initializer()
    mcProxy.initializer()
}

// endregion

// region MCProxy
fun pulse(dv: Float, type: Int): Int {
    val me = mcProxy.hpm[type] ?: Motor()
    val ce = mcProxy.hpc[type] ?: 200f
    return me.pulseCount(dv, ce)
}
// endregion

// region SerialProxy
data class Step(
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
        axisStr.append("0,${pulse(it.y, 0)},0,0,")
        volumeStr.append("${pulse(it.v1, 1)},${pulse(it.v2, 2)},${pulse(it.v3, 3)},")
    }

    serialProxy.sendHex(
        index = 0,
        hex = v1 {
            fn = "05"
            pa = "04"
            data = "0101" + axisStr.toString().asciiToHex()
        }
    )
    serialProxy.sendHex(
        index = 3,
        hex = v1 {
            fn = "05"
            pa = "04"
            data = "0101" + volumeStr.toString().asciiToHex()
        },
        lock = true
    )

}

/**
 * 收集hex回复
 * @param block (String?) -> Unit
 */
suspend fun collectHex(block: (Pair<Int, String?>) -> Unit) {
    serialProxy.callback.collect {
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
    serialProxy.sendHex(index, V1().apply(block).toHex(), lock = true)
}

/**
 * 串口发送 lock = false
 *
 * @param index 串口号
 * @param block V1协议
 */
fun asyncHex(index: Int, block: V1.() -> Unit) {
    serialProxy.sendHex(index, V1().apply(block).toHex())
}

/**
 * 串口发送 lock = true
 *
 * @param index 串口号
 * @param block V1协议
 */
suspend fun waitSyncHex(index: Int, block: V1.() -> Unit) {
    while (serialProxy.lock.value) {
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
    while (serialProxy.lock.value) {
        delay(100L)
    }
    asyncHex(index, block)
}

/**
 * 收集锁
 * @param block Boolean
 */
suspend fun collectLock(block: (Boolean) -> Unit) {
    serialProxy.lock.collect {
        block(it)
    }
}

/**
 * 判定是否有锁
 * @param block YesNo
 */
suspend fun decideLock(block: YesNo.() -> Unit) {
    val yesNo = YesNo().apply(block)
    if (serialProxy.lock.value) {
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
    while (serialProxy.lock.value) {
        delay(100L)
    }
    block()
}
// endregion


