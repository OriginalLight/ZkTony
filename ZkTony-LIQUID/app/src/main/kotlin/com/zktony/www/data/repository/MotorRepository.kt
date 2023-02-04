package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.MotorDao
import com.zktony.www.common.room.entity.Motor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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

    suspend fun update(motor: Motor) {
        dao.update(motor)
    }

    fun getAll(): Flow<List<Motor>> {
        return dao.getAll()
    }

    fun getByBoardAndAddress(board: Int, address: Int): Flow<Motor> {
        return dao.getByBoardAndAddress(board, address)
    }

    suspend fun init() {
        val motors = dao.getAll().firstOrNull()
        if (motors.isNullOrEmpty()) {
            val motorList = mutableListOf<Motor>()
            motorList.add(Motor(name = "X轴", address = 1))
            motorList.add(Motor(name = "Y轴", address = 2))
            for (i in 1..4) {
                val motor = Motor(
                    name = "泵$i",
                    address = if (i == 1) i + 2 else i - 1,
                    board = if (i == 1) 0 else 1,
                    motorType = 1,
                )
                motorList.add(motor)
            }
            dao.insertBatch(motorList)
        }
    }
}