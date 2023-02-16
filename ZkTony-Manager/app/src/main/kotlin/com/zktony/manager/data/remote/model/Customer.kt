package com.zktony.manager.data.remote.model

import com.zktony.www.common.extension.currentTime
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 16:33
 */
data class Customer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val source: String = "",
    val industry: String = "",
    val remarks: String = "",
    val create_by: String = "",
    val create_time: String = currentTime()
)
