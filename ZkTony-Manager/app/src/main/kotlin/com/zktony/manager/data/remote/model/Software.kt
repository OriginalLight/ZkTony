package com.zktony.manager.data.remote.model

import com.zktony.www.common.extension.currentTime

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:16
 */
data class Software(
    val id: String = "",
    val `package`: String = "",
    val version_name: String = "",
    val version_code: Int = 0,
    val build_type: String = "",
    val remarks: String = "",
    val create_by: String = "",
    val create_time: String = currentTime(),
)