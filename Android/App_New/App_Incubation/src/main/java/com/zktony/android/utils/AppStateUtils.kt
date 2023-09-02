package com.zktony.android.utils

import java.util.concurrent.ConcurrentHashMap

/**
 * @author 刘贺贺
 * @date 2023/8/30 13:40
 */
data class AppState(
    /**
     * 阀门状态
     */
    val valve: MutableMap<Int, Boolean> = ConcurrentHashMap(),
    /**
     * 绝对位置
     */
    val location: MutableMap<Int, Int> = ConcurrentHashMap(),
    /**
     * 校准曲线
     */
    val curve: MutableMap<Int, (Double) -> Double?> = ConcurrentHashMap()
)