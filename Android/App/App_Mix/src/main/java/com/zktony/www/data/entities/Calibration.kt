package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.core.ext.nextId
import com.zktony.www.data.CalibrationDataConverters
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
@Entity(tableName = "calibration")
@TypeConverters(CalibrationDataConverters::class)
data class Calibration(
    @PrimaryKey
    val id: Long = nextId(),
    val name: String = "默认",
    val data: List<CalibrationData> = emptyList(),
    val active: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
) {
    fun avgRate(): List<Double> {
        val vl = mutableListOf<Double>()
        for (i in 0..2) {
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