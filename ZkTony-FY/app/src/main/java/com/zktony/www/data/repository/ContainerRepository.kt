package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.ContainerDao
import com.zktony.www.common.room.entity.Container
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 9:24
 */
class ContainerRepository @Inject constructor(
    private val dao: ContainerDao
) {
    suspend fun insert(container: Container) {
        dao.insert(container)
    }

    fun getAll(): Flow<List<Container>> {
        return dao.getAll()
    }
}