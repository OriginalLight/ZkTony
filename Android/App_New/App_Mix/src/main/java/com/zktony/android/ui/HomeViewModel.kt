package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ext.getGpio
import com.zktony.android.utils.ext.serial
import com.zktony.android.utils.model.ExecuteType
import com.zktony.android.utils.model.MoveType
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
            launch {
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
            launch {
                initializer()
            }
        }
    }

    fun uiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Reset -> initializer()
            is HomeUiEvent.Start -> start()
            is HomeUiEvent.Stop -> stop()
            is HomeUiEvent.NavTo -> _page.value = event.page
            is HomeUiEvent.ToggleSelected -> _selected.value = event.id
            is HomeUiEvent.Clean -> clean()
            is HomeUiEvent.Syringe -> syringe(event.index)
            is HomeUiEvent.Pipeline -> pipeline(event.index)
        }
    }

    private fun initializer() {
        viewModelScope.launch {
            _loading.value = 1
            try {
                // Initialize the axes and syringe within a timeout of 60 seconds
                withTimeout(2 * 60 * 1000L) {
                    val ids = listOf(1, 0)
                    // 查询GPIO状态
                    serial {
                        delay = 300L
                        queryGpio(ids)
                    }
                    // 针对每个电机进行初始化
                    ids.forEach {
                        // 如果电机未初始化，则进行初始化
                        if (!getGpio(it)) {
                            // 进行电机初始化
                            serial {
                                timeout = 1000L * 30
                                move(MoveType.MOVE_PULSE) {
                                    index = it
                                    pulse = 3200L * -30
                                }
                            }
                        }

                        // 进行正向运动
                        serial {
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
                        serial {
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

                    delay(150L)
                    serial { queryGpio(listOf(2)) }
                    delay(100L)
                    serial { valve(2 to 0) }
                    serial {
                        timeout = 1000L * 30
                        move(MoveType.MOVE_PULSE) {
                            index = 2
                            pulse = Constants.MAX_SYRINGE * -1
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

    private fun start() {
        viewModelScope.launch {
            val selected = _uiState.value.entities.find { it.id == _selected.value }!!
            _job.value = launch(Dispatchers.Default) {
                // Step 1: Move the Z axis to 0
                serial {
                    move {
                        index = 1
                        dv = 0.0
                    }
                }
                // Step 2: Move the Y axis to 0
                serial {
                    move {
                        index = 0
                        dv = 0.0
                    }
                }

                // Step 3: Open the valve
                serial {
                    delay = 100L
                    valve(2 to 1)
                }

                // Perform the pre-dispense operation
                // Step 4: Perform the pre-dispense operation
                serial {
                    timeout = 1000L * 60 * 1
                    move {
                        index = 2
                        dv = selected.dosage.preCoagulant
                    }
                    repeat(6) {
                        move {
                            index = it + 3
                            dv = selected.dosage.preColloid
                        }
                    }
                }

                // Move to the dispensing position
                // Step 5: Move the Z axis to 1
                serial {
                    move {
                        index = 0
                        dv = selected.coordinate.abscissa
                    }
                }
                // Step 6: Move the Y axis to 1
                serial {
                    move {
                        index = 1
                        dv = selected.coordinate.ordinate
                    }
                }

                // Perform the dispensing operation
                // Step 7: Perform the dispensing operation
                serial {
                    timeout = 1000L * 60 * 3
                    move {
                        index = 2
                        dv = selected.dosage.coagulant
                    }
                    repeat(6) {
                        move {
                            index = it + 3
                            dv = selected.dosage.colloid / 2
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
                    serial {
                        move {
                            index = 1
                            dv = 0.0
                        }
                    }
                    serial {
                        move {
                            index = 0
                            dv = 0.0
                        }
                    }

                    // Close the valve
                    serial {
                        delay = 100L
                        valve(2 to 0)
                    }

                    // Perform a syringe operation to clear the system
                    serial {
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

    private fun stop() {
        viewModelScope.launch {
            // Cancel and join the current job
            _job.value?.cancelAndJoin()
            _job.value = null

            // Reset the screen and stop the motors
            _loading.value = 1
            serial {
                delay = 500L
                reset()
            }
            serial {
                move {
                    index = 1
                    dv = 0.0
                }
            }
            serial {
                move {
                    index = 0
                    dv = 0.0
                }
            }

            // Close the syringe valve
            serial {
                delay = 100L
                valve(2 to 0)
            }

            // Perform a syringe operation to clear the system
            serial {
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
                serial { stop(9) }
            } else {
                _loading.value = 2
                serial {
                    executeType = ExecuteType.ASYNC
                    move(MoveType.MOVE_PULSE) {
                        this.index = 9
                        pulse = 3200L * 10000L
                    }
                }
            }
        }
    }

    private fun syringe(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _loading.value = 0
                syringeJob?.cancelAndJoin()
                syringeJob = null
                serial {
                    timeout = 1000L * 60
                    move(MoveType.MOVE_PULSE) {
                        this.index = 2
                        pulse = 0
                    }
                }
            } else {
                _loading.value = 3
                syringeJob = launch {
                    while (true) {
                        serial {
                            delay = 100L
                            valve(2 to if (index == 1) 1 else 0)
                        }
                        serial {
                            timeout = 1000L * 60
                            move(MoveType.MOVE_PULSE) {
                                this.index = 2
                                pulse = 0
                            }
                        }
                        serial {
                            delay = 100L
                            valve(2 to if (index == 1) 0 else 1)
                        }
                        serial {
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

    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _loading.value = 0
                serial { stop(3, 4, 5, 6, 7, 8) }
            } else {
                _loading.value = 4
                serial {
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

data class HomeUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
    val loading: Int = 0,
    val job: Job? = null,
)

sealed class HomeUiEvent {
    data object Reset : HomeUiEvent()
    data object Start : HomeUiEvent()
    data object Stop : HomeUiEvent()
    data object Clean : HomeUiEvent()
    data class NavTo(val page: PageType) : HomeUiEvent()
    data class ToggleSelected(val id: Long) : HomeUiEvent()
    data class Syringe(val index: Int) : HomeUiEvent()
    data class Pipeline(val index: Int) : HomeUiEvent()
}