package com.zktony.www.ui.log.model

import java.util.Date

sealed class LogIntent {
    object InitLogRecord : LogIntent()
    data class ChangeLogRecord(val start: Date, val end: Date) : LogIntent()
    data class ChangeLogData(val id: String) : LogIntent()
}
