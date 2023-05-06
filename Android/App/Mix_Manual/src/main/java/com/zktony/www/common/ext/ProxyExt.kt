package com.zktony.www.common.ext

import com.zktony.serialport.ext.asciiToHex
import com.zktony.serialport.protocol.V1
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

/**
 * 脉冲
 *
 * @param dv Float
 * @param type Int
 * @return Int
 */
fun pulse(dv: Float, type: Int): Int {
    val me = mcProxy.hpm[type] ?: Motor()
    val ce = mcProxy.hpc[type] ?: 200f
    return me.pulseCount(dv, ce)
}

// endregion

// region SerialProxy
data class Step(
    var v1: Float = 0f,
    var v2: Float = 0f,
    var v3: Float = 0f,
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
        s1.append("${pulse(it.v1, 0)},${pulse(it.v2, 1)},${pulse(it.v3, 2)},")
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
    serialProxy.callback.collect {
        block(it)
    }
}

/**
 * 串口发送 lock = true
 *
 * @param block V1协议
 */
fun syncHex(block: V1.() -> Unit) {
    serialProxy.sendHex(V1().apply(block).toHex(), lock = true)
}

/**
 * 串口发送 lock = false
 *
 * @param block V1协议
 */
fun asyncHex(block: V1.() -> Unit) {
    serialProxy.sendHex(V1().apply(block).toHex())
}

/**
 * 串口发送 lock = true
 *
 * @param block V1协议
 */
suspend fun waitSyncHex(block: V1.() -> Unit) {
    while (serialProxy.lock.value) {
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
    while (serialProxy.lock.value) {
        delay(100L)
    }
    asyncHex(block)
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
suspend fun waitLock(block: () -> Unit) {
    while (serialProxy.lock.value) {
        delay(100L)
    }
    block()
}

// endregion