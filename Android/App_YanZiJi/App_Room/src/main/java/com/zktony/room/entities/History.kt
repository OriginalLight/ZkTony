package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:31
 */
@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val createTime: Long = System.currentTimeMillis()
)