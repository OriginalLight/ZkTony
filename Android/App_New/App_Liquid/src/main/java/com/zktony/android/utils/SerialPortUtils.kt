package com.zktony.android.utils

import com.zktony.android.utils.AppStateUtils.hpa
import com.zktony.android.utils.AppStateUtils.hpc
import com.zktony.android.utils.AppStateUtils.hpg
import com.zktony.android.utils.AppStateUtils.hpp
import com.zktony.android.utils.LogUtils.logE
import com.zktony.android.utils.internal.ControlType
import com.zktony.android.utils.internal.ExceptionPolicy
import com.zktony.android.utils.internal.ExecuteType
import com.zktony.android.utils.internal.StartBuilder
import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.writeInt8
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

object SerialPortUtils {
    /**
     * 串口通信
     */
    val serialHelper = object : AbstractSerialHelper(SerialConfig()) {
        override fun callbackHandler(byteArray: ByteArray) {
            Protocol.Protocol.callbackHandler(byteArray) { code, rx ->
                when (code) {
                    Protocol.RX_0X01 -> {
                        for (i in 0 until rx.data.size / 2) {
                            val index = rx.data.readInt8(offset = i * 2)
                            val status = rx.data.readInt8(offset = i * 2 + 1)
                            hpa[index] = status == 1
                        }
                    }

                    Protocol.RX_0X02 -> {
                        for (i in 0 until rx.data.size / 2) {
                            val index = rx.data.readInt8(offset = i * 2)
                            val status = rx.data.readInt8(offset = i * 2 + 1)
                            hpg[index] = status == 1
                        }
                    }
                }
            }
        }

        override fun exceptionHandler(e: Exception) {
            logE(message = "Serial Exception: ${e.message}")
        }
    }

    /**
     * 脉冲转换
     */
    fun <T : Number> pulse(index: Int, dvp: T): Long {
        val p = when (dvp) {
            is Double -> when (index) {
                0 -> (dvp / 27.89 * 3200L).toLong()
                1 -> (dvp / 18.34 * 3200L).toLong()
                else -> ((hpc[index - 2] ?: { x -> x * 100 }).invoke(dvp) ?: 0.0).toLong()
            }

            is Long -> dvp
            else -> dvp.toLong()
        }

        return if (index in 0..1) {
            val d = p - (hpp[index] ?: 0L)
            hpp[index] = maxOf(p, 0L)
            d
        } else {
            p
        }
    }

    /**
     * 发送协议
     */
    inline fun sendProtocol(block: Protocol.() -> Unit) =
        serialHelper.sendByteArray(Protocol().apply(block).toByteArray())

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
    fun init() {
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
    fun stop(ids: List<Int>) {
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

    fun stop(vararg ids: Int) = stop(ids.toList())

    /**
     * 查询轴状态
     */
    fun query(ids: List<Int>) {
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

    fun query(vararg ids: Int) = query(ids.toList())

    /**
     * 查询 GPIO 状态
     */
    fun gpio(ids: List<Int>) {
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

    fun gpio(vararg ids: Int) = gpio(ids.toList())

    /**
     * 设置阀门状态
     */
    fun valve(ids: List<Pair<Int, Int>>) {
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

    fun valve(vararg ids: Pair<Int, Int>) = valve(ids.toList())
}