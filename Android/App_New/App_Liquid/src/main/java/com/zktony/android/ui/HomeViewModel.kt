package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.getGpio
import com.zktony.android.utils.extra.internal.ExecuteType
import com.zktony.android.utils.extra.serial
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class HomeViewModel constructor(private val dao: ProgramDao) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _loading = MutableStateFlow(0)
    private val helper: RuntimeViewModel = RuntimeViewModel()

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                combine(
                    dao.getAll(),
                    _selected,
                    _page,
                    _loading,
                    helper.state,
                ) { entities, selected, page, loading, runtime ->
                    HomeUiState(
                        entities = entities,
                        selected = selected,
                        page = page,
                        loading = loading,
                        runtime = runtime
                    )
                }.catch { ex ->
                    ex.printStackTrace()
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                initializer()
            }
        }
    }

    fun uiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Reset -> initializer()
            is HomeUiEvent.Runtime -> {
                when (event.action) {
                    RuntimeAction.START -> helper.start()
                    RuntimeAction.PAUSE -> helper.pause()
                    RuntimeAction.RESUME -> helper.resume()
                    RuntimeAction.STOP -> helper.stop()
                }
            }

            is HomeUiEvent.NavTo -> _page.value = event.page
            is HomeUiEvent.ToggleSelected -> {
                _selected.value = event.id
                helper.toggleProgram(uiState.value.entities.find { it.id == event.id }!!)
            }

            is HomeUiEvent.Pipeline -> pipeline(event.index)
        }
    }

    private fun initializer() {
        viewModelScope.launch {
            _loading.value = 1
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
                _loading.value = 0
            } finally {
                _loading.value = 0
            }
        }
    }

    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _loading.value = 0
                serial { stop(2, 3, 4, 5, 6, 7) }
            } else {
                _loading.value = index + 1
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
}

data class HomeUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.HOME,
    val loading: Int = 0,
    val runtime: RuntimeState = RuntimeState(),
)

sealed class HomeUiEvent {
    data object Reset : HomeUiEvent()
    data class Runtime(val action: RuntimeAction) : HomeUiEvent()
    data class NavTo(val page: PageType) : HomeUiEvent()
    data class ToggleSelected(val id: Long) : HomeUiEvent()
    data class Pipeline(val index: Int) : HomeUiEvent()
}

enum class RuntimeAction {
    START, STOP, PAUSE, RESUME
}

