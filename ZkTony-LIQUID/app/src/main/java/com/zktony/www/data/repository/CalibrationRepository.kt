package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.CalibrationDao
import com.zktony.www.common.room.entity.Calibration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:14
 */
class CalibrationRepository @Inject constructor(
    private val dao: CalibrationDao
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
    }

    fun getAll(): Flow<List<Calibration>> {
        return dao.getAll()
    }

    suspend fun init() {
        val calibrations = dao.getAll().firstOrNull()
        if (calibrations.isNullOrEmpty()) {
            dao.insert(Calibration(enable = 1))
        }
    }
}