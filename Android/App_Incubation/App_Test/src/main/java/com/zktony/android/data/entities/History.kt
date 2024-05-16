package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.LogConverters
import com.zktony.android.data.entities.internal.Log
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:31
 */
@Entity(tableName = "history")
@TypeConverters(LogConverters::class)
@Immutable
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val logs: List<Log> = emptyList(),
    val createTime: Date = Date(System.currentTimeMillis())
)