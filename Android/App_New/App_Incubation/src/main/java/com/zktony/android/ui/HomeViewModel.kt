package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.History
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Log
import com.zktony.android.data.entities.internal.Process
import com.zktony.android.ui.utils.JobEvent
import com.zktony.android.ui.utils.JobExecutorUtils
import com.zktony.android.ui.utils.JobState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.readWithTemperature
import com.zktony.android.utils.SerialPortUtils.writeRegister
import com.zktony.android.utils.SerialPortUtils.writeWithTemperature
import com.zktony.serialport.command.runze.RunzeProtocol
import com.zktony.serialport.lifecycle.SerialStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    private val historyDao: HistoryDao,
    private val dataStore: DataSaverDataStore,
    private val jobExecutorUtils: JobExecutorUtils
) : ViewModel() {

    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())
    private val _message = MutableStateFlow<String?>(null)
    private val _selected = MutableStateFlow(0)
    private val _jobList = MutableStateFlow(listOf<JobState>())
    private val _insulation = MutableStateFlow(List(9) { 0.0 })
    private val _shaker = MutableStateFlow(false)

    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val message = _message.asStateFlow()
    val selected = _selected.asStateFlow()
    val jobList = _jobList.asStateFlow()
    val insulation = _insulation.asStateFlow()
    val shaker = _shaker.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        prepare()
        deployJobCallback()
    }

    fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Message -> _message.value = intent.message
            is HomeIntent.UiFlags -> _uiFlags.value = intent.uiFlags
            is HomeIntent.NavTo -> _page.value = intent.page
            is HomeIntent.ToggleSelected -> _selected.value = intent.id
            is HomeIntent.Start -> start(intent.index)
            is HomeIntent.Pause -> pause(intent.index)
            is HomeIntent.Stop -> stop(intent.index)
            is HomeIntent.ToggleProcess -> toggleProcess(intent.index, intent.program)
            is HomeIntent.Shaker -> shaker()
        }
    }

    private fun prepare() {
        viewModelScope.launch {
            // 根据模块数量配置
            val coll = dataStore.readData(Constants.ZT_0000, 4)
            repeat(coll / 2) {
                // 读取阀门状态
                SerialStoreUtils.get("rtu")?.sendByteArray(bytes = RunzeProtocol().apply {
                    this.slaveAddr = it.toByte()
                    funcCode = 0x3E
                    data = byteArrayOf(0x00, 0x00)
                }.toByteArray())
                delay(300L)
            }
            // 设置初始温度
            writeWithTemperature(0, dataStore.readData(Constants.ZT_0001, 4.0))
            delay(500L)
            repeat(coll) {
                writeWithTemperature(it + 1, 26.0)
                delay(500L)
            }
            // 设置定时查询温度
            while (true) {
                delay(3000L)
                repeat(coll + 1) {
                    readWithTemperature(it) { address, temp ->
                        _insulation.value = _insulation.value.toMutableList().apply {
                            this[address] = temp
                        }
                    }
                    delay(100L)
                }
            }
        }
    }

    private fun deployJobCallback() {
        viewModelScope.launch {
            jobExecutorUtils.callback = { event ->
                when (event) {
                    is JobEvent.Changed -> {
                        val jobList = _jobList.value.toMutableList()
                        val jobIndex = jobList.indexOfFirst { it.index == event.state.index }
                        if (jobIndex != -1) {
                            jobList[jobIndex] = event.state
                        }
                        _jobList.value = jobList
                    }

                    is JobEvent.Error -> {
                        _uiFlags.value = UiFlags.error(event.ex.message ?: "Unknown")
                    }

                    is JobEvent.Shaker -> {
                        _shaker.value = event.shaker
                    }

                    is JobEvent.Logs -> {
                        val startTime =
                            (event.logs.firstOrNull() ?: Log(message = "None")).createTime
                        val logs = event.logs.sortedBy { it.index }
                        viewModelScope.launch {
                            historyDao.insert(History(logs = logs, createTime = startTime))
                        }
                    }
                }
            }
        }
    }

    private fun start(index: Int) {
        viewModelScope.launch {
            val jobList = _jobList.value.toMutableList()
            val jobIndex = jobList.indexOfFirst { it.index == index }
            if (jobIndex != -1) {
                val job = jobList[jobIndex]
                val processes = job.processes.map { it.copy(status = Process.UPCOMING) }
                jobExecutorUtils.create(job.copy(processes = processes))
            } else {
                _message.value = "INFO 请选择一个程序"
            }
        }
    }

    private fun pause(index: Int) {
        viewModelScope.launch {
            val jobList = _jobList.value.toMutableList()
            val jobIndex = jobList.indexOfFirst { it.index == index }
            if (jobIndex == -1) {
                jobList.add(JobState(index = index, status = 2))
            } else {
                jobList[jobIndex] = jobList[jobIndex].copy(status = 2)
            }
            _jobList.value = jobList
        }
    }

    private fun stop(index: Int) {
        viewModelScope.launch {
            jobExecutorUtils.destroy(index)
        }
    }

    private fun toggleProcess(index: Int, program: Program) {
        viewModelScope.launch {
            val jobList = _jobList.value.toMutableList()
            val jobIndex = jobList.indexOfFirst { it.index == index }
            if (jobIndex == -1) {
                jobList.add(
                    JobState(
                        index = index,
                        id = program.id,
                        processes = program.processes
                    )
                )
            } else {
                jobList[jobIndex] = jobList[jobIndex].copy(
                    id = program.id,
                    processes = program.processes
                )
            }
            _jobList.value = jobList
        }
    }

    private fun shaker() {
        viewModelScope.launch {
            try {
                if (_shaker.value) {
                    writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
                    delay(300L)
                    writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
                } else {
                    writeRegister(slaveAddr = 0, startAddr = 200, value = 1)
                }
            } catch (ex: Exception) {
                _message.value = ex.message
            } finally {
                _shaker.value = !_shaker.value
            }
        }
    }
}

sealed class HomeIntent {
    data class Message(val message: String?) : HomeIntent()
    data class UiFlags(val uiFlags: com.zktony.android.ui.utils.UiFlags) : HomeIntent()
    data class NavTo(val page: Int) : HomeIntent()
    data class Pause(val index: Int) : HomeIntent()
    data class Start(val index: Int) : HomeIntent()
    data class Stop(val index: Int) : HomeIntent()
    data class ToggleProcess(val index: Int, val program: Program) : HomeIntent()
    data class ToggleSelected(val id: Int) : HomeIntent()
    data object Shaker : HomeIntent()
}