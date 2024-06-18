package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zktony.android.data.OrificePlateConverters
import com.zktony.android.data.entities.internal.OrificePlate
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "program")
@TypeConverters(OrificePlateConverters::class)
@Immutable
data class Program(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val displayText: String = "None",
    val orificePlates: List<OrificePlate> = listOf(OrificePlate(orifices = OrificePlate().generateOrifices())),
    val createTime: Date = Date(System.currentTimeMillis())
)