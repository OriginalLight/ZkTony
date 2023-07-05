package com.zktony.android.core.ext

import com.zktony.android.core.utils.Snowflake

val snowflake = Snowflake(1)

fun nextId(): Long {
    return snowflake.nextId()
}