package com.zktony.room.repository

import com.zktony.room.dao.LogDao
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val logDao: LogDao
) {
}