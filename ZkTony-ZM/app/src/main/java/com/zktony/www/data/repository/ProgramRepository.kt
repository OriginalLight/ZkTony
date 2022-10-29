package com.zktony.www.data.repository

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.common.room.dao.ProgramDao
import com.zktony.www.common.room.entity.Program
import com.zktony.www.common.network.service.ProgramService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:49
 */
class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao,
    private val service: ProgramService
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

    suspend fun uploadProgram(programs: List<Program>): NetworkResponse<Any> {
        return service.uploadProgram(programs)
    }

}