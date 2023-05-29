package com.zktony.android.logic

import com.zktony.core.ext.loge
import com.zktony.core.ext.logi
import com.zktony.serialport.AbstractSerial
import com.zktony.serialport.command.IProtocol
import com.zktony.serialport.command.protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class SerialPort : AbstractSerial(), IProtocol {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _byteArrayFlow = MutableStateFlow(byteArrayOf())

    val byteArrayFlow = _byteArrayFlow.asStateFlow()
    val array: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()

    init {
        scope.launch {
            repeat(16) { array.add(0) }
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
        // 验证包长 >=10
        if (byteArray.size < 12) {
            "byteArray size < 12".loge()
        } else {
            //验证包头和包尾
            val head = byteArray.copyOfRange(0, 1)
            val end = byteArray.copyOfRange(byteArray.size - 4, byteArray.size)
            val expectHead = byteArrayOf(0xEE.toByte())
            val expectEnd = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte())
            if (!head.contentEquals(expectHead) || !end.contentEquals(expectEnd)) {
                "head or end error".loge()
            } else {
                // crc 校验
                val crc = byteArray.copyOfRange(byteArray.size - 6, byteArray.size - 4)
                val bytes = byteArray.copyOfRange(0, byteArray.size - 6)
                if (bytes.crc16LE().contentEquals(crc)) {
                    block(byteArray)
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
        _byteArrayFlow.value = byteArray

        val rec = byteArray.protocol()
        if (rec.id == 0x02.toByte()) {
            when (rec.cmd) {
                0x01.toByte() -> function0x01(rec.data)
                0x02.toByte() -> function0x02(rec.data)
                0x03.toByte() -> function0x03(rec.data)
                0xFF.toByte() -> exception(rec.data)
            }
        }
    }

    override fun exception(byteArray: ByteArray) {
        when (byteArray.readInt16LE()) {
            1 -> "CMD_Error_Header".loge()
            2 -> "CMD_Error_Addr".loge()
            3 -> "CMD_Error_Crc".loge()
            4 -> "CMD_NO_COM".loge()
        }
    }

    override fun function0x01(byteArray: ByteArray) {
        for (i in 0 until byteArray.size / 2) {
            val index = byteArray.readInt8(i * 2)
            val status = byteArray.readInt8(i * 2 + 1)
            array[index] = status
        }
    }

    override fun function0x02(byteArray: ByteArray) {
        for (i in 0 until byteArray.size / 2) {
            val index = byteArray.readInt8(i * 2)
            val status = byteArray.readInt8(i * 2 + 1)
            array[index] = status
        }
    }

    override fun function0x03(byteArray: ByteArray) {
        for (i in 0 until byteArray.size / 2) {
            val index = byteArray.readInt8(i * 2)
            val status = byteArray.readInt8(i * 2 + 1)
            array[index] = status
        }
    }

    /**
     * 初始化
     */
    fun initializer() {
        "SerialPort initializer".logi()
    }
}
