package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.zktony.android.data.ListPointConverters
import com.zktony.android.data.entities.internal.Point
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Entity(tableName = "new_calibration")
@Immutable
data class NewCalibration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    var higeLiquidVolume1: Double = 1.8,
    var higeLiquidVolume2: Double = 1.8,
    var higeLiquidVolume3: Double = 1.8,
    var higeAvg: Double = 1.8,
    var lowLiquidVolume1: Double = 1.8,
    var lowLiquidVolume2: Double = 1.8,
    var lowLiquidVolume3: Double = 1.8,
    var lowAvg: Double = 1.8,
    var coagulantLiquidVolume1: Double = 0.9,
    var coagulantLiquidVolume2: Double = 0.9,
    var coagulantLiquidVolume3: Double = 0.9,
    var coagulantAvg: Double = 0.9,
    var rinseLiquidVolume1: Double = 4.5,
    var rinseLiquidVolume2: Double = 4.5,
    var rinseLiquidVolume3: Double = 4.5,
    var rinseAvg: Double = 4.5

)