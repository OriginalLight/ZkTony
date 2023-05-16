package com.zktony.android.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.errorprone.annotations.Immutable
import com.zktony.android.data.CalibrationDataConverters
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
@Entity(
    tableName = "calibrations",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
@Immutable
@TypeConverters(CalibrationDataConverters::class)
data class CalibrationEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long = nextId(),
    @ColumnInfo(name = "name") val name: String = "默认",
    @ColumnInfo(name = "data") val data: List<CalibrationData> = emptyList(),
    @ColumnInfo(name = "active") val active: Boolean = false,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
)