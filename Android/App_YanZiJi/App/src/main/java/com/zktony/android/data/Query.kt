package com.zktony.android.data

data class NameTimeRangeQuery(
    val name: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null
)

data class NameQuery(
    val name: String? = null
)