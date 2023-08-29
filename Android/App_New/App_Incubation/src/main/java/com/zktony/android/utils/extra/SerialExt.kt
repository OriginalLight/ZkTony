package com.zktony.android.utils.extra

import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.extra.internal.AppStateObserver
import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.modbus.toRtuProtocol
import com.zktony.serialport.command.runze.toRunzeProtocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.checkSumLE
import com.zktony.serialport.ext.crc16LE
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

val x: AtomicLong = AtomicLong(0L)
val y: AtomicLong = AtomicLong(0L)


/**
 * 轴状态
 */
val hpa: MutableMap<Int, Boolean> = ConcurrentHashMap<Int, Boolean>().apply {
    repeat(16) { put(it, false) }
}


/**
 * 电机信息
 */
val hpm: MutableMap<Int, Motor> = ConcurrentHashMap<Int, Motor>().apply {
    repeat(16) { put(it, Motor()) }
}

/**
 * 校准信息
 */
val hpc: MutableMap<Int, Double> = ConcurrentHashMap<Int, Double>().apply {
    repeat(16) { put(it, 0.01) }
}

/**
 * 串口通信
 */
val serialHelper = object : AbstractSerialHelper(SerialConfig(device = "/dev/ttyS3")) {
    override fun callbackVerify(byteArray: ByteArray, block: (ByteArray) -> Unit) {

        if (byteArray[0] == 0xCC.toByte()) {
            // checksum 校验
            val crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
            val bytes = byteArray.copyOfRange(0, byteArray.size - 2)
            if (!bytes.checkSumLE().contentEquals(crc)) {
                throw Exception("RX Crc Error")
            }
        } else {
            // crc 校验
            val crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
            val bytes = byteArray.copyOfRange(0, byteArray.size - 2)
            if (!bytes.crc16LE().contentEquals(crc)) {
                throw Exception("RX Crc Error")
            }
        }

        // 校验通过
        block(byteArray)
    }

    override fun callbackProcess(byteArray: ByteArray) {
        // 解析协议
        if (byteArray[0] == 0xCC.toByte()) {
            val rx = byteArray.toRunzeProtocol()
            when (rx.funcCode) {
                0xFE.toByte() -> {
                    // 任务挂起
                }

                else -> {}
            }
        } else {
            val rx = byteArray.toRtuProtocol()
            when (rx.funcCode) {
                // TODO
            }
        }
    }
}

/**
 * 应用状态观察者
 */
val observer = object : AppStateObserver() {
    override fun callbackOne(list: List<Motor>) {
        list.forEach {
            hpm[it.index] = it
        }
    }

    override fun callbackTwo(list: List<Double>) {
        list.forEachIndexed { index, d ->
            hpc[index] = d
        }
    }
}

/**
 * 脉冲转换
 *
 * @param index Int
 * @param dvp T
 * @return Long
 */
fun <T : Number> pulse(index: Int, dvp: T): Long {
    return when (dvp) {
        is Double -> (dvp / hpc[index]!!).toLong()
        is Long -> dvp
        else -> dvp.toLong()
    }
}