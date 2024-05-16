package com.zktony.android.data.entities

/**
 * @author 刘贺贺
 * @date 2023/8/1 15:38
 */
data class OrificePlate(
    val id: Long = 0L,
    val text: String = "",
    val row: Int = 12,
    val column: Int = 8,
    val coordinate: Array<Array<Double>> = emptyArray(),
    val orifices: Array<Array<Orifice>> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrificePlate

        if (id != other.id) return false
        if (text != other.text) return false
        if (row != other.row) return false
        if (column != other.column) return false
        if (!coordinate.contentEquals(other.coordinate)) return false
        if (!orifices.contentDeepEquals(other.orifices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + row
        result = 31 * result + column
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + orifices.contentDeepHashCode()
        return result
    }

    fun getOrifice(row: Int, column: Int): Orifice {
        return orifices[row][column]
    }

    fun generateOrifices(): Array<Array<Orifice>> {
        val arrays =
            if (orifices.isNotEmpty()) orifices else Array(row) { Array(column) { Orifice() } }
        val rowCoordinate = (coordinate[1][0] - coordinate[0][0]) / (row - 1)
        val columnCoordinate = (coordinate[1][1] - coordinate[0][1]) / (column - 1)
        for (i in 0 until row) {
            for (j in 0 until column) {
                arrays[i][j] = arrays[i][j].copy(
                    coordinate = arrayOf(
                        coordinate[0][0] + rowCoordinate * i,
                        coordinate[0][1] + columnCoordinate * j
                    )
                )
            }
        }
        return arrays
    }
}

data class Orifice(
    val volume: Double = 0.0,
    val active: Boolean = false,
    val coordinate: Array<Double> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Orifice

        if (volume != other.volume) return false
        if (active != other.active) return false
        if (!coordinate.contentDeepEquals(other.coordinate)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = volume.hashCode()
        result = 31 * result + active.hashCode()
        result = 31 * result + coordinate.contentDeepHashCode()
        return result
    }
}
