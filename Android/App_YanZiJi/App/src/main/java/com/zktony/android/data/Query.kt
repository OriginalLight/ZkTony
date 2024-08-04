package com.zktony.android.data

data class ProgramQuery(
    val name: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null
)

data class UserQuery(
    val name: String? = null
)