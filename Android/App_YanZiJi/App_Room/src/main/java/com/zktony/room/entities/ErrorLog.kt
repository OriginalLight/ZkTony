package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("error_logs")
data class ErrorLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val code: Long = 0L,
    val channel: Int = 0,
    val description: String = "",
    val createTime: Long = System.currentTimeMillis()
)