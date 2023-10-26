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
    val orifices: List<List<Orifice>> = List(column) { List(row) { Orifice() } },
) {
    fun generateOrifices(): List<List<Orifice>> {
        val lists = if (orifices.isNotEmpty()) orifices.toMutableList()
            .map { it.toMutableList() } else MutableList(column) { MutableList(row) { Orifice() } }
        val rowCoordinate = (points[1].x - points[0].x) / (row - 1)
        val columnCoordinate = (points[1].y - points[0].y) / (column - 1)
        for (i in 0 until column) {
            for (j in 0 until row) {
                lists[i][j] = lists[i][j].copy(
                    point = Point(
                        points[0].x + rowCoordinate * j,
                        points[0].y + columnCoordinate * i
                    )
                )
            }
        }
        return lists
    }

    fun getSelected(): List<Triple<Int, Int, Color>> {
        val list = mutableListOf<Triple<Int, Int, Color>>()
        for (i in orifices.indices) {
            for (j in orifices[i].indices) {
                if (orifices[i][j].selected) {
                    list.add(Triple(i, j, Color.Green))
                }
            }
        }
        return list
    }

    fun isSelectAll() = orifices.flatten().all { it.selected }

    fun selectAll(boolean: Boolean): List<List<Orifice>> {
        val lists = orifices.toMutableList().map { it.toMutableList() }
        for (i in lists.indices) {
            for (j in lists[i].indices) {
                lists[i][j] = lists[i][j].copy(selected = boolean)
            }
        }
        return lists
    }

    fun getVolume(): List<Double> {
        val orifice = orifices.flatten().firstOrNull() ?: Orifice()
        return orifice.volume
    }

    fun setVolume(volume: List<Double>): List<List<Orifice>> {
        val lists = orifices.toMutableList().map { it.toMutableList() }
        for (i in lists.indices) {
            for (j in lists[i].indices) {
                lists[i][j] = lists[i][j].copy(volume = volume)
            }
        }
        return lists
    }

    fun getInfo(): List<String> {
        val list = mutableListOf<String>()
        list.add("规格：$column x $row")
        list.add("已选：${orifices.flatten().count { it.selected }} 孔")
        list.add("模式：" + if (type == 0) "分液模式" else "混合模式")
        list.add("延时：$delay 秒")
        list.add("预排：$previous 微升")
        if (type == 0) {
            list.add("液量：${orifices.flatten().firstOrNull()?.volume?.firstOrNull() ?: 0.0} 微升")
        }
        return list
    }
}