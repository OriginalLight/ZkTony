package com.zktony.www.ui.home

import com.zktony.www.core.ext.asyncHex
import com.zktony.www.core.ext.execute
import com.zktony.www.core.ext.syncHex
import com.zktony.www.core.ext.temp
import com.zktony.www.core.ext.waitDrawer
import com.zktony.www.core.ext.waitLock
import com.zktony.www.data.entities.Action
import com.zktony.www.data.entities.Container
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:43
 */
class CommandExecutor constructor(
    private val module: Int,
    private val con: Container,
    private val recycle: Boolean,
    private val event: (String) -> Unit = { }
) {
    private lateinit var action: Action
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun initAction(action: Action) {
        this.action = action
    }

    /**
     * 添加封闭液
     * @param block
     */
    suspend fun addBlockingLiquid(block: suspend () -> Unit) {
        waitForFree {
            // 设置温度
            scope.launch { temp(addr = module + 1, temp = action.temperature.toString()) }
            addLiquid(yAxis = con.blockY, zAxis = con.blockZ)
            event("加液中")
            delay(100L)
            waitLock(20L) {
                block.invoke()
            }
        }
    }

    /**
     * 添加一抗代码块
     * @param block
     */
    suspend fun addAntibodyOne(block: suspend () -> Unit) {
        waitForFree {
            // 设置温度
            scope.launch { temp(addr = module + 1, temp = action.temperature.toString()) }
            addLiquid(yAxis = con.oneY, zAxis = con.oneZ)
            event("加液中")
            delay(100L)
            waitLock(20L) {
                block.invoke()
            }
        }
    }

    /**
     * 回收一抗代码块
     * @param block
     */
    suspend fun recycleAntibodyOne(block: suspend () -> Unit) {
        waitForFree {
            asyncHex(0) {
                pa = "0B"
                data = "0100"
            }
            delay(100L)
            recycleLiquid(
                yAxis = if (recycle) con.oneY else con.wasteY,
                zAxis = if (recycle) con.recycleOneZ else con.wasteZ
            )
            event("回收中")
            delay(100L)
            waitLock(20L) {
                asyncHex(0) {
                    pa = "0B"
                    data = "0101"
                }
                delay(100L)
                block.invoke()
            }
        }
    }

    /**
     * 添加二抗代码块
     * @param block
     */
    suspend fun addAntibodyTwo(block: suspend () -> Unit) {
        waitForFree {
            // 设置温度
            scope.launch { temp(addr = module + 1, temp = action.temperature.toString()) }
            addLiquid(yAxis = con.twoY, zAxis = con.twoZ)
            event("加液中")
            delay(100L)
            waitLock(20L) {
                block.invoke()
            }
        }
    }

    /**
     * 洗涤液代码块
     * @param block
     */
    suspend fun addWashingLiquid(block: suspend () -> Unit) {
        waitForFree {
            // 设置温度
            scope.launch { temp(addr = module + 1, temp = action.temperature.toString()) }
            // 主板运动
            addLiquid(yAxis = con.washY, zAxis = con.washZ)
            event("加液中")
            delay(100L)
            waitLock(20L) {
                block.invoke()
            }
        }
    }

    /**
     * 回收到废液槽
     *
     * @param block
     */
    suspend fun wasteLiquid(block: suspend () -> Unit) {
        waitForFree {
            asyncHex(0) {
                pa = "0B"
                data = "0100"
            }
            delay(100L)
            recycleLiquid(yAxis = con.wasteY, zAxis = con.wasteZ)
            event("清理中")
            delay(100L)
            waitLock(20L) {
                asyncHex(0) {
                    pa = "0B"
                    data = "0101"
                }
                delay(100L)
                block.invoke()
            }
        }
    }

    /**
     * 主机命令生成器
     */
    private fun addLiquid(yAxis: Float, zAxis: Float) {
        execute {
            step {
                y = yAxis
            }
            step {
                y = yAxis
                z = zAxis
                v1 = if (module == 0) action.liquidVolume else 0f
                v2 = if (module == 1) action.liquidVolume else 0f
                v3 = if (module == 2) action.liquidVolume else 0f
                v4 = if (module == 3) action.liquidVolume else 0f
                v5 = if (action.mode == 3) action.liquidVolume else 0f
            }
            step {
                y = yAxis
                v1 = if (module == 0) 15000f else 0f
                v2 = if (module == 1) 15000f else 0f
                v3 = if (module == 2) 15000f else 0f
                v4 = if (module == 3) 15000f else 0f
                v5 = if (action.mode == 3) 15000f else 0f
            }
        }
    }

    /**
     * 从机排液命令生成器
     * @return [List]<[String]>
     */
    private fun recycleLiquid(yAxis: Float, zAxis: Float) {
        val volume = -(action.liquidVolume + 20000f)
        execute {
            step {
                y = yAxis
            }
            step {
                y = yAxis
                z = zAxis
                v1 = if (module == 0) volume else 0f
                v2 = if (module == 1) volume else 0f
                v3 = if (module == 2) volume else 0f
                v4 = if (module == 3) volume else 0f
                v6 = action.liquidVolume + 20000f
            }
            step {
                y = yAxis
            }
        }
    }

    /**
     * 等待机构空闲
     * @param block suspend () -> Unit 代码块
     */
    private suspend fun waitForFree(block: suspend () -> Unit) {
        event("等待中")
        waitLock {
            syncHex(0) {
                pa = "0C"
            }
            delay(500L)
            waitDrawer(
                timerTask = { event("抽屉未关闭") },
                block = { block.invoke() }
            )
        }
    }
}

