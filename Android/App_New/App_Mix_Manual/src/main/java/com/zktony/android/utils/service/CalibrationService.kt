package com.zktony.android.utils.service

import android.util.Log
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.NewCalibrationDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.dao.SportsLogDao
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/9/1 10:29
 */
class CalibrationService @Inject constructor(
    private val dataStore: DataSaverDataStore,
    private val ncDao: NewCalibrationDao,
    private val slDao: SettingDao,
    private val sportsLogDao: SportsLogDao,
) : AbstractService() {
    override fun create() {
        job = scope.launch {
            val coagulantpulse = dataStore.readData("coagulantpulse", 550000)
            val waste = dataStore.readData(key = "waste", default = 0f)
            if (waste > 0) {
                dataStore.saveData("waste", 0f)
            }

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
                        hpc[2] = calculateCalibrationFactorNew(51200 * 50, 5.0 * 1000)
                    } else {
                        hpc[2] =
                            calculateCalibrationFactorNew(51200 * 50, newCalibration.higeAvg * 1000)
                    }
                    if (newCalibration.lowAvg == 0.0) {
                        hpc[3] = calculateCalibrationFactorNew(51200 * 50, 5.0 * 1000)
                    } else {
                        hpc[3] =
                            calculateCalibrationFactorNew(51200 * 50, newCalibration.lowAvg * 1000)

                    }
                    if (newCalibration.rinseAvg == 0.0) {
                        hpc[4] = calculateCalibrationFactorNew(3200 * 50, 5.0 * 1000)
                    } else {
                        hpc[4] =
                            calculateCalibrationFactorNew(3200 * 50, newCalibration.rinseAvg * 1000)

                    }


                } else {
                    Log.d(
                        "CalibrationService",
                        "=========插入默认校准数据========"
                    )
                    ncDao.insert(
                        NewCalibration(
                            1, 1.6, 1.6, 1.6,
                            1.6, 1.6, 1.6, 1.6,
                            1.6, 1.0, 1.0, 1.0,
                            1.0, 1.6, 1.6, 1.6, 1.6
                        )
                    )
                    Log.d(
                        "CalibrationService",
                        "=========插入默认校准数据完成....插入默认设置数据开始========"
                    )
                    slDao.insert(
                        Setting(
                            1, 0.0, 0.0, 0.0, 500.0, 500.0, 500.0, 0.0, 0.0, 5.0, 1.0, 3.0,
                            5.0, 3.0, 5.0, 3.0, 5.0, 3.0
                        )
                    )
                    Log.d(
                        "CalibrationService",
                        "=========插入默认设置数据完成========"
                    )
                }
            }


            val calendar = Calendar.getInstance() // 创建一个 Calendar 对象表示当前时间

            calendar.add(Calendar.DAY_OF_MONTH, -3) // 将日期向前调整三天

            val year = calendar[Calendar.YEAR] // 年份
            val month = calendar[Calendar.MONTH] + 1 // 月份（注意需要加上1）
            val dayOfMonth = calendar[Calendar.DAY_OF_MONTH] // 日期

            val date = "$year-$month-$dayOfMonth"

            val format = SimpleDateFormat("yyyy-MM-dd")
            val date1 = format.parse(date)
            sportsLogDao.deleteByDate(date1)
        }


    }
}
