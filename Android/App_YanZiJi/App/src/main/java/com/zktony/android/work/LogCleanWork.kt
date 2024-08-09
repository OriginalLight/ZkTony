package com.zktony.android.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zktony.log.LogUtils
import com.zktony.room.repository.LogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltWorker
class LogCleanWork @AssistedInject constructor(
    private val repo: LogRepository,
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result {
        return try {
            CoroutineScope(Dispatchers.IO).launch {
                repo.clearExpiredLog()
                LogUtils.info("LogCleanWork: clearExpiredLog", true)
            }
            return Result.success()
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            Result.failure()
        }
    }
}