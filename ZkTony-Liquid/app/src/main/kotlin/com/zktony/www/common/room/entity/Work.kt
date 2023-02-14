package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "work")
data class Work(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val upload: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
)
