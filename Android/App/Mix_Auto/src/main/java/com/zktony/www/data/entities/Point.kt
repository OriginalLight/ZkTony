package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-01-28 15:54
 */
@Entity(tableName = "point")
data class Point(
    @PrimaryKey(autoGenerate = true)
    val id: Long = nextId(),
    val subId: Long = 0L,
    val thirdId: Long = 0L,
    val index: Int = 0,
    val axis: Float = 0f,
    val waste: Float = 0f,
    val v1: Int = 0,
    val v2: Int = 0,
    val v3: Int = 0,
    val v4: Int = 0,
    val enable: Boolean = false,
    val createTime: Date = Date(System.currentTimeMillis()),
)