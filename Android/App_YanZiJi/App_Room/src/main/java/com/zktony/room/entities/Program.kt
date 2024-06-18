package com.zktony.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.room.IncubationStageConverters
import com.zktony.room.entities.internal.IncubationStage
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
@TypeConverters(IncubationStageConverters::class)
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val stages: List<IncubationStage> = emptyList(),
    val createTime: Date = Date(System.currentTimeMillis())
)