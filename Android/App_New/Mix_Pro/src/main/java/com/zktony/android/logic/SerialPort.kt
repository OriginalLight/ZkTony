package com.zktony.android.logic

import com.zktony.core.ext.logi
import com.zktony.serialport.AbstractSerial
import com.zktony.serialport.command.protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.splitByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class SerialPort : AbstractSerial() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _byteArrayFlow = MutableStateFlow(byteArrayOf())

    val byteArrayFlow = _byteArrayFlow.asStateFlow()
    val arrayList: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList<Int>()

    init {
        scope.launch {
            repeat(16) { arrayList.add(0) }
            openDevice(SerialConfig())
        }
    }

    /**
     * callbackProcess
     * 包括分包、crc校验等
     *
     * @param byteArray ByteArray
     * @param block Function1<ByteArray, Unit>
     */
    override fun callbackProcess(byteArray: ByteArray, block: (ByteArray) -> Unit) {
        byteArray.splitByteArray(
            head = byteArrayOf(0xEE.toByte()),
            end = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte()),
        ).forEach {
            val crc = it.copyOfRange(it.size - 6, it.size - 4)
            val bytes = it.copyOfRange(0, it.size - 6)
            if (bytes.crc16LE().contentEquals(crc)) {
                block(it)
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
                0x01.toByte() -> {
                    val index = rec.data[0].toInt()
                    val value = rec.data[1].toInt()
                    arrayList[index] = value
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
