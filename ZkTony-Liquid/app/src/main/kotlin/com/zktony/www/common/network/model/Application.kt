package com.zktony.www.common.network.model

data class Application(
    val id: Int,
    val application_id: String,
    val build_type: String,
    val download_url: String,
    val version_name: String,
    val version_code: Int,
    val description: String,
    val create_time: String,
)
