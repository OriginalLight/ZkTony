package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.ListPointConverters
import com.zktony.android.data.entities.internal.Point
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Entity(tableName = "calibration")
@TypeConverters(ListPointConverters::class)
@Immutable
data class Calibration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val index: Int = 0,
    val displayText: String,
    val points: List<Point> = emptyList(),
    val enable: Boolean = true,
    val createTime: Date = Date(System.currentTimeMillis())
)