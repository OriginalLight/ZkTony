package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.ContainerDao
import com.zktony.www.data.local.room.entity.Container
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:52
 */
class ContainerRepository @Inject constructor(
    private val dao: ContainerDao,
    private val plateRepository: PlateRepository,
) {

    suspend fun insert(container: Container) {
        dao.insert(container)
    }

    suspend fun update(container: Container) {
        dao.update(container)
    }

    fun getById(id: Long): Flow<Container> {
        return dao.getById(id)
    }

//    suspend fun init() {
//        val con = dao.getAll().firstOrNull() ?: emptyList()
//        if (con.isEmpty()) {
//            dao.insert(Container(
//                id = 1,
//                name = "默认容器",
//            ))
//            plateRepository.init()
//        }
//    }
}