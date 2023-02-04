package com.zktony.www.common.model

/**
 * @author: 刘贺贺
 * @date: 2022-12-12 13:18
 */
data class Coordinate(
    val x: Double,
    val y: Double
) {
    fun distanceTo(other: Coordinate): Pair<Double, Double> {
        val dx = x - other.x
        val dy = y - other.y
        return Pair(dx, dy)
    }
}