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
) {
    // 时间转换
    fun timeSeconds() = ((time.toDoubleOrNull() ?: 0.0) * 60).toInt()

    // 获取胶浓度字符串
    fun getGlueConcentrationStr(): String {
        val list = glueConcentration.split(",")
        return if (glueType == 0) {
            "${list.getOrNull(0) ?: "0"}%"
        } else {
            "${list.getOrNull(0) ?: "0"}% - ${list.getOrNull(1) ?: "0"}%"
        }
    }

    // 获取胶浓度列表
    fun getGlueConcentrationList(): List<String> {
        val list = glueConcentration.split(",")
        return listOf(list.getOrNull(0) ?: "0", list.getOrNull(1) ?: "0")
    }

    // 是否可以保存
    fun canSave(): Boolean {
        val bool = (value.toDoubleOrNull() ?: 0.0) > 0  && (time.toDoubleOrNull() ?: 0.0) > 0
        return if (experimentalType == 0) {
            bool && (flowSpeed.toDoubleOrNull() ?: 0.0) > 0.0
        } else {
            bool
        }
    }

    // 是否可以开始
    fun canStart(opt1: Int, opt2: Int): Boolean {
        val bool = (value.toDoubleOrNull() ?: 0.0) > 0  && (time.toDoubleOrNull() ?: 0.0) > 0
        return if (experimentalType == 0) {
            bool && (flowSpeed.toDoubleOrNull() ?: 0.0) > 0 && opt1 == 1
        } else  {
            bool && opt2 == 1
        }
    }
}