package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class PlateRepository @Inject constructor(
    private val plateDao: PlateDao,
    private val holeDao: HoleDao,
) {
    suspend fun updatePlate(plate: Plate, calculate: Boolean = true) {
        plateDao.update(plate)
        if (calculate) {
            calculatePoreCoordinate(plate)
        }
    }

    fun getPlateBySort(id: Int): Flow<Plate> {
        return plateDao.getPlateBySort(id)
    }

    fun getAllPlate(): Flow<List<Plate>> {
        return plateDao.getAll()
    }

    fun getHoleByPlateId(plateId: String): Flow<List<Hole>> {
        return holeDao.getByPlateId(plateId)
    }

    suspend fun init() {
        val plates = plateDao.getAll().firstOrNull()
        if (plates.isNullOrEmpty()) {
            val plate1 = Plate(sort = 0)
            val plate2 = Plate(sort = 1)
            val plate3 = Plate(sort = 2)
            val plate4 = Plate(sort = 3)
            val plate5 = Plate(sort = 4)
            plateDao.insertAll(
                listOf(
                    plate1,
                    plate2,
                    plate3,
                    plate4,
                    plate5
                )
            )
            calculatePoreCoordinate(plate1)
            calculatePoreCoordinate(plate2)
            calculatePoreCoordinate(plate3)
            calculatePoreCoordinate(plate4)
        }
    }

    suspend fun load(): Flow<List<Plate>> {
        return flow {
            plateDao.getAll().distinctUntilChanged().collect {
                it.filter { plate -> plate.sort != 4 }.forEach { plate ->
                    val holes = holeDao.getByPlateId(plate.id).firstOrNull()
                    holes?.let { plate.holes = holes }
                }
                emit(it)
            }
        }
    }

    /**
     * 计算孔位坐标
     */
    private suspend fun calculatePoreCoordinate(plate: Plate) {
        val x = plate.x2 - plate.x1
        val y = plate.y2 - plate.y1
        val xSpace = x / if (plate.column == 1) 1 else (plate.column - 1)
        val ySpace = y / if (plate.row == 1) 1 else (plate.row - 1)
        val holeList = mutableListOf<Hole>()
        for (i in 0 until plate.row) {
            for (j in 0 until plate.column) {
                holeList.add(
                    Hole(
                        plateId = plate.id,
                        x = j,
                        y = i,
                        xAxis = plate.x1 + xSpace * j,
                        yAxis = plate.y1 + ySpace * i
                    )
                )
            }
        }
        holeDao.deleteByPlateId(plate.id)
        holeDao.insertAll(holeList)
    }
}