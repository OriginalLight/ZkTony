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
    var higeLiquidVolume1: Double = 0.0,
    var higeLiquidVolume2: Double = 0.0,
    var higeLiquidVolume3: Double = 0.0,
    var higeAvg: Double = 0.0,
    var lowLiquidVolume1: Double = 0.0,
    var lowLiquidVolume2: Double = 0.0,
    var lowLiquidVolume3: Double = 0.0,
    var lowAvg: Double = 0.0,
    var coagulantLiquidVolume1: Double = 0.0,
    var coagulantLiquidVolume2: Double = 0.0,
    var coagulantLiquidVolume3: Double = 0.0,
    var coagulantAvg: Double = 0.0,
    var rinseLiquidVolume1: Double = 0.0,
    var rinseLiquidVolume2: Double = 0.0,
    var rinseLiquidVolume3: Double = 0.0,
    var rinseAvg: Double = 0.0

)