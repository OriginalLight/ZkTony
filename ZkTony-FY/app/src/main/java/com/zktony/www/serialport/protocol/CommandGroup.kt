package com.zktony.www.serialport.protocol

import com.zktony.www.common.app.SettingState
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.SerialPortEnum.*
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.ui.home.ModuleEnum
import kotlinx.coroutines.delay

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandGroup {

    private val serialPortManager = SerialPortManager.instance
    private lateinit var module: ModuleEnum
    private lateinit var settingState: SettingState

    fun initModule(module: ModuleEnum) {
        this.module = module
    }

    fun initSettingState(settingState: SettingState) {
        this.settingState = settingState

    }


    /**
     * 添加封闭液
     * @param block
     */
    suspend fun addBlockingLiquid(block: suspend () -> Unit) {
        val motionMotor = settingState.motionMotor
        val calibration = settingState.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.blockingLiquidTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.blockingLiquidTankPosition, calibration.blockingLiquidTankHeight) +
                            motionMotor.toMultiPointHex(calibration.blockingLiquidTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SERIAL_TWO, Command.multiPoint("0,0,0,6400,0,0,0,0,0,32000,0,0,")),
            CommandBlock.Hex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,0,0,0,0,0,0,")),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock, 45 * 1000L)
        block.invoke()
    }

    /**
     * 添加一抗代码块
     * @param block
     */
    suspend fun addAntibodyOne(block: suspend () -> Unit) {
        val motionMotor = settingState.motionMotor
        val calibration = settingState.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, calibration.antibodyOneTankHeight) +
                            motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SERIAL_TWO, Command.multiPoint("0,0,0,6400,0,0,0,0,0,32000,0,0,")),
            CommandBlock.Hex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,0,0,0,0,0,0,")),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock, 45 * 1000L)
        block.invoke()
    }

    /**
     * 回收一抗代码块
     * @param block
     */
    suspend fun recycleAntibodyOne(block: suspend () -> Unit) {
        val motionMotor = settingState.motionMotor
        val calibration = settingState.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, calibration.wasteTankHeight) +
                            motionMotor.toMultiPointHex(calibration.antibodyOneTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SERIAL_TWO, Command.multiPoint("0,0,0,-32000,0,0,0,0,0,0,0,0,")),
            CommandBlock.Hex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,0,0,0,0,0,0,")),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock, 45 * 1000L)
        block.invoke()
    }

    /**
     * 添加二抗代码块
     * @param block
     */
    suspend fun addAntibodyTwo(block: suspend () -> Unit) {
        val motionMotor = settingState.motionMotor
        val calibration = settingState.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.antibodyTwoTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.antibodyTwoTankPosition, calibration.antibodyTwoTankHeight) +
                            motionMotor.toMultiPointHex(calibration.antibodyTwoTankPosition, 0f) +
                            motionMotor.toMultiPointHex(0f, 0f)
                )
            ),
            CommandBlock.Hex(SERIAL_TWO, Command.multiPoint("0,0,0,6400,0,0,0,0,0,32000,0,0,")),
            CommandBlock.Hex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,0,0,0,0,0,0,")),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock, 45 * 1000L)
        block.invoke()
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        val motionMotor = settingState.motionMotor
        val calibration = settingState.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.washTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.washTankPosition, calibration.washTankHeight) +
                            motionMotor.toMultiPointHex(calibration.washTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.washTankPosition, 0f)
                )
            ),
            CommandBlock.Hex(SERIAL_TWO, Command.multiPoint("0,0,0,6400,0,0,0,0,0,32000,0,0,")),
            CommandBlock.Hex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,0,0,0,0,0,0,")),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock, 45 * 1000L)
        block.invoke()
    }

    /**
     * 回收到废液槽
     *
     * @param block
     */
    suspend fun wasteLiquid(block: suspend () -> Unit) {
        val motionMotor = settingState.motionMotor
        val calibration = settingState.calibration
        val commandBlock = listOf(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint(
                    motionMotor.toMultiPointHex(calibration.wasteTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.wasteTankPosition, calibration.wasteTankHeight) +
                            motionMotor.toMultiPointHex(calibration.wasteTankPosition, 0f) +
                            motionMotor.toMultiPointHex(calibration.wasteTankPosition, 0f)
                )
            ),
            CommandBlock.Hex(SERIAL_TWO, Command.multiPoint("0,0,0,-96000,0,0,0,0,0,0,0,0,")),
            CommandBlock.Hex(SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,0,0,0,0,0,0,")),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock, 25 * 1000L)
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