package com.zktony.www.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.common.utils.Snowflake
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "work")
data class Work(
    @PrimaryKey
    val id: Long = Snowflake(1).nextId(),
    val name: String = "",
    val count: Int = 0,
    val upload: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
)
