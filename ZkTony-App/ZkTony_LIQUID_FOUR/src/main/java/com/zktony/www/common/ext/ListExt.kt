package com.zktony.www.common.ext

import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Hole
import com.zktony.www.room.entity.Plate
import com.zktony.www.room.entity.Point

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


fun List<Point>.calculateCoordinate(con: Container): List<Point> {
    val hl = mutableListOf<Point>()
    val x0y0 = this.find { it.x == 0 && it.y == 0 }
    val xmyn = this.find { it.x == con.x - 1 && it.y == con.y - 1 }
    if (x0y0 != null && xmyn != null) {
        val x = (xmyn.xAxis - x0y0.xAxis) / if (con.x == 1) 1 else (con.x - 1)
        val y = (xmyn.yAxis - x0y0.yAxis) / if (con.y == 1) 1 else (con.y - 1)
        for (i in 0 until con.x) {
            for (j in 0 until con.y) {
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