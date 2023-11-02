package com.zktony.android.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.internal.OrificePlate
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.getGpio
import com.zktony.android.utils.SerialPortUtils.gpio
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.SerialPortUtils.stop
import com.zktony.android.utils.internal.ExecuteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.math.ceil

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    private val dataStore: DataSaverDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow(UiFlags.NONE)

    private val _jobState = MutableStateFlow(JobState())
    private val _status = MutableStateFlow(JobState.STEPPED)
    private val _orificePlate = MutableStateFlow(OrificePlate())
    private val _finished = MutableStateFlow(emptyList<Triple<Int, Int, Color>>())
    private val _job = MutableStateFlow<Job?>(null)

    private val _message: MutableStateFlow<String?> = MutableStateFlow(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            launch {
                combine(
                    _selected, _page, _uiFlags, _jobState
                ) { selected, page, uiFlags, jobState ->
                    HomeUiState(selected, page, uiFlags, jobState)
                }.catch { ex ->
                    _message.value = ex.message
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                combine(
                    _status, _orificePlate, _finished, _job
                ) { status, orificePlate, finished, job ->
                    JobState(status, orificePlate, finished, job)
                }.catch { ex ->
                    _message.value = ex.message
                }.collect {
                    _jobState.value = it
                }
            }
            launch {
                init()
            }
        }
    }

    fun uiEvent(uiEvent: HomeUiEvent) {
        when (uiEvent) {
            is HomeUiEvent.Message -> _message.value = uiEvent.message
            is HomeUiEvent.NavTo -> _page.value = uiEvent.page
            is HomeUiEvent.Pause -> _status.value = JobState.PAUSED
            is HomeUiEvent.Pipeline -> pipeline(uiEvent.index)
            is HomeUiEvent.Reset -> init()
            is HomeUiEvent.Resume -> _status.value = JobState.RUNNING
            is HomeUiEvent.UiFlags -> _uiFlags.value = uiEvent.uiFlags
            is HomeUiEvent.Start -> startJob()
            is HomeUiEvent.Stop -> stopJob()
            is HomeUiEvent.ToggleSelected -> toggleSelected(uiEvent.id)
        }
    }

    private fun init() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.RESET
            try {
                withTimeout(60 * 1000L) {
                    gpio(0, 1)
                    delay(300L)
                    val job: MutableList<Job?> = MutableList(2) { null }
                    repeat(2) {
                        job[it] = launch {
                            if (!getGpio(it)) {
                                start {
                                    with(
                                        index = it,
                                        pdv = 3200L * -30,
                                        ads = Triple(50, 80, 100)
                                    )
                                }
                            }

                            start {
                                with(
                                    index = it,
                                    pdv = 3200L,
                                    ads = Triple(50, 80, 100)
                                )
                            }

                            start {
                                with(
                                    index = it,
                                    pdv = 3200L * -2,
                                    ads = Triple(50, 80, 100)
                                )
                            }
                        }
                    }
                    job.forEach { it?.join() }
                }
            } catch (ex: Exception) {
                _message.value = ex.message
            } finally {
                _uiFlags.value = UiFlags.NONE
            }
        }
    }

    private fun startJob() {
        _job.value = viewModelScope.launch {
            _status.value = JobState.RUNNING
            val selected = dao.getById(_selected.value).firstOrNull()
            if (selected == null) {
                _message.value = "不存在该程序，请检查！！！"
                _status.value = JobState.STEPPED
                return@launch
            }
            if (selected.orificePlates.isEmpty()) {
                _message.value = "该程序没有孔板，请检查！！！"
                _status.value = JobState.STEPPED
                return@launch
            }

            val orificePlates = selected.orificePlates

            orificePlates.forEach { op ->
                _orificePlate.value = op
                previousAlgorithm(op)

                if (op.type == 0) {
                    separationAlgorithm(op)
                } else {
                    hybridAlgorithm(op)
                }
            }
            delay(100L)
            _uiFlags.value = UiFlags.DIALOG
        }
        _job.value?.invokeOnCompletion {
            _finished.value = emptyList()
            _status.value = JobState.STEPPED
        }
    }

    private fun stopJob() {
        viewModelScope.launch {
            _job.value?.cancel()
            stop(0, 1, 2, 3, 4, 5, 6, 7)
        }
    }

    private fun toggleSelected(id: Long) {
        viewModelScope.launch {
            _selected.value = id
            _orificePlate.value =
                dao.getById(_selected.value).firstOrNull()?.orificePlates?.getOrNull(0)
                    ?: OrificePlate()
        }
    }

    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _uiFlags.value = UiFlags.NONE
                stop(2, 3, 4, 5, 6, 7)
            } else {
                _uiFlags.value = index + 1
                start {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        with(
                            index = it + 2,
                            pdv = 3200L * 10000 * if (index < 3) 1 else -1
                        )
                    }
                }
            }
        }
    }

    private suspend fun previousAlgorithm(op: OrificePlate) {
        if (op.previous > 0.0) {
            val x = dataStore.readData(Constants.ZT_0003, 0.0)
            val y = dataStore.readData(Constants.ZT_0004, 0.0)
            start {
                with(index = 0, pdv = x)
                with(index = 1, pdv = y)
            }
            start {
                repeat(6) {
                    with(index = 2 + it, pdv = op.previous)
                }
            }
        }
    }

    private suspend fun separationAlgorithm(op: OrificePlate) {
        val row = op.row
        val column = op.column
        for (i in 0 until ceil(row / 6.0).toInt()) {
            for (j in if (i % 2 == 0) 0 until column else column - 1 downTo 0) {
                // 检查该步是否有需要加液的
                var next = false
                repeat(6) {
                    if (i * 6 + it < row) {
                        val orifice = op.orifices[j][i * 6 + it]
                        val pvd = orifice.volume.getOrNull(0) ?: 0.0
                        if (pvd > 0.0) {
                            next = true
                        }
                    }
                }
                if (!next) continue

                delay(10L)
                while (_status.value == JobState.PAUSED) {
                    delay(100)
                }

                // 移动到加液位置
                val coordinate = op.orifices[j][i * 6].point
                start {
                    with(index = 0, pdv = coordinate.x)
                    with(index = 1, pdv = coordinate.y)
                }

                delay(10L)
                while (_status.value == JobState.PAUSED) {
                    delay(100)
                }

                // 加液
                val list = mutableListOf<Triple<Int, Int, Color>>()
                start {
                    timeOut = 1000L * 30
                    repeat(6) {
                        if (i * 6 + it < row) {
                            val orifice = op.orifices[j][i * 6 + it]
                            if (orifice.selected) {
                                with(index = 2 + it, pdv = orifice.volume.getOrNull(0) ?: 0.0)
                                list += Triple(j, i * 6 + it, Color.Green)
                            }
                        }
                    }
                }

                // 记录已完成的
                _finished.value += list

                // 延时
                delay((op.delay * 1000L).toLong())
            }
        }
        _finished.value = emptyList()
    }

    private suspend fun hybridAlgorithm(op: OrificePlate) {
        val row = op.row
        val column = op.column
        val coordinate = op.points
        val rowSpace = (coordinate[1].x - coordinate[0].x) / (row - 1)
        for (i in 0 until row + 5) {
            for (j in if (i % 2 == 0) 0 until column else column - 1 downTo 0) {
                // 检查该步是否有需要加液的
                var next = false
                repeat(6) {
                    if (i - 5 + it in 0 until row) {
                        val orifice = op.orifices[j][i - 5 + it]
                        if (orifice.selected) {
                            val pvd = orifice.volume.getOrNull(it) ?: 0.0
                            if (pvd > 0.0) {
                                next = true
                            }
                        }
                    }
                }
                if (!next) continue

                delay(10L)
                while (_status.value == JobState.PAUSED) {
                    delay(100)
                }

                // 移动到加液位置
                start {
                    with(
                        index = 0, pdv = if (i < 6) {
                            op.orifices[j][0].point.x - (5 - i) * rowSpace
                        } else {
                            op.orifices[j][i - 5].point.x
                        }
                    )
                    with(index = 1, pdv = op.orifices[j][0].point.y)
                }

                delay(10L)
                while (_status.value == JobState.PAUSED) {
                    delay(100)
                }

                // 加液
                val list = mutableListOf<Triple<Int, Int, Color>>()
                start {
                    timeOut = 1000L * 30
                    repeat(6) {
                        if (i - 5 + it in 0 until row) {
                            val orifice = op.orifices[j][i - 5 + it]
                            if (orifice.selected) {
                                val pdv = orifice.volume.getOrNull(it) ?: 0.0
                                if (pdv > 0.0) {
                                    with(index = 2 + it, pdv = pdv)
                                    list += Triple(
                                        j, i - 5 + it, when (it) {
                                            0 -> Color.Green
                                            1 -> Color.Blue
                                            2 -> Color.Red
                                            3 -> Color.Yellow
                                            4 -> Color.Cyan
                                            5 -> Color.Magenta
                                            else -> Color.Black
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 记录已完成的
                list.forEach { triple ->
                    _finished.value -= _finished.value.filter { it.first == triple.first && it.second == triple.second }
                }
                _finished.value += list

                // 延时
                delay((op.delay * 1000L).toLong())
            }
        }
        _finished.value = emptyList()
    }
}

data class HomeUiState(
    val selected: Long = 0L,
    val page: Int = PageType.HOME,
    val uiFlags: Int = UiFlags.NONE,
    val jobState: JobState = JobState(),
)

sealed class HomeUiEvent {
    data class NavTo(val page: Int) : HomeUiEvent()
    data class Pipeline(val index: Int) : HomeUiEvent()
    data class ToggleSelected(val id: Long) : HomeUiEvent()
    data class Message(val message: String?) : HomeUiEvent()
    data class UiFlags(val uiFlags: Int) : HomeUiEvent()
    data object Pause : HomeUiEvent()
    data object Reset : HomeUiEvent()
    data object Resume : HomeUiEvent()
    data object Start : HomeUiEvent()
    data object Stop : HomeUiEvent()
}

data class JobState(
    val status: Int = 0,
    val orificePlate: OrificePlate = OrificePlate(),
    val finished: List<Triple<Int, Int, Color>> = emptyList(),
    val job: Job? = null,
) {
    companion object {
        const val STEPPED = 0
        const val RUNNING = 1
        const val PAUSED = 2
    }
}