package com.zktony.android.ui.utils

import androidx.compose.runtime.Composable
import com.zktony.android.data.ZktyError
import com.zktony.android.utils.extra.dateFormat
import com.zktony.room.entities.ErrorLog
import com.zktony.room.entities.Log
import com.zktony.room.entities.Program

// Log
@Composable
fun Log.getAttributes(): List<Pair<String, String>?> {
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

@Composable
fun Log.getItemAttributes(index: Int): List<Pair<String, Float>?> {
    return listOf(
        Pair((index + 1).toString(), 1f),
        Pair((channel + 1).toString(), 1f),
        Pair(name, 4f),
        Pair(if (experimentalType == 0) "转膜" else "染色", 2f),
        Pair(
            when (experimentalMode) {
                0 -> "恒压"
                1 -> "恒流"
                2 -> "恒功率"
                else -> "未知"
            }, 2f
        ),
        Pair(createTime.dateFormat("HH:mm\nyyyy-MM-dd"), 3f),
        Pair(
            when (status) {
                0 -> "完成"
                1 -> "中止"
                2 -> "出错"
                else -> "未知"
            }, 2f
        )
    )
}

@Composable
fun logHeaderItems(): List<Pair<String, Float>?> {
    return listOf(
        Pair("序号", 1f),
        Pair("通道", 1f),
        Pair("程序名称", 4f),
        Pair("实验类型", 2f),
        Pair("工作模式", 2f),
        Pair("开始时间", 3f),
        Pair("状态", 2f)
    )
}

// Program
@Composable
fun Program.getItemAttributes(index: Int): List<Pair<String, Float>?> {
    return listOf(
        Pair((index + 1).toString(), 1f),
        Pair(name, 4f),
        Pair(if (experimentalType == 0) "转膜" else "染色", 2f),
        Pair(
            when (experimentalMode) {
                0 -> "恒压"
                1 -> "恒流"
                2 -> "恒功率"
                else -> "未知"
            }, 2f
        ),
        Pair(
            value + when (experimentalMode) {
                0 -> "V"
                1 -> "A"
                2 -> "W"
                else -> "/"
            }, 2f
        ),
        Pair(time, 2f),
        Pair(createTime.dateFormat("HH:mm\nyyyy-MM-dd"), 3f),
    )
}

@Composable
fun Program.getItemExpandAttributes(): List<String?> {
    return listOf(
        if (experimentalType == 0) "$flowSpeed mL/min" else null,
        if (glueType == 0) "普通胶" else "梯度胶",
        getGlueConcentrationStr(),
        "${
            when (glueThickness) {
                0 -> "0.75"
                1 -> "1.0"
                2 -> "1.5"
                else -> "未知"
            }
        } mm",
        "$proteinSize kDa",
        if (bufferType == 0) "厂家缓冲液" else "其他缓冲液"
    )
}

@Composable
fun programHeaderItems(): List<Pair<String, Float>?> {
    return listOf(
        Pair("序号", 1f),
        Pair("程序名称", 4f),
        Pair("实验类型", 2f),
        Pair("工作模式", 2f),
        Pair("数值", 2f),
        Pair("时间(min)", 2f),
        Pair("创建时间", 3f),
        Pair("操作", 1f)
    )
}

//User
@Composable
fun userHeaderItems(): List<Pair<String, Float>?> {
    return listOf(
        Pair("序号", 1f),
        Pair("用户名", 4f),
        Pair("用户角色", 3f),
        Pair("是否启用", 3f),
        Pair("上次登录时间", 3f)
    )
}

// ErrorLog
@Composable
fun errorLogHeaderItems(): List<Pair<String, Float>?> {
    return listOf(
        Pair("序号", 1f),
        Pair("通道", 1f),
        Pair("错误码", 2f),
        Pair("错误信息", 4f),
        Pair("严重程度", 2f),
        Pair("发生时间", 3f),
    )
}

fun ErrorLog.getItemAttributes(index: Int): List<Pair<String, Float>?> {
    val error = ZktyError.fromCodeSignal(code)
    return listOf(
        Pair((index + 1).toString(), 1f),
        Pair((channel + 1).toString(), 1f),
        Pair(error?.name ?: "UNKNOWN_ERROR", 2f),
        Pair(error?.message ?: "未知" , 4f),
        Pair(
            when (error?.severity) {
                0 -> "警告"
                1 -> "错误"
                else -> "未知"
            }, 2f
        ),
        Pair(createTime.dateFormat("HH:mm\nyyyy-MM-dd"), 3f),
    )
}
