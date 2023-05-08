package com.zktony.www.common.ext

import com.zktony.serialport.ext.asciiToHex
import com.zktony.serialport.protocol.V1
import com.zktony.www.proxy.*
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

private val containerProxy: ContainerProxy by inject(ContainerProxy::class.java)
private val serialProxy: SerialProxy by inject(SerialProxy::class.java)
private val workerProxy: WorkerProxy by inject(WorkerProxy::class.java)
private val mcProxy: MCProxy by inject(MCProxy::class.java)

// region Common
/**
 *初始化
 */
fun proxyInitializer() {
    containerProxy.initializer()
    serialProxy.initializer()
    workerProxy.initializer()
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

/**
 * 脉冲
 *
 * @param dv List<Float>
 * @param type List<Int>
 * @return List<Int>
 */
fun pulse(dv: List<Float>, type: List<Int>): List<Int> {
    val list = mutableListOf<Int>()
    for (i in dv.indices) {
        list.add(pulse(dv[i], type[i]))
    }
    return list
}

// endregion

// region SerialProxy
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

    val s1 = StringBuilder()
    val s2 = StringBuilder()
    val s3 = StringBuilder()
    list.forEach {
        val l1 = pulse(
            listOf(it.x, it.y, it.z, it.v1, it.v2, it.v3, it.v4, it.v5, it.v6),
            listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
        )
        s1.append("${l1[0]},${l1[1]},${l1[2]},")
        s2.append("${l1[3]},${l1[4]},${l1[5]},")
        s3.append("${l1[6]},${l1[7]},${l1[8]},")
    }

    asyncHex(0) {
        fn = "05"
        pa = "04"
        data = "0101" + s1.toString().asciiToHex()
    }

    asyncHex(1) {
        fn = "05"
        pa = "04"
        data = "0101" + s2.toString().asciiToHex()
    }

    syncHex(2) {
        fn = "05"
        pa = "04"
        data = "0101" + s3.toString().asciiToHex()
    }
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
 * 串口发送 text
 */
fun asyncText(text: String) = serialProxy.sendText(text)

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
suspend fun waitLock(interval: Long = 500L, block: suspend () -> Unit) {
    while (serialProxy.lock.value) {
        delay(interval)
    }
    block()
}

suspend fun waitDrawer(timerTask: () -> Unit, block: suspend () -> Unit) {
    while (serialProxy.drawer.get()) {
        timerTask()
        delay(1000L)
    }
    block()
}

suspend fun temp(temp: String, addr: Int) {
    asyncText("TC1:TCSW=0@$addr\r")
    delay(30 * 1000L)
    asyncText("TC1:TCSW=1@$addr\r")
    delay(1000L)
    asyncText("TC1:TCADJUSTTEMP=$temp@$addr\r")
}

// endregion