package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "log_snapshots", indices = [Index(value = ["subId"])])
data class LogSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    // 日志编号
    val subId: Long,
    // 运行状态
    val runState: Int = 0,
    // 实验类型
    val experimentType: Int = 0,
    // 运行模式
    val experimentalMode: Int = 0,
    // 故障信息
    val errorInfo: Long = 0L,
    // 当前电流
    val current: String = "0",
    // 当前电压
    val voltage: String = "0",
    // 当前功率
    val power: String = "0",
    // 当前温度
    val temperature: String = "0",
    // 当前计时（秒，向上计时）
    val time: Int = 0,
    // 当前运行步骤
    val step: Int = 0,
    // 到位光耦1状态
    val opt1: Int = 0,
    // 到位光耦2状态
    val opt2: Int = 0,
    // 气泡传感器1状态
    val bub1: Int = 0,
    // 气泡传感器2状态
    val bub2: Int = 0,
    // 操作描述
    val description: String = "",
    // 创建时间
    val createTime: Long = System.currentTimeMillis()
)
