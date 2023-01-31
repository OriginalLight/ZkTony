package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.CalibrationDataDao
import com.zktony.www.common.room.entity.CalibrationData
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 13:56
 */
class CalibrationDataRepository @Inject constructor(
    private val dao: CalibrationDataDao
) {
    fun getByCaliId(id: String) = dao.getByCaliId(id)
    suspend fun insert(calibrationData: CalibrationData) = dao.insert(calibrationData)
    suspend fun delete(calibrationData: CalibrationData) = dao.delete(calibrationData)
    suspend fun deleteByCaliId(id: String) = dao.deleteById(id)
}