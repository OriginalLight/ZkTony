package com.zktony.android.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.errorprone.annotations.Immutable
import com.zktony.android.data.PointConverters
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(
    tableName = "containers",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
@Immutable
@TypeConverters(PointConverters::class)
data class Container(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long = nextId(),
    @ColumnInfo(name = "name") val name: String = "默认",
    @ColumnInfo(name = "data") val data: List<Point> = emptyList(),
    @ColumnInfo(name = "active") val active: Int = 0,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
)