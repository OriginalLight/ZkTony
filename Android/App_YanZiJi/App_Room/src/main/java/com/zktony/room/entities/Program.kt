package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "programs")
@Serializable
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    // 名称
    val name: String = "",
    // 实验类型
    val experimentalType: Int = 0,
    // 工作模式
    val workMode: Int = 1,
    // 值
    val value: String,
    // 流速
    val flowSpeed: String = "0",
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
    val description: String = "None",
    // 创建时间
    val createTime: Long = System.currentTimeMillis()
)