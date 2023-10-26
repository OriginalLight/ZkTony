package com.zktony.android.utils

import java.util.concurrent.ConcurrentHashMap

/**
 * @author 刘贺贺
 * @date 2023/8/30 13:40
 */
object AppStateUtils {
    /**
     * 阀门通道
     */
    val hpv: MutableMap<Int, Int> = ConcurrentHashMap()

    /**
     * 绝对位置
     */
    val hpp: MutableMap<Int, Int> = ConcurrentHashMap()

    /**
     * 校准函数
     */
    val hpc: MutableMap<Int, (Double) -> Double?> = ConcurrentHashMap()

    /**
     * 温度
     */
    val hpt: MutableMap<Int, Double> = ConcurrentHashMap()

}