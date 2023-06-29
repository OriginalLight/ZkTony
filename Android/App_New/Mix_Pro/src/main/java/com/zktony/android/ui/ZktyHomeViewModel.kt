package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.ProgramDao
import com.zktony.android.logic.data.entities.ProgramEntity
import com.zktony.android.logic.ext.axisInitializer
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ZktyHomeViewModel constructor(
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
            axisInitializer()
            _loading.value = 0
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
            _loading.value = index + 2
            delay(1000L)
            _loading.value = 0
        }
    }

    private fun pipeline(index: Int) {
        viewModelScope.launch {
            _loading.value = index + 4
            delay(1000L)
            _loading.value = 0
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