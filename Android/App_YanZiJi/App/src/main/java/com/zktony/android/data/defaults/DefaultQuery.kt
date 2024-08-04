package com.zktony.android.data.defaults

import com.zktony.android.data.ProgramQuery
import com.zktony.android.data.UserQuery

fun defaultProgramQuery() = ProgramQuery(
    name = null,
    startTime = null,
    endTime = null
)

fun defaultUserQuery() = UserQuery(
    name = null
)