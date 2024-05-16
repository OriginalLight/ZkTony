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

@Entity(tableName = "errorrecord")
@Immutable
data class ErrorRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,


    /**
     * 状态详情
     */
    var detail: String = "",

    /**
     * 创建时间
     */
    val createTime: Date = Date(System.currentTimeMillis())
)