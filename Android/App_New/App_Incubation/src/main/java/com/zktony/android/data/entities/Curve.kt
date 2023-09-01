package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.PointConverters
import com.zktony.android.utils.extra.Point
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Entity(tableName = "curve")
@TypeConverters(PointConverters::class)
@Immutable
data class Curve(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val index: Int = 0,
    val displayText: String,
    val points: List<Point> = emptyList(),
    val enable: Boolean = true,
    val createTime: Date = Date(System.currentTimeMillis())
)