package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.internal.Process
import com.zktony.android.ui.utils.*
import com.zktony.android.utils.AppStateUtils.hpp
import com.zktony.android.utils.AppStateUtils.hpt
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.readWithPosition
import com.zktony.android.utils.SerialPortUtils.readWithValve
import com.zktony.android.utils.SerialPortUtils.writeRegister
import com.zktony.android.utils.SerialPortUtils.writeWithPulse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    dataStore: DataSaverDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow(UiFlags.NONE)
    private val _message = MutableStateFlow<String?>(null)
    private val _jobList = MutableStateFlow(listOf<JobState>())
    private val _stand = MutableStateFlow(StandState())

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    private val jobExecutorUtils = JobExecutorUtils(
        recoup = dataStore.readData(Constants.ZT_0002, 0L)
    ) { event ->
        viewModelScope.launch {
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
                    _message.value = event.ex.message
                }

                is JobEvent.Shaker -> {
                    _stand.value = _stand.value.copy(shaker = event.shaker)
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            launch {
                combine(
                    _selected, _page, _uiFlags, _jobList, _stand
                ) { selected, page, uiFlags, jobList, stand ->
                    HomeUiState(selected, page, uiFlags, jobList, stand)
                }.catch { ex ->
                    _message.value = ex.message
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                init()
                loop()
            }
        }
    }

    fun uiEvent(uiEvent: HomeUiEvent) {
        when (uiEvent) {
            is HomeUiEvent.NavTo -> _page.value = uiEvent.page
            is HomeUiEvent.ToggleSelected -> _selected.value = uiEvent.id
            is HomeUiEvent.Start -> viewModelScope.launch {
                val jobList = _jobList.value.toMutableList()
                val jobIndex = jobList.indexOfFirst { it.index == uiEvent.index }
                if (jobIndex != -1) {
                    val job = jobList[jobIndex]
                    val processes = job.processes.map { it.copy(status = Process.UPCOMING) }
                    jobExecutorUtils.create(job.copy(processes = processes))
                }
            }

            is HomeUiEvent.Pause -> viewModelScope.launch {
                val jobList = _jobList.value.toMutableList()
                val jobIndex = jobList.indexOfFirst { it.index == uiEvent.index }
                if (jobIndex == -1) {
                    jobList.add(JobState(index = uiEvent.index, status = 2))
                } else {
                    jobList[jobIndex] = jobList[jobIndex].copy(status = 2)
                }
                _jobList.value = jobList
            }

            is HomeUiEvent.Stop -> viewModelScope.launch {
                jobExecutorUtils.destroy(uiEvent.index)
            }

            is HomeUiEvent.ToggleProcess -> viewModelScope.launch {
                val jobList = _jobList.value.toMutableList()
                val jobIndex = jobList.indexOfFirst { it.index == uiEvent.index }
                if (jobIndex == -1) {
                    jobList.add(
                        JobState(
                            index = uiEvent.index,
                            id = uiEvent.id,
                            processes = uiEvent.processes
                        )
                    )
                } else {
                    jobList[jobIndex] =
                        jobList[jobIndex].copy(id = uiEvent.id, processes = uiEvent.processes)
                }
                _jobList.value = jobList
            }

            is HomeUiEvent.Shaker -> viewModelScope.launch {
                val shaker = _stand.value.shaker
                try {
                    if (shaker) {
                        writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
                        delay(300L)

                        readWithPosition(slaveAddr = 0)
                        delay(100L)

                        val pulse = (hpp[0] ?: 0) % 6400L
                        writeWithPulse(0, if (pulse > 3200) 6400L - pulse else -pulse)
                    } else {
                        writeRegister(slaveAddr = 0, startAddr = 200, value = 1)
                    }
                } catch (ex: Exception) {
                    _message.value = ex.message
                } finally {
                    _stand.value = _stand.value.copy(shaker = !shaker)
                }
            }
        }
    }

    private suspend fun init() {
        repeat(3) {
            // 读取绝对位置
            readWithPosition(slaveAddr = it)
            delay(300L)
        }
        repeat(4) {
            // 读取阀门状态
            readWithValve(slaveAddr = it)
            delay(300L)
        }
    }

    private suspend fun loop() {
        while (true) {
            _stand.value = _stand.value.copy(insulation = hpt.values.toList())
            delay(3000L)
        }
    }
}

data class HomeUiState(
    val selected: Int = 0,
    val page: Int = PageType.HOME,
    val uiFlags: Int = UiFlags.NONE,
    val jobList: List<JobState> = listOf(),
    val stand: StandState = StandState()
)

data class StandState(
    val insulation: List<Double> = emptyList(),
    val shaker: Boolean = false
)

sealed class HomeUiEvent {
    data object Shaker : HomeUiEvent()
    data class NavTo(val page: Int) : HomeUiEvent()
    data class ToggleSelected(val id: Int) : HomeUiEvent()
    data class Start(val index: Int) : HomeUiEvent()
    data class Pause(val index: Int) : HomeUiEvent()
    data class Stop(val index: Int) : HomeUiEvent()
    data class ToggleProcess(val index: Int, val id: Long, val processes: List<Process>) :
        HomeUiEvent()
}

