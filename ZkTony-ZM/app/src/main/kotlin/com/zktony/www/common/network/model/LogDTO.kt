package com.zktony.www.common.network.model

import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-13 13:08
 */
data class LogDTO(
    val id: String,
    val sub_id: String,
    val log_type: String,
    val content: String,
    val create_time: Date,
)