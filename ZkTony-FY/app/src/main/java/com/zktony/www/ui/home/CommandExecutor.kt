package com.zktony.www.ui.home

import com.zktony.serialport.util.Serial.*
import com.zktony.www.common.app.Settings
import com.zktony.www.common.room.entity.Action
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandExecutor constructor(
    private val serial: SerialManager = SerialManager.instance,
    private val module: Int,
    private val settings: Settings,
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
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = V1.setTemp(
                    addr = (module + 1).toString(),
                    temp = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = V1.multiPoint(
                    moveTo(
                        distance = settings.container.blockY,
                        height = settings.container.blockZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = V1.multiPoint(addLiquid().first)
            )
            serial.sendHex(
                serial = TTYS2,
                hex = V1.multiPoint(addLiquid().second)
            )
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
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = V1.setTemp(
                    addr = (module + 1).toString(),
                    temp = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0, hex = V1.multiPoint(
                    moveTo(
                        distance = settings.container.oneY,
                        height = settings.container.oneZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = V1.multiPoint(addLiquid().first)
            )
            serial.sendHex(
                serial = TTYS2,
                hex = V1.multiPoint(addLiquid().second)
            )
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
        waitForFree("等待回收") {
            serial.lock(true)
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = V1.multiPoint(
                    moveTo(
                        distance = settings.container.oneY,
                        height = settings.container.recycleOneZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = V1.multiPoint(recycleLiquid().first)
            )
            serial.sendHex(
                serial = TTYS2,
                hex = V1.multiPoint(recycleLiquid().second)
            )
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
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = V1.setTemp(
                    addr = (module + 1).toString(),
                    temp = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = V1.multiPoint(
                    moveTo(
                        distance = settings.container.twoY,
                        height = settings.container.twoZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1, hex = V1.multiPoint(addLiquid().first)
            )
            serial.sendHex(
                serial = TTYS2, hex = V1.multiPoint(addLiquid().second)
            )
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
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = V1.setTemp(
                    addr = (module + 1).toString(),
                    temp = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = V1.multiPoint(
                    moveTo(
                        distance = settings.container.washY,
                        height = settings.container.washZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = V1.multiPoint(addLiquid().first)
            )
            serial.sendHex(
                serial = TTYS2,
                hex = V1.multiPoint(addLiquid().second)
            )
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
        waitForFree("等待清理") {
            serial.lock(true)
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = V1.multiPoint(
                    moveTo(
                        distance = settings.container.wasteY,
                        height = settings.container.wasteZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = V1.multiPoint(recycleLiquid().first)
            )
            serial.sendHex(
                serial = TTYS2,
                hex = V1.multiPoint(recycleLiquid().second)
            )
            block.invoke()
        }
    }

    /**
     * 主机命令生成器
     */
    private fun moveTo(distance: Float, height: Float): String {
        return settings.motorUnits.toMotionHex(distance, 0f) +
                settings.motorUnits.toMotionHex(distance, height) +
                settings.motorUnits.toMotionHex(distance, 0f) +
                settings.motorUnits.toMotionHex(0f, 0f)
    }

    /**
     * 从板子加液命令生成器
     * @return [List]<[String]>
     */
    private fun addLiquid(): Pair<String, String> {
        val zero = "0,0,0,"
        // 吸液
        val stepOne = settings.motorUnits.toPumpHex(
            one = if (module == 0) action.liquidVolume else 0f,
            two = if (module == 1) action.liquidVolume else 0f,
            three = if (module == 2) action.liquidVolume else 0f,
            four = if (module == 3) action.liquidVolume else 0f,
            five = if (action.mode == 3) action.liquidVolume else 0f
        )
        // 清空管路
        val stepTwo = settings.motorUnits.toPumpHex(
            one = if (module == 0) settings.motorUnits.cali.p1 * settings.container.extract / 1000 else 0f,
            two = if (module == 1) settings.motorUnits.cali.p2 * settings.container.extract / 1000 else 0f,
            three = if (module == 2) settings.motorUnits.cali.p3 * settings.container.extract / 1000 else 0f,
            four = if (module == 3) settings.motorUnits.cali.p4 * settings.container.extract / 1000 else 0f,
        )
        return Pair(
            zero + stepOne.first + zero + stepTwo.first,
            zero + stepOne.second + zero + stepTwo.second
        )
    }

    /**
     * 从机排液命令生成器
     * @return [List]<[String]>
     */
    private fun recycleLiquid(): Pair<String, String> {
        val zero = "0,0,0,"
        val recycle = settings.motorUnits.toPumpHex(
            one = if (module == 0) -(action.liquidVolume + settings.motorUnits.cali.p1 * settings.container.extract / 1000) else 0f,
            two = if (module == 1) -(action.liquidVolume + settings.motorUnits.cali.p2 * settings.container.extract / 1000) else 0f,
            three = if (module == 2) -(action.liquidVolume + settings.motorUnits.cali.p3 * settings.container.extract / 1000) else 0f,
            four = if (module == 3) -(action.liquidVolume + settings.motorUnits.cali.p4 * settings.container.extract / 1000) else 0f,
        )
        return Pair(
            zero + recycle.first + zero + zero,
            zero + recycle.second + zero + zero
        )
    }

    /**
     * 等待机构空闲
     * @param msg String 提示信息
     * @param block suspend () -> Unit 代码块
     */
    private suspend fun waitForFree(msg: String, block: suspend () -> Unit) {
        if (!serial.runtimeLock.value && !serial.drawer) {
            block.invoke()
        } else {
            _wait.value = msg
            delay(300L)
            waitForFree(msg, block)
        }
    }
}

