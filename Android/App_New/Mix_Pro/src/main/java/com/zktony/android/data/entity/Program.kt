package com.zktony.android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.PointConverters
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
@TypeConverters(PointConverters::class)
data class Program(
    @PrimaryKey
    val id: Long = nextId(),
    val name: String = "None",
    val data: List<Point> = emptyList(),
    val count: Int = 0,
    val active: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
)