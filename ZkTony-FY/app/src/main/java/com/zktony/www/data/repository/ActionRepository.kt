package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.ActionDao
import com.zktony.www.common.room.entity.Action
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 11:29
 */
class ActionRepository @Inject constructor(
    private val actionDao: ActionDao
) {
    suspend fun insert(action: Action) {
        actionDao.insert(action)
    }

    suspend fun delete(action: Action) {
        actionDao.delete(action)
    }

    suspend fun deleteByProgramId(programId: String) {
        actionDao.deleteByProgramId(programId)
    }

    suspend fun update(action: Action) {
        actionDao.update(action)
    }

    suspend fun updateBatch(actions: List<Action>) {
        actionDao.updateBatch(actions)
    }

    fun getByProgramId(programId: String): Flow<List<Action>> {
        return actionDao.getByProgramId(programId)
    }
}