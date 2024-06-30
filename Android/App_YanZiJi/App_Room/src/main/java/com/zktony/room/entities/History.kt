package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:31
 */
@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val createTime: Date = Date(System.currentTimeMillis())
)