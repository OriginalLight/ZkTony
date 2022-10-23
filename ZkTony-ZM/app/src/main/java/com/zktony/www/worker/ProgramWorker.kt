package com.zktony.www.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.AppLog
import com.zktony.www.common.http.adapter.isSuccess
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.services.ProgramService
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

    @Inject
    lateinit var service: ProgramService

    override suspend fun doWork(): Result {
        try {
            programRepository.withoutUpload().first().let {
                if (it.isEmpty()) {
                    AppLog.d("ProgramWorker", "上传程序为空")
                    return Result.success()
                }
                val res = service.uploadProgram(it)
                if (res.isSuccess) {
                    it.forEach { program ->
                        program.upload = 1
                    }
                    programRepository.updateBatch(it)
                    AppLog.d("ProgramWorker", "上传程序成功")
                } else {
                    AppLog.d("ProgramWorker", "上传程序失败")
                    return Result.failure()
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}