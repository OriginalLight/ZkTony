package com.zktony.www.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.room.repository.TestRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
@HiltWorker
class LogWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val testRepository: TestRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}