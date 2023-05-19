package com.zktony.android.logic

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.command.protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.crc16
import com.zktony.serialport.ext.splitByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Vector

class SerialPort {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val helper by lazy { SerialHelper(SerialConfig()) }

    private val _callback = MutableStateFlow(byteArrayOf())
    val callback = _callback.asStateFlow()
    val vector = Vector<Int>(16)

    init {
        scope.launch {
            for (i in 0..15) {
                vector.add(0)
            }
            helper.openDevice()
            helper.callback = {
                callbackProcess(it) { bytes ->
                    protocolHandler(bytes)
                }
            }
        }
    }

    /**
     * Send byte array
     *
     * @param bytes ByteArray
     */
    fun sendByteArray(bytes: ByteArray) {
        helper.sendByteArray(bytes = bytes)
    }

    /**
     * Send hex string
     *
     * @param hex String
     */
    fun sendHexString(hex: String) {
        helper.sendHexString(hex = hex)
    }

    /**
     * Send ascii string
     *
     * @param ascii String
     */
    fun sendAsciiString(ascii: String) {
        helper.sendAsciiString(ascii = ascii)
    }

    /**
     * Send protocol
     *
     * @param protocol Protocol
     */
    fun sendProtocol(protocol: Protocol) {
        helper.sendProtocol(protocol = protocol)
    }

    /**
     * Callback process
     * 包括分包、crc校验等
     *
     * @param byteArray ByteArray
     * @param block Function1<ByteArray, Unit>
     */
    private fun callbackProcess(byteArray: ByteArray, block: (ByteArray) -> Unit) {
        byteArray.splitByteArray(
            head = byteArrayOf(0xEE.toByte()),
            end = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte()),
        ).forEach {
            val crc = it.copyOfRange(it.size - 6, it.size - 4)
            val bytes = it.copyOfRange(0, it.size - 6)
            if (bytes.crc16().contentEquals(crc)) {
                block(bytes)
            }
        }
    }

    /**
     * Protocol handler
     *
     * @param bytes ByteArray
     */
    private fun protocolHandler(bytes: ByteArray) {
        val rec = bytes.protocol()
        if (rec.addr == 0x02.toByte()) {
            _callback.value = bytes
            protocolProcess(rec)
        }
    }

    /**
     * Protocol process
     *
     * @param protocol Protocol
     */
    private fun protocolProcess(protocol: Protocol) {
        when (protocol.fn) {
            0x01.toByte() -> {
                val index = protocol.data[0].toInt()
                val value = protocol.data[1].toInt()
                vector[index] = value
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
