package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.ActionDao
import com.zktony.www.data.local.room.entity.Action
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 11:29
 */
class ActionRepository @Inject constructor(
    private val dao: ActionDao
) {
    suspend fun insert(action: Action) {
        dao.insert(action)
    }

    suspend fun delete(action: Action) {
        dao.delete(action)
    }

    suspend fun deleteByProgramId(programId: String) {
        dao.deleteByProgramId(programId)
    }

    suspend fun update(action: Action) {
        dao.update(action)
    }

    suspend fun updateBatch(actions: List<Action>) {
        dao.updateBatch(actions)
    }

    fun getByProgramId(programId: String): Flow<List<Action>> {
        return dao.getByProgramId(programId)
    }
}