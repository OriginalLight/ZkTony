package com.zktony.android.ui.utils

import com.zktony.android.data.entities.internal.Process
import com.zktony.android.utils.AppStateUtils.hpc
import com.zktony.android.utils.AppStateUtils.hpp
import com.zktony.android.utils.SerialPortUtils.readWithPosition
import com.zktony.android.utils.SerialPortUtils.writeRegister
import com.zktony.android.utils.SerialPortUtils.writeWithPulse
import com.zktony.android.utils.SerialPortUtils.writeWithValve
import kotlinx.coroutines.*
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author 刘贺贺
 * @date 2023/9/12 14:52
 */
class JobExecutorUtils(
    private val recoup: Long,
    private val callback: (JobEvent) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val hashMap = HashMap<Int, JobState>()
    private val jobMap = HashMap<Int, Job>()
    private val lock = AtomicBoolean(false)

    fun create(jobState: JobState) {
        val job = scope.launch { interpreter(jobState) }
        jobMap[jobState.index] = job
        job.invokeOnCompletion { jobMap.remove(jobState.index) }
    }

    fun destroy(index: Int) {
        jobMap[index]?.cancel()
        jobMap.remove(index)
        val state = hashMap[index] ?: return
        callback(JobEvent.Changed(state.copy(status = JobState.STOPPED)))
        hashMap.remove(index)
        if (jobMap.isEmpty()) {
            scope.launch {
                writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
                delay(300L)

                readWithPosition(slaveAddr = 0)
                delay(100L)

                val p = (hpp[0] ?: 0) % 6400L
                writeWithPulse(0, if (p > 3200) 6400L - p else -p)
                delay(100L)
            }
        }
    }

    private suspend fun interpreter(jobState: JobState) {

        val index = jobState.index
        val processes = jobState.processes
        val linkedList = LinkedList<Process>()
        linkedList.addAll(processes)
        hashMap[index] = jobState

        linkedList.forEach { process ->
            when (process.type) {
                Process.BLOCKING -> blocking(index, process)
                Process.PRIMARY_ANTIBODY -> primaryAntibody(index, process)
                Process.SECONDARY_ANTIBODY -> secondaryAntibody(index, process)
                Process.WASHING -> washing(index, process)
                Process.PHOSPHATE_BUFFERED_SALINE -> phosphateBufferedSaline(index, process)
            }
        }
    }

    val blocking: suspend (Int, Process) -> Unit = start@{ index, process ->
        // check
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            return@start
        }

        var state = hashMap[index]!!
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        waitForLock(state) {
            state = state.copy(status = JobState.LIQUID)
            callback(JobEvent.Changed(state))
            liquid(index, process, 9, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.RUNNING)
            state = state.copy(status = JobState.RUNNING, processes = processes)
            callback(JobEvent.Changed(state))
        }

        countDown(state, (process.duration * 60 * 60).toLong())

        waitForLock(state) {
            state = state.copy(status = JobState.WASTE)
            callback(JobEvent.Changed(state))
            clean(index, process, 11, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.FINISHED)
            state = state.copy(status = JobState.FINISHED, processes = processes)
            callback(JobEvent.Changed(state))
            hashMap[index] = state
        }
    }

    val primaryAntibody: suspend (Int, Process) -> Unit = start@{ index, process ->
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            return@start
        }

        var state = hashMap[index]!!
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        waitForLock(state) {
            state = state.copy(status = JobState.LIQUID)
            callback(JobEvent.Changed(state))
            val inChannel =
                if (process.origin == 0) (index - ((index / 4) * 4)) + 1 else process.origin
            liquid(index, process, inChannel, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.RUNNING)
            state = state.copy(status = JobState.RUNNING, processes = processes)
            callback(JobEvent.Changed(state))
        }

        countDown(state, (process.duration * 60 * 60).toLong())
        waitForLock(state) {
            val inChannel =
                if (process.origin == 0) (index - (index / 4) * 4) + 1 else process.origin
            if (process.recycle) {
                state = state.copy(status = JobState.RECYCLE)
                callback(JobEvent.Changed(state))
                liquid(index, process, inChannel, (index - ((index / 4) * 4)) + 1)
            } else {
                state = state.copy(status = JobState.WASTE)
                callback(JobEvent.Changed(state))
                clean(index, process, 11, (index - ((index / 4) * 4)) + 1)
            }
            processes[processIndex] = process.copy(status = Process.FINISHED)
            state = state.copy(status = JobState.FINISHED, processes = processes)
            callback(JobEvent.Changed(state))
            hashMap[index] = state
        }
    }

    val secondaryAntibody: suspend (Int, Process) -> Unit = start@{ index, process ->
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            return@start
        }

        var state = hashMap[index]!!
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        waitForLock(state) {
            state = state.copy(status = JobState.LIQUID)
            callback(JobEvent.Changed(state))
            val inChannel =
                if (process.origin == 0) (index - ((index / 4) * 4)) + 5 else process.origin
            liquid(index, process, inChannel, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.RUNNING)
            state = state.copy(status = JobState.RUNNING, processes = processes)
            callback(JobEvent.Changed(state))
        }

        countDown(state, (process.duration * 60 * 60).toLong())

        waitForLock(state) {
            state = state.copy(status = JobState.WASTE)
            callback(JobEvent.Changed(state))
            clean(index, process, 11, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.FINISHED)
            state = state.copy(status = JobState.FINISHED, processes = processes)
            callback(JobEvent.Changed(state))
            hashMap[index] = state
        }
    }

    val washing: suspend (Int, Process) -> Unit = start@{ index, process ->
        // check
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            return@start
        }

        var state = hashMap[index]!!
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        repeat(process.times) {
            waitForLock(state) {
                state = state.copy(status = JobState.LIQUID)
                callback(JobEvent.Changed(state))
                liquid(index, process, 10, (index - ((index / 4) * 4)) + 1)
                processes[processIndex] = process.copy(status = Process.RUNNING)
                state = state.copy(status = JobState.RUNNING, processes = processes)
                callback(JobEvent.Changed(state))
                lock.set(false)
            }

            countDown(state, (process.duration * 60).toLong())

            waitForLock(state) {
                state = state.copy(status = JobState.WASTE)
                callback(JobEvent.Changed(state))
                clean(index, process, 11, (index - ((index / 4) * 4)) + 1)
            }
        }

        processes[processIndex] = process.copy(status = Process.FINISHED)
        state = state.copy(status = JobState.FINISHED, processes = processes)
        callback(JobEvent.Changed(state))
        hashMap[index] = state
    }

    val phosphateBufferedSaline: suspend (Int, Process) -> Unit = start@{ index, process ->
        // check
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            return@start
        }

        var state = hashMap[index]!!
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        waitForLock(state) {
            state = state.copy(status = JobState.LIQUID)
            callback(JobEvent.Changed(state))
            liquid(index, process, 10, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.RUNNING)
            state = state.copy(status = JobState.RUNNING, processes = processes)
            callback(JobEvent.Changed(state))
        }

        countDown(state, (process.duration * 60 * 60).toLong())

        waitForLock(state) {
            state = state.copy(status = JobState.WASTE)
            callback(JobEvent.Changed(state))
            clean(index, process, 11, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.FINISHED)
            state = state.copy(status = JobState.FINISHED, processes = processes)
            callback(JobEvent.Changed(state))
            hashMap[index] = state
        }
    }

    private fun verify(index: Int, process: Process) {
        var state = hashMap[index] ?: throw Exception("程序不存在")
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        if (process.dosage == 0.0) {
            throw Exception("加液量为零错误")
        }

        (hpc[index / 4 + 1] ?: { x -> x * 100 }).invoke(process.dosage)
            ?: throw Exception("校准方法错误")

        if (processIndex != -1) {
            processes[processIndex] = process.copy(status = Process.RUNNING)
            state = state.copy(status = JobState.RUNNING, processes = processes)
            callback(JobEvent.Changed(state))
        } else {
            throw Exception("进程不存在")
        }
    }

    private suspend fun liquid(index: Int, process: Process, inChannel: Int, outChannel: Int) {
        val group = index / 4
        val pulse = (hpc[group + 1] ?: { x -> x * 100 }).invoke(process.dosage)!!
        val inAddr = 2 * group
        val outAddr = 2 * group + 1
        callback(JobEvent.Shaker(true))
        writeRegister(slaveAddr = 0, startAddr = 200, value = 1)
        delay(100L)
        writeWithValve(inAddr, inChannel)
        delay(100L)
        writeWithValve(outAddr, outChannel)
        delay(100L)
        writeWithPulse(
            slaveAddr = group + 1,
            value = pulse.toLong() + recoup
        )
        delay(100L)
        if (recoup > 0.0) {
            // 后边段残留的液体
            writeWithValve(inAddr, 12)
            delay(100L)
            writeWithPulse(slaveAddr = group + 1, value = recoup * 2)
            delay(100L)
            // 退回前半段残留的液体
            writeWithValve(inAddr, inChannel)
            delay(100L)
            writeWithValve(outAddr, 6)
            delay(100L)
            writeWithPulse(
                slaveAddr = group + 1,
                value = -recoup * 2
            )
            delay(100L)
        }
    }

    private suspend fun clean(index: Int, process: Process, inChannel: Int, outChannel: Int) {
        val group = index / 4
        val pulse = (hpc[group + 1] ?: { x -> x * 100 }).invoke(process.dosage)!!
        val inAddr = 2 * group
        val outAddr = 2 * group + 1
        callback(JobEvent.Shaker(false))
        writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
        delay(300L)
        readWithPosition(slaveAddr = 0)
        delay(100L)
        val p = (hpp[0] ?: 0) % 6400L
        writeWithPulse(0, if (p > 3200) 6400L - p else -p)
        delay(100L)
        writeWithValve(inAddr, inChannel)
        delay(100L)
        writeWithValve(outAddr, outChannel)
        delay(100L)
        writeWithPulse(group + 1, -(pulse + recoup).toLong() * 2)
        delay(100L)
    }

    private suspend fun waitForLock(state: JobState, block: suspend () -> Unit) {
        if (lock.get()) {
            callback(JobEvent.Changed(state.copy(status = JobState.WAITING)))
            while (lock.get()) {
                delay(1000L)
            }
        }
        lock.set(true)
        block()
        lock.set(false)
    }

    private suspend fun countDown(state: JobState, allTime: Long) {
        repeat(allTime.toInt()) {
            callback(JobEvent.Changed(state.copy(time = allTime - it)))
            delay(1000L)
        }
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

sealed class JobEvent {
    data class Changed(val state: JobState) : JobEvent()
    data class Error(val ex: Exception) : JobEvent()
    data class Shaker(val shaker: Boolean) : JobEvent()
}