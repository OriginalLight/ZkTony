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
    private fun getGlueConcentrationStr(): String {
        val list = glueConcentration.split(",")
        return if (glueType == 0) {
            "${list.getOrNull(0) ?: "0"}%"
        } else {
            "${list.getOrNull(0) ?: "0"}% - ${list.getOrNull(1) ?: "0"}%"
        }
    }

    // 获取属性列表
    fun getAttributes(): List<Pair<String, String>?> {
        return listOf(
            Pair("实验名称：", name),
            Pair("实验类型：", if (experimentalType == 0) "转膜" else "染色"),
            Pair(
                "实验模式：", when (experimentalMode) {
                    0 -> "恒压"
                    1 -> "恒流"
                    2 -> "恒功率"
                    else -> "未知"
                }
            ),
            Pair(
                "数值：", "$value ${
                    when (experimentalMode) {
                        0 -> "V"
                        1 -> "A"
                        2 -> "W"
                        else -> "未知"
                    }
                }"
            ),
            if (experimentalType == 0) Pair(
                "流量：",
                "$flowSpeed mL/min"
            ) else null,
            Pair("时间：", "$time min"),
            Pair("胶种类：", if (glueType == 0) "普通胶" else "梯度胶"),
            Pair("胶浓度：", getGlueConcentrationStr()),
            Pair(
                "胶厚度：", "${
                    when (glueThickness) {
                        0 -> "0.75"
                        1 -> "1.0"
                        2 -> "1.5"
                        else -> "未知"
                    }
                } mm"
            ),
            Pair("蛋白大小：", "$proteinSize kDa"),
            Pair("缓冲液类型：", if (bufferType == 0) "厂家" else "其他"),
        )
    }
}