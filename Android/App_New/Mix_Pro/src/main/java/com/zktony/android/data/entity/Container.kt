package com.zktony.android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.PointConverters
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(tableName = "container")
@TypeConverters(PointConverters::class)
data class Container(
    @PrimaryKey
    val id: Long = nextId(),
    val name: String = "默认",
    val data: List<Point> = emptyList(),
    val active: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
)