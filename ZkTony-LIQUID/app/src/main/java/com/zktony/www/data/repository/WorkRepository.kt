package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.HoleDao
import com.zktony.www.common.room.dao.WorkDao
import com.zktony.www.common.room.dao.WorkPlateDao
import com.zktony.www.common.room.entity.Work
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class WorkRepository @Inject constructor(
    private val workDao: WorkDao,
    private val workPlateDao: WorkPlateDao,
    private val holeDao: HoleDao
) {

    suspend fun insertWork(work: Work) {
        workDao.insert(work)
    }

    suspend fun deleteWork(work: Work) {
        workDao.delete(work)
        workPlateDao.deleteByWorkId(work.id)
        holeDao.deleteByWorkId(work.id)
    }

    fun getAllWork(): Flow<List<Work>> {
        return workDao.getAll()
    }

    fun getWorkById(id: String): Flow<Work> {
        return workDao.getById(id).zip(workPlateDao.getByWorkId(id)) { work, workPlates ->
            work.plates = workPlates
            work
        }.zip(holeDao.getByWorkId(id)) { work, holes ->
            work.plates.forEach { workPlate ->
                workPlate.holes = holes.filter { it.plateId == workPlate.id }
            }
            work
        }
    }

}