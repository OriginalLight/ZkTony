package com.zktony.manager.data.remote.model

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 16:19
 */
data class ProductQueryDTO(
    val id: String = "",
    val software_id: String = "",
    val customer_id: String = "",
    val equipment_id: String = "",
    val express_number: String = "",
    val express_company: String = "",
    val equipment_number: String = "",
    val create_by: String = "",
    val begin_time: String = "",
    val end_time: String = "",
)
