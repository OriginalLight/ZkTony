package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.tx.ExecuteType
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.initializer
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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
    private val _page = MutableStateFlow(PageType.LIST)
    private val _loading = MutableStateFlow(0)
    private val _job = MutableStateFlow<Job?>(null)
    private var syringeJob: Job? = null

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(), // Step 1: Observe changes in the database by calling the getAll function
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
                _uiState.value = it // Step 2: Update the UI state with the new values
            }
        }
    }

    /**
     * Handles the given Home screen event.
     *
     * @param event The Home screen event to handle.
     */
    fun event(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Reset -> reset()
            is HomeUiEvent.Start -> start()
            is HomeUiEvent.Stop -> stop()
            is HomeUiEvent.NavTo -> _page.value = event.page
            is HomeUiEvent.ToggleSelected -> _selected.value = event.id
            is HomeUiEvent.Clean -> clean()
            is HomeUiEvent.Pipeline -> pipeline(event.index)
        }
    }

    /**
     * Resets the Home screen by initializing the axes and syringe.
     */
    private fun reset() {
        viewModelScope.launch {
            _loading.value = 1
            try {
                // Initialize the axes and syringe within a timeout of 60 seconds
                withTimeout(60 * 1000L) {
                    initializer()
                }
            } catch (ex: Exception) {
                _loading.value = 0
            } finally {
                _loading.value = 0
            }
        }
    }

    private fun start() {
        viewModelScope.launch {
            val selected = _uiState.value.entities.find { it.id == _selected.value }!!

            // Handle the completion of the job
            _job.value?.invokeOnCompletion {
                launch {
                    // Reset the screen and stop the motors
                    _job.value = null
                    _loading.value = 1


                    // Close the valve
                    tx {
                        delay = 100L
                        valve(2 to 0)
                    }

                    // Set the loading state to 0 to indicate that the execution has stopped
                    _loading.value = 0
                }
            }
        }
    }

    private fun stop() {
        viewModelScope.launch {
            // Cancel and join the current job
            _job.value?.cancelAndJoin()
            _job.value = null

            // Reset the screen and stop the motors
            _loading.value = 1
            tx {
                delay = 500L
                reset()
            }

            // Close the syringe valve
            tx {
                delay = 100L
                valve(2 to 0)
            }

            // Perform a syringe operation to clear the system
            tx {
                timeout = 1000L * 60
                move(MoveType.MOVE_PULSE) {
                    index = 2
                    pulse = 0
                }
            }

            // Set the loading state to 0 to indicate that the execution has stopped
            _loading.value = 0
        }
    }

    private fun clean() {
        viewModelScope.launch {
            if (_loading.value == 2) {
                _loading.value = 0
                tx { stop(9) }
            } else {
                _loading.value = 2
                tx {
                    executeType = ExecuteType.ASYNC
                    move(MoveType.MOVE_PULSE) {
                        this.index = 9
                        pulse = 3200L * 10000L
                    }
                }
            }
        }
    }

    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                // Stop all pipeline operations
                _loading.value = 0
                tx { stop(2, 3, 4, 5, 6, 7) }
            } else {
                // Start a pipeline operation on the selected program entity
                _loading.value = 3
                tx {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        move(MoveType.MOVE_PULSE) {
                            this.index = it + 2
                            pulse = 3200L * 10000L * if (index == 1) 1 else -1
                        }
                    }
                }
            }
        }
    }
}

data class HomeUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
    val loading: Int = 0,
    val job: Job? = null,
)

sealed class HomeUiEvent {
    data object Reset : HomeUiEvent()
    data object Clean : HomeUiEvent()
    data object Start : HomeUiEvent()
    data object Stop : HomeUiEvent()
    data class NavTo(val page: PageType) : HomeUiEvent()
    data class ToggleSelected(val id: Long) : HomeUiEvent()
    data class Pipeline(val index: Int) : HomeUiEvent()
}