package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.OrificePlateConverters
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
@TypeConverters(OrificePlateConverters::class)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "text")
    val text: String = "None",
    @ColumnInfo(name = "orifice_plates")
    val orificePlates: List<OrificePlate?> = List(4) { null },
    @ColumnInfo(name = "create_time")
    val createTime: Date = Date(System.currentTimeMillis()),
)