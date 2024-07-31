package com.zktony.room.repository

import androidx.paging.PagingSource
import com.zktony.room.dao.ProgramDao
import com.zktony.room.entities.Program
import javax.inject.Inject

class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao
) {

    /**
     * Get by page.
     * @param name String?.
     * @param startTime Long?.
     * @param endTime Long?.
     * @return PagingSource<Int, Program>.
     */
    fun getByPage(
        name: String? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): PagingSource<Int, Program> {
        return programDao.getByPage(name, startTime, endTime)
    }

    /**
     * Get by id.
     * @param id Long.
     * @return Program.
     */
    fun getById(id: Long): Program? {
        return programDao.getById(id)
    }

    /**
     * Insert.
     * @param program Program.
     * @return program with id if success, 1 if program exists.
     */
    suspend fun insert(program: Program): Boolean {
        val pl = programDao.getByName(program.name)
        if (pl.isNotEmpty()) error(1)
        val id = programDao.insert(program)
        return id > 0
    }

    /**
     * Update.
     * @param program Program.
     * @return effect with success, 1 if program exists.
     */
    suspend fun update(program: Program): Boolean {
        val pl = programDao.getByName(program.name)
        if (pl.any { p -> p.id != program.id }) error(1)
        val effect = programDao.update(program)
        return effect > 0
    }

    /**
     * Delete.
     * @param ids List<Program>.
     * @return effect with success.
     */
    suspend fun deleteByIds(ids: List<Long>): Boolean {
        val effect = programDao.deleteByIds(ids)
        return effect > 0
    }
}