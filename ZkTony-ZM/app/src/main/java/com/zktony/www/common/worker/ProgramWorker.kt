package com.zktony.www.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.result.NetworkResult
import com.zktony.www.common.utils.Logger
import com.zktony.www.data.repository.ProgramRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-20 14:47
 */
@HiltWorker
class ProgramWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var programRepository: ProgramRepository

    override suspend fun doWork(): Result {
        try {
            programRepository.withoutUpload().first().let { programs ->
                if (programs.isEmpty()) {
                    Logger.d("ProgramWorker", "上传程序为空")
                    return Result.success()
                }
                programRepository.uploadProgram(programs).collect { res ->
                    when (res) {
                        is NetworkResult.Success -> {
                            programRepository.updateBatch(programs.map { it.copy(upload = 1) })
                        }
                        is NetworkResult.Error -> {
                            Logger.d("ProgramWorker", "上传程序失败")
                        }
                        else -> {}
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}