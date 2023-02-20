package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.MotorDao
import com.zktony.www.data.local.room.entity.Motor
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

    fun getById(id: Int): Flow<Motor> {
        return dao.getById(id)
    }

    suspend fun init() {
        val motors = dao.getAll().firstOrNull()
        if (motors.isNullOrEmpty()) {
            dao.insertBatch(
                listOf(
                    Motor(id = 0, name = "X轴", address = 1),
                    Motor(id = 1, name = "Y轴", address = 2),
                    Motor(id = 2, name = "泵一", address = 3),
                    Motor(id = 3, name = "泵二", address = 1),
                    Motor(id = 4, name = "泵三", address = 2),
                    Motor(id = 5, name = "泵四", address = 3)
                )
            )
        }
    }
}