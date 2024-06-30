package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val createTime: Date = Date(System.currentTimeMillis())
)