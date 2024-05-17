package com.zktony.android.data.entities.internal

import androidx.annotation.Keep
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:43
 */
@Keep
data class Log(
    val index: Int = 0,
    val level: String = "INFO",
    val message: String,
    val createTime: Date = Date(System.currentTimeMillis())
)