package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.IncubationStageConverters
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
@TypeConverters(IncubationStageConverters::class)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "text")
    val text: String = "None",
    @ColumnInfo(name = "stages")
    val stages: List<IncubationStage> = emptyList(),
    @ColumnInfo(name = "create_time")
    val createTime: Date = Date(System.currentTimeMillis()),
)