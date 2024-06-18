package com.zktony.android.utils

import com.zktony.room.entities.internal.Point

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:06
 */
object AlgorithmUtils {

    fun calculateCalibrationFactor(points: List<Point>): (Double) -> Double {
        if (points.isEmpty()) return { x -> x * 100 }
        val slopeList = mutableListOf<Double>()
        points.forEach {
            if (it.x == 0.0) return@forEach
            slopeList.add(it.y * 6400 / it.x)
        }
        if (slopeList.isEmpty()) {
            return { x -> x * 100 }
        } else {
            return { x -> x * slopeList.average() }
        }
    }
}


