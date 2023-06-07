package com.zktony.android.logic.ext

import com.zktony.android.logic.SerialPort
import com.zktony.android.logic.utils.DV
import com.zktony.android.logic.utils.ExceptionStrategy
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.writeInt8
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)

/**
 * 发送命令
 *
 * @param byteArray ByteArray
 * @return Unit
 */
fun sendByteArray(byteArray: ByteArray) {
    serialPort.sendByteArray(byteArray)
}

/**
 * 发送命令
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<Protocol, Unit>
 * @return Unit
 */
fun sendProtocol(block: Protocol.() -> Unit) {
    serialPort.sendProtocol(Protocol().apply(block))
}

/**
 * 发送命令
 *
 * @param hex String
 * @return Unit
 */
fun sendHexString(hex: String) {
    serialPort.sendHexString(hex)
}

/**
 * 发送命令
 *
 * @param ascii String
 * @return Unit
 */
fun sendAsciiString(ascii: String) {
    serialPort.sendAsciiString(ascii)
}

/**
 * 组合发送
 *
 * 同步发送 一发一收算作完成
 *
 * @param timeOut Long
 * @param strategy ExceptionStrategy
 * @param block [@kotlin.ExtensionFunctionType] Function1<DV, Unit>
 * @return Unit
 */
suspend fun syncTransmit(
    timeOut: Long = 5000L,
    strategy: ExceptionStrategy = ExceptionStrategy.SKIP,
    block: DV.() -> Unit
) {
    val dv = DV().apply(block)
    try {
        setLock(dv.indexList)
        withTimeout(timeOut) {
            sendProtocol {
                data = dv.byteList.toByteArray()
            }
            delay(100L)
            while (getLock(dv.indexList)) {
                delay(100L)
            }
        }
    } catch (e: Exception) {
        when (strategy) {
            ExceptionStrategy.RETRY -> syncTransmit(timeOut, strategy, block)
            ExceptionStrategy.QUERY -> queryLock(dv.indexList)
            ExceptionStrategy.SKIP -> freeLock(dv.indexList)
            ExceptionStrategy.RESET -> resetLowPower()
            ExceptionStrategy.THROW -> throw e
        }
    }
}

/**
 * 组合发送
 *
 * 异步发送 不管之前是否完成
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<DV, Unit>
 * @return Unit
 */
fun asyncTransmit(block: DV.() -> Unit) {
    val dv = DV().apply(block)
    sendProtocol {
        data = dv.byteList.toByteArray()
    }
}

/**
 * 查询锁
 *
 * @param list List<Int>
 * @return Unit
 */
fun queryLock(list: List<Int>) {
    val byteArray = ByteArray(list.size)
    list.forEachIndexed { index, i ->
        byteArray.writeInt8(i, index)
    }
    sendProtocol {
        cmd = 0x03.toByte()
        data = byteArray
    }
}

/**
 * 查询锁
 *
 * @param list IntArray
 * @return Unit
 */
fun queryLock(vararg list: Int) {
    val byteArray = ByteArray(list.size)
    list.forEachIndexed { index, i ->
        byteArray.writeInt8(i, index)
    }
    sendProtocol {
        data = byteArray
    }
}

/**
 * 设置锁
 *
 * @param list List<Int>
 * @return Unit
 */
fun setLock(list: List<Int>) {
    list.forEach {
        serialPort.arrayList[it] = 1
    }
}

/**
 * 设置锁
 *
 * @param list IntArray
 * @return Unit
 */
fun setLock(vararg list: Int) {
    list.forEach {
        serialPort.arrayList[it] = 1
    }
}

/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(list: List<Int>): Boolean {
    return list.any {
        serialPort.arrayList[it] == 1
    }
}

/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(vararg list: Int): Boolean {
    return list.any {
        serialPort.arrayList[it] == 1
    }
}

/**
 * 释放锁
 *
 * @param list List<Int>
 * @return Unit
 */
fun freeLock(list: List<Int>) {
    list.forEach {
        serialPort.arrayList[it] = 0
    }
}

/**
 * 释放锁
 *
 * @param list IntArray
 * @return Unit
 */
fun freeLock(vararg list: Int) {
    list.forEach {
        serialPort.arrayList[it] = 0
    }
}

/**
 * 下位机复位
 *
 * @return Unit
 */
fun resetLowPower() {
    sendProtocol {
        cmd = 0x00.toByte()
        data = byteArrayOf(0xFF.toByte())
    }
}