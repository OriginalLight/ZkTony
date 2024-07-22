package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val password: String = "",
    val role: String = "USER",
    val enable: Boolean = true,
    val createTime: Date = Date(System.currentTimeMillis()),
    val lastLoginTime: Date = Date(System.currentTimeMillis())
)