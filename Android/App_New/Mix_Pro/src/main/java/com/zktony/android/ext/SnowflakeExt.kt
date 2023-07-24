package com.zktony.android.ext

import com.zktony.android.ext.utils.Snowflake

val snowflake = Snowflake(1)

fun nextId(): Long {
    return snowflake.nextId()
}