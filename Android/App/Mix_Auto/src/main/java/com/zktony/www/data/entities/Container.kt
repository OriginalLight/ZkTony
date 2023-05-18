package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 16:00
 */
@Entity(tableName = "container")
data class Container(
    @PrimaryKey
    val id: Long = nextId(),
    val name: String = "默认",
    val size: Int = 10,
    val axis: Float = 0f,
    val type: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis()),
)