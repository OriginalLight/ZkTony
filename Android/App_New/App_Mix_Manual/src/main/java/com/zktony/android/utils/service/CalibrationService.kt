package com.zktony.android.utils.service

import android.util.Log
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.NewCalibrationDao
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
    private val dataStore: DataSaverDataStore,
    private val ncDao: NewCalibrationDao
) : AbstractService() {
    override fun create() {
        job = scope.launch {
            val coagulantpulse = dataStore.readData("coagulantpulse", 67500)
            Log.d(
                "CalibrationService",
                "coagulantpulse=========$coagulantpulse"
            )
            var newCalibrations = ncDao.getById(1L)
            Log.d(
                "CalibrationService",
                "newCalibrations=========$newCalibrations"
            )
            newCalibrations.collect { newCalibration ->
                Log.d(
                    "CalibrationService",
                    "newCalibration=========$newCalibration"
                )
                if (newCalibration != null) {
                    hpc[0] = calculateCalibrationFactorNew(64000, 120.0)
                    if (newCalibration.coagulantAvg == 0.0) {
                        hpc[1] = calculateCalibrationFactorNew(coagulantpulse, 1.0 * 1000)
                    } else {
                        hpc[1] = calculateCalibrationFactorNew(
                            coagulantpulse,
                            newCalibration.coagulantAvg * 1000
                        )
                    }
                    if (newCalibration.higeAvg == 0.0) {
                        hpc[2] = calculateCalibrationFactorNew(64000, 5.0 * 1000)
                    } else {
                        hpc[2] = calculateCalibrationFactorNew(64000, newCalibration.higeAvg * 1000)
                    }
                    if (newCalibration.lowAvg == 0.0) {
                        hpc[3] = calculateCalibrationFactorNew(64000, 5.0 * 1000)
                    } else {
                        hpc[3] = calculateCalibrationFactorNew(64000, newCalibration.lowAvg * 1000)

                    }
                    if (newCalibration.rinseAvg == 0.0) {
                        hpc[4] = calculateCalibrationFactorNew(64000, 5.0 * 1000)
                    } else {
                        hpc[4] =
                            calculateCalibrationFactorNew(64000, newCalibration.rinseAvg * 1000)

                    }
                }
            }

        }


    }
}
