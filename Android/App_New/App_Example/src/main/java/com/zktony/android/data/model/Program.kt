package com.zktony.android.data.model

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.FloatConverters
import com.zktony.android.data.IntConverters
import com.zktony.android.utils.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(
    tableName = "programs",
    indices = [
        Index(value = ["text"], unique = true)
    ]
)
@TypeConverters(
    IntConverters::class,
    FloatConverters::class,
)
@Immutable
data class Program(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long = nextId(),
    @ColumnInfo(name = "text") val text: String = "None",
    @ColumnInfo(name = "active") val active: List<Int> = listOf(0, 1, 2, 3, 4, 5),
    @ColumnInfo(name = "axis") val axis: List<Float> = listOf(0f, 0f),
    @ColumnInfo(name = "volume") val volume: List<Float> = listOf(0f, 0f, 0f, 0f),
    @ColumnInfo(name = "count") val count: Int = 0,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
)