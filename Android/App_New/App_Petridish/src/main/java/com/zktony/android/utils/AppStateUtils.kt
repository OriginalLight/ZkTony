package com.zktony.android.utils

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
}