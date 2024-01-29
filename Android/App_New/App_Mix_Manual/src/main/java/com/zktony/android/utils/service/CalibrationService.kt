package com.zktony.android.utils.service

import android.util.Log
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.NewCalibrationDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Setting
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactor
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactorNew
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpc
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/9/1 10:29
 */
class CalibrationService @Inject constructor(
    private val dataStore: DataSaverDataStore,
    private val ncDao: NewCalibrationDao,
    private val slDao: SettingDao,
) : AbstractService() {
    override fun create() {
        job = scope.launch {
            val coagulantpulse = dataStore.readData("coagulantpulse", 1080000)

            var newCalibrations = ncDao.getById(1L)
            newCalibrations.collect { newCalibration ->
                Log.d(
                    "CalibrationService",
                    "newCalibration=========$newCalibration"
                )
                if (newCalibration != null) {
                    Log.d(
                        "CalibrationService",
                        "newCalibration.higeAvg=========" + newCalibration.higeAvg
                    )

                    Log.d(
                        "CalibrationService",
                        "newCalibration.lowAvg=========" + newCalibration.lowAvg
                    )

                    Log.d(
                        "CalibrationService",
                        "newCalibration.rinseAvg=========" + newCalibration.rinseAvg
                    )

                    Log.d(
                        "CalibrationService",
                        "newCalibration.coagulantAvg=========" + newCalibration.coagulantAvg
                    )
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
                        hpc[2] = calculateCalibrationFactorNew(51200 * 10, 5.0 * 1000)
                    } else {
                        hpc[2] =
                            calculateCalibrationFactorNew(51200 * 10, newCalibration.higeAvg * 1000)
                    }
                    if (newCalibration.lowAvg == 0.0) {
                        hpc[3] = calculateCalibrationFactorNew(51200 * 10, 5.0 * 1000)
                    } else {
                        hpc[3] =
                            calculateCalibrationFactorNew(51200 * 10, newCalibration.lowAvg * 1000)

                    }
                    if (newCalibration.rinseAvg == 0.0) {
                        hpc[4] = calculateCalibrationFactorNew(32000, 5.0 * 1000)
                    } else {
                        hpc[4] =
                            calculateCalibrationFactorNew(32000, newCalibration.rinseAvg * 1000)

                    }


                } else {
                    Log.d(
                        "CalibrationService",
                        "=========插入默认校准数据========"
                    )
                    ncDao.insert(
                        NewCalibration(
                            1, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0, 0.0
                        )
                    )
                    Log.d(
                        "CalibrationService",
                        "=========插入默认校准数据完成....插入默认设置数据开始========"
                    )
                    slDao.insert(
                        Setting(
                            1, 0.0, 0.0, 0.0, 500.0, 500.0, 500.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0
                        )
                    )
                    Log.d(
                        "CalibrationService",
                        "=========插入默认设置数据完成========"
                    )
                }
            }

//            var settings = slDao.getById(1L)
//            settings.collect { setting ->
//                Log.d(
//                    "CalibrationService",
//                    "setting=========$setting"
//                )
//                if (setting == null) {
//                    slDao.insert(
//                        Setting(
//                            1, 0.0, 0.0, 0.0, 500.0, 500.0, 500.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0
//                        )
//                    )
//                }
//            }

        }


    }
}
