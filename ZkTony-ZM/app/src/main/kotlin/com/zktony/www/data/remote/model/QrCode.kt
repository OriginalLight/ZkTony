package com.zktony.www.data.remote.model

import com.zktony.www.BuildConfig
import com.zktony.www.common.extension.currentTime

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:16
 */
data class QrCode(
    val id: String = "",
    val `package`: String = "www.zktony.com.zm",
    val version_name: String = BuildConfig.VERSION_NAME,
    val version_code: Int = BuildConfig.VERSION_CODE,
    val build_type: String = BuildConfig.BUILD_TYPE,
    val remarks: String = "",
    val create_by: String = "App",
    val create_time: String = currentTime(),
)