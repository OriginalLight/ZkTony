package com.zktony.android.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.internal.OrificePlate
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.getGpio
import com.zktony.android.utils.extra.internal.ExecuteType
import com.zktony.android.utils.extra.serial
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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
    private val dao: ProgramDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow(0)

    private val _jobState = MutableStateFlow(JobState())
    private val _status = MutableStateFlow(0)
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
            is HomeUiEvent.Reset -> init()
            is HomeUiEvent.Start -> start()
            is HomeUiEvent.Pause -> _status.value = 2
            is HomeUiEvent.Resume -> _status.value = 0
            is HomeUiEvent.Stop -> stop()
            is HomeUiEvent.NavTo -> _page.value = uiEvent.page
            is HomeUiEvent.ToggleSelected -> toggleSelected(uiEvent.id)
            is HomeUiEvent.Pipeline -> pipeline(uiEvent.index)
        }
    }

    private fun init() {
        viewModelScope.launch {
            _uiFlags.value = 1
            try {
                withTimeout(60 * 1000L) {
                    serial { gpio(0, 1) }
                    delay(300L)
                    val job: MutableList<Job?> = MutableList(2) { null }
                    repeat(2) {
                        job[it] = launch {
                            if (!getGpio(it)) {
                                serial {
                                    timeout = 1000L * 30
                                    start(index = it, pdv = 3200L * -30)
                                }
                            }

                            serial {
                                start(
                                    index = it,
                                    pdv = 3200L * 2,
                                    ads = Triple(50, 80, 100)
                                )
                            }

                            serial {
                                start(
                                    index = it,
                                    pdv = 3200L * -3,
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
                _uiFlags.value = 0
            }
        }
    }

    private fun start() {
        _status.value = 1
        _uiFlags.value = 5
        _job.value = viewModelScope.launch {
            try {
                val selected =
                    dao.getById(_selected.value).firstOrNull() ?: throw Exception("程序为空")
                if (selected.orificePlates.isEmpty()) throw Exception("加液孔板为空")

                val orificePlates = selected.orificePlates

                orificePlates.forEach { orificePlate ->
                    _orificePlate.value = orificePlate

                    if (orificePlate.type == 0) {
                        separationAlgorithm(orificePlate)
                    } else {
                        hybridAlgorithm(orificePlate)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                _message.value = ex.message
            } finally {
                _finished.value = emptyList()
                _status.value = 0
                _uiFlags.value = 0
            }
        }
    }

    private fun stop() {
        viewModelScope.launch {
            _job.value?.cancel()
            _job.value = null
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
                _uiFlags.value = 0
                serial { stop(2, 3, 4, 5, 6, 7) }
            } else {
                _uiFlags.value = index + 1
                serial {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        start(
                            index = it + 2,
                            pdv = 3200L * 10000 * if (index < 3) 1 else -1
                        )
                    }
                }
            }
        }
    }

    private suspend fun separationAlgorithm(orificePlate: OrificePlate) {
        val row = orificePlate.row
        val column = orificePlate.column
        for (i in 0 until ceil(row / 6.0).toInt()) {
            for (j in if (i % 2 == 0) 0 until column else column - 1 downTo 0) {
                while (_status.value == 2) {
                    delay(100)
                }
                val coordinate = orificePlate.orifices[j][i * 6].point
                serial {
                    start(index = 0, pdv = coordinate.x)
                    start(index = 1, pdv = coordinate.y)
                }

                while (_status.value == 2) {
                    delay(100)
                }

                val list = mutableListOf<Triple<Int, Int, Color>>()

                serial {
                    timeout = 1000L * 30
                    repeat(6) {
                        if (i * 6 + it < row) {
                            val orifice = orificePlate.orifices[j][i * 6 + it]
                            if (orifice.selected) {
                                start(index = 2 + it, pdv = orifice.volume.getOrNull(0) ?: 0.0)
                                list += Triple(j, i * 6 + it, Color.Green)
                            }
                        }
                    }
                }
                _finished.value += list

                delay(orificePlate.delay)
            }
        }
        _finished.value = emptyList()
    }

    private suspend fun hybridAlgorithm(orificePlate: OrificePlate) {
        val row = orificePlate.row
        val column = orificePlate.column
        val coordinate = orificePlate.points
        val rowSpace = (coordinate[1].x - coordinate[0].x) / (row - 1)
        for (i in 0 until row + 5) {
            for (j in if (i % 2 == 0) 0 until column else column - 1 downTo 0) {
                while (_status.value == 2) {
                    delay(100)
                }
                val abscissa = if (i < 6) {
                    orificePlate.orifices[j][0].point.x - (5 - i) * rowSpace
                } else {
                    orificePlate.orifices[j][i - 5].point.x
                }

                serial {
                    start(index = 0, pdv = abscissa)
                    start(index = 1, pdv = orificePlate.orifices[j][0].point.y)
                }

                while (_status.value == 2) {
                    delay(100)
                }

                val list = mutableListOf<Triple<Int, Int, Color>>()

                serial {
                    timeout = 1000L * 30
                    repeat(6) {
                        if (i - 5 + it in 0 until row) {
                            val orifice = orificePlate.orifices[j][i - 5 + it]
                            if (orifice.selected) {
                                start(index = 2 + it, pdv = orifice.volume.getOrNull(it) ?: 0.0)
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

                list.forEach { triple ->
                    _finished.value -= _finished.value.filter { it.first == triple.first && it.second == triple.second }
                }
                _finished.value += list

                delay(orificePlate.delay)
            }
        }
        _finished.value = emptyList()
    }
}

data class HomeUiState(
    val selected: Long = 0L,
    val page: PageType = PageType.HOME,
    val uiFlags: Int = 0,
    val jobState: JobState = JobState(),
)

sealed class HomeUiEvent {
    data object Reset : HomeUiEvent()
    data object Start : HomeUiEvent()
    data object Pause : HomeUiEvent()
    data object Resume : HomeUiEvent()
    data object Stop : HomeUiEvent()
    data class NavTo(val page: PageType) : HomeUiEvent()
    data class ToggleSelected(val id: Long) : HomeUiEvent()
    data class Pipeline(val index: Int) : HomeUiEvent()
}

data class JobState(
    val status: Int = 0,
    val orificePlate: OrificePlate = OrificePlate(),
    val finished: List<Triple<Int, Int, Color>> = emptyList(),
    val job: Job? = null,
)