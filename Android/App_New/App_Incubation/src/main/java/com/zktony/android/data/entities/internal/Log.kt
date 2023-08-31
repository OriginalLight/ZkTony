package com.zktony.android.data.entities.internal

import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:43
 */
data class Log(
    val level: String = "INFO",
    val message: String,
    val createTime: Date = Date(System.currentTimeMillis())
)