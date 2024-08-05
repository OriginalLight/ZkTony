package com.zktony.android.data

import androidx.compose.ui.graphics.Color
import com.zktony.android.ui.utils.zktyYellow

enum class ExperimentalState(val color: Color, val text: String) {
    NONE(Color.Gray, "模块未插入"),
    READY(Color.Green, "已就绪"),
    PAUSE(zktyYellow, "暂停中"),
    STARTING(Color.Green, "开始中"),
    TIMING(zktyYellow, "实验中"),
    FILL(zktyYellow, "填充中"),
    DRAIN(zktyYellow, "排液中"),
    ERROR(Color.Red, "错误");
}

fun ExperimentalState.isRunning(): Boolean {
    return this == ExperimentalState.STARTING ||
            this == ExperimentalState.TIMING ||
            this == ExperimentalState.FILL ||
            this == ExperimentalState.PAUSE
}

fun ExperimentalState.disableEdit(): Boolean {
    return this == ExperimentalState.STARTING ||
            this == ExperimentalState.TIMING ||
            this == ExperimentalState.FILL
}