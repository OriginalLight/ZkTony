package com.zktony.manager.data.remote.model

import com.zktony.www.common.extension.currentTime
import java.util.UUID

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 16:19
 */
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val software_id: String = "",
    val customer_id: String = "",
    val equipment_id: String = "",
    val express_number: String = "",
    val express_company: String = "",
    val equipment_number: String = "",
    val equipment_time: String = "",
    val attachment: String = "",
    val remarks: String = "",
    val create_by: String = "",
    val create_time: String = currentTime()
)
