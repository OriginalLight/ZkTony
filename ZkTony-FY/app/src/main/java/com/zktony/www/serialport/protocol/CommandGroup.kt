package com.zktony.www.serialport.protocol

import com.zktony.www.common.Logger
import com.zktony.www.common.app.SettingState
import com.zktony.www.data.entity.Action
import com.zktony.www.data.entity.Calibration
import com.zktony.www.data.entity.MotionMotor
import com.zktony.www.data.entity.PumpMotor
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
        val commandBlock = mutableListOf<CommandBlock>()
        // 暂停摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.pauseShakeBed()))
        // 设置温度
        commandBlock.add(
            CommandBlock.Text(
                SERIAL_FOUR,
                Command.setTemperature(module.index.toString(), action.temperature.toString())
            )
        )
        // 主板运动命令
        commandBlock.add(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    move(
                        calibration.blockingLiquidTankPosition, calibration.blockingLiquidTankHeight
                    )
                )
            )
        )
        // 从板子一运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_TWO, Command.multiPoint(addLiquidB())))
        // 从板子二运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_THREE, Command.multiPoint(addLiquidC())))
        // 运动加液延时
        commandBlock.add(
            CommandBlock.Delay(
                motionMotor.delayTime(
                    calibration.blockingLiquidTankPosition * 2,
                    calibration.blockingLiquidTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        // 恢复摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.resumeShakeBed()))
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
        val commandBlock = mutableListOf<CommandBlock>()
        // 暂停摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.pauseShakeBed()))
        // 设置温度
        commandBlock.add(
            CommandBlock.Text(
                SERIAL_FOUR,
                Command.setTemperature(module.index.toString(), action.temperature.toString())
            )
        )
        // 主板运动命令
        commandBlock.add(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    move(
                        calibration.antibodyOneTankPosition, calibration.antibodyOneTankHeight
                    )
                )
            )
        )
        // 从板子一运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_TWO, Command.multiPoint(addLiquidB())))
        // 从板子二运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_THREE, Command.multiPoint(addLiquidC())))
        // 运动加液延时
        commandBlock.add(
            CommandBlock.Delay(
                motionMotor.delayTime(
                    calibration.antibodyOneTankPosition * 2, calibration.antibodyOneTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        // 恢复摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.resumeShakeBed()))
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
        val commandBlock = mutableListOf<CommandBlock>()
        // 暂停摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.pauseShakeBed()))
        // 设置温度
        commandBlock.add(
            CommandBlock.Text(
                SERIAL_FOUR,
                Command.setTemperature(module.index.toString(), action.temperature.toString())
            )
        )
        // 主板运动命令
        commandBlock.add(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    move(
                        calibration.antibodyOneTankPosition,
                        calibration.recycleAntibodyOneTankHeight
                    )
                )
            )
        )
        // 从板子一运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_TWO, Command.multiPoint(recycleLiquidB())))
        // 从板子二运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_THREE, Command.multiPoint(recycleLiquidC())))
        // 运动加液延时
        commandBlock.add(
            CommandBlock.Delay(
                motionMotor.delayTime(
                    calibration.antibodyOneTankPosition * 2,
                    calibration.recycleAntibodyOneTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        // 恢复摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.resumeShakeBed()))
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
        val commandBlock = mutableListOf<CommandBlock>()
        // 暂停摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.pauseShakeBed()))
        // 设置温度
        commandBlock.add(
            CommandBlock.Text(
                SERIAL_FOUR,
                Command.setTemperature(module.index.toString(), action.temperature.toString())
            )
        )
        // 主板运动命令
        commandBlock.add(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    move(
                        calibration.antibodyTwoTankPosition, calibration.antibodyTwoTankHeight
                    )
                )
            )
        )
        // 从板子一运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_TWO, Command.multiPoint(addLiquidB())))
        // 从板子二运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_THREE, Command.multiPoint(addLiquidC())))
        // 运动加液延时
        commandBlock.add(
            CommandBlock.Delay(
                motionMotor.delayTime(
                    calibration.antibodyTwoTankPosition * 2, calibration.antibodyTwoTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        // 恢复摇床
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
        val commandBlock = mutableListOf<CommandBlock>()
        // 暂停摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.pauseShakeBed()))
        // 设置温度
        commandBlock.add(
            CommandBlock.Text(
                SERIAL_FOUR,
                Command.setTemperature(module.index.toString(), action.temperature.toString())
            )
        )
        // 主板运动命令
        commandBlock.add(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    move(
                        calibration.washTankPosition, calibration.washTankHeight
                    )
                )
            )
        )
        // 从板子一运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_TWO, Command.multiPoint(addLiquidB())))
        // 从板子二运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_THREE, Command.multiPoint(addLiquidC())))
        // 运动加液延时
        commandBlock.add(
            CommandBlock.Delay(
                motionMotor.delayTime(
                    calibration.washTankPosition * 2, calibration.washTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        // 恢复摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.resumeShakeBed()))
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
        val commandBlock = mutableListOf<CommandBlock>()
        // 暂停摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.pauseShakeBed()))
        // 设置温度
        commandBlock.add(
            CommandBlock.Text(
                SERIAL_FOUR,
                Command.setTemperature(module.index.toString(), action.temperature.toString())
            )
        )
        // 主板运动命令
        commandBlock.add(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    move(
                        calibration.wasteTankPosition, calibration.wasteTankHeight
                    )
                )
            )
        )
        // 从板子一运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_TWO, Command.multiPoint(recycleLiquidB())))
        // 从板子二运动命令
        commandBlock.add(CommandBlock.Hex(SERIAL_THREE, Command.multiPoint(recycleLiquidC())))
        // 运动加液延时
        commandBlock.add(
            CommandBlock.Delay(
                motionMotor.delayTime(
                    calibration.wasteTankPosition * 2, calibration.wasteTankHeight * 2
                ) + addLiquidDelay() + 5000
            )
        )
        // 恢复摇床
        commandBlock.add(CommandBlock.Hex(SERIAL_ONE, Command.resumeShakeBed()))
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
    private fun move(distance: Float, height: Float): String {
        return motionMotor.toMultiPointHex(distance, 0f) + motionMotor.toMultiPointHex(
            distance,
            height
        ) + motionMotor.toMultiPointHex(distance, 0f) + motionMotor.toMultiPointHex(0f, 0f)
    }

    /**
     * 从机B加液命令生成器
     * @return [String] 命令字符串
     */
    private fun addLiquidB(): String {
        when (module) {
            A -> {
                return pumpMotor.toMultiPointHexB(
                    0f,
                    0f,
                    0f
                ) + pumpMotor.toMultiPointHexB(
                    action.liquidVolume,
                    0f,
                    0f
                ) + pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    action.liquidVolume + (200 * pumpMotor.pumpOne.subdivision * calibration.drainDistance),
                    0f,
                    0f
                )
            }
            B -> {
                return pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    0f,
                    action.liquidVolume,
                    0f
                ) + pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    0f,
                    action.liquidVolume + (200 * pumpMotor.pumpOne.subdivision * calibration.drainDistance),
                    0f
                )
            }
            C -> {
                return pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    0f,
                    0f,
                    action.liquidVolume
                ) + pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    0f,
                    0f,
                    action.liquidVolume + (200 * pumpMotor.pumpOne.subdivision * calibration.drainDistance)
                )
            }
            D -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
        }
    }

    /**
     * 从机C加液命令生成器
     * @return [String] 命令字符串
     */
    private fun addLiquidC(): String {
        when (module) {
            A -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
            B -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
            C -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
            D -> {
                return pumpMotor.toMultiPointHexC(
                    0f,
                    0f
                ) + pumpMotor.toMultiPointHexC(
                    action.liquidVolume,
                    0f
                ) + pumpMotor.toMultiPointHexC(0f, 0f) + pumpMotor.toMultiPointHexC(
                    action.liquidVolume + (200 * pumpMotor.pumpOne.subdivision * calibration.drainDistance),
                    0f
                )
            }
        }
    }

    /**
     * 从机B排液命令生成器
     * @return [String] 命令字符串
     */
    private fun recycleLiquidB(): String {
        when (module) {
            A -> {
                return pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    -(action.liquidVolume + (calibration.pumpOneDistance * calibration.drainDistance)),
                    0f,
                    0f
                ) + pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(0f, 0f, 0f)
            }
            B -> {
                return pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    0f,
                    -(action.liquidVolume + (calibration.pumpTwoDistance * calibration.drainDistance)),
                    0f
                ) + pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(0f, 0f, 0f)
            }
            C -> {
                return pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(
                    0f,
                    0f,
                    -(action.liquidVolume + (calibration.pumpThreeDistance * calibration.drainDistance))
                ) + pumpMotor.toMultiPointHexB(0f, 0f, 0f) + pumpMotor.toMultiPointHexB(0f, 0f, 0f)
            }
            D -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
        }
    }

    /**
     * 从机C排液命令生成器
     * @return [String] 命令字符串
     */
    private fun recycleLiquidC(): String {
        when (module) {
            A -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
            B -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
            C -> {
                return "0,0,0,0,0,0,0,0,0,0,0,0,"
            }
            D -> {
                return pumpMotor.toMultiPointHexC(0f, 0f) + pumpMotor.toMultiPointHexC(
                    -(action.liquidVolume + (calibration.pumpFourDistance * calibration.drainDistance)),
                    0f
                ) + pumpMotor.toMultiPointHexC(0f, 0f) + pumpMotor.toMultiPointHexC(0f, 0f)
            }
        }
    }

    /**
     * 加液等待时间
     * @return [Long]
     */
    private fun addLiquidDelay(): Long {
        return when (module) {
            A -> {
                pumpMotor.delayTimeB(
                    action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpOneDistance,
                    0f,
                    0f
                )
            }
            B -> {
                pumpMotor.delayTimeB(
                    0f,
                    action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpTwoDistance,
                    0f
                )
            }
            C -> {
                pumpMotor.delayTimeB(
                    0f,
                    0f,
                    action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpThreeDistance
                )
            }
            D -> {
                pumpMotor.delayTimeC(
                    action.liquidVolume * 2 + calibration.drainDistance * calibration.pumpFourDistance,
                    0f
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
    data class Hex(val serialPort: SerialPortEnum, val hex: String) : CommandBlock()
    data class Text(val serialPort: SerialPortEnum, val text: String) : CommandBlock()
    data class Delay(val delay: Long) : CommandBlock()
}