package com.zktony.android.utils.ext

import com.zktony.android.utils.Snowflake

val snowflake = Snowflake(1)

fun nextId(): Long {
    return snowflake.nextId()
}