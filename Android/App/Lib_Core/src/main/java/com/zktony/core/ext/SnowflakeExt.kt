package com.zktony.core.ext

import com.zktony.core.utils.Snowflake

val snowflake = Snowflake(1)

fun nextId(): Long {
    return snowflake.nextId()
}