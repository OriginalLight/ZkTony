package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.IncubationStageConverters
import com.zktony.android.data.entities.internal.IncubationStage
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
@TypeConverters(IncubationStageConverters::class)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val stages: List<IncubationStage> = emptyList(),
    val createTime: Date = Date(System.currentTimeMillis())
)  {

    fun getPreviewVolume(): List<String> {
        var s1 = 0.0
        var s2 = 0.0
        var s3 = 0.0
        var s4 = 0.0
        stages.forEach {
            when(it.type) {
                0 -> { s1 += it.dosage / 1000.0 }
                1 -> {
                    s2 += it.dosage / 1000.0
                    s4 += 10.0
                }
                2 -> {
                    s3 += it.dosage / 1000.0
                    s4 += 10.0
                }
                3 -> s4 += (it.dosage / 1000.0) * it.times
                4 -> s4 += it.dosage / 1000.0
            }
        }
        s4 += 20.0
        return listOf(
            s1.toBigDecimal().stripTrailingZeros().toPlainString(),
            s2.toBigDecimal().stripTrailingZeros().toPlainString(),
            s3.toBigDecimal().stripTrailingZeros().toPlainString(),
            s4.toBigDecimal().stripTrailingZeros().toPlainString()
        )
    }
}