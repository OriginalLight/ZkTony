package com.zktony.android.utils.service

import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactor
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactorNew
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpc
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/9/1 10:29
 */
class CalibrationService @Inject constructor(
    private val dataStore: DataSaverDataStore
) : AbstractService() {
    override fun create() {

        val higeLiquidVolume1 = dataStore.readData("higeLiquidVolume1", 0.0)
        val higeLiquidVolume2 = dataStore.readData("higeLiquidVolume2", 0.0)
        val higeLiquidVolume3 = dataStore.readData("higeLiquidVolume3", 0.0)

        val lowLiquidVolume1 = dataStore.readData("lowLiquidVolume1", 0.0)
        val lowLiquidVolume2 = dataStore.readData("lowLiquidVolume2", 0.0)
        val lowLiquidVolume3 = dataStore.readData("lowLiquidVolume3", 0.0)

        val rinseLiquidVolume1 = dataStore.readData("rinseLiquidVolume1", 0.0)
        val rinseLiquidVolume2 = dataStore.readData("rinseLiquidVolume2", 0.0)
        val rinseLiquidVolume3 = dataStore.readData("rinseLiquidVolume3", 0.0)

        val coagulantLiquidVolume1 = dataStore.readData("coagulantLiquidVolume1", 0.0)
        val coagulantLiquidVolume2 = dataStore.readData("coagulantLiquidVolume2", 0.0)
        val coagulantLiquidVolume3 = dataStore.readData("coagulantLiquidVolume3", 0.0)

        val coagulantpulse = dataStore.readData("coagulantpulse", 67500)

        val higeAvg = (higeLiquidVolume1 + higeLiquidVolume2 + higeLiquidVolume3) / 3
        val lowAvg = (lowLiquidVolume1 + lowLiquidVolume2 + lowLiquidVolume3) / 3
        val rinseAvg = (rinseLiquidVolume1 + rinseLiquidVolume2 + rinseLiquidVolume3) / 3
        var coagulantAvg =
            (coagulantLiquidVolume1 + coagulantLiquidVolume1 + coagulantLiquidVolume1) / 3

        if (coagulantAvg == 0.0) coagulantAvg = 1.0
        if (higeAvg == 0.0) coagulantAvg = 5.0
        if (lowAvg == 0.0) coagulantAvg = 5.0
        if (rinseAvg == 0.0) coagulantAvg = 5.0

        hpc[0] = calculateCalibrationFactorNew(64000, 10.toDouble())

        hpc[1] = calculateCalibrationFactorNew(coagulantpulse, coagulantAvg * 1000)

        hpc[2] = calculateCalibrationFactorNew(64000, higeAvg * 1000)

        hpc[3] = calculateCalibrationFactorNew(64000, lowAvg * 1000)

        hpc[4] = calculateCalibrationFactorNew(64000, rinseAvg * 1000)


    }
}
