package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.PlateDao
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:52
 */
class PlateRepository @Inject constructor(
    private val dao: PlateDao,
    private val holeRepository: HoleRepository,
) {

//    suspend fun insert(plate: Plate) {
//        dao.insert(plate)
//    }
//
//    suspend fun update(plate: Plate) {
//        dao.update(plate)
//    }
//
//    suspend fun delete(plate: Plate) {
//        dao.delete(plate)
//    }
//
//    fun getById(id: Int): Flow<Plate> {
//        return dao.getById(id).zip(holeRepository.getBySubId(id)) { plate, holes ->
//            plate.copy(holeList = holes)
//        }
//    }
//
//    fun getBySubId(id: Long): Flow<List<Plate>> {
//        return flow {
//            val plateList = dao.getBySubId(id).firstOrNull() ?: emptyList()
//            val newList = mutableListOf<Plate>()
//            for (plate in plateList) {
//                val holeList = holeRepository.getBySubId(plate.id).firstOrNull() ?: emptyList()
//                newList.add(plate.copy(holeList = holeList))
//            }
//            emit(newList)
//        }
//    }
//
//    suspend fun init() {
//        val plate = dao.getById(1).firstOrNull()
//        if(plate == null) {
//            dao.insert(Plate(
//                id = 1L,
//                subId = 1,
//                x = 10,
//                y = 1,
//            ))
//            val holeList = mutableListOf<Hole>()
//            for (i in 0 until  10) {
//                holeList.add(Hole(
//                    subId = 1L,
//                    x = i,
//                    y = 1,
//                ))
//            }
//        }
//    }


    /**
     * 计算孔位坐标
     */
//    private suspend fun calculatePoreCoordinate(plate: Plate) {
//        val holeList = holeDao.getByPlateId(plate.id).firstOrNull() ?: emptyList()
//        val min = holeList.filter { it.xAxis != 0f }.minByOrNull { it.x }
//        val max = holeList.filter { it.xAxis != 0f }.maxByOrNull { it.x }
//        if (min == null || max == null) {
//            return
//        } else {
//            val minIndex = min.x
//            val maxIndex = max.x
//            if (maxIndex - minIndex >= 2) {
//                val minAxis = min.xAxis
//                val maxAxis = max.xAxis
//                val distance = (maxAxis - minAxis) / (maxIndex - minIndex)
//                for (i in minIndex + 1 until  maxIndex) {
//                    val hole = holeList.find { it.x == i && it.xAxis == 0f }
//                    hole?.let {
//                        holeDao.update(it.copy(
//                            xAxis = minAxis + (i - minIndex) * distance
//                        ))
//                    }
//                }
//            }
//        }
//    }
}