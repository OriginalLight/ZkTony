package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.ProgramDao
import com.zktony.www.common.room.entity.Program
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:49
 */
class ProgramRepository @Inject constructor(
    private val dao: ProgramDao
) {
    suspend fun insert(program: Program) {
        dao.insert(program)
    }

    fun getAll(): Flow<List<Program>> {
        return dao.getAll()
    }

    fun getById(id: String): Flow<Program> {
        return dao.getById(id)
    }

    fun getByName(name: String): Flow<Program> {
        return dao.getByName(name)
    }

    suspend fun delete(program: Program) {
        dao.delete(program)
    }

    suspend fun update(program: Program) {
        dao.update(program)
    }

    suspend fun updateBatch(programs: List<Program>) {
        dao.updateBatch(programs)
    }

    fun withoutUpload(): Flow<List<Program>> {
        return dao.withoutUpload()
    }
}