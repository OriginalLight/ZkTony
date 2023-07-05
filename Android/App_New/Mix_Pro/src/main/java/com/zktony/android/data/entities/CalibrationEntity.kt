package com.zktony.android.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.errorprone.annotations.Immutable
import com.zktony.android.data.CalibrationDataConverters
import com.zktony.android.core.ext.nextId
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
@TypeConverters(CalibrationDataConverters::class)
data class CalibrationEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long = nextId(),
    @ColumnInfo(name = "text") val text: String = "默认",
    @ColumnInfo(name = "data") val data: List<CalibrationData> = emptyList(),
    @ColumnInfo(name = "active") val active: Boolean = false,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
) {
    fun vps(): List<Double> {
        val vl = mutableListOf<Double>()
        for (i in 0..13) {
            val dataList = this.data.filter { it.index == i }
            if (dataList.isNotEmpty()) {
                val avg = dataList.map { data -> data.vps }.average()
                vl.add(avg)
            } else {
                vl.add(0.01)
            }
        }
        return vl
    }
}