package com.zktony.www.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 9:56
 */

@Entity
data class Action(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var programId: String = "",
    var mode: Int = 0,
    var order: Int = 0,
    var temperature: Float = 0f,
    var liquidVolume: Float = 0f,
    var count: Int = 0,
    var time: Float = 0f,
    var upload: Int = 0,
    var createTime: Date = Date(System.currentTimeMillis())
)