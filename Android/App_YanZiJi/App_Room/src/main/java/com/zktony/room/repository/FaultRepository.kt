package com.zktony.room.repository

import com.zktony.room.dao.FaultDao
import com.zktony.room.entities.Fault
import javax.inject.Inject

class FaultRepository @Inject constructor(private val faultDao: FaultDao) {
    suspend fun insert(fault: Fault) {
        try {
            faultDao.insert(fault)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}