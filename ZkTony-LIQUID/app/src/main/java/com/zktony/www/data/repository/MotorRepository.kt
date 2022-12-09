package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.MotorDao
import com.zktony.www.common.room.entity.Motor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class MotorRepository @Inject constructor(
    private val dao: MotorDao
) {
    suspend fun insert(motor: Motor) {
        dao.insert(motor)
    }

    suspend fun insertBatch(motors: List<Motor>) {
        dao.insertBatch(motors)
    }

    suspend fun update(motor: Motor) {
        dao.update(motor)
    }

    fun getAll(): Flow<List<Motor>> {
        return dao.getAll()
    }

    fun getByBoardAndAddress(board: Int, address: Int): Flow<Motor> {
        return dao.getByBoardAndAddress(board, address)
    }
}