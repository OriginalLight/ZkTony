package com.zktony.manager.data.remote.model

import com.zktony.www.common.extension.currentTime
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 16:36
 */
data class Equipment(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val model: String = "",
    val voltage: String = "",
    val power: String = "",
    val frequency: String = "",
    val attachment: String = "",
    val remarks: String = "",
    val create_by: String = "",
    val create_time: String = currentTime()
)
