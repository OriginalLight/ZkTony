package com.zktony.android.utils

import com.zktony.android.utils.AppStateUtils.hpa
import com.zktony.android.utils.AppStateUtils.hpd
import com.zktony.android.utils.AppStateUtils.hpe
import com.zktony.android.utils.AppStateUtils.hpg
import com.zktony.android.utils.internal.ControlType
import com.zktony.android.utils.internal.ExceptionPolicy
import com.zktony.android.utils.internal.ExecuteType
import com.zktony.android.utils.internal.StartBuilder
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.writeInt8
import com.zktony.serialport.lifecycle.SerialStoreUtils
import com.zktony.serialport.serialPortOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

object SerialPortUtils {
    fun with() {
        // 初始化zkty串口
        serialPortOf {
            log = true
            device = "/dev/ttyS0"
        }?.let { SerialStoreUtils.put("zkty", it) }

        // 初始化tec串口
        serialPortOf {
            log = true
            device = "/dev/ttyS3"
        }?.let { SerialStoreUtils.put("led", it) }
        // rtu串口全局回调
        SerialStoreUtils.get("zkty")?.registerCallback("globe") { bytes ->
            Protocol.verifyProtocol(bytes) { protocol ->
//                println("protocol.func===${protocol.func}")
//                println("protocol.data===${protocol.data.size}")
//                println("protocol.data===${protocol.data.readInt8()}")
////                println("protocol.data===${protocol.data.toAsciiString()}")
//                println("hpa===0==${hpa[0]}===1===${hpa[1]}===2===${hpa[2]}===3===${hpa[3]}===4===${hpa[4]}")
                if (protocol.func == 0xFF.toByte()) {
//                    hpe[1] = false
                    when (protocol.data.readInt16LE()) {
                        1 -> throw Exception("TX Header Error")
                        2 -> throw Exception("TX Addr Error")
                        3 -> throw Exception("TX Crc Error")
                        4 -> throw Exception("TX No Com")
                    }
                } else {
//                    println("protocol.func===${protocol.func}")
//                    println("protocol.data===${protocol.data.readInt8()}")
//                    println("protocol.data===${protocol.data.toAsciiString()}")
                    when (protocol.func) {
                        0x01.toByte() -> {
                            for (i in 0 until protocol.data.size / 2) {
                                val index = protocol.data.readInt8(offset = i * 2)
                                val status = protocol.data.readInt8(offset = i * 2 + 1)
                                hpa[index] = status == 1
                            }
                        }

                        0x02.toByte() -> {
                            for (i in 0 until protocol.data.size / 2) {
                                val index = protocol.data.readInt8(offset = i * 2)
                                val status = protocol.data.readInt8(offset = i * 2 + 1)
                                hpg[index] = status == 1
                            }
                        }

                        0x09.toByte() -> {
                            hpe[1] = true
                        }
                        else -> {}
                    }
                }
            }
        }

        // rtu串口全局回调
        SerialStoreUtils.get("led")?.registerCallback("globe") { bytes ->
            Protocol.verifyProtocol(bytes) { protocol ->
                if (protocol.func == 0xFF.toByte()) {
                    when (protocol.data.readInt16LE()) {
                        1 -> throw Exception("TX Header Error")
                        2 -> throw Exception("TX Addr Error")
                        3 -> throw Exception("TX Crc Error")
                        4 -> throw Exception("TX No Com")
                    }
                } else {
                    when (protocol.data[0]) {
                        0x0B.toByte() -> {
                            hpd[protocol.data[0].toInt()] = true
                        }

                        0x0C.toByte() -> {
                            hpd[protocol.data[0].toInt()] = true
                        }

                        0x0D.toByte() -> {
                            hpd[protocol.data[0].toInt()] = true
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    /**
     * 脉冲转换
     */
    fun <T : Number> pulse(index: Int, dvp: T): Long {
        val p = when (dvp) {
            is Double -> ((AppStateUtils.hpc[index] ?: { x -> x * 100 }).invoke(dvp)).toLong()
            is Long -> dvp
            else -> dvp.toLong()

        }
        return if (index == 0) {
            val d = p - (AppStateUtils.hpp[index] ?: 0L)
            AppStateUtils.hpp[index] = maxOf(p, 0L)
            d
        } else {
            p

        }

    }


    /**
     * 发送协议
     */
    suspend inline fun sendProtocol(block: Protocol.() -> Unit) =
        SerialStoreUtils.get("zkty")?.sendByteArray(Protocol().apply(block).serialization())

    /**
     * 设置轴锁定状态
     */
    fun setLock(ids: List<Int>, isLock: Boolean = true) = ids.forEach { hpa[it] = isLock }

    /**
     * 设置轴锁定状态
     */
    fun setLock(vararg ids: Int, isLock: Boolean = true) = setLock(ids.toList(), isLock)

    /**
     * 获取轴锁定状态
     */
    fun getLock(ids: List<Int>) = ids.any { hpa[it] ?: false }

    /**
     * 获取轴锁定状态
     */
    fun getLock(vararg ids: Int) = getLock(ids.toList())

    /**
     * 获取 GPIO 状态
     */
    fun getGpio(id: Int): Boolean = hpg[id] ?: false


    /**
     * 初始化下位机
     */
    suspend fun init() {
        sendProtocol {
            func = ControlType.RESET
            data = byteArrayOf(0x00)
        }
    }


    /**
     * 启动运动
     */
    suspend fun start(block: StartBuilder.() -> Unit) {
        val builder = StartBuilder().apply(block)
        when (builder.executeType) {
            // 同步运动
            ExecuteType.SYNC -> {
                try {
                    // 设置超时时间
                    withTimeout(builder.timeOut) {
                        // 发送运动命令
                        if (builder.byteList.isNotEmpty()) {
                            setLock(builder.indexList)
                            sendProtocol {
                                func = ControlType.START
                                data = builder.byteList.toByteArray()
                            }
                            delay(10L)
                            // 等待运动完成
                            while (getLock(builder.indexList)) {
                                delay(10L)
                            }
                        }
                    }
                } catch (ex: Exception) {
                    // 根据异常处理策略进行处理
                    when (builder.exceptionPolicy) {
                        // 重试
                        ExceptionPolicy.RETRY -> start(block)
                        // 查询轴状态
                        ExceptionPolicy.QUERY -> query(builder.indexList)
                        // 跳过
                        ExceptionPolicy.SKIP -> setLock(builder.indexList, false)
                        // 复位
                        ExceptionPolicy.RESET -> init()
                        // 抛出异常
                        ExceptionPolicy.THROW -> throw ex
                    }
                }
            }

            // 异步运动
            ExecuteType.ASYNC -> {
                if (builder.byteList.isNotEmpty()) {
                    setLock(builder.indexList)
                    sendProtocol {
                        func = ControlType.START
                        data = builder.byteList.toByteArray()
                    }
                }
            }
        }
    }

    /**
     * 停止运动
     */
    suspend fun stop(ids: List<Int>) {
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        if (byteArray.isNotEmpty()) {
            sendProtocol {
                func = ControlType.STOP
                data = byteArray
            }
        }
    }

    suspend fun stop(vararg ids: Int) = stop(ids.toList())

    /**
     * 查询轴状态
     */
    suspend fun query(ids: List<Int>) {

        withTimeout(1000L) {
            val byteArray = ByteArray(ids.size)
            ids.forEachIndexed { index, i ->
                byteArray.writeInt8(i, index)
            }
            if (byteArray.isNotEmpty()) {
                sendProtocol {
                    func = ControlType.QUERY
                    data = byteArray
                }
            }
        }


    }

//    suspend fun queryNew(ids: List<Int>): Boolean {
//
//        val serialPort = SerialStoreUtils.get("zkty") ?: return false
//
//        try {
//            // 注册回调
//            serialPort.registerCallback(ids.toString()) { bytes ->
//                Protocol.verifyProtocol(bytes) {
//                    for (i in 0 until it.data.size / 2) {
//                        val index = it.data.readInt8(offset = i * 2)
//                        val status = it.data.readInt8(offset = i * 2 + 1)
//                        hpa[index] = status == 1
//                    }
//                }
//            }
//            // 发送版本查询命令
//            serialPort.sendByteArray(Protocol().apply {
//                func = 0x0A.toByte()
//            }.serialization())
//            // 等待版本响应
//            withTimeout(1000L) {
//                repeat(3) {
//                    queryNew(ids)
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("EmbeddedExt", e.message ?: "Unknown")
//        } finally {
//            // 取消注册
//            serialPort.unregisterCallback(ids.toString())
//        }
//        // 返回版本号
//        return true
//    }

    suspend fun query(vararg ids: Int) = query(ids.toList())

    /**
     * 查询 GPIO 状态
     */
    suspend fun gpio(ids: List<Int>) {
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        if (byteArray.isNotEmpty()) {
            sendProtocol {
                func = ControlType.GPIO
                data = byteArray
            }
        }
    }

    suspend fun gpio(vararg ids: Int) = gpio(ids.toList())

    /**
     * 设置阀门状态
     */
    suspend fun valve(ids: List<Pair<Int, Int>>) {
        val byteArray = ByteArray(ids.size * 2)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i.first, index * 2)
            byteArray.writeInt8(i.second, index * 2 + 1)
        }
        if (byteArray.isNotEmpty()) {
            sendProtocol {
                func = ControlType.VALVE
                data = byteArray
            }
        }
    }


    /**
     * 心跳
     */
    suspend fun sendPING() {
        sendProtocol {
            func = ControlType.SEND
            data = byteArrayOf(0x01)
//            func = 0x0B
        }
    }

    suspend fun version() {
        sendProtocol {
            func = ControlType.VERSION
        }
    }

    /**
     * 红-常亮
     */
    suspend fun lightRed() {
        SerialStoreUtils.get("led")?.sendByteArray(Protocol().apply {
            func = 0x01
            data = byteArrayOf(0x01)
        }.serialization())
    }

    /**
     * 黄-常亮
     */
    suspend fun lightYellow() {
        SerialStoreUtils.get("led")?.sendByteArray(Protocol().apply {
            func = 0x01
            data = byteArrayOf(0x02)
        }.serialization())
    }

    /**
     * 绿-常亮
     */
    suspend fun lightGreed() {
        SerialStoreUtils.get("led")?.sendByteArray(Protocol().apply {
            func = 0x01
            data = byteArrayOf(0x03)
        }.serialization())
    }

    /**
     * 黄-闪烁
     */
    suspend fun lightFlashYellow() {
        SerialStoreUtils.get("led")?.sendByteArray(Protocol().apply {
            func = 0x01
            data = byteArrayOf(0x04)
        }.serialization())
    }

    /**
     * 黄-快闪
     */
    suspend fun endStartFlashYellow() {
        SerialStoreUtils.get("led")?.sendByteArray(Protocol().apply {
            func = 0x01
            data = byteArrayOf(0x06)
        }.serialization())
    }

    /**
     * 关闭灯光
     */
    suspend fun cleanLight() {
        SerialStoreUtils.get("led")?.sendByteArray(Protocol().apply {
            func = 0x01
            data = byteArrayOf(0x05)
        }.serialization())

    }

    suspend fun valve(vararg ids: Pair<Int, Int>) = valve(ids.toList())


}