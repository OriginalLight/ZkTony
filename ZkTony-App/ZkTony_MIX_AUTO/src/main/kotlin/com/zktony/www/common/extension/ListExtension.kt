package com.zktony.www.common.extension

import com.zktony.www.data.local.room.entity.Hole

fun List<Hole>.total(): Int {
    var total = 0
    forEach {
        if (it.enable) {
            if (it.v1 > 0f) total += 1
            if (it.v2 > 0f) total += 1
            if (it.v3 > 0f) total += 1
        }
    }
    return total
}