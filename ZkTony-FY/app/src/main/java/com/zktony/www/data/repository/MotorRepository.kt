package com.zktony.www.data.repository

import com.zktony.www.data.dao.MotorDao
import com.zktony.www.data.entity.Motor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class MotorRepository @Inject constructor(
    private val motorDao: MotorDao
) {
    suspend fun insert(motor: Motor) {
        motorDao.insert(motor)
    }

    suspend fun insertBatch(motors: List<Motor>) {
        motorDao.insertBatch(motors)
    }

    suspend fun update(motor: Motor) {
        motorDao.update(motor)
    }

    fun getAll(): Flow<List<Motor>> {
        return motorDao.getAll()
    }

    fun getByBoardAndAddress(board: Int, address: Int): Flow<Motor> {
        return motorDao.getByBoardAndAddress(board, address)
    }
}