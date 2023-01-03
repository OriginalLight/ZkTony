package com.zktony.www.data.repository

import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.model.Calibration
import kotlinx.coroutines.flow.Flow
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

    suspend fun delete(calibration: Calibration) {
        dao.delete(calibration)
    }

    fun getAll(): Flow<List<Calibration>> {
        return dao.getAll()
    }

    fun getByName(name: String): Flow<List<Calibration>> {
        return dao.getByName(name)
    }

    fun getById(id: String): Flow<Calibration> {
        return dao.getById(id)
    }

    fun getDefault(): Flow<List<Calibration>> {
        return dao.getDefault()
    }

    suspend fun select(cali: Calibration) {
        dao.removeDefault()
        dao.update(cali.copy(status = 1))
    }
}