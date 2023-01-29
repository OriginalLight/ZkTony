package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.PlateDao
import com.zktony.www.common.room.dao.PoreDao
import com.zktony.www.common.room.entity.Plate
import com.zktony.www.common.room.entity.Pore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class PoreRepository @Inject constructor(
    private val dao: PoreDao
) {
    suspend fun insert(pore: Pore) {
        dao.insert(pore)
    }

    suspend fun insertBatch(pores: List<Pore>) {
        dao.insertBatch(pores)
    }

    suspend fun deleteByPlateId(plateId: String) {
        dao.deleteByPlateId(plateId)
    }

   fun getPlatePore(plateId: String): Flow<List<Pore>> {
        return dao.getPlatePore(plateId)
    }
}