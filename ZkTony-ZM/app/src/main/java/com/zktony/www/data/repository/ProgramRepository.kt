package com.zktony.www.data.repository

import com.zktony.www.common.network.adapter.toResult
import com.zktony.www.common.network.service.ProgramService
import com.zktony.www.common.result.NetworkResult
import com.zktony.www.common.room.dao.ProgramDao
import com.zktony.www.common.room.entity.Program
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:49
 */
class ProgramRepository @Inject constructor(
    private val dao: ProgramDao,
    private val service: ProgramService
) {
    suspend fun insert(program: Program) {
        dao.insert(program)
    }

    fun getAll(): Flow<List<Program>> {
        return dao.getAll()
    }

    suspend fun updateDefaultByKind(kind: Int) {
        dao.updateDefaultByKind(kind)
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

    suspend fun uploadProgram(programs: List<Program>) = flow {
        emit(NetworkResult.Loading)
        emit(service.uploadProgram(programs).toResult())
    }

}