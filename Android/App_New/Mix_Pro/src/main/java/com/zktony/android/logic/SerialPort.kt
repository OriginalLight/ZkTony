package com.zktony.android.logic

import com.zktony.core.ext.logi
import com.zktony.serialport.AbstractSerial
import com.zktony.serialport.command.protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.splitByteArray
import com.zktony.serialport.ext.toHexString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class SerialPort : AbstractSerial() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val axis: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>()
    val gpio: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>()

    init {
        scope.launch {
            repeat(16) {
                axis.add(false)
                gpio.add(false)
            }
            openDevice(SerialConfig())
        }
    }

    /**
     * callbackProcess
     * crc校验等
     *
     * @param byteArray ByteArray
     * @param block Function1<ByteArray, Unit>
     */
    override fun callbackProcess(byteArray: ByteArray, block: (ByteArray) -> Unit) {
        byteArray.toHexString().logi()
        // 验证包长 >= 12
        if (byteArray.size < 12) {
            throw Exception("RX Length Error")
        } else {
            // 分包处理
            val expectHead = byteArrayOf(0xEE.toByte())
            val expectEnd = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte())
            byteArray.splitByteArray(expectHead, expectEnd).forEach {
                //验证包头和包尾
                val head = it.copyOfRange(0, 1)
                val end = it.copyOfRange(it.size - 4, it.size)

                if (!head.contentEquals(expectHead) || !end.contentEquals(expectEnd)) {
                    throw Exception("RX Header or End Error")
                } else {
                    // crc 校验
                    val crc = it.copyOfRange(it.size - 6, it.size - 4)
                    val bytes = it.copyOfRange(0, it.size - 6)
                    if (bytes.crc16LE().contentEquals(crc)) {
                        block(it)
                    } else {
                        throw Exception("RX Crc Error")
                    }
                }
            }
        }
    }

    /**
     * byteArrayProcess
     *
     * @param byteArray ByteArray
     */
    override fun byteArrayProcess(byteArray: ByteArray) {
        val rec = byteArray.protocol()
        if (rec.addr == 0x02.toByte()) {
            when (rec.cmd) {
                0x01.toByte() -> {
                    for (i in 0 until rec.data.size / 2) {
                        val index = rec.data.readInt8(offset = i * 2)
                        val status = rec.data.readInt8(offset = i * 2 + 1)
                        axis[index] = status == 1
                    }
                }

                0x02.toByte() -> {
                    for (i in 0 until rec.data.size / 2) {
                        val index = rec.data.readInt8(offset = i * 2)
                        val status = rec.data.readInt8(offset = i * 2 + 1)
                        gpio[index] = status == 1
                    }
                }

                0xFF.toByte() -> {
                    when (rec.data.readInt16LE()) {
                        1 -> throw Exception("TX Header Error")
                        2 -> throw Exception("TX Addr Error")
                        3 -> throw Exception("TX Crc Error")
                        4 -> throw Exception("TX No Com")
                    }
                }
            }
        }
    }

    /**
     * 初始化
     */
    fun initializer() {
        "SerialPort initializer".logi()
    }
}
