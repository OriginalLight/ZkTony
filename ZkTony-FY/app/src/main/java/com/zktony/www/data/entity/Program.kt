package com.zktony.www.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "program")
data class Program(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var actions: String = "",
    var runCount: Int = 0,
    var actionCount: Int = 0,
    var time: String = "",
    var upload: Int = 0,
    var createTime: Date = Date(System.currentTimeMillis())
)