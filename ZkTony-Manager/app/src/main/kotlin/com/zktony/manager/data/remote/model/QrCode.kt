package com.zktony.manager.data.remote.model

import com.zktony.www.common.extension.currentTime

data class QrCode (
    val id: String = "",
    val `package`: String = "com.zktony.manager",
    val version_name: String = "1.0.0",
    val version_code: Int = 1,
    val build_type: String = "debug",
    val remarks: String = "",
    val create_by: String = "",
    val create_time: String = currentTime(),
)