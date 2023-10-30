package com.zktony.android.ui.utils

import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.internal.Log
import com.zktony.android.data.entities.internal.Process
import com.zktony.android.utils.AppStateUtils.hpc
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.writeRegister
import com.zktony.android.utils.SerialPortUtils.writeWithPulse
import com.zktony.android.utils.SerialPortUtils.writeWithTemperature
import com.zktony.android.utils.SerialPortUtils.writeWithValve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/9/12 14:52
 */
class JobExecutorUtils @Inject constructor(
    private val dataStore: DataSaverDataStore
) {
    var callback: (JobEvent) -> Unit = {}
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val hashMap = HashMap<Int, JobState>()
    private val jobMap = HashMap<Int, Job>()
    private val lock = AtomicBoolean(false)
    private val logLinkedList = LinkedList<Log>()

    fun create(jobState: JobState) {
        val job = scope.launch { interpreter(jobState) }
        jobMap[jobState.index] = job
        job.invokeOnCompletion {
            jobMap.remove(jobState.index)
            if (jobMap.isEmpty()) {
                scope.launch {
                    val logs = logLinkedList.toList()
                    callback(JobEvent.Logs(logs))
                    logLinkedList.clear()
                }
            }
        }
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
                writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
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

    private suspend fun blocking(index: Int, process: Process) {
        // check
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            logLinkedList.add(
                Log(
                    index = index,
                    level = "ERROR",
                    message = "${ex.message}"
                )
            )
            return
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
        logLinkedList.add(
            Log(
                index = index,
                level = "INFO",
                message = "添加封闭液完成 √ \n加液量：${process.dosage} \n进液通道：9 \n出液通道：${(index - ((index / 4) * 4)) + 1}"
            )
        )

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
        logLinkedList.add(
            Log(
                index = index,
                level = "INFO",
                message = "封闭液废液处理完成 √ \n进液通道：11 \n出液通道：${(index - ((index / 4) * 4)) + 1}"
            )
        )
    }

    private suspend fun primaryAntibody(index: Int, process: Process) {
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            logLinkedList.add(
                Log(
                    index = index,
                    level = "ERROR",
                    message = "${ex.message}"
                )
            )
            return
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
        logLinkedList.add(
            Log(
                index = index,
                level = "INFO",
                message = "添加一抗完成 √ \n加液量：${process.dosage} \n进液通道：${if (process.origin == 0) (index - ((index / 4) * 4)) + 1 else process.origin} \n出液通道：${(index - ((index / 4) * 4)) + 1}"
            )
        )

        countDown(state, (process.duration * 60 * 60).toLong())


        waitForLock(state) {
            val inChannel =
                if (process.origin == 0) (index - (index / 4) * 4) + 1 else process.origin
            if (process.recycle) {
                state = state.copy(status = JobState.RECYCLE)
                callback(JobEvent.Changed(state))
                clean(index, process, inChannel, (index - ((index / 4) * 4)) + 1)
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
        logLinkedList.add(
            Log(
                index = index,
                level = "INFO",
                message = "一抗废液${if (process.recycle) "回收" else "清理"}完成 √ \n进液通道：${if (process.origin == 0) (index - ((index / 4) * 4)) + 1 else process.origin} \n出液通道：${(index - ((index / 4) * 4)) + 1}"
            )
        )
    }

    private suspend fun secondaryAntibody(index: Int, process: Process) {
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            logLinkedList.add(
                Log(
                    index = index,
                    level = "ERROR",
                    message = "${ex.message}"
                )
            )
            return
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
        logLinkedList.add(
            Log(
                index = index,
                level = "INFO",
                message = "添加二抗完成 √ \n加液量：${process.dosage} \n进液通道：${if (process.origin == 0) (index - ((index / 4) * 4)) + 5 else process.origin} \n出液通道：${(index - ((index / 4) * 4)) + 1}"
            )
        )

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
        logLinkedList.add(
            Log(
                index = index,
                level = "INFO",
                message = "二抗废液清理完成 √ \n进液通道：${if (process.origin == 0) (index - ((index / 4) * 4)) + 5 else process.origin} \n出液通道：${(index - ((index / 4) * 4)) + 1}"
            )
        )
    }

    private suspend fun washing(index: Int, process: Process) {
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            logLinkedList.add(
                Log(
                    index = index,
                    level = "ERROR",
                    message = "${ex.message}"
                )
            )
            return
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
            logLinkedList.add(
                Log(
                    index = index,
                    level = "INFO",
                    message = "第${it + 1}次洗涤完成"
                )
            )
        }

        processes[processIndex] = process.copy(status = Process.FINISHED)
        state = state.copy(status = JobState.FINISHED, processes = processes)
        callback(JobEvent.Changed(state))
        hashMap[index] = state
    }

    private suspend fun phosphateBufferedSaline(index: Int, process: Process) {
        try {
            verify(index, process)
        } catch (ex: Exception) {
            callback(JobEvent.Error(ex))
            logLinkedList.add(
                Log(
                    index = index,
                    level = "ERROR",
                    message = "${ex.message}"
                )
            )
            return
        }

        var state = hashMap[index]!!
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        waitForLock(state) {
            state = state.copy(status = JobState.LIQUID)
            callback(JobEvent.Changed(state))
            liquid(index, process, 10, (index - ((index / 4) * 4)) + 1)
            processes[processIndex] = process.copy(status = Process.FINISHED)
            state = state.copy(status = JobState.FINISHED, processes = processes)
            callback(JobEvent.Changed(state))
        }
        logLinkedList.add(
            Log(
                index = index,
                level = "INFO",
                message = "添加缓冲液完成"
            )
        )
    }

    private fun verify(index: Int, process: Process) {
        var state = hashMap[index] ?: throw Exception("程序不存在")
        val processes = state.processes.toMutableList()
        val processIndex = processes.indexOf(process)

        if (process.dosage == 0.0) {
            throw Exception("加液量为零错误")
        }

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
        val pulse = (hpc[group] ?: { x -> x * 100 }).invoke(process.dosage)
        val inAddr = 2 * group
        val outAddr = 2 * group + 1
        val recoup = dataStore.readData(Constants.ZT_0002, 0L)
        callback(JobEvent.Shaker(true))
        writeWithTemperature(index + 1, process.temperature)
        writeRegister(slaveAddr = 0, startAddr = 200, value = 1)
        delay(100L)
        writeWithValve(inAddr, inChannel)
        writeWithValve(outAddr, outChannel)
        writeWithPulse(group + 1, pulse.toLong() + recoup)
        if (recoup > 0.0) {
            // 后边段残留的液体
            writeWithValve(inAddr, 12)
            writeWithPulse(slaveAddr = group + 1, value = recoup * 2)
            // 退回前半段残留的液体
            writeWithValve(inAddr, inChannel)
            writeWithValve(outAddr, 6)
            writeWithPulse(group + 1, -recoup * 2)
        }
    }

    private suspend fun clean(index: Int, process: Process, inChannel: Int, outChannel: Int) {
        val group = index / 4
        val pulse = (hpc[group] ?: { x -> x * 100 }).invoke(process.dosage)
        val inAddr = 2 * group
        val outAddr = 2 * group + 1
        val recoup = dataStore.readData(Constants.ZT_0002, 0L)
        callback(JobEvent.Shaker(false))
        writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
        delay(300L)
        writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
        writeWithValve(inAddr, inChannel)
        writeWithValve(outAddr, outChannel)
        writeWithPulse(group + 1, -(pulse + recoup).toLong() * 2)
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

    fun isRunning() = status != RUNNING && status != FINISHED

    companion object {
        const val STOPPED = 0
        const val RUNNING = 1
        const val FINISHED = 2
        const val WAITING = 3
        const val LIQUID = 4
        const val WASTE = 5
        const val RECYCLE = 6
    }
}

sealed class JobEvent {
    data class Changed(val state: JobState) : JobEvent()
    data class Error(val ex: Exception) : JobEvent()
    data class Shaker(val shaker: Boolean) : JobEvent()
    data class Logs(val logs: List<Log>) : JobEvent()
}