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
    val value: String = "",
    // 流速
    val flowSpeed: String = "",
    // 时间
    val time: String = "",
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
    // 状态 0完成 1中止 2出错
    val status: Int = 0,
    // 描述
    val description: String = "",
    // 创建时间
    val createTime: Long = System.currentTimeMillis(),
    // 结束时间
    val endTime: Long = 0L
) {
    // 获取胶浓度字符串
    fun getGlueConcentrationStr(): String {
        val list = glueConcentration.split(",")
        return if (glueType == 0) {
            "${list.getOrNull(0) ?: "0"}%"
        } else {
            "${list.getOrNull(0) ?: "0"}% - ${list.getOrNull(1) ?: "0"}%"
        }
    }
}