package com.zktony.www.ui.log.model

import com.zktony.www.data.entity.LogData
import com.zktony.www.data.entity.LogRecord

sealed class LogState {
    data class ChangeLogRecord(val logRecordList: List<LogRecord>) : LogState()
    data class ChangeLogData(val logDataList: List<LogData>) : LogState()
}
