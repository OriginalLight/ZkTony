package com.zktony.www.common.ext

import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate

fun List<Hole>.total(): Int {
    var total = 0
    forEach {
        if (it.enable) {
            if (it.v1 > 0f) total += 1
            if (it.v2 > 0f) total += 1
            if (it.v3 > 0f) total += 1
            if (it.v4 > 0f) total += 1
        }
    }
    return total
}


fun List<Hole>.calculateCoordinate(plate: Plate): List<Hole> {
    val hl = mutableListOf<Hole>()
    val x0y0 = this.find { it.x == 0 && it.y == 0 }
    val x1y1 = this.find { it.x == plate.x - 1 && it.y == plate.y - 1 }
    if (x0y0 != null && x1y1 != null) {
        val x = (x1y1.xAxis - x0y0.xAxis) / if (plate.x == 1) 1 else (plate.x - 1)
        val y = (x1y1.yAxis - x0y0.yAxis) / if (plate.y == 1) 1 else (plate.y - 1)
        for (i in 0 until plate.x) {
            for (j in 0 until plate.y) {
                val hole = this.find { it.x == i && it.y == j }!!
                hl.add(
                    hole.copy(
                        xAxis = x0y0.xAxis + i * x,
                        yAxis = x0y0.yAxis + j * y
                    )
                )
            }
        }
    }
    return hl
}