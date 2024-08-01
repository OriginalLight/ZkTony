package com.zktony.android.data

import androidx.compose.ui.graphics.Color

enum class ExperimentalState(val color: Color, val text: String) {
    NONE(Color.Gray, "未插入"),
    READY(Color.Green, "已就绪"),
    STARTING(Color.Green, "开始中"),
    TIMING(Color.Yellow, "计时中"),
    FILL(Color.Yellow, "填充中"),
    DRAIN(Color.Yellow, "排液中"),
    ERROR(Color.Red, "错误"),
}