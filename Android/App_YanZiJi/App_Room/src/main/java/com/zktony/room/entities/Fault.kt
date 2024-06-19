package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("faults")
data class Fault(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val code: String,
    val description: String,
    val severity: Int,
    val timestamp: Long = System.currentTimeMillis()
)