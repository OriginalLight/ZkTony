package com.zktony.www.ui.log.model

import java.util.*

sealed class LogIntent {
    data class ChangeLogRecord(val start: Date, val end: Date) : LogIntent()
    data class ChangeLogData(val id: String) : LogIntent()
}
