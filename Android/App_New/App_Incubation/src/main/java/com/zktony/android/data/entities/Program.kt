package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.FloatConverters
import com.zktony.android.data.IntConverters
import java.util.Date
import java.util.concurrent.TimeUnit

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
    IntConverters::class,
    FloatConverters::class,
)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "text") val text: String = "None",
    @ColumnInfo(name = "active") val active: List<Int> = listOf(0, 1, 2, 3, 4, 5),
    @ColumnInfo(name = "axis") val axis: List<Float> = listOf(0f, 0f),
    @ColumnInfo(name = "volume") val volume: List<Float> = listOf(0f, 0f, 0f, 0f),
    @ColumnInfo(name = "count") val count: Int = 0,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
)


data class IncubationStage(
    val date: Date,
    val parameters: IncubationParameters,
    val status: IncubationStatus,
)

sealed class IncubationParameters(
    open val temperature: Float,
    open val dosage: Int,
    open val time: Float,
    open val timeUnit: TimeUnit,
) {
    data class Blocking(
        override val temperature: Float,
        override val dosage: Int,
        override val time: Float,
        override val timeUnit: TimeUnit,
    ) : IncubationParameters(temperature, dosage, time, timeUnit)

    data class PrimaryAntibody(
        override val temperature: Float,
        override val dosage: Int,
        override val time: Float,
        override val timeUnit: TimeUnit,
        val recycle: Boolean,
    ) : IncubationParameters(temperature, dosage, time, timeUnit)

    data class SecondaryAntibody(
        override val temperature: Float,
        override val dosage: Int,
        override val time: Float,
        override val timeUnit: TimeUnit,
    ) : IncubationParameters(temperature, dosage, time, timeUnit)

    data class Wash(
        override val temperature: Float,
        override val dosage: Int,
        override val time: Float,
        override val timeUnit: TimeUnit,
        val frequency: Int,
    ) : IncubationParameters(temperature, dosage, time, timeUnit)

    data class Buffer(
        override val temperature: Float,
        override val dosage: Int,
        override val time: Float,
        override val timeUnit: TimeUnit,
    ) : IncubationParameters(temperature, dosage, time, timeUnit)
}

enum class IncubationStatus {
    FINISHED,
    CURRENT,
    UPCOMING
}