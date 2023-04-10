package com.zktony.common.model

import com.zktony.common.ext.currentTime

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:16
 */
data class QrCode(
    val id: String = "",
    val `package`: String = "www.zktony.com.liquid",
    val version_name: String = "1.0.0",
    val version_code: Int = 1,
    val build_type: String = "debug",
    val remarks: String = "",
    val create_by: String = "App",
    val create_time: String = currentTime(),
)