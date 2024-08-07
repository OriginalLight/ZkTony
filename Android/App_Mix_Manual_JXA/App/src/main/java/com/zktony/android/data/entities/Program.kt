package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.DosageConverters
import com.zktony.android.data.PointConverters
import com.zktony.android.data.SpeedConverters
import java.util.Calendar
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    var displayText: String = "None",

    /**
     * 开始浓度
     */
    var startRange: Double = 0.0,

    /**
     * 结束浓度
     */
    var endRange: Double = 0.0,

    /**
     * 常用厚度
     * 0.75，1.0，1.5
     */
    var thickness: String = "1.0",

    /**
     * 促凝剂体积
     */
    var coagulant: Int = 0,

    /**
     * 胶液体积
     */
    var volume: Double = 0.0,

    /**
     * 创建人
     */
    var founder: String = "",

    /**
     * 创建时间
     */
    val createTime: Date = Date(System.currentTimeMillis())
)