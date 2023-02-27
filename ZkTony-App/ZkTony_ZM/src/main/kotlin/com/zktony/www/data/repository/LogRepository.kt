package com.zktony.www.data.repository

import com.zktony.www.data.remote.model.LogDTO
import com.zktony.www.data.remote.model.LogDetailDTO
import com.zktony.www.data.remote.service.LogService
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val service: LogService
) {
    fun uploadLogRecords(logList: List<LogDTO>) =
        service.uploadLog(logList)

    fun uploadLogData(logDetailLiat: List<LogDetailDTO>) =
        service.uploadLogDetail(logDetailLiat)
}