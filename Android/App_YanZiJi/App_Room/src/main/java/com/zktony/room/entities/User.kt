package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val password: String = "",
    val role: String = "USER",
    val enable: Boolean = true,
    val createTime: Long = System.currentTimeMillis(),
    val lastLoginTime: Long = System.currentTimeMillis()
)