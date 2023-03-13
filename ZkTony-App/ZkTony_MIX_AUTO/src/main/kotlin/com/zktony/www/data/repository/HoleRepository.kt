package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.entity.Hole
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:52
 */
class HoleRepository @Inject constructor(
    private val dao: HoleDao,
) {
    suspend fun insertAll(holes: List<Hole>) {
        dao.insertAll(holes)
    }

    suspend fun update(hole: Hole) {
        dao.update(hole)
    }

    suspend fun deleteAll(hole: Hole) {
        dao.delete(hole)
    }

    suspend fun deleteBySubId(id: Long) {
        dao.deleteBySubId(id)
    }

    fun getBySubId(id: Long): Flow<List<Hole>> {
        return dao.getBySubId(id)
    }

}