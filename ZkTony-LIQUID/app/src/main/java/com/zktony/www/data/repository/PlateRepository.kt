package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.PlateDao
import com.zktony.www.common.room.dao.PoreDao
import com.zktony.www.common.room.entity.Plate
import com.zktony.www.common.room.entity.Pore
import com.zktony.www.common.utils.Logger
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class PlateRepository @Inject constructor(
    private val plateDao: PlateDao,
    private val poreDao: PoreDao,
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

    fun getPoreByPlateId(plateId: String): Flow<List<Pore>> {
        return poreDao.getPlatePore(plateId)
    }

    suspend fun init() {
        val plates = plateDao.getAllPlate().firstOrNull()
        if (plates.isNullOrEmpty()) {
            plateDao.insertBatch(
                listOf(
                    Plate(sort = 0),
                    Plate(sort = 1),
                    Plate(sort = 2),
                    Plate(sort = 3),
                    Plate(sort = 4),
                )
            )
        }
    }

    suspend fun load() : Flow<List<Plate>> {
        return flow {
            plateDao.getAllPlate().distinctUntilChanged().collect{
                it.filter { plate -> plate.sort != 4 }.forEach { plate ->
                    val pores = poreDao.getPlatePore(plate.id).firstOrNull()
                    pores?.let { plate.pores = pores }
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
        val xSpace = x / (plate.column - 1)
        val ySpace = y / (plate.row - 1)
        val poreList = mutableListOf<Pore>()
        for (i in 0 until plate.row) {
            for (j in 0 until plate.column) {
                poreList.add(
                    Pore(
                        plateId = plate.id,
                        x = j,
                        y = i,
                        xAxis = plate.x1 + xSpace * j,
                        yAxis = plate.y1 + ySpace * i
                    )
                )
            }
        }
        poreDao.deleteByPlateId(plate.id)
        poreDao.insertBatch(poreList)
    }
}