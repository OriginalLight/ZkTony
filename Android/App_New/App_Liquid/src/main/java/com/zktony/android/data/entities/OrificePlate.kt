package com.zktony.android.data.entities

/**
 * @author 刘贺贺
 * @date 2023/8/1 15:38
 */
data class OrificePlate(
    val column: Int = 8,
    val delay: Long = 0L,
    val row: Int = 12,
    val type: Int = 0,
    val coordinate: List<Coordinate> = List(2) { Coordinate() },
    val orifices: List<List<Orifice>> = emptyList(),
) {
    fun generateOrifices(): List<List<Orifice>> {
        val lists = if (orifices.isNotEmpty()) orifices.toMutableList()
            .map { it.toMutableList() } else MutableList(column) { MutableList(row) { Orifice() } }
        val rowCoordinate = (coordinate[1].abscissa - coordinate[0].abscissa) / (row - 1)
        val columnCoordinate = (coordinate[1].ordinate - coordinate[0].ordinate) / (column - 1)
        for (i in 0 until column) {
            for (j in 0 until row) {
                lists[i][j] = lists[i][j].copy(
                    coordinate = Coordinate(
                        coordinate[0].abscissa + rowCoordinate * j,
                        coordinate[0].ordinate + columnCoordinate * i
                    )
                )
            }
        }
        return lists
    }

    fun getSelected(): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        for (i in orifices.indices) {
            for (j in orifices[i].indices) {
                if (orifices[i][j].selected) {
                    list.add(Pair(i, j))
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
}

data class Orifice(
    val volume: List<Double> = List(6) { 0.0 },
    val selected: Boolean = false,
    val coordinate: Coordinate = Coordinate(),
)

data class Coordinate(
    val abscissa: Double = 0.0,
    val ordinate: Double = 0.0,
)