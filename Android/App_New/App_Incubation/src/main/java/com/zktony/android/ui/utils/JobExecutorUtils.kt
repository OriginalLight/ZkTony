package com.zktony.android.ui.utils

import com.zktony.android.data.entities.internal.Process
import com.zktony.android.utils.extra.appState
import com.zktony.android.utils.extra.writeWithPulse
import com.zktony.android.utils.extra.writeWithValve
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author 刘贺贺
 * @date 2023/9/12 14:52
 */
class JobExecutorUtils(
    private val recoup: Long,
    private val callback: (JobState) -> Unit,
    private val exception: (Exception) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val hashMap = HashMap<Int, JobState>()
    private val jobMap = HashMap<Int, Job>()
    private val lock = AtomicBoolean(false)

    fun create(jobState: JobState) {
        jobMap[jobState.index] = scope.launch { interpreter(jobState) }
    }

    fun destroy(index: Int) {
        jobMap[index]?.cancel()
        jobMap.remove(index)
    }

    private suspend fun interpreter(jobState: JobState) {

        val index = jobState.index
        val processes = jobState.processes

        hashMap[index] = jobState

        processes.forEach { process ->
            when (process.type) {
                Process.BLOCKING -> {
                    blocking(index, process)
                }

                Process.PRIMARY_ANTIBODY -> {
                    primaryAntibody(index, process)
                }

                Process.SECONDARY_ANTIBODY -> {
                    secondaryAntibody(index, process)
                }

                Process.WASHING -> {
                    washing(index, process)
                }

                Process.PHOSPHATE_BUFFERED_SALINE -> {
                    phosphateBufferedSaline(index, process)
                }
            }
        }

    }

    val blocking: suspend (Int, Process) -> Unit = start@{ index, process ->
        // check
        var state = hashMap[index] ?: return@start
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)
        val allTime = process.duration * 60 * 60
        val group = index / 4
        val pulse = (appState.hpc[group + 1] ?: { x -> x * 100 }).invoke(process.dosage)

        if (process.dosage == 0.0) {
            exception(Exception("加液量为零错误"))
            return@start
        }

        if (pulse == null) {
            exception(Exception("校准方法错误"))
            return@start
        }

        if (processIndex != -1) {
            processes[processIndex] = process.copy(status = Process.RUNNING)
            state = state.copy(status = JobState.RUNNING, processes = processes)
            callback(state)
        } else {
            exception(Exception("进程不存在"))
            return@start
        }

        while (lock.get()) {
            state = state.copy(status = JobState.WAITING)
            callback(state)
            delay(1000L)
        }

        // add lock
        lock.set(true)
        // 加液
        try {
            state = state.copy(status = JobState.LIQUID)
            callback(state)

            if (appState.hpv[2 * group] != IN_9) {
                writeWithValve(2 * group, IN_9)
            }

            if (appState.hpv[2 * group + 1] != index % 4) {
                writeWithValve(2 * group + 1, index % 4)
            }

            writeWithPulse(group + 1, pulse.toLong() + recoup)

            if (recoup > 0.0) {
                writeWithValve(2 * group + 1, OUT_6)
                writeWithPulse(group + 1, -recoup)
            }
        } catch (ex: Exception) {
            exception(ex)
        } finally {
            state = state.copy(status = JobState.RUNNING)
            callback(state)
            lock.set(false)
        }

        repeat(allTime.toInt()) {
            state = state.copy(time = (allTime - it).toLong())
            callback(state)
            delay(1000L)
        }

        while (lock.get()) {
            state = state.copy(status = JobState.WAITING)
            callback(state)
            delay(1000L)
        }

        lock.set(true)
        // 废液
        try {
            state = state.copy(status = JobState.WASTE)
            callback(state)

            if (appState.hpv[2 * group] != IN_11) {
                writeWithValve(2 * group, IN_11)
            }

            if (appState.hpv[2 * group + 1] != index % 4) {
                writeWithValve(2 * group + 1, index % 4)
            }

            writeWithPulse(group + 1, -(pulse + recoup).toLong() * 2)
        } catch (ex: Exception) {
            exception(ex)
        } finally {
            processes[processIndex] = process.copy(status = Process.FINISHED)
            state = state.copy(status = JobState.RUNNING, processes = processes)
            callback(state)
            lock.set(false)
        }
    }

    val primaryAntibody: suspend (Int, Process) -> Unit = start@{ index, process ->

    }

    val secondaryAntibody: suspend (Int, Process) -> Unit = start@{ index, process ->

    }

    val washing: suspend (Int, Process) -> Unit = start@{ index, process ->

    }

    val phosphateBufferedSaline: suspend (Int, Process) -> Unit = start@{ index, process ->

    }

    companion object {
        const val IN_1 = 1
        const val IN_2 = 2
        const val IN_3 = 3
        const val IN_4 = 4
        const val IN_5 = 5
        const val IN_6 = 6
        const val IN_7 = 7
        const val IN_8 = 8
        const val IN_9 = 9
        const val IN_10 = 10
        const val IN_11 = 11
        const val IN_12 = 12

        const val OUT_1 = 1
        const val OUT_2 = 2
        const val OUT_3 = 3
        const val OUT_4 = 4
        const val OUT_5 = 5
        const val OUT_6 = 6
    }
}

data class JobState(
    val index: Int = 0,
    val id: Long = 0L,
    val processes: List<Process> = listOf(),
    val status: Int = 0,
    val time: Long = 0L
) {
    companion object {
        const val STOPPED = 0
        const val RUNNING = 1
        const val PAUSED = 2
        const val FINISHED = 3
        const val WAITING = 4
        const val LIQUID = 5
        const val WASTE = 6
        const val RECYCLE = 7
    }
}