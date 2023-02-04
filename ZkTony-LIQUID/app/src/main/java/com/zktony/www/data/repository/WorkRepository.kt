package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.HoleDao
import com.zktony.www.common.room.dao.WorkDao
import com.zktony.www.common.room.dao.WorkPlateDao
import com.zktony.www.common.room.entity.Hole
import com.zktony.www.common.room.entity.Plate
import com.zktony.www.common.room.entity.Work
import com.zktony.www.common.room.entity.WorkPlate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        return workDao.getById(id)
    }

    fun getWorkPlateByWorkId(id: String): Flow<List<WorkPlate>> {
        return workPlateDao.getByWorkId(id)
    }

    fun getHoleByWorkId(id: String): Flow<List<Hole>> {
        return holeDao.getByWorkId(id)
    }

    suspend fun removePlate(plate: WorkPlate) {
        workPlateDao.delete(plate)
        holeDao.deleteByPlateId(plate.id)
    }

    suspend fun addPlate(p: Plate, id: String) {
        val plate = WorkPlate(
            workId = id,
            sort = p.sort,
            row = p.row,
            column = p.column,
        )
        workPlateDao.insert(plate)

        val holes = mutableListOf<Hole>()
        holeDao.getByPlateId(p.id).first().forEach {
            holes.add(Hole(
                plateId = plate.id,
                workId = id,
                x = it.x,
                y = it.y,
                xAxis = it.xAxis,
                yAxis = it.yAxis,
            ))
        }
        holeDao.insertBatch(holes)
    }

}