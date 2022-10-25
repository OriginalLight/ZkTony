package com.zktony.www.data.repository

import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.entity.Calibration
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:14
 */
class CalibrationRepository @Inject constructor(
    private val calibrationDao: CalibrationDao
) {
    suspend fun insert(calibration: Calibration) {
        calibrationDao.insert(calibration)
    }

    suspend fun update(calibration: Calibration) {
        calibrationDao.update(calibration)
    }

    fun getCailbration(): Flow<List<Calibration>> {
        return calibrationDao.getCailbration()
    }
}