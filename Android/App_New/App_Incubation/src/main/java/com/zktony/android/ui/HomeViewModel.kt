package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.internal.Process
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.extra.*
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
    private val dao: ProgramDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow(UiFlags.NONE)
    private val _message = MutableStateFlow<String?>(null)
    private val _jobList = MutableStateFlow(listOf<JobState>())
    private val _common = MutableStateFlow(CommonState())

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            launch {
                combine(
                    _selected, _page, _uiFlags, _jobList, _common
                ) { selected, page, uiFlags, jobList, common ->
                    HomeUiState(selected, page, uiFlags, jobList, common)
                }.catch { ex ->
                    _message.value = ex.message
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                init()
                jobLoop()
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
                if (jobIndex == -1) {
                    jobList.add(JobState(index = uiEvent.index, status = 1))
                } else {
                    jobList[jobIndex] = jobList[jobIndex].copy(status = 1)
                }
                _jobList.value = jobList
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
                val jobList = _jobList.value.toMutableList()
                val jobIndex = jobList.indexOfFirst { it.index == uiEvent.index }
                if (jobIndex == -1) {
                    jobList.add(JobState(index = uiEvent.index, status = 0))
                } else {
                    jobList[jobIndex] = jobList[jobIndex].copy(status = 0)
                }
                _jobList.value = jobList
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
                val shaker = _common.value.shaker
                try {
                    if (shaker) {
                        val before = appState.hpp[0] ?: 0
                        writeWithSwitch(0, 0)
                        while (before == (appState.hpp[0] ?: 0)) {
                            delay(200L)
                            readWithPulse(0)
                        }
                        val after = appState.hpp[0] ?: 0
                        val remainder = after % 6400
                        val value = if (remainder > 3200) {
                            after + 6400 - remainder
                        } else {
                            after - remainder
                        }
                        writeWithPosition(0, value.toLong())
                    } else {
                        writeWithSwitch(0, 1)
                    }
                } catch (ex: Exception) {
                    _message.value = ex.message
                } finally {
                    _common.value = _common.value.copy(shaker = !shaker)
                }

            }
        }
    }

    private suspend fun init() {
        repeat(3) {
            // 读取绝对位置
            readWithPulse(slaveAddr = it)
            delay(200L)
        }
        repeat(4) {
            // 读取阀门状态
            readWithValve(slaveAddr = it)
            delay(200L)
        }
    }

    private suspend fun jobLoop() {
        var startTime = 0L
        var endTime = 0L

        while (true) {
            try {
                startTime = System.currentTimeMillis()
                // logic
                endTime = System.currentTimeMillis()
            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                delay(1000L - (endTime - startTime))
            }
        }
    }
}

data class HomeUiState(
    val selected: Int = 0,
    val page: Int = PageType.HOME,
    val uiFlags: Int = UiFlags.NONE,
    val jobList: List<JobState> = listOf(),
    val common: CommonState = CommonState()
)

data class JobState(
    val index: Int = 0,
    val id: Long = 0L,
    val processes: List<Process> = listOf(),
    val status: Int = 0,
    val temperature: Double = 0.0,
    val time: Long = 0L
) {
    companion object {
        const val STOPPED = 0
        const val RUNNING = 1
        const val PAUSED = 2
        const val FINISHED = 3
        const val WAITING = 4
    }
}

data class CommonState(
    val temperature: Double = 0.0,
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

