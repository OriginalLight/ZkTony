package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.TripleConverters
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
@Entity(
    tableName = "calibrations",
    indices = [
        Index(value = ["text"], unique = true)
    ]
)
@Immutable
@TypeConverters(TripleConverters::class)
data class Calibration(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "text") val text: String = "Default",
    @ColumnInfo(name = "data") val data: List<Triple<Int, Double, Double>> = emptyList(),
    @ColumnInfo(name = "active") val active: Boolean = false,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
) {
    // 计算每个泵每一步的出液量
    fun vps(): List<Double> {
        val vl = mutableListOf<Double>()
        for (i in 0..13) {
            val dataList = this.data.filter { it.first == i }
            if (dataList.isNotEmpty()) {
                val avg = dataList.map { data -> data.second / data.third }.average()
                vl.add(avg)
            } else {
                vl.add(0.01)
            }
        }
        return vl
    }
}