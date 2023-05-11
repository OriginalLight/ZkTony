package com.zktony.android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
@Entity(tableName = "calibration")
data class Calibration(
    @PrimaryKey
    val id: Long = nextId(),
    // 校准名称
    val name: String = "默认",
    // 是否选用
    val active: Int = 0,
    // 创建时间
    val createTime: Date = Date(System.currentTimeMillis()),
)
