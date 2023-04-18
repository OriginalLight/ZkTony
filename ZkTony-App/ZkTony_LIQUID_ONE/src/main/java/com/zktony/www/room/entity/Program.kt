package com.zktony.www.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.utils.Snowflake
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
data class Program(
    @PrimaryKey
    val id: Long = Snowflake(1).nextId(),
    val name: String = "",
    val count: Int = 0,
    val upload: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
)
