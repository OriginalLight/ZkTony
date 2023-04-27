package com.zktony.www.common.ext

import com.zktony.core.ext.toHex
import com.zktony.serialport.protocol.V1
import com.zktony.serialport.protocol.v1
import com.zktony.www.manager.MotorManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

/**
 * 收集hex回复
 * @param block (String?) -> Unit
 */
suspend fun collectHex(block: (Pair<Int, String?>) -> Unit) {
    SM.callback.collect {
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
    SM.sendHex(index, V1().apply(block).toHex(), lock = true)
}

/**
 * 串口发送 lock = false
 *
 * @param index 串口号
 * @param block V1协议
 */
fun asyncHex(index: Int, block: V1.() -> Unit) {
    SM.sendHex(index, V1().apply(block).toHex())
}

/**
 * 串口发送 text
 */
fun asyncText(text: String) = SM.sendText(text)

/**
 * 串口发送 lock = true
 *
 * @param index 串口号
 * @param block V1协议
 */
suspend fun waitSyncHex(index: Int, block: V1.() -> Unit) {
    while (SM.lock.value) {
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
    while (SM.lock.value) {
        delay(100L)
    }
    asyncHex(index, block)
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
suspend fun waitLock(interval: Long = 500L, block: suspend () -> Unit) {
    while (SM.lock.value) {
        delay(interval)
    }
    block()
}

suspend fun waitDrawer(timerTask: () -> Unit, block: suspend () -> Unit) {
    while (SM.drawer.get()) {
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



