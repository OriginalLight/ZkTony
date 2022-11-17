package com.zktony.www.serialport.protocol

import com.zktony.www.common.app.SettingState
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.MotionMotor
import com.zktony.www.common.room.entity.PumpMotor
import com.zktony.www.common.utils.Logger
import com.zktony.www.serialport.SerialPort.*
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.ui.home.ModuleEnum
import com.zktony.www.ui.home.ModuleEnum.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandGroup {

    private val serial = SerialPortManager.instance
    private lateinit var module: ModuleEnum
    private lateinit var settingState: SettingState
    private lateinit var action: Action
    private lateinit var motionMotor: MotionMotor
    private lateinit var pumpMotor: PumpMotor
    private lateinit var calibration: Calibration

    private val _wait = MutableStateFlow("等待中...")
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
        waitForFree {
            serial.run(true)
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
                        calibration.blockingLiquidTankPosition, calibration.blockingLiquidTankHeight
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
        waitForFree {
            serial.run(true)
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
                        calibration.antibodyOneTankPosition, calibration.antibodyOneTankHeight
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
        waitForFree {
            serial.run(true)
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
        waitForFree {
            serial.run(true)
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
        waitForFree {
            serial.run(true)
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
        waitForFree {
            serial.run(true)
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
        val list = mutableListOf<String>()
        when (module) {
            A -> {
                val stepOne = pumpMotor.toPumpHex(
                    one = action.liquidVolume,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
                val stepTwo = pumpMotor.toPumpHex(
                    one = action.liquidVolume + pumpMotor.volumeOne * calibration.drainDistance
                )

                list.add(zero + stepOne[0] + zero + stepTwo[0])
                list.add(zero + stepOne[1] + zero + stepTwo[1])
            }
            B -> {
                val stepOne = pumpMotor.toPumpHex(
                    two = action.liquidVolume,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
                val stepTwo = pumpMotor.toPumpHex(
                    two = action.liquidVolume + pumpMotor.volumeTwo * calibration.drainDistance
                )

                list.add(zero + stepOne[0] + zero + stepTwo[0])
                list.add(zero + stepOne[1] + zero + stepTwo[1])
            }
            C -> {
                val stepOne = pumpMotor.toPumpHex(
                    three = action.liquidVolume,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
                val stepTwo = pumpMotor.toPumpHex(
                    three = action.liquidVolume + pumpMotor.volumeThree * calibration.drainDistance
                )

                list.add(zero + stepOne[0] + zero + stepTwo[0])
                list.add(zero + stepOne[1] + zero + stepTwo[1])
            }
            D -> {
                val stepOne = pumpMotor.toPumpHex(
                    four = action.liquidVolume,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
                val stepTwo = pumpMotor.toPumpHex(
                    four = action.liquidVolume + pumpMotor.volumeFour * calibration.drainDistance
                )

                list.add(zero + stepOne[0] + zero + stepTwo[0])
                list.add(zero + stepOne[1] + zero + stepTwo[1])
            }
        }
        return list
    }

    /**
     * 从机排液命令生成器
     * @return [List]<[String]>
     */
    private fun recycleLiquid(): List<String> {
        val zero = "0,0,0,"
        val list = mutableListOf<String>()
        when (module) {
            A -> {
                val recycle = pumpMotor.toPumpHex(
                    one = -(action.liquidVolume + pumpMotor.volumeOne * calibration.drainDistance),
                )
                list.add(zero + recycle[0] + zero + zero)
                list.add(zero + recycle[1] + zero + zero)
            }
            B -> {
                val recycle = pumpMotor.toPumpHex(
                    two = -(action.liquidVolume + pumpMotor.volumeTwo * calibration.drainDistance),
                )
                list.add(zero + recycle[0] + zero + zero)
                list.add(zero + recycle[1] + zero + zero)
            }
            C -> {
                val recycle = pumpMotor.toPumpHex(
                    three = -(action.liquidVolume + pumpMotor.volumeThree * calibration.drainDistance),
                )
                list.add(zero + recycle[0] + zero + zero)
                list.add(zero + recycle[1] + zero + zero)
            }
            D -> {
                val recycle = pumpMotor.toPumpHex(
                    four = -(action.liquidVolume + pumpMotor.volumeFour * calibration.drainDistance),
                )
                list.add(zero + recycle[0] + zero + zero)
                list.add(zero + recycle[1] + zero + zero)
            }
        }
        return list
    }

    private suspend fun waitForFree(block: suspend () -> Unit) {
        if (serial.isRunning()) {
            _wait.value = "等待中..."
            delay(300L)
            waitForFree(block)
        } else {
            block.invoke()
        }
    }


    companion object {
        @JvmStatic
        val instance: CommandGroup by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CommandGroup()
        }
    }
}

