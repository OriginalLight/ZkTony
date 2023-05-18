package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "program")
data class Program(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val actions: String = "没有任何操作，去添加吧",
    val runCount: Int = 0,
    val actionCount: Int = 0,
    val time: String = "",
    val upload: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis())
)