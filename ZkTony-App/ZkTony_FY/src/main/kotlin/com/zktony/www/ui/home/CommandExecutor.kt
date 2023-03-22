package com.zktony.www.ui.home

import com.zktony.serialport.util.Serial.TTYS0
import com.zktony.www.data.local.room.entity.Action
import com.zktony.www.data.local.room.entity.Container
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandExecutor constructor(
    private val module: Int,
    private val con: Container,
    private val event: (String) -> Unit = { }
) {
    private lateinit var action: Action
    private val serialManager: SerialManager by inject(SerialManager::class.java)
    private val executionManager: ExecutionManager by inject(ExecutionManager::class.java)

    fun initAction(action: Action) {
        this.action = action
    }

    /**
     * 添加封闭液
     * @param block
     */
    suspend fun addBlockingLiquid(block: suspend () -> Unit) {
        serialManager.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serialManager.setTemp(
                addr = module + 1,
                temp = action.temperature.toString()
            )
            addLiquid(y = con.blockY, z = con.blockZ)
            event("加液中")
            delay(100L)
            while (serialManager.lock.value) {
                delay(20L)
            }
            block.invoke()
        }
    }

    /**
     * 添加一抗代码块
     * @param block
     */
    suspend fun addAntibodyOne(block: suspend () -> Unit) {
        serialManager.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serialManager.setTemp(
                addr = module + 1,
                temp = action.temperature.toString()
            )
            addLiquid(y = con.oneY, z = con.oneZ)
            event("加液中")
            delay(100L)
            while (serialManager.lock.value) {
                delay(20L)
            }
            block.invoke()
        }
    }

    /**
     * 回收一抗代码块
     * @param block
     */
    suspend fun recycleAntibodyOne(block: suspend () -> Unit) {
        serialManager.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            recycleLiquid(y = con.oneY, z = con.recycleOneZ)
            event("回收中")
            delay(100L)
            while (serialManager.lock.value) {
                delay(20L)
            }
            block.invoke()
        }
    }

    /**
     * 添加二抗代码块
     * @param block
     */
    suspend fun addAntibodyTwo(block: suspend () -> Unit) {
        serialManager.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serialManager.setTemp(
                addr = module + 1,
                temp = action.temperature.toString()
            )
            addLiquid(y = con.twoY, z = con.twoZ)
            event("加液中")
            delay(100L)
            while (serialManager.lock.value) {
                delay(20L)
            }
            block.invoke()
        }
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        serialManager.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            // 设置温度
            serialManager.setTemp(
                addr = module + 1,
                temp = action.temperature.toString()
            )
            // 主板运动
            addLiquid(y = con.washY, z = con.washZ)
            event("加液中")
            delay(100L)
            while (serialManager.lock.value) {
                delay(20L)
            }
            block.invoke()
        }
    }

    /**
     * 回收到废液槽
     *
     * @param block
     */
    suspend fun wasteLiquid(block: suspend () -> Unit) {
        serialManager.sendHex(TTYS0, V1.queryDrawer())
        delay(500L)
        waitForFree {
            recycleLiquid(y = con.washY, z = con.wasteZ)
            event("清理中")
            delay(100L)
            while (serialManager.lock.value) {
                delay(20L)
            }
            block.invoke()
        }
    }

    /**
     * 主机命令生成器
     */
    private fun addLiquid(y: Float, z: Float) {
        executionManager.executor(
            executionManager.generator(y = y),
            executionManager.generator(
                y = y,
                z = z,
                v1 = if (module == 0) action.liquidVolume else 0f,
                v2 = if (module == 1) action.liquidVolume else 0f,
                v3 = if (module == 2) action.liquidVolume else 0f,
                v4 = if (module == 3) action.liquidVolume else 0f,
                v5 = if (action.mode == 3) action.liquidVolume else 0f
            ),
            executionManager.generator(
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
        executionManager.executor(
            executionManager.generator(y = y),
            executionManager.generator(
                y = y,
                z = z,
                v1 = if (module == 0) volume else 0f,
                v2 = if (module == 1) volume else 0f,
                v3 = if (module == 2) volume else 0f,
                v4 = if (module == 3) volume else 0f,
                v6 = action.liquidVolume + 10000f
            ),
            executionManager.generator(y = y)
        )
    }

    /**
     * 等待机构空闲
     * @param block suspend () -> Unit 代码块
     */
    private suspend fun waitForFree(block: suspend () -> Unit) {
        if (serialManager.drawer.value) {
            event("抽屉未关闭")
            delay(1000L)
            waitForFree(block)
        } else {
            if (serialManager.lock.value) {
                event("等待中")
                delay(1000L)
                waitForFree(block)
            } else {
                block.invoke()
            }
        }
    }
}

