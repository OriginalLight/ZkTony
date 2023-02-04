package com.zktony.www.common.room.repository

import com.zktony.www.common.room.dao.TestDao
import com.zktony.www.common.room.entity.Test
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:52
 */
class TestRepository @Inject constructor(
    private val dao: TestDao
) {
    suspend fun insert(test: Test) {
        dao.insert(test)
    }

    suspend fun update(test: Test) {
        dao.update(test)
    }

    suspend fun delete(test: Test) {
        dao.delete(test)
    }
}