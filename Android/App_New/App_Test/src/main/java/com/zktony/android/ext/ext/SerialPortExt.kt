package com.zktony.android.ext.ext

import com.zktony.android.ext.SerialPortHelper
import com.zktony.serialport.command.Protocol
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPortHelper by inject(SerialPortHelper::class.java)

/**
 * 发送命令
 */
fun sendByteArray(byteArray: ByteArray) {
    serialPort.sendByteArray(byteArray)
}

fun sendProtocol(block: Protocol.() -> Unit) {
    serialPort.sendByteArray(Protocol().apply(block).toByteArray())
}

fun sendHexString(hex: String) {
    serialPort.sendHexString(hex)
}

fun sendAsciiString(ascii: String) {
    serialPort.sendAsciiString(ascii)
}

fun setLock(list: List<Int>) {
    list.forEach {
        serialPort.array[it] = 1
    }
}

fun setLock(vararg list: Int) {
    list.forEach {
        serialPort.array[it] = 1
    }
}

/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(list: List<Int>): Boolean {
    return list.any { serialPort.array[it] == 1 }
}


/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(vararg list: Int): Boolean {
    return list.any { serialPort.array[it] == 1 }
}

/**
 * 释放锁
 *
 * @param list List<Int>
 */
fun freeLock(list: List<Int>) {
    list.forEach {
        serialPort.array[it] = 0
    }
}

/**
 * 收集hex回复
 * @param block (String) -> Unit
 */
suspend fun collectCallback(block: (ByteArray) -> Unit) {
    serialPort.byteArrayFlow.collect {
        block(it)
    }
}