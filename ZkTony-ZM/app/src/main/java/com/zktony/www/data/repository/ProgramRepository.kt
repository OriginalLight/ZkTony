package com.zktony.www.data.repository

import com.zktony.www.data.dao.ProgramDao
import com.zktony.www.data.entity.Program
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:49
 */
class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao
) {
    suspend fun insert(program: Program) {
        programDao.insert(program)
    }

    fun getAll(): Flow<List<Program>> {
        return programDao.getAll()
    }

    suspend fun updateDefaultByKind(kind: Int) {
        programDao.updateDefaultByKind(kind)
    }

    suspend fun delete(program: Program) {
        programDao.delete(program)
    }

    suspend fun update(program: Program) {
        programDao.update(program)
    }

    suspend fun updateBatch(programs: List<Program>) {
        programDao.updateBatch(programs)
    }

    fun withoutUpload(): Flow<List<Program>> {
        return programDao.withoutUpload()
    }

}