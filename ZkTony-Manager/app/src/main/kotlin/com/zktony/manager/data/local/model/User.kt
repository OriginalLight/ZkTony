package com.zktony.manager.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.www.common.extension.currentTime

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 10:05
 */
@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String = "",
    val phone: String = "",
    val create_time: String = currentTime(),
)
