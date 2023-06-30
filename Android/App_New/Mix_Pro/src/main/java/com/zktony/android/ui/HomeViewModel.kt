package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.ProgramEntity
import com.zktony.android.core.dsl.axisInitializer
import com.zktony.android.core.dsl.setLock
import com.zktony.android.core.dsl.tx
import com.zktony.android.core.utils.ExecuteType
import com.zktony.android.ui.utils.PageType
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
class HomeViewModel constructor(
    private val dao: ProgramDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.LIST)
    private val _loading = MutableStateFlow(0)
    private val _job = MutableStateFlow<Job?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
                _page,
                _loading,
                _job,
            ) { entities, selected, page, loading, job ->
                HomeUiState(
                    entities = entities,
                    selected = selected,
                    page = page,
                    loading = loading,
                    job = job,
                )
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun event(event: HomeEvent) {
        when (event) {
            is HomeEvent.Reset -> reset()
            is HomeEvent.Start -> start()
            is HomeEvent.NavTo -> _page.value = event.page
            is HomeEvent.ToggleSelected -> _selected.value = event.id
            is HomeEvent.Syringe -> syringe(event.index)
            is HomeEvent.Pipeline -> pipeline(event.index)
        }
    }

    private fun reset() {
        viewModelScope.launch {
            _loading.value = 1
            try {
                withTimeout(60 * 1000L) {
                    axisInitializer(1, 0)
                }
            } catch (ex: Exception) {
                _loading.value = 0
            } finally {
                _loading.value = 0
            }
        }
    }

    private fun start() {
        _job.value = viewModelScope.launch {
            delay(10000L)
        }
        _job.value?.invokeOnCompletion {
            _job.value = null
        }
    }

    private fun syringe(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _loading.value = 0
            } else {
                _loading.value = index + 1
            }
        }
    }

    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _loading.value = 0
                tx { stop(3, 4, 5, 6, 7, 8) }
            } else {
                _loading.value = index + 3
                tx {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        setLock(it + 3)
                        mpm {
                            this.index = it + 3
                            pulse = 3200L * 10000L * if (index == 1) 1 else -1
                        }
                    }
                }
            }
        }
    }
}

data class HomeUiState(
    val entities: List<ProgramEntity> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
    /*
     * 0: loading closed
     * 1: resting
     */
    val loading: Int = 0,
    val job: Job? = null,
)

sealed class HomeEvent {
    object Reset : HomeEvent()
    object Start : HomeEvent()
    data class NavTo(val page: PageType) : HomeEvent()
    data class ToggleSelected(val id: Long) : HomeEvent()
    data class Syringe(val index: Int) : HomeEvent()
    data class Pipeline(val index: Int) : HomeEvent()
}