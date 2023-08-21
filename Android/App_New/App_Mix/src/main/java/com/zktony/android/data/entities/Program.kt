package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.CoordinateConverters
import com.zktony.android.data.DosageConverters
import com.zktony.android.data.SpeedConverters
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(
    tableName = "programs",
    indices = [
        Index(value = ["text"], unique = true)
    ]
)
@TypeConverters(
    CoordinateConverters::class,
    DosageConverters::class,
    SpeedConverters::class
)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "text")
    val text: String = "None",
    @ColumnInfo(name = "coordinate")
    val coordinate: Coordinate = Coordinate(),
    @ColumnInfo(name = "dosage")
    val dosage: Dosage = Dosage(),
    @ColumnInfo(name = "speed")
    val speed: Speed = Speed(),
    @ColumnInfo(name = "create_time")
    val createTime: Date = Date(System.currentTimeMillis()),
)

data class Coordinate(
    val abscissa: Double = 0.0,
    val ordinate: Double = 0.0,
)

data class Dosage(
    val colloid: Double = 0.0,
    val coagulant: Double = 0.0,
    val preColloid: Double = 0.0,
    val preCoagulant: Double = 0.0,
)

data class Speed(
    val glue: Double = 80.0,
    val pre: Double = 300.0,
)