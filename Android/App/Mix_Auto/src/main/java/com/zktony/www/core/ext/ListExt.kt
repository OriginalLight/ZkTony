package com.zktony.www.core.ext

import com.zktony.www.data.entities.Point

fun List<Point>.total(): Int {
    var total = 0
    forEach {
        if (it.enable) {
            if (it.v1 > 0f && it.v2 > 0f) total += 1
        }
    }
    return total
}