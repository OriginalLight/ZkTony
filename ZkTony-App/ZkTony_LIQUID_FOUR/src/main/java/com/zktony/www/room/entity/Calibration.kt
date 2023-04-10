package com.zktony.www.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.common.utils.Snowflake
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
    // x轴电机一圈走的距离
    val x: Float = 87.6f,
    // y轴电机一圈走的距离
    val y: Float = 57.62f,
    // 蠕动泵一一圈走的进液量
    val v1: Float = 200f,
    // 蠕动泵二一圈走的进液量
    val v2: Float = 200f,
    // 蠕动泵三一圈走的进液量
    val v3: Float = 200f,
    // 蠕动泵四一圈走的进液量
    val v4: Float = 200f,
    // 是否选用
    val enable: Int = 0,
    // 创建时间
    val createTime: Date = Date(System.currentTimeMillis()),
)
