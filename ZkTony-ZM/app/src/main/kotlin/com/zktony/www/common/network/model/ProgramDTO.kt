package com.zktony.www.common.network.model

import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-13 13:08
 */
data class ProgramDTO(
    val id: String,
    val name: String,
    val content: String,
    val create_time: Date,
)