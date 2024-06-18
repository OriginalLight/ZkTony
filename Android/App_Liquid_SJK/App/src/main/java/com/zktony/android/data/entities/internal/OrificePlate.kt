package com.zktony.android.data.entities.internal

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color

/**
 * @author 刘贺贺
 * @date 2023/8/1 15:38
 */
@Keep
data class OrificePlate(
    val column: Int = 8,
    val delay: Double = 0.0,
    val previous: Double = 0.0,
    val row: Int = 12,
    val type: Int = 0,
    val points: List<Point> = List(2) { Point() },
    val orifices: List<Orifice> = emptyList(),
) {
    fun generateOrifices(): List<Orifice> {
        val lists = mutableListOf<Orifice>()
        val rowCoordinate = (points[1].x - points[0].x) / (row - 1)
        val columnCoordinate = (points[1].y - points[0].y) / (column - 1)
        for (i in 0 until column) {
            for (j in 0 until row) {
                lists.add(
                    Orifice(
                        point = Point(
                            points[0].x + rowCoordinate * j,
                            points[0].y + columnCoordinate * i
                        ),
                        row = j,
                        column = i
                    )
                )
            }
        }
        return lists
    }

    fun getVolume(): List<Double> {
        val orifice = orifices.firstOrNull() ?: Orifice()
        return orifice.volume
    }

    fun setVolume(volume: List<Double>): List<Orifice> {
        val lists = mutableListOf<Orifice>()
        orifices.forEach {
            lists.add(it.copy(volume = volume))
        }
        return lists
    }

    fun getInfo(): List<String> {
        val list = mutableListOf<String>()
        list.add("规格：$column x $row")
        list.add("已选：${orifices.count { it.status == 1 }} 孔")
        list.add("延时：$delay 秒")
        list.add("预排：$previous 微升")
        list.add("液量：${orifices.firstOrNull()?.volume?.firstOrNull() ?: 0.0} 微升")
        return list
    }
}