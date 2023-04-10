package com.zktony.www.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.utils.Snowflake
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(tableName = "container")
data class Container(
    @PrimaryKey val id: Long = Snowflake(1).nextId(),
    val name: String = "",
    // 废液槽坐标
    val wasteX: Float = 0f,
    val wasteY: Float = 0f,
    // 创建时间
    val createTime: Date = Date(System.currentTimeMillis()),
)