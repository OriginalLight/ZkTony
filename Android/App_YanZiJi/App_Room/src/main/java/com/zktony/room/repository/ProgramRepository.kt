package com.zktony.room.repository

import com.zktony.room.dao.ProgramDao
import com.zktony.room.entities.Program
import javax.inject.Inject

class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao
) {

    /**
     * Get by page.
     */
    fun getByPage(name: String? = null, startTime: Long? = null, endTime: Long? = null) =
        programDao.getByPage(name, startTime, endTime)

    /**
     * Insert.
     * @param program Program.
     * @return program with id if success, 1 if program exists. 2 if failed.
     */
    suspend fun insert(program: Program): Result<Program> {
        programDao.getByName(program.name)?.let { return Result.failure(Exception("1")) }
        val id = programDao.insert(program)
        return if (id > 0) Result.success(program.copy(id = id)) else Result.failure(Exception("2"))
    }

    /**
     * Delete.
     * @param programs List<Program>.
     * @return effect with success, 1 if failed.
     */
    suspend fun deleteAll(programs: List<Program>): Result<Int> {
        val effect = programDao.deleteAll(programs)
        return if (effect > 0) Result.success(effect) else Result.failure(Exception("1"))
    }

    /**
     * Get by id.
     */
    fun getById(id: Long) = programDao.getById(id)
}