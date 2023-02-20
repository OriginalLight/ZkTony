package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.ProgramDao
import com.zktony.www.data.local.room.entity.Program
import com.zktony.www.data.remote.model.ProgramDTO
import com.zktony.www.data.remote.result.getNetworkResult
import com.zktony.www.data.remote.service.ProgramService
import kotlinx.coroutines.flow.Flow
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

    fun getById(id: String): Flow<Program> {
        return dao.getById(id)
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

    suspend fun uploadProgram(programs: List<ProgramDTO>) =
        service.uploadProgram(programs).getNetworkResult()

}