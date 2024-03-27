package com.zktony.android.utils

import com.zktony.android.data.entities.Motor
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 刘贺贺
 * @date 2023/8/30 13:40
 */
object AppStateUtils {
    /**
     * 轴状态
     */
    val hpa: MutableMap<Int, Boolean> = ConcurrentHashMap()

    /**
     * GPIO 状态
     */
    val hpg: MutableMap<Int, Boolean> = ConcurrentHashMap()

    /**
     * 电机信息
     */
    val hpm: MutableMap<Int, Motor> = ConcurrentHashMap()

    /**
     * 绝对位置
     */
    val hpp: MutableMap<Int, Long> = ConcurrentHashMap()

    /**
     * 校准曲线
     */
    val hpc: MutableMap<Int, (Double) -> Double> = ConcurrentHashMap()

    /**
     * 灯光回复
     */
    val hpd: MutableMap<Int, Boolean> = ConcurrentHashMap()


}