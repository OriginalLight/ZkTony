package com.zktony.android.logic.ext

import com.zktony.android.logic.SerialPort
import com.zktony.android.logic.utils.DVP
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
 * 设置电机状态
 *
 * @param list List<Int>
 * @return Unit
 */
fun setLock(list: List<Int>, isLock: Boolean = true) {
    list.forEach {
        serialPort.axis[it] = isLock
    }
}

/**
 * 设置电机状态
 *
 * @param list IntArray
 * @return Unit
 */
fun setLock(vararg list: Int, isLock: Boolean = true) {
    list.forEach {
        serialPort.axis[it] = isLock
    }
}

/**
 * 获取电机状态
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(list: List<Int>): Boolean {
    return list.any {
        serialPort.axis[it] == true
    }
}

/**
 * 获取电机状态
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(vararg list: Int): Boolean {
    return list.any { serialPort.axis[it] }
}

/**
 * 获取光电状态
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getGpio(list: List<Int>): Boolean {
    return list.any {
        serialPort.gpio[it] == true
    }
}

/**
 * 获取光电状态
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getGpio(vararg list: Int): Boolean {
    return list.any { serialPort.gpio[it] }
}


/**
 * 下位机复位
 *
 *  0x00
 *
 * @return Unit
 */
fun cmdInitializer() {
    sendProtocol {
        cmd = 0x00.toByte()
        data = byteArrayOf(0xFF.toByte())
    }
}

/**
 * 组合发送
 *
 * 0x01
 *
 * 同步发送 一发一收算作完成
 *
 * @param timeOut Long
 * @param strategy ExceptionStrategy
 * @param block [@kotlin.ExtensionFunctionType] Function1<DV, Unit>
 * @return Unit
 */
suspend fun syncTx(
    timeOut: Long = 5000L,
    strategy: ExceptionStrategy = ExceptionStrategy.SKIP,
    block: DVP.() -> Unit
) {
    val dvp = DVP().apply(block)
    try {
        setLock(dvp.indexList)
        withTimeout(timeOut) {
            sendProtocol {
                data = dvp.byteList.toByteArray()
            }
            delay(100L)
            while (getLock(dvp.indexList)) {
                delay(100L)
            }
        }
    } catch (e: Exception) {
        when (strategy) {
            ExceptionStrategy.RETRY -> syncTx(timeOut, strategy, block)
            ExceptionStrategy.QUERY -> queryTx(dvp.indexList)
            ExceptionStrategy.SKIP -> setLock(dvp.indexList, false)
            ExceptionStrategy.RESET -> cmdInitializer()
            ExceptionStrategy.THROW -> throw e
        }
    }
}

/**
 * 组合发送
 *
 * 0x01
 *
 * 异步发送 不管之前是否完成
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<DV, Unit>
 * @return Unit
 */
fun asyncTx(block: DVP.() -> Unit) {
    val dvp = DVP().apply(block)
    sendProtocol {
        data = dvp.byteList.toByteArray()
    }
}

/**
 * 停止电机
 *
 * 0x02
 *
 * @param list List<Int>
 * @return Unit
 */
fun stopTx(list: List<Int>) {
    val byteArray = ByteArray(list.size)
    list.forEachIndexed { index, i ->
        byteArray.writeInt8(i, index)
    }
    sendProtocol {
        cmd = 0x02.toByte()
        data = byteArray
    }
}

/**
 * 停止电机
 *
 * 0x02
 *
 * @param list IntArray
 * @return Unit
 */
fun stopTx(vararg list: Int) {
    val byteArray = ByteArray(list.size)
    list.forEachIndexed { index, i ->
        byteArray.writeInt8(i, index)
    }
    sendProtocol {
        cmd = 0x02.toByte()
        data = byteArray
    }
}

/**
 * 查询电机状态
 *
 * 0x03
 *
 * @param list List<Int>
 * @return Unit
 */
fun queryTx(list: List<Int>) {
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
 * 查询电机状态
 *
 * 0x03
 *
 * @param list IntArray
 * @return Unit
 */
fun queryTx(vararg list: Int) {
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
 * 查询光电状态
 *
 * 0x04
 *
 * @param list List<Int>
 * @return Unit
 */
fun queryGpioTx(list: List<Int>) {
    val byteArray = ByteArray(list.size)
    list.forEachIndexed { index, i ->
        byteArray.writeInt8(i, index)
    }
    sendProtocol {
        cmd = 0x04.toByte()
        data = byteArray
    }
}

/**
 * 查询光电状态
 *
 * 0x04
 *
 * @param list IntArray
 * @return Unit
 */
fun queryGpioTx(vararg list: Int) {
    val byteArray = ByteArray(list.size)
    list.forEachIndexed { index, i ->
        byteArray.writeInt8(i, index)
    }
    sendProtocol {
        cmd = 0x04.toByte()
        data = byteArray
    }
}

/**
 * 运动电机开机复位
 *
 * @return Unit
 */
suspend fun axisInitializer() {
    delay(1000L)
    repeat(2) {
        queryGpioTx(it)
        delay(100L)
        if (getGpio(it)) {
            syncTx {
                pulse {
                    index = it
                    pulse = 3200L * 2
                    acc = 30
                    dec = 30
                    speed = 50
                }
            }
            syncTx {
                pulse {
                    index = it
                    pulse = 3200L * -3
                    acc = 15
                    dec = 15
                    speed = 30
                }
            }
        } else {
            syncTx(timeOut = 10000L) {
                pulse {
                    index = it
                    pulse = 3200L * -100
                    acc = 50
                    dec = 80
                    speed = 100
                }
            }
            syncTx {
                pulse {
                    index = it
                    pulse = 3200L * 2
                    acc = 30
                    dec = 30
                    speed = 50
                }
            }
            syncTx {
                pulse {
                    index = it
                    pulse = 3200L * -3
                    acc = 15
                    dec = 15
                    speed = 30
                }
            }
        }
    }
}