package com.zktony.www.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.utils.Snowflake
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
@Entity(tableName = "calibration")
data class Calibration(
    @PrimaryKey
    val id: Long = Snowflake(1).nextId(),
    // 校准名称
    val name: String = "默认",
    // y轴电机一圈走的距离
    val y: Float = 4f,
    // 蠕动泵一一圈走的进液量
    val v1: Float = 100f,
    // 蠕动泵二一圈走的进液量
    val v2: Float = 100f,
    // 蠕动泵三一圈走的进液量
    val v3: Float = 100f,
    // 是否选用
    val enable: Int = 0,
    // 创建时间
    val createTime: Date = Date(System.currentTimeMillis()),
)
