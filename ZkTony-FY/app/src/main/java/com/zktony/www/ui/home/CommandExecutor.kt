package com.zktony.www.ui.home

import com.zktony.www.common.app.Settings
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.MotionMotor
import com.zktony.www.common.room.entity.PumpMotor
import com.zktony.www.serialport.Serial.*
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.serialport.protocol.Command
import com.zktony.www.ui.home.ModuleEnum.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandExecutor {

    private val serial = SerialPortManager.instance
    private lateinit var module: ModuleEnum
    private lateinit var settings: Settings
    private lateinit var action: Action
    private lateinit var motionMotor: MotionMotor
    private lateinit var pumpMotor: PumpMotor
    private lateinit var calibration: Calibration

    private val _wait = MutableStateFlow("等待中")
    val wait = _wait.asStateFlow()

    fun initModule(module: ModuleEnum) {
        this.module = module
    }

    fun initSettingState(settings: Settings) {
        this.settings = settings
        this.motionMotor = settings.motionMotor
        this.pumpMotor = settings.pumpMotor
        this.calibration = settings.calibration

    }

    fun initAction(action: Action) {
        this.action = action
    }

    /**
     * 添加封闭液
     * @param block
     */
    suspend fun addBlockingLiquid(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, Command.queryDrawer())
        delay(500L)
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = Command.setTemperature(
                    address = module.address.toString(),
                    temperature = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = Command.multiPoint(
                    moveTo(
                        distance = calibration.blockingY,
                        height = calibration.blockingZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serial = TTYS2,
                hex = Command.multiPoint(addLiquid()[1])
            )
            block.invoke()
        }
    }

    /**
     * 添加一抗代码块
     * @param block
     */
    suspend fun addAntibodyOne(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, Command.queryDrawer())
        delay(500L)
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = Command.setTemperature(
                    address = module.address.toString(),
                    temperature = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0, hex = Command.multiPoint(
                    moveTo(
                        distance = calibration.antibodyOneY,
                        height = calibration.antibodyOneZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serial = TTYS2,
                hex = Command.multiPoint(addLiquid()[1])
            )
            block.invoke()
        }
    }

    /**
     * 回收一抗代码块
     * @param block
     */
    suspend fun recycleAntibodyOne(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, Command.queryDrawer())
        delay(500L)
        waitForFree("等待回收") {
            serial.lock(true)
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = Command.multiPoint(
                    moveTo(
                        distance = calibration.antibodyOneY,
                        height = calibration.recycleAntibodyOneZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = Command.multiPoint(recycleLiquid()[0])
            )
            serial.sendHex(
                serial = TTYS2,
                hex = Command.multiPoint(recycleLiquid()[1])
            )
            block.invoke()
        }
    }

    /**
     * 添加二抗代码块
     * @param block
     */
    suspend fun addAntibodyTwo(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, Command.queryDrawer())
        delay(500L)
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = Command.setTemperature(
                    address = module.address.toString(),
                    temperature = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = Command.multiPoint(
                    moveTo(
                        distance = calibration.antibodyTwoY,
                        height = calibration.antibodyTwoZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1, hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serial = TTYS2, hex = Command.multiPoint(addLiquid()[1])
            )
            block.invoke()
        }
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        serial.sendHex(TTYS0, Command.queryDrawer())
        delay(500L)
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serial = TTYS3,
                text = Command.setTemperature(
                    address = module.address.toString(),
                    temperature = action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = Command.multiPoint(
                    moveTo(
                        distance = calibration.washingY,
                        height = calibration.washingZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serial = TTYS2,
                hex = Command.multiPoint(addLiquid()[1])
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
        serial.sendHex(TTYS0, Command.queryDrawer())
        delay(500L)
        waitForFree("等待清理") {
            serial.lock(true)
            // 主板运动
            serial.sendHex(
                serial = TTYS0,
                hex = Command.multiPoint(
                    moveTo(
                        distance = calibration.wasteY,
                        height = calibration.wasteZ
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serial = TTYS1,
                hex = Command.multiPoint(recycleLiquid()[0])
            )
            serial.sendHex(
                serial = TTYS2,
                hex = Command.multiPoint(recycleLiquid()[1])
            )
            block.invoke()
        }
    }

    /**
     * 主机命令生成器
     */
    private fun moveTo(distance: Float, height: Float): String {
        return motionMotor.toMotionHex(distance, 0f) +
                motionMotor.toMotionHex(distance, height) +
                motionMotor.toMotionHex(distance, 0f) +
                motionMotor.toMotionHex(0f, 0f)
    }

    /**
     * 从板子加液命令生成器
     * @return [List]<[String]>
     */
    private fun addLiquid(): List<String> {
        val zero = "0,0,0,"
        // 吸液
        val stepOne = pumpMotor.toPumpHex(
            one = if (module == A) action.liquidVolume else 0f,
            two = if (module == B) action.liquidVolume else 0f,
            three = if (module == C) action.liquidVolume else 0f,
            four = if (module == D) action.liquidVolume else 0f,
            five = if (action.mode == 3) action.liquidVolume else 0f
        )
        // 清空管路
        val stepTwo = pumpMotor.toPumpHex(
            one = if (module == A) pumpMotor.volumeOne * calibration.extract / 1000 else 0f,
            two = if (module == B) pumpMotor.volumeTwo * calibration.extract / 1000 else 0f,
            three = if (module == C) pumpMotor.volumeThree * calibration.extract / 1000 else 0f,
            four = if (module == D) pumpMotor.volumeFour * calibration.extract / 1000 else 0f,
        )
        return listOf(
            zero + stepOne[0] + zero + stepTwo[0],
            zero + stepOne[1] + zero + stepTwo[1]
        )
    }

    /**
     * 从机排液命令生成器
     * @return [List]<[String]>
     */
    private fun recycleLiquid(): List<String> {
        val zero = "0,0,0,"
        val recycle = pumpMotor.toPumpHex(
            one = if (module == A) -(action.liquidVolume + pumpMotor.volumeOne * calibration.extract / 1000) else 0f,
            two = if (module == B) -(action.liquidVolume + pumpMotor.volumeTwo * calibration.extract / 1000) else 0f,
            three = if (module == C) -(action.liquidVolume + pumpMotor.volumeThree * calibration.extract / 1000) else 0f,
            four = if (module == D) -(action.liquidVolume + pumpMotor.volumeFour * calibration.extract / 1000) else 0f,
        )
        return listOf(
            zero + recycle[0] + zero + zero,
            zero + recycle[1] + zero + zero
        )
    }

    /**
     * 等待机构空闲
     * @param msg String 提示信息
     * @param block suspend () -> Unit 代码块
     */
    private suspend fun waitForFree(msg: String, block: suspend () -> Unit) {
        if (serial.isLock() || serial.isDrawerOpen()) {
            _wait.value = msg
            delay(300L)
            waitForFree(msg, block)
        } else {
            block.invoke()
        }
    }

    companion object {
        @JvmStatic
        val instance: CommandExecutor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CommandExecutor()
        }
    }
}

