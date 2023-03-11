package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.CalibrationDao
import com.zktony.www.data.local.room.dao.CalibrationDataDao
import com.zktony.www.data.local.room.entity.Calibration
import com.zktony.www.data.local.room.entity.CalibrationData
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
        dao.updateAll(calibrations)
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
        var v1 = 200f
        var v2 = 200f
        var v3 = 200f
        var v4 = 200f
        if (!dataList.isNullOrEmpty()) {
            dataList.filter { it.pumpId == 0 }.forEach{ v1 *= it.percent }
            dataList.filter { it.pumpId == 1 }.forEach{ v2 *= it.percent }
            dataList.filter { it.pumpId == 2 }.forEach{ v3 *= it.percent }
            dataList.filter { it.pumpId == 3 }.forEach{ v4 *= it.percent }
        }
        dao.update(cali!!.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4))
    }
}