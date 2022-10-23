package com.zktony.www.serialport.protocol

import com.zktony.www.model.enum.ModuleEnum
import com.zktony.www.model.enum.SerialPortEnum.*
import com.zktony.www.model.state.SettingState
import com.zktony.www.serialport.SerialPortManager
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
        val commandBlock = listOf(
            CommandBlock.Hex(
                SERIAL_ONE, Command.multiPoint("0,6400,0,0,0,6400,9600,0,0,0,0,0,")
            ),
            CommandBlock.Hex(
                SERIAL_TWO, Command.multiPoint("3200,0,0,3200,0,0,0,0,0,")
            ),
            CommandBlock.Hex(
                SERIAL_THREE, Command.multiPoint("0,0,0,0,0,0,0,0,0,")
            ),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        block.invoke()
    }

    /**
     * 添加一抗代码块
     * @param block
     */
    suspend fun addAntibodyOne(block: suspend () -> Unit) {
        val commandBlock = listOf(
            CommandBlock.Text(SERIAL_FOUR, "${module.value}一抗"),
            CommandBlock.Delay(1000L),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        block.invoke()
    }

    /**
     * 回收一抗代码块
     * @param block
     */
    suspend fun recycleAntibodyOne(block: suspend () -> Unit) {
        val commandBlock = listOf(
            CommandBlock.Text(SERIAL_FOUR, "${module.value}回收一抗"),
            CommandBlock.Delay(3000L),
            CommandBlock.Text(SERIAL_FOUR, "${module.value}放回一抗"),
            CommandBlock.Delay(5000L),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        block.invoke()
    }

    /**
     * 添加二抗代码块
     * @param block
     */
    suspend fun addAntibodyTwo(block: suspend () -> Unit) {
        val commandBlock = listOf(
            CommandBlock.Text(SERIAL_FOUR, "${module.value}吸取二抗"),
            CommandBlock.Delay(3000L),
            CommandBlock.Text(SERIAL_FOUR, "${module.value}添加二抗"),
            CommandBlock.Delay(5000L),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        block.invoke()
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        val commandBlock = listOf(
            CommandBlock.Text(SERIAL_FOUR, "${module.value}吸取洗涤液"),
            CommandBlock.Delay(3000L),
            CommandBlock.Text(SERIAL_FOUR, "${module.value}添加洗涤液"),
            CommandBlock.Delay(5000L),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
        block.invoke()
    }

    /**
     * 回收到废液槽
     *
     * @param block
     */
    suspend fun wasteLiquid(block: suspend () -> Unit) {
        val commandBlock = listOf(
            CommandBlock.Text(SERIAL_FOUR, "${module.value}移动到废液槽"),
            CommandBlock.Delay(3000L),
            CommandBlock.Text(SERIAL_FOUR, "${module.value}排入废液槽"),
            CommandBlock.Delay(5000L),
        )
        serialPortManager.commandQueue.enqueue(commandBlock)
        waitUntilComplete(commandBlock)
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