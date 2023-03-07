package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.WorkDao
import com.zktony.www.data.local.room.dao.WorkPlateDao
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.data.local.room.entity.Work
import com.zktony.www.data.local.room.entity.WorkPlate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    fun getWorkPlateById(id: String): Flow<WorkPlate> {
        return workPlateDao.getById(id)
    }

    fun getHoleByWorkId(id: String): Flow<List<Hole>> {
        return holeDao.getByWorkId(id)
    }

    fun getHoleByPlateId(id: String): Flow<List<Hole>> {
        return holeDao.getByPlateId(id)
    }

    suspend fun updatePlate(workPlate: WorkPlate) {
        workPlateDao.update(workPlate)
    }

    suspend fun updateHole(hole: Hole) {
        holeDao.update(hole)
    }

    suspend fun updateHoleBatch(holes: List<Hole>) {
        holeDao.updateAll(holes)
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
            holes.add(
                Hole(
                    plateId = plate.id,
                    workId = id,
                    x = it.x,
                    y = it.y,
                    xAxis = it.xAxis,
                    yAxis = it.yAxis,
                )
            )
        }
        holeDao.insertAll(holes)
    }

}