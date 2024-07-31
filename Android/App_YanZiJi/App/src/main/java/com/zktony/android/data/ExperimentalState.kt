package com.zktony.android.data

import androidx.compose.ui.graphics.Color

enum class ExperimentalState(val color: Color, val text: String) {
    NONE(Color.Gray, "未插入"),
    READY(Color.Green, "已就绪"),
    TRANSFER(Color.Yellow, "转膜中"),
    STAIN(Color.Yellow, "染色中"),
    ERROR(Color.Red, "错误"),
}