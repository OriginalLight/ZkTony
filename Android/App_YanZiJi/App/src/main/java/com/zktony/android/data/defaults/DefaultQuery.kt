package com.zktony.android.data.defaults

import com.zktony.android.data.NameQuery
import com.zktony.android.data.NameTimeRangeQuery

fun defaultNameTimeRangeQuery() = NameTimeRangeQuery(
    name = null,
    startTime = null,
    endTime = null
)

fun defaultNameQuery() = NameQuery(
    name = null
)