package com.zktony.android.logic.ext

import com.zktony.android.logic.SerialPort
import com.zktony.serialport.command.Protocol
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)

/**
 * 发送命令
 */
fun sendByteArray(byteArray: ByteArray) {
    serialPort.sendByteArray(byteArray)
}

fun sendProtocol(block: Protocol.() -> Unit) {
    serialPort.sendProtocol(Protocol().apply(block))
}

fun sendHexString(hex: String) {
    serialPort.sendHexString(hex)
}

fun sendAsciiString(ascii: String) {
    serialPort.sendAsciiString(ascii)
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
        lock = lock || serialPort.array[it] != 0
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