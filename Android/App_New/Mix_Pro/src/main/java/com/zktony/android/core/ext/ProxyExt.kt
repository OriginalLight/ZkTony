package com.zktony.android.core.ext

import com.zktony.android.core.proxy.MCProxy
import com.zktony.android.core.proxy.SerialProxy
import com.zktony.android.data.entity.Motor
import com.zktony.serialport.ext.intToHex
import com.zktony.serialport.protocol.v2
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.atomic.AtomicInteger

private val mcProxy: MCProxy by inject(MCProxy::class.java)
private val serialProxy: SerialProxy by inject(SerialProxy::class.java)

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

// x轴坐标
private var x: AtomicInteger = AtomicInteger(0)
private var y: AtomicInteger = AtomicInteger(0)
private var z: AtomicInteger = AtomicInteger(0)

/**
 * 脉冲
 *
 * @param index Int
 * @param dv Int
 * @return Int
 */
fun pulse(index: Int, dv: Float): Int {
    val p = (dv / mcProxy.hpc[index]!! * 3200).toInt()
    return when (index) {
        0 -> {
            val d = p - x.get()
            x.set(maxOf(p, 0))
            d
        }

        1 -> {
            val d = p - y.get()
            y.set(maxOf(p, 0))
            d
        }

        2 -> {
            val d = p - z.get()
            z.set(maxOf(p, 0))
            d
        }

        else -> p
    }
}

/**
 * pulse with config
 *
 * @param index Int
 * @param dv Int
 * @return String
 */
fun pwc(index: Int, dv: Float, config: Motor): String {
    return index.intToHex() + pulse(index, dv).intToHex(4) + config.hex()
}

// endregion

// region SerialProxy

/**
 * 同步发送Hex
 *
 * 一发一收算作完成
 *
 * @param hex String
 */
fun sendHex(hex: String) {
    serialProxy.sendHex(hex)
}

/**
 * 组合类
 *
 * 组合类用于组合多个指令
 *
 * @property list MutableList<Pair<Int, Float>>
 */
class Compose {
    val list = mutableListOf<Triple<Int, Float, Motor>>()

    fun x(dv: Float, config: Motor = mcProxy.hpm[0]!!) {
        list.add(Triple(0, dv, config))
    }

    fun y(dv: Float, config: Motor = mcProxy.hpm[1]!!) {
        list.add(Triple(1, dv, config))
    }

    fun z(dv: Float, config: Motor = mcProxy.hpm[2]!!) {
        list.add(Triple(2, dv, config))
    }

    fun v1(dv: Float, config: Motor = mcProxy.hpm[3]!!) {
        list.add(Triple(3, dv, config))
    }

    fun v2(dv: Float, config: Motor = mcProxy.hpm[4]!!) {
        list.add(Triple(4, dv, config))
    }

    fun v3(dv: Float, config: Motor = mcProxy.hpm[5]!!) {
        list.add(Triple(5, dv, config))
    }

    fun v4(dv: Float, config: Motor = mcProxy.hpm[6]!!) {
        list.add(Triple(6, dv, config))
    }

    fun v5(dv: Float, config: Motor = mcProxy.hpm[7]!!) {
        list.add(Triple(7, dv, config))
    }

    fun v6(dv: Float, config: Motor = mcProxy.hpm[8]!!) {
        list.add(Triple(8, dv, config))
    }

    fun v7(dv: Float, config: Motor = mcProxy.hpm[9]!!) {
        list.add(Triple(9, dv, config))
    }

    fun v8(dv: Float, config: Motor = mcProxy.hpm[10]!!) {
        list.add(Triple(10, dv, config))
    }

    fun v9(dv: Float, config: Motor = mcProxy.hpm[11]!!) {
        list.add(Triple(11, dv, config))
    }

    fun v10(dv: Float, config: Motor = mcProxy.hpm[12]!!) {
        list.add(Triple(12, dv, config))
    }

    fun v11(dv: Float, config: Motor = mcProxy.hpm[13]!!) {
        list.add(Triple(13, dv, config))
    }

    fun v12(dv: Float, config: Motor = mcProxy.hpm[14]!!) {
        list.add(Triple(14, dv, config))
    }

    fun v13(dv: Float, config: Motor = mcProxy.hpm[15]!!) {
        list.add(Triple(15, dv, config))
    }
}

/**
 * 组合发送
 *
 * 同步发送 一发一收算作完成
 *
 * @param block Compose.() -> Unit
 */
suspend fun syncHex(block: Compose.() -> Unit, timeOut: Long = 1000) {
    val compose = Compose().apply(block)
    val list = compose.list
    val hex = list.joinToString("") {
        pwc(it.first, it.second, it.third)
    }
    try {
        withTimeout(timeOut) {
            sendHex(v2 { data = hex })
            delay(100L)
            while (getLock(list.map { it.first })) {
                delay(100L)
            }
        }
    } catch (e: Exception) {
        freeLock(list.map { it.first })
    }
}

/**
 * 组合发送
 *
 * 异步发送 不管之前是否完成
 *
 * @param block Compose.() -> Unit
 */
fun asyncHex(block: Compose.() -> Unit) {
    val compose = Compose().apply(block)
    val list = compose.list
    val hex = list.joinToString("") {
        pwc(it.first, it.second, it.third)
    }
    sendHex(v2 { data = hex })
}

/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(list: List<Int>): Boolean {
    var lock = false
    list.forEach {
        lock = lock || serialProxy.map[it]!! != 0
    }
    return lock
}

/**
 * 释放锁
 *
 * @param list List<Int>
 */
fun freeLock(list: List<Int>) {
    list.forEach {
        serialProxy.map[it] = 0
    }
}

/**
 * 收集hex回复
 * @param block (String) -> Unit
 */
suspend fun collectHex(block: (String) -> Unit) {
    serialProxy.callback.collect {
        it?.let { block(it) }
    }
}
// endregion