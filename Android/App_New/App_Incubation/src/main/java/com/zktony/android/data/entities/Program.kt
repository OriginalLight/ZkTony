package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.ProcessConverters
import com.zktony.android.data.entities.internal.Process
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
@TypeConverters(ProcessConverters::class)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val processes: List<Process> = emptyList(),
    val createTime: Date = Date(System.currentTimeMillis())
)