package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "program")
data class Program(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val motor: Int = 0,
    val voltage: Float = 0f,
    val time: Float = 0f,
    val count: Int = 0,
    val thickness: String = "0.75",
    val glueType: Int = 0,
    val glueConcentration: Float = 0f,
    val glueMaxConcentration: Float = 0f,
    val glueMinConcentration: Float = 0f,
    val proteinMaxSize: Float = 0f,
    val proteinMinSize: Float = 0f,
    val proteinName: String = "",
    val bufferType: String = "厂家",
    val model: Int = 0,
    val status: Int = 0,
    val def: Int = 0,
    val upload: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis())
)