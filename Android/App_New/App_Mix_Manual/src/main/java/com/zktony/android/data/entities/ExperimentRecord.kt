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
 * 实验记录实体类
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */

@Entity(tableName = "experimentrecord")
@Immutable
data class ExperimentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,


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
    var coagulant: Double = 0.0,

    /**
     * 胶液体积
     */
    var volume: Double = 0.0,

    /**
     * 制胶数量
     */
    var number: Int = 0,

    /**
     * 状态；0-运行中；1-已完成；2-中止；3-故障
     */
    var status: String = "",

    /**
     * 状态详情
     */
    var detail: String = "",

    /**
     * 创建时间
     */
    val createTime: Date = Date(System.currentTimeMillis())
)