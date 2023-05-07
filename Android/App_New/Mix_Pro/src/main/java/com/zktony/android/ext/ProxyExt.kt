package com.zktony.android.ext

import com.zktony.android.proxy.MCProxy
import com.zktony.android.proxy.SerialProxy
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
fun pwc(index: Int, dv: Float): String {
    return index.intToHex() + pulse(index, dv).intToHex(4) + mcProxy.hpm[index]!!.hex()
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
    val list = mutableListOf<Pair<Int, Float>>()

    fun x(dv: Float) {
        list.add(Pair(0, dv))
    }

    fun y(dv: Float) {
        list.add(Pair(1, dv))
    }

    fun z(dv: Float) {
        list.add(Pair(2, dv))
    }

    fun v1(dv: Float) {
        list.add(Pair(3, dv))
    }

    fun v2(dv: Float) {
        list.add(Pair(4, dv))
    }

    fun v3(dv: Float) {
        list.add(Pair(5, dv))
    }

    fun v4(dv: Float) {
        list.add(Pair(6, dv))
    }

    fun v5(dv: Float) {
        list.add(Pair(7, dv))
    }

    fun v6(dv: Float) {
        list.add(Pair(8, dv))
    }

    fun v7(dv: Float) {
        list.add(Pair(9, dv))
    }

    fun v8(dv: Float) {
        list.add(Pair(10, dv))
    }

    fun v9(dv: Float) {
        list.add(Pair(11, dv))
    }

    fun v10(dv: Float) {
        list.add(Pair(12, dv))
    }

    fun v11(dv: Float) {
        list.add(Pair(13, dv))
    }

    fun v12(dv: Float) {
        list.add(Pair(14, dv))
    }

    fun v13(dv: Float) {
        list.add(Pair(15, dv))
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
        pwc(it.first, it.second)
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
        pwc(it.first, it.second)
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