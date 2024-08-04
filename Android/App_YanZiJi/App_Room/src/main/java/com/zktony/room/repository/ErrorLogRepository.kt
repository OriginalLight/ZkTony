package com.zktony.room.repository

import com.zktony.room.dao.ErrorLogDao
import com.zktony.room.entities.ErrorLog
import javax.inject.Inject

class ErrorLogRepository @Inject constructor(
    private val errorLogDao: ErrorLogDao
) {
}