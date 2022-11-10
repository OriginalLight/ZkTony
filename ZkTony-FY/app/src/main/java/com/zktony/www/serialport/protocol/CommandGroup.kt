package com.zktony.www.serialport.protocol

import com.zktony.www.common.app.SettingState
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.MotionMotor
import com.zktony.www.common.room.entity.PumpMotor
import com.zktony.www.common.utils.Logger
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.SerialPortEnum.*
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.ui.home.ModuleEnum
import com.zktony.www.ui.home.ModuleEnum.*
import kotlinx.coroutines.delay

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandGroup {

    private val serialPortManager = SerialPortManager.instance
    private lateinit var module: ModuleEnum
    private lateinit var settingState: SettingState
    private lateinit var action: Action
    private lateinit var motionMotor: MotionMotor
    private lateinit var pumpMotor: PumpMotor
    private lateinit var calibration: Calibration

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
        Logger.d(msg = "${module.value} 添加封闭液")
        val commandBlock = listOf(
            // 设置温度
            CommandBlock.Text(
                serialPort = SERIAL_FOUR,
                module = module,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            ),
            // 主板运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_ONE,
                module = module,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.blockingLiquidTankPosition, calibration.blockingLiquidTankHeight
                    )
                )
            ),
            // 从板子一运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_TWO,
                module = module,
                hex = Command.multiPoint(addLiquid()[0])
            ),
            // 从板子二运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_THREE,
                module = module,
                hex = Command.multiPoint(addLiquid()[1])
            ),
            // 运动加液延时
            CommandBlock.Delay(
                module = module,
                delay = motionMotor.delayTime(
                    calibration.blockingLiquidTankPosition * 2,
                    calibration.blockingLiquidTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        Logger.d(msg = "${module.value} 添加封闭液完成")
        block.invoke()
    }

    /**
     * 添加一抗代码块
     * @param block
     */
    suspend fun addAntibodyOne(block: suspend () -> Unit) {
        Logger.d(msg = "${module.value} 添加一抗")
        val commandBlock = listOf(
            // 设置温度
            CommandBlock.Text(
                serialPort = SERIAL_FOUR,
                module = module,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            ),
            // 主板运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_ONE,
                module = module,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.antibodyOneTankPosition, calibration.antibodyOneTankHeight
                    )
                )
            ),
            // 从板子一运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_TWO,
                module = module,
                hex = Command.multiPoint(addLiquid()[0])
            ),
            // 从板子二运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_THREE,
                module = module,
                hex = Command.multiPoint(addLiquid()[1])
            ),
            // 运动加液延时
            CommandBlock.Delay(
                module = module,
                delay = motionMotor.delayTime(
                    calibration.antibodyOneTankPosition * 2, calibration.antibodyOneTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        Logger.d(msg = "添加一抗完成")
        block.invoke()
    }

    /**
     * 回收一抗代码块
     * @param block
     */
    suspend fun recycleAntibodyOne(block: suspend () -> Unit) {
        Logger.d(msg = "${module.value} 回收一抗")
        val commandBlock = listOf(
            // 设置温度
            CommandBlock.Text(
                serialPort = SERIAL_FOUR,
                module = module,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            ),
            // 主板运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_ONE,
                module = module,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.antibodyOneTankPosition,
                        calibration.recycleAntibodyOneTankHeight
                    )
                )
            ),
            // 从板子一运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_TWO,
                module = module,
                hex = Command.multiPoint(recycleLiquid()[0])
            ),
            // 从板子二运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_THREE,
                module = module,
                hex = Command.multiPoint(recycleLiquid()[1])
            ),
            // 运动加液延时
            CommandBlock.Delay(
                module = module,
                delay = motionMotor.delayTime(
                    calibration.antibodyOneTankPosition * 2,
                    calibration.recycleAntibodyOneTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        Logger.d(msg = "${module.value} 回收一抗完成")
        block.invoke()
    }

    /**
     * 添加二抗代码块
     * @param block
     */
    suspend fun addAntibodyTwo(block: suspend () -> Unit) {
        Logger.d(msg = "${module.value} 添加二抗")
        val commandBlock = listOf(
            // 设置温度
            CommandBlock.Text(
                serialPort = SERIAL_FOUR,
                module = module,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            ),
            // 主板运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_ONE,
                module = module,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.antibodyTwoTankPosition, calibration.antibodyTwoTankHeight
                    )
                )
            ),
            // 从板子一运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_TWO,
                module = module,
                hex = Command.multiPoint(addLiquid()[0])
            ),
            // 从板子二运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_THREE,
                module = module,
                hex = Command.multiPoint(addLiquid()[1])
            ),
            // 运动加液延时
            CommandBlock.Delay(
                module = module,
                delay = motionMotor.delayTime(
                    calibration.antibodyTwoTankPosition * 2, calibration.antibodyTwoTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        Logger.d(msg = "${module.value} 添加二抗完成")
        block.invoke()
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        Logger.d(msg = "${module.value} 添加洗涤液")
        val commandBlock = listOf(
            // 设置温度
            CommandBlock.Text(
                serialPort = SERIAL_FOUR,
                module = module,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            ),
            // 主板运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_ONE,
                module = module,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.washTankPosition, calibration.washTankHeight
                    )
                )
            ),
            // 从板子一运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_TWO,
                module = module,
                hex = Command.multiPoint(addLiquid()[0])
            ),
            // 从板子二运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_THREE,
                module = module,
                hex = Command.multiPoint(addLiquid()[1])
            ),
            // 运动加液延时
            CommandBlock.Delay(
                module = module,
                delay = motionMotor.delayTime(
                    calibration.washTankPosition * 2, calibration.washTankHeight * 2
                ) + addLiquidDelay() + 5000
            )

        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        Logger.d(msg = "${module.value} 添加洗涤液完成")
        block.invoke()
    }

    /**
     * 回收到废液槽
     *
     * @param block
     */
    suspend fun wasteLiquid(block: suspend () -> Unit) {
        Logger.d(msg = "${module.value} 回收到废液槽")
        val commandBlock = listOf(
            // 设置温度
            CommandBlock.Text(
                serialPort = SERIAL_FOUR,
                module = module,
                text = Command.setTemperature(
                    module.index.toString(),
                    action.temperature.toString()
                )
            ),
            // 主板运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_ONE,
                module = module,
                hex = Command.multiPoint(
                    moveTo(
                        calibration.wasteTankPosition, calibration.wasteTankHeight
                    )
                )
            ),
            // 从板子一运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_TWO,
                module = module,
                hex = Command.multiPoint(recycleLiquid()[0])
            ),
            // 从板子二运动命令
            CommandBlock.Hex(
                serialPort = SERIAL_THREE,
                module = module,
                hex = Command.multiPoint(recycleLiquid()[1])
            ),
            // 运动加液延时
            CommandBlock.Delay(
                module = module,
                delay = motionMotor.delayTime(
                    calibration.wasteTankPosition * 2, calibration.wasteTankHeight * 2
                ) + addLiquidDelay() + 5000
            )

        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        Logger.d(msg = "回收到废液槽完成")
        block.invoke()
    }

    /**
     * 等待指令执行完成
     * @param commandBlock
     */
    private suspend fun waitUntilComplete(commandBlock: List<CommandBlock>, delay: Long = 1000L) {
        delay(1000L)
        if (serialPortManager.checkQueueExistBlock(commandBlock)) {
            waitUntilComplete(commandBlock)
        } else {
            delay(delay)
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

    /**
     * 加液等待时间
     * @return [Long]
     */
    private fun addLiquidDelay(): Long {
        return when (module) {
            A -> {
                pumpMotor.delayTime(
                    one = action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpOneDistance,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
            }
            B -> {
                pumpMotor.delayTime(
                    two = action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpTwoDistance,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
            }
            C -> {
                pumpMotor.delayTime(
                    three = action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpThreeDistance,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
            }
            D -> {
                pumpMotor.delayTime(
                    four = action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpFourDistance,
                    five = if (action.mode == 3) action.liquidVolume else 0f
                )
            }
        }
    }


    companion object {
        @JvmStatic
        val instance: CommandGroup by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CommandGroup()
        }
    }

}

sealed class CommandBlock {
    data class Hex(val serialPort: SerialPortEnum, val module: ModuleEnum, val hex: String) :
        CommandBlock()

    data class Text(val serialPort: SerialPortEnum, val module: ModuleEnum, val text: String) :
        CommandBlock()

    data class Delay(val module: ModuleEnum, val delay: Long) : CommandBlock()
}