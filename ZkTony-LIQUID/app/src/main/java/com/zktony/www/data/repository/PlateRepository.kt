package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.PlateDao
import com.zktony.www.common.room.entity.Plate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class PlateRepository @Inject constructor(
    private val dao: PlateDao
) {
    suspend fun insert(plate: Plate) {
        dao.insert(plate)
    }

    suspend fun update(plate: Plate) {
        dao.update(plate)
    }

    fun getPlateBySort(id: Int): Flow<Plate> {
        return dao.getPlateBySort(id)
    }

    suspend fun init() {
        val plates = dao.getAllPlate().firstOrNull()
        if (plates.isNullOrEmpty()) {
            dao.insertBatch(listOf(
                Plate(sort = 0),
                Plate(sort = 1),
                Plate(sort = 2),
                Plate(sort = 3),
            ))
        }
    }
}