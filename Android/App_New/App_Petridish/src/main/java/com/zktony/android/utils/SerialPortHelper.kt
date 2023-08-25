package com.zktony.android.utils

import com.zktony.android.utils.ext.loge
import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.*
import java.util.concurrent.CopyOnWriteArrayList

class SerialPortHelper : AbstractSerialHelper(
    SerialConfig(
        device = "/dev/ttyS4"
    )
) {

    val axis: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>().apply {
        repeat(16) { add(false) }
    }
    val gpio: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>().apply {
        repeat(16) { add(false) }
    }

    /**
     * callbackVerify
     * crc校验等
     *
     * @param byteArray ByteArray
     * @param block Function1<ByteArray, Unit>
     */
    override fun callbackVerify(byteArray: ByteArray, block: (ByteArray) -> Unit) {
        byteArray.toHexString().loge()
        // 验证包长 >= 12
        if (byteArray.size < 12) {
            throw Exception("RX Length Error")
        }

        // 分包处理
        val expectHead = byteArrayOf(0xEE.toByte())
        val expectEnd = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte())
        byteArray.splitByteArray(expectHead, expectEnd).forEach { packet ->
            // 验证包头和包尾
            val head = packet.copyOfRange(0, 1)
            if (!head.contentEquals(expectHead)) {
                throw Exception("RX Header Error")
            }
            val end = packet.copyOfRange(packet.size - 4, packet.size)
            if (!end.contentEquals(expectEnd)) {
                throw Exception("RX End Error")
            }

            // crc 校验
            val crc = packet.copyOfRange(packet.size - 6, packet.size - 4)
            val bytes = packet.copyOfRange(0, packet.size - 6)
            if (!bytes.crc16LE().contentEquals(crc)) {
                throw Exception("RX Crc Error")
            }

            // 校验通过
            block(packet)
        }
    }

    /**
     * callbackProcess
     *
     * @param byteArray ByteArray
     */
    override fun callbackProcess(byteArray: ByteArray) {
        // 解析协议
        val rec = byteArray.protocol()

        // 处理地址为 0x02 的数据包
        if (rec.addr == 0x02.toByte()) {
            when (rec.func) {
                // 处理轴状态数据
                0x01.toByte() -> {
                    for (i in 0 until rec.data.size / 2) {
                        val index = rec.data.readInt8(offset = i * 2)
                        val status = rec.data.readInt8(offset = i * 2 + 1)
                        axis[index] = status == 1
                    }
                }
                // 处理 GPIO 状态数据
                0x02.toByte() -> {
                    for (i in 0 until rec.data.size / 2) {
                        val index = rec.data.readInt8(offset = i * 2)
                        val status = rec.data.readInt8(offset = i * 2 + 1)
                        gpio[index] = status == 1
                    }
                }
                // 处理错误信息
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

    companion object {
        val instance: SerialPortHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { SerialPortHelper() }
    }
}