package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.MotorDao
import com.zktony.www.common.room.entity.Motor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    fun getById(id: Int): Flow<Motor> {
        return dao.getById(id)
    }

    suspend fun init() {
        dao.getAll().first().run {
            if (this.isEmpty()) {
                val motorList = mutableListOf<Motor>()
                motorList.add(Motor(id = 0, name = "X轴", address = 1))
                motorList.add(Motor(id = 1, name = "Y轴", address = 2))
                motorList.add(Motor(id = 2, name = "Z轴", address = 3))
                for (i in 1..5) {
                    val motor = Motor(
                        id = i + 2,
                        name = "泵$i",
                        address = if (i <= 3) i else i - 3,
                    )
                    motorList.add(motor)
                }
                dao.insertBatch(motorList)
            }
        }
    }
}