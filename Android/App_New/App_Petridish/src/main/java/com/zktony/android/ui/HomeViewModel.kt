package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.tx.ExecuteType
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.getGpio
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

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
    fun event(event: HomeEvent) {
        when (event) {
            is HomeEvent.Reset -> reset()
            is HomeEvent.Start -> start()
            is HomeEvent.Stop -> stop()
            is HomeEvent.NavTo -> _page.value = event.page
            is HomeEvent.ToggleSelected -> _selected.value = event.id
            is HomeEvent.Clean -> clean(event.index)
            is HomeEvent.Syringe -> syringe(event.index)
            is HomeEvent.Pipeline -> pipeline(event.index)
            else -> {}
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


                    val ids = listOf(0, 1)
                    // 查询GPIO状态
                    tx {
                        delay = 300L
                        queryGpio(ids)
                    }
                    // 针对每个电机进行初始化
                    ids.forEach {
                        // 如果电机未初始化，则进行初始化
                        if (!getGpio(it)) {
                            // 进行电机初始化
                            tx {
                                timeout = 1000L * 60
                                move(MoveType.MOVE_PULSE) {
                                    index = it
                                    pulse = 3200L * -30
                                }

                            }
                        }

                        // 进行正向运动
                        tx {
                            timeout = 1000L * 10
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 3200L * 2
                                acc = 50
                                dec = 80
                                speed = 100
                            }
                        }

                        // 进行反向运动
                        tx {
                            timeout = 1000L * 15
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 3200L * -3
                                acc = 50
                                dec = 80
                                speed = 100
                            }
                        }
                    }


                }
            } catch (ex: Exception) {
                _loading.value = 0
            } finally {
                _loading.value = 0
            }
        }
    }

    /**
     * Starts the execution of the selected program entity.
     */
    private fun start() {
        viewModelScope.launch {
            val selected = _uiState.value.entities.find { it.id == _selected.value }!!

            // Launch a new job to execute the program entity
            _job.value = launch(Dispatchers.Default) {
                // Move to the starting position
                // Step 1: Move the Z axis to 0
                tx {
                    move {
                        index = 1
                        dv = 0f
                    }
                }
                // Step 2: Move the Y axis to 0
                tx {
                    move {
                        index = 0
                        dv = 0f
                    }
                }

                // Open the valve to start the process
                // Step 3: Open the valve
                tx {
                    delay = 100L
                    valve(2 to 1)
                }

                // Perform the pre-dispense operation
                // Step 4: Perform the pre-dispense operation
                tx {
                    timeout = 1000L * 60 * 1
                    move {
                        index = 2
                        dv = selected.volume[3]
                    }
                    repeat(6) {
                        move {
                            index = it + 3
                            dv = selected.volume[2]
                        }
                    }
                }

                // Move to the dispensing position
                // Step 5: Move the Z axis to 1
                tx {
                    move {
                        index = 0
                        dv = selected.axis[0]
                    }
                }
                // Step 6: Move the Y axis to 1
                tx {
                    move {
                        index = 1
                        dv = selected.axis[1]
                    }
                }

                // Perform the dispensing operation
                // Step 7: Perform the dispensing operation
                tx {
                    timeout = 1000L * 60 * 3
                    move {
                        index = 2
                        dv = selected.volume[1]
                    }
                    repeat(6) {
                        move {
                            index = it + 3
                            dv = selected.volume[0] / 2
                        }
                    }
                }
            }

            // Handle the completion of the job
            _job.value?.invokeOnCompletion {
                launch {
                    // Reset the screen and stop the motors
                    _job.value = null
                    _loading.value = 1
                    tx {
                        move {
                            index = 1
                            dv = 0f
                        }
                    }
                    tx {
                        move {
                            index = 0
                            dv = 0f
                        }
                    }

                    // Close the valve
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
        }
    }

    /**
     * Stops the execution of the Home screen.
     */
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
            tx {
                move {
                    index = 1
                    dv = 0f
                }
            }
            tx {
                move {
                    index = 0
                    dv = 0f
                }
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

    /**
     * Performs a clean operation on a program entity.
     *
     * @param index The index of the program entity to perform the operation on.
     */
    private fun clean(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                // Stop all clean operations
                _loading.value = 0
                tx { stop(9) }
            } else {
                // Start a clean operation on the selected program entity
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

    /**
     * Performs a syringe operation on a program entity.
     *
     * @param index The index of the program entity to perform the operation on.
     */
    private fun syringe(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                // Stop all syringe operations
                _loading.value = 0
                syringeJob?.cancelAndJoin()
                syringeJob = null
                tx {
                    timeout = 1000L * 60
                    move(MoveType.MOVE_PULSE) {
                        this.index = 2
                        pulse = 0
                    }
                }
            } else {
                // Start a syringe operation on the selected program entity
                _loading.value = 3
                syringeJob = launch {
                    while (true) {
                        tx {
                            delay = 100L
                            valve(2 to if (index == 1) 1 else 0)
                        }
                        tx {
                            timeout = 1000L * 60
                            move(MoveType.MOVE_PULSE) {
                                this.index = 2
                                pulse = 0
                            }
                        }
                        tx {
                            delay = 100L
                            valve(2 to if (index == 1) 0 else 1)
                        }
                        tx {
                            timeout = 1000L * 60
                            move(MoveType.MOVE_PULSE) {
                                this.index = 2
                                pulse = 0
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs a pipeline operation on a program entity.
     *
     * @param index The index of the program entity to perform the operation on.
     */
    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                // Stop all pipeline operations
                _loading.value = 0
                tx { stop(3, 4, 5, 6, 7, 8) }
            } else {
                // Start a pipeline operation on the selected program entity
                _loading.value = 4
                tx {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        move(MoveType.MOVE_PULSE) {
                            this.index = it + 3
                            pulse = 3200L * 10000L * if (index == 1) 1 else -1
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for the UI state of the Home screen.
 *
 * @param entities The list of program entities to display.
 * @param selected The ID of the selected program entity.
 * @param page The current page type.
 * @param loading The loading state of the screen.
 * @param job The current execution job.
 */
data class HomeUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
    /*
     * 0: loading closed
     * 1: resting
     * 2: clean
     * 3: syringe 1
     * 4: syringe 2
     * 5: pipeline 1
     * 6: pipeline 2
     */
    val loading: Int = 0,
    val job: Job? = null,
)

/**
 * Sealed class for the events of the Home screen.
 */
sealed class HomeEvent {
    data object Reset : HomeEvent()
    data object Start : HomeEvent()
    data object Stop : HomeEvent()
    data class NavTo(val page: PageType) : HomeEvent()
    data class ToggleSelected(val id: Long) : HomeEvent()
    data class Clean(val index: Int) : HomeEvent()
    data class Syringe(val index: Int) : HomeEvent()
    data class Pipeline(val index: Int) : HomeEvent()
}