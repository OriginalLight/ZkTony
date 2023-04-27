package com.zktony.www.common.ext

import com.zktony.core.ext.toHex
import com.zktony.serialport.protocol.V1
import com.zktony.www.manager.MotorManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

private val SM: SerialManager by inject(SerialManager::class.java)
private val MM: MotorManager by inject(MotorManager::class.java)


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
        s1.append("${MM.pulse(it.v1, 0)},${MM.pulse(it.v2, 1)},${MM.pulse(it.v3, 2)},")
    }

    if (type == 1) {
        syncHex {
            fn = "05"
            pa = "01"
            data = "0101" + s1.toString().toHex()
        }
    } else {
        syncHex {
            fn = "05"
            pa = "04"
            data = "0101" + s1.toString().toHex()
        }
    }
}

/**
 * 收集hex回复
 * @param block (String?) -> Unit
 */
suspend fun collectHex(block: (String?) -> Unit) {
    SM.callback.collect {
        block(it)
    }
}

/**
 * 串口发送 lock = true
 *
 * @param block V1协议
 */
fun syncHex(block: V1.() -> Unit) {
    SM.sendHex(V1().apply(block).toHex(), lock = true)
}

/**
 * 串口发送 lock = false
 *
 * @param block V1协议
 */
fun asyncHex(block: V1.() -> Unit) {
    SM.sendHex(V1().apply(block).toHex())
}

/**
 * 串口发送 lock = true
 *
 * @param block V1协议
 */
suspend fun waitSyncHex(block: V1.() -> Unit) {
    while (SM.lock.value) {
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
    while (SM.lock.value) {
        delay(100L)
    }
    asyncHex(block)
}

/**
 * 收集锁
 * @param block Boolean
 */
suspend fun collectLock(block: (Boolean) -> Unit) {
    SM.lock.collect {
        block(it)
    }
}

/**
 * 判定是否有锁
 * @param block YesNo
 */
suspend fun decideLock(block: YesNo.() -> Unit) {
    val yesNo = YesNo().apply(block)
    if (SM.lock.value) {
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
    while (SM.lock.value) {
        delay(100L)
    }
    block()
}





