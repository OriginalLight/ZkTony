package com.zktony.www.ui.home

import com.zktony.serialport.util.Serial.TTYS0
import com.zktony.www.data.local.room.entity.Action
import com.zktony.www.data.local.room.entity.Container
import com.zktony.www.control.motion.MotionManager
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.control.serial.protocol.V1
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandExecutor constructor(
    private val serial: SerialManager = SerialManager.instance,
    private val manager: MotionManager = MotionManager.instance,
    private val module: Int,
    private val con: Container,
) {
    private lateinit var action: Action


    private val _wait = MutableStateFlow("等待中")
    val wait = _wait.asStateFlow()

    fun initAction(action: Action) {
        this.action = action
    }

    /**
     * 添加封闭液
     * @param block
     */
    suspend fun addBlockingLiquid(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serial.setTemp(
                addr = (module + 1).toString(),
                temp = action.temperature.toString()
            )
            addLiquid(y = con.blockY, z = con.blockZ)
            block.invoke()
        }
    }

    /**
     * 添加一抗代码块
     * @param block
     */
    suspend fun addAntibodyOne(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serial.setTemp(
                addr = (module + 1).toString(),
                temp = action.temperature.toString()
            )
            addLiquid(y = con.oneY, z = con.oneZ)
            block.invoke()
        }
    }

    /**
     * 回收一抗代码块
     * @param block
     */
    suspend fun recycleAntibodyOne(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            recycleLiquid(y = con.oneY, z = con.recycleOneZ)
            block.invoke()
        }
    }

    /**
     * 添加二抗代码块
     * @param block
     */
    suspend fun addAntibodyTwo(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serial.setTemp(
                addr = (module + 1).toString(),
                temp = action.temperature.toString()
            )
            addLiquid(y = con.twoY, z = con.twoZ)
            block.invoke()
        }
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serial.setTemp(
                addr = (module + 1).toString(),
                temp = action.temperature.toString()
            )
            // 主板运动
            addLiquid(y = con.washY, z = con.washZ)
            block.invoke()
        }
    }

    /**
     * 回收到废液槽
     *
     * @param block
     */
    suspend fun wasteLiquid(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            recycleLiquid(y = con.washY, z = con.wasteZ)
            block.invoke()
        }
    }

    /**
     * 主机命令生成器
     */
    private fun addLiquid(y: Float, z: Float) {
        manager.executor(
            manager.generator(y = y),
            manager.generator(
                y = y,
                z = z,
                v1 = if (module == 0) action.liquidVolume else 0f,
                v2 = if (module == 1) action.liquidVolume else 0f,
                v3 = if (module == 2) action.liquidVolume else 0f,
                v4 = if (module == 3) action.liquidVolume else 0f,
                v5 = if (action.mode == 3) action.liquidVolume else 0f
            ),
            manager.generator(
                y = y,
                v1 = if (module == 0) 15000f else 0f,
                v2 = if (module == 1) 15000f else 0f,
                v3 = if (module == 2) 15000f else 0f,
                v4 = if (module == 3) 15000f else 0f,
            )
        )
    }

    /**
     * 从机排液命令生成器
     * @return [List]<[String]>
     */
    private fun recycleLiquid(y: Float, z: Float) {
        val volume = -(action.liquidVolume + 10000f)
        manager.executor(
            manager.generator(y = y),
            manager.generator(
                y = y,
                z = z,
                v1 = if (module == 0) volume else 0f,
                v2 = if (module == 1) volume else 0f,
                v3 = if (module == 2) volume else 0f,
                v4 = if (module == 3) volume else 0f,
                v6 = action.liquidVolume + 10000f
            ),
            manager.generator(y = y)
        )
    }

    /**
     * 等待机构空闲
     * @param block suspend () -> Unit 代码块
     */
    private suspend fun waitForFree(block: suspend () -> Unit) {
        if (!serial.lock.value) {
            if (!serial.drawer.value) {
                block.invoke()
            } else {
                _wait.value = "抽屉未关闭"
                delay(300L)
                waitForFree(block)
            }
        } else {
            _wait.value = "等待加液"
            delay(300L)
            waitForFree(block)
        }
    }
}

