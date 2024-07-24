package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Operation log.
 *
 */
@Entity(tableName = "operation_logs")
data class OperationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val createTime: Date = Date(System.currentTimeMillis())
)