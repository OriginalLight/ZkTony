package com.zktony.www.ui.home

import com.zktony.www.common.app.SettingState
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.MotionMotor
import com.zktony.www.common.room.entity.PumpMotor
import com.zktony.www.serialport.SerialPort.*
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
    private lateinit var settingState: SettingState
    private lateinit var action: Action
    private lateinit var motionMotor: MotionMotor
    private lateinit var pumpMotor: PumpMotor
    private lateinit var calibration: Calibration

    private val _wait = MutableStateFlow("等待中")
    val wait = _wait.asStateFlow()

    fun initModule(module: ModuleEnum) {
        this.module = module
    }

    fun initSettingState(settingState: SettingState) {
        this.settingState = settingState
        this.motionMotor = settingState.motionMotor
        this.pumpMotor = settingState.pumpMotor
        this.calibration = settingState.calibration

    }

    fun initAction(action: Action) {
        this.action = action
    }

    /**
     * 添加封闭液
     * @param block
     */
    suspend fun addBlockingLiquid(block: suspend () -> Unit) {
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serialPort = SERIAL_FOUR,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serialPort = SERIAL_ONE,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.blockingLiquidTankPosition,
                        calibration.blockingLiquidTankHeight
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serialPort = SERIAL_TWO,
                hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serialPort = SERIAL_THREE,
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
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serialPort = SERIAL_FOUR,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serialPort = SERIAL_ONE,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.antibodyOneTankPosition,
                        calibration.antibodyOneTankHeight
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serialPort = SERIAL_TWO,
                hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serialPort = SERIAL_THREE,
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
        waitForFree("等待回收") {
            serial.lock(true)
            // 主板运动
            serial.sendHex(
                serialPort = SERIAL_ONE,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.antibodyOneTankPosition,
                        calibration.recycleAntibodyOneTankHeight
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serialPort = SERIAL_TWO,
                hex = Command.multiPoint(recycleLiquid()[0])
            )
            serial.sendHex(
                serialPort = SERIAL_THREE,
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
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serialPort = SERIAL_FOUR,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serialPort = SERIAL_ONE,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.antibodyTwoTankPosition,
                        calibration.antibodyTwoTankHeight
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serialPort = SERIAL_TWO,
                hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serialPort = SERIAL_THREE,
                hex = Command.multiPoint(addLiquid()[1])
            )
            block.invoke()
        }
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        waitForFree("等待加液") {
            serial.lock(true)
            // 设置温度
            serial.sendText(
                serialPort = SERIAL_FOUR,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            )
            // 主板运动
            serial.sendHex(
                serialPort = SERIAL_ONE,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.washTankPosition,
                        calibration.washTankHeight
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serialPort = SERIAL_TWO,
                hex = Command.multiPoint(addLiquid()[0])
            )
            serial.sendHex(
                serialPort = SERIAL_THREE,
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
        waitForFree("等待清理") {
            serial.lock(true)
            // 主板运动
            serial.sendHex(
                serialPort = SERIAL_ONE,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.wasteTankPosition,
                        calibration.wasteTankHeight
                    )
                )
            )
            // 泵运动
            serial.sendHex(
                serialPort = SERIAL_TWO,
                hex = Command.multiPoint(recycleLiquid()[0])
            )
            serial.sendHex(
                serialPort = SERIAL_THREE,
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
        val stepOne = pumpMotor.toPumpHex(
            one = if (module == A) action.liquidVolume else 0f,
            two = if (module == B) action.liquidVolume else 0f,
            three = if (module == C) action.liquidVolume else 0f,
            four = if (module == D) action.liquidVolume else 0f,
            five = if (action.mode == 3) action.liquidVolume else 0f
        )
        val stepTwo = pumpMotor.toPumpHex(
            one = if(module == A) action.liquidVolume + pumpMotor.volumeOne * calibration.drainDistance else 0f,
            two = if(module == B) action.liquidVolume + pumpMotor.volumeTwo * calibration.drainDistance else 0f,
            three = if(module == C) action.liquidVolume + pumpMotor.volumeThree * calibration.drainDistance else 0f,
            four = if(module == D) action.liquidVolume + pumpMotor.volumeFour * calibration.drainDistance else 0f,
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
            one = if (module == A) -(action.liquidVolume + pumpMotor.volumeOne * calibration.drainDistance) else 0f,
            two = if (module == B) -(action.liquidVolume + pumpMotor.volumeTwo * calibration.drainDistance) else 0f,
            three = if (module == C) -(action.liquidVolume + pumpMotor.volumeThree * calibration.drainDistance) else 0f,
            four = if (module == D) -(action.liquidVolume + pumpMotor.volumeFour * calibration.drainDistance) else 0f,
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
        if (serial.isLock()) {
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

