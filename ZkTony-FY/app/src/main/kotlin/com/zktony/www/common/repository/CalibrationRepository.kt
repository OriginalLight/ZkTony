package com.zktony.www.common.repository

import com.zktony.www.common.room.dao.CalibrationDao
import com.zktony.www.common.room.dao.CalibrationDataDao
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.CalibrationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:14
 */
class CalibrationRepository @Inject constructor(
    private val dao: CalibrationDao,
    private val dataDao: CalibrationDataDao
) {
    suspend fun insert(calibration: Calibration) {
        dao.insert(calibration)
    }

    suspend fun update(calibration: Calibration) {
        dao.update(calibration)
    }

    suspend fun updateBatch(calibrations: List<Calibration>) {
        dao.updateBatch(calibrations)
    }

    suspend fun delete(calibration: Calibration) {
        dao.delete(calibration)
        dataDao.deleteByCalibrationId(calibration.id)
    }

    fun getAll(): Flow<List<Calibration>> {
        return dao.getAll()
    }

    fun getById(id: String): Flow<Calibration> {
        return dao.getById(id)
    }

    suspend fun insertData(calibrationData: CalibrationData) {
        dataDao.insert(calibrationData)
        calculateActual(calibrationData.calibrationId)
    }

    suspend fun deleteData(calibrationData: CalibrationData) {
        dataDao.delete(calibrationData)
        calculateActual(calibrationData.calibrationId)
    }

    fun getDataById(id: String): Flow<List<CalibrationData>> {
        return dataDao.getByCalibrationId(id)
    }

    suspend fun init() {
        val calibrations = dao.getAll().firstOrNull()
        if (calibrations.isNullOrEmpty()) {
            dao.insert(Calibration(enable = 1))
        }
    }

    // 计算实际值
    private suspend fun calculateActual(id: String) {
        val cali = dao.getById(id).firstOrNull()
        val dataList = dataDao.getByCalibrationId(id).firstOrNull()
        var v1 = 180f
        var v2 = 180f
        var v3 = 180f
        var v4 = 180f
        var v5 = 180f
        var v6 = 180f
        if (dataList != null) {
            if (dataList.isNotEmpty()) {
                val list1 = dataList.filter { it.pumpId == 0 }
                val list2 = dataList.filter { it.pumpId == 1 }
                val list3 = dataList.filter { it.pumpId == 2 }
                val list4 = dataList.filter { it.pumpId == 3 }
                val list5 = dataList.filter { it.pumpId == 4 }
                val list6 = dataList.filter { it.pumpId == 5 }
                if (list1.isNotEmpty()) {
                    v1 = list1.map { it.percent * 180f }.average().toFloat()
                }
                if (list2.isNotEmpty()) {
                    v2 = list2.map { it.percent * 180f }.average().toFloat()
                }
                if (list3.isNotEmpty()) {
                    v3 = list3.map { it.percent * 180f }.average().toFloat()
                }
                if (list4.isNotEmpty()) {
                    v4 = list4.map { it.percent * 180f }.average().toFloat()
                }
                if (list5.isNotEmpty()) {
                    v5 = list5.map { it.percent * 180f }.average().toFloat()
                }
                if (list6.isNotEmpty()) {
                    v6 = list6.map { it.percent * 180f }.average().toFloat()
                }
            }
        }
        dao.update(cali!!.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4, v5 = v5, v6 = v6))
    }
}