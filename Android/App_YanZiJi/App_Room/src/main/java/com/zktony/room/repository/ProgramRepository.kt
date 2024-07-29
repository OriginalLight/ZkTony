package com.zktony.room.repository

import com.zktony.room.dao.ProgramDao
import com.zktony.room.entities.Program
import javax.inject.Inject

class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao
) {

    suspend fun init() {
        repeat(100) {
            programDao.insert(
                Program(
                    name = "Program $it",
                    value = "100",
                    time = "100",
                    description = "Description $it"
                )
            )
        }
    }

    /**
     * Get by page.
     */
    fun getByPage() = programDao.getByPage()

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
}