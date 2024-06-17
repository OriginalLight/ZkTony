package com.zktony.android.utils.service

import android.util.Log
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.ExpectedDao
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.NewCalibrationDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.dao.SportsLogDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.Expected
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Setting
import com.zktony.android.ui.EPStatus
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactor
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactorNew
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpc
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
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
    private val expectedDao: ExpectedDao,
    private val erDao: ExperimentRecordDao,
) : AbstractService() {
    override fun create() {
        job = scope.launch {


            val calendar = Calendar.getInstance() // 创建一个 Calendar 对象表示当前时间

            calendar.add(Calendar.DAY_OF_MONTH, -3) // 将日期向前调整三天

            val year = calendar[Calendar.YEAR] // 年份
            val month = calendar[Calendar.MONTH] + 1 // 月份（注意需要加上1）
            val dayOfMonth = calendar[Calendar.DAY_OF_MONTH] // 日期

            val date = "$year-$month-$dayOfMonth"

            val format = SimpleDateFormat("yyyy-MM-dd")
            val date1 = format.parse(date)


            var binList =
                File("sdcard/Download").listFiles { _, name -> name.endsWith(".txt") }?.toList()
                    ?: emptyList()

            val currentTime = System.currentTimeMillis()

            Log.d(
                "writeThread",
                "判断删除命令日志文件的当前时间:$currentTime"
            )

            if (binList.size > 7) {
                binList.forEach {
                    Log.d(
                        "writeThread",
                        "Filename======${it.name}"
                    )
                    Log.d(
                        "writeThread",
                        "命令日志文件名称:${it.name},文件最后修改的时间:${it.lastModified()}"
                    )

                    if (currentTime - it.lastModified() > TimeUnit.DAYS.toMillis(7)) {
                        it.delete()
                        Log.d(
                            "writeThread",
                            "删除命令日志文件是否成功:${it.delete()},删除的文件名称:${it.name}"
                        )
                    } else {
                        Log.d(
                            "writeThread",
                            "删除命令日志文件的时间不够,时间差是:${currentTime - it.lastModified()}"
                        )
                    }

                }
            } else {
                Log.d(
                    "writeThread",
                    "命令日志文件小于7的具体文件数量:${binList.size}"
                )
            }




            sportsLogDao.deleteByDate(date1)
            val erAll = erDao.getList()

            Log.d(
                "experimentRecord",
                "=========experimentRecord========${erAll.size}"
            )
            if (erAll.isNotEmpty()) {
                erAll.forEach {
                    Log.d(
                        "experimentRecord",
                        "=========experimentRecord========$it"
                    )
                    if (it.status == EPStatus.RUNNING) {
                        it.status = EPStatus.ABORT
                        erDao.update(it)
                    }
                }
            }

            val coagulantpulse = dataStore.readData("coagulantpulse", 550000)

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
                            5.0, 3.0, 5.0, 3.0, 5.0, 3.0, 10.0
                        )
                    )

                    expectedDao.insert(Expected())

                    Log.d(
                        "CalibrationService",
                        "=========插入默认设置数据完成========"
                    )
                }
            }


        }
    }
}
