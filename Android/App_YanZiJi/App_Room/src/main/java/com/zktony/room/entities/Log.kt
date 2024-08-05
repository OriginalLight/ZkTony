package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:31
 */
@Entity(tableName = "logs")
data class Log(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    // 通道
    val channel: Int = 0,
    // 名称
    val name: String = "",
    // 实验类型
    val experimentalType: Int = 0,
    // 模式
    val experimentalMode: Int = 0,
    // 值
    val value: String,
    // 流速
    val flowSpeed: String = "",
    // 时间
    val time: String,
    // 胶种类
    val glueType: Int = 0,
    // 胶浓度
    val glueConcentration: String = "0",
    // 胶厚度
    val glueThickness: Int = 0,
    // 蛋白大小
    val proteinSize: String = "0",
    // 缓冲液类型
    val bufferType: Int = 0,
    // 描述
    val description: String = "",
    // 创建时间
    val createTime: Long = System.currentTimeMillis()
)