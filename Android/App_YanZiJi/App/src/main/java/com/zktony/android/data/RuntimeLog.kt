package com.zktony.android.data

import java.util.Date

data class RuntimeLog(
    val name: String,
    val size: String,
    val createTime: Date,
    val absolutePath: String
)