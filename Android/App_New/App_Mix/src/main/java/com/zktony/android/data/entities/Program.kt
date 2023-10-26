package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.DosageConverters
import com.zktony.android.data.PointConverters
import com.zktony.android.data.SpeedConverters
import com.zktony.android.data.entities.internal.Dosage
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.data.entities.internal.Speed
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
@TypeConverters(
    PointConverters::class,
    DosageConverters::class,
    SpeedConverters::class
)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val point: Point = Point(),
    val dosage: Dosage = Dosage(),
    val speed: Speed = Speed(),
    val createTime: Date = Date(System.currentTimeMillis()),
)