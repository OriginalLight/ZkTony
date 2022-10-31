package com.zktony.www.data.repository

import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.room.dao.ActionDao
import com.zktony.www.common.room.dao.ProgramDao
import com.zktony.www.common.room.entity.ActionEnum
import com.zktony.www.common.room.entity.Program
import com.zktony.www.common.room.entity.getActionEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:49
 */
class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao,
    private val actionDao: ActionDao
) {
    suspend fun insert(program: Program) {
        programDao.insert(program)
    }

    fun getAll(): Flow<List<Program>> {
        return programDao.getAll()
    }

    fun getByName(name: String): Flow<Program> {
        return programDao.getByName(name)
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

    suspend fun updateActions(programId: String) {
        actionDao.getByProgramId(programId).firstOrNull()?.let {
            var actions = ""
            var time = 0f
            if (it.isNotEmpty()) {
                it.forEachIndexed { index, action ->
                    actions += getActionEnum(action.mode).value
                    if (index != it.size - 1) {
                        actions += " -> "
                    }
                    time += if (action.mode == ActionEnum.WASHING.index) {
                        action.time * action.count
                    } else {
                        action.time * 60
                    }
                }
            } else {
                actions = "没有任何操作，去添加吧"
            }
            programDao.getById(programId).firstOrNull()?.let { program ->
                programDao.update(program.copy(actions = actions, actionCount = it.size))
            }
        }
    }

}