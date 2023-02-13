package com.zktony.www.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zktony.www.common.network.model.ProgramDTO
import com.zktony.www.common.network.result.NetworkResult
import com.zktony.www.common.utils.Logger
import com.zktony.www.common.repository.ProgramRepository
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
                val list = mutableListOf<ProgramDTO>()
                programs.forEach {
                    val content = StringBuilder()
                    content.append("程序名称：${it.name}，泵速：${it.motor}，电压：${it.voltage}，时长：${it.time}")
                    content.append("蛋白名称：${it.proteinName}，最小蛋白：${it.proteinMinSize}，最大蛋白：${it.proteinMaxSize}")
                    content.append("胶种类：${if (it.glueType == 0) "常规胶" else "梯度胶"}，胶厚度：${it.glueConcentration}，最小浓度：${it.glueMinConcentration}%， 最大浓度：${it.glueMaxConcentration}%")
                    content.append("模式：${if (it.model == 0) "转膜" else "染色"}，缓冲液：${it.bufferType}")
                    list.add(
                        ProgramDTO(
                            id = it.id,
                            name = it.name,
                            content = content.toString(),
                            create_time = it.createTime,
                        )
                    )
                }
                programRepository.uploadProgram(list).collect { res ->
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