package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.dataSaver
import com.zktony.android.utils.extra.getGpio
import com.zktony.android.utils.extra.pulse
import com.zktony.android.utils.extra.serial
import com.zktony.android.utils.model.ExecuteType
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
    private val _page = MutableStateFlow(PageType.HOME)
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
                delay(1000L)
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
                withTimeout(2 * 60 * 1000L) {
                    serial { gpio(0, 1, 2) }
                    delay(500L)
                    val job = launch {
                        if (!getGpio(2)) {
                            serial { valve(2 to 1) }
                            delay(30L)
                            serial {
                                timeout = 1000L * 60
                                start(
                                    index = 2,
                                    pulse = Constants.ZT_0005 * -1,
                                    ads = Triple(300, 400, 600)
                                )
                            }
                        }
                    }

                    listOf(1, 0).forEach {
                        if (!getGpio(it)) {
                            serial {
                                timeout = 1000L * 30
                                start(index = it, pulse = 3200L * -30, ads = Triple(300, 400, 600))
                            }
                        }

                        serial {
                            timeout = 1000L * 10
                            start(index = it, pulse = 3200L * 2, ads = Triple(300, 400, 600))
                        }

                        serial {
                            timeout = 1000L * 15
                            start(index = it, pulse = 3200L * -3, ads = Triple(300, 400, 600))
                        }
                    }
                    job.join()
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
            val abscissa = dataSaver.readData(Constants.ZT_0003, 0.0)
            val ordinate = dataSaver.readData(Constants.ZT_0004, 0.0)
            _job.value?.cancel()
            _job.value = launch {
                serial {
                    timeout = 1000L * 60L
                    start(index = 1, dv = ordinate)
                }
                serial {
                    timeout = 1000L * 60L
                    start(index = 0, dv = abscissa)
                }
                serial { valve(2 to 1) }
                delay(30L)
                serial {
                    timeout = 1000L * 60 * 1

                    val s = selected.speed.pre
                    val p1 = pulse(index = 2, dvp = selected.dosage.preCoagulant * 6)
                    val p2 = pulse(index = 3, dvp = selected.dosage.preColloid * 2)
                    val p3 = pulse(index = 4, dvp = selected.dosage.preColloid * 2)
                    val p4 = pulse(index = 5, dvp = selected.dosage.preColloid * 2)

                    val ad = (2 * s * 100).toLong()
                    val s1 = (p1 / (maxOf(p2, p3, p4) / s) * 100).toLong()
                    val s2 = (s * 100).toLong()

                    glue(index = 2, pulse = p1, ads = Triple(ad, ad, s1))
                    glue(index = 3, pulse = p2, ads = Triple(ad, ad, s2))
                    glue(index = 4, pulse = p3, ads = Triple(ad, ad, s2))
                    glue(index = 5, pulse = p4, ads = Triple(ad, ad, s2))
                }

                serial {
                    timeout = 1000L * 60L
                    start(index = 0, dv = selected.coordinate.abscissa)
                }
                serial {
                    timeout = 1000L * 60L
                    start(index = 1, dv = selected.coordinate.ordinate)
                }

                serial {
                    timeout = 1000L * 60 * 10
                    val s = selected.speed.glue
                    val p1 = pulse(index = 2, dvp = selected.dosage.coagulant * 6)
                    val p2 = pulse(index = 3, dvp = selected.dosage.colloid)
                    val p3 = pulse(index = 4, dvp = selected.dosage.colloid)
                    val p4 = pulse(index = 5, dvp = selected.dosage.colloid)
                    val p5 = pulse(index = 6, dvp = selected.dosage.colloid)
                    val p6 = pulse(index = 7, dvp = selected.dosage.colloid)
                    val p7 = pulse(index = 8, dvp = selected.dosage.colloid)

                    val pv1 = (p2 + p5) / 2
                    val pv2 = (p3 + p6) / 2
                    val pv3 = (p4 + p7) / 2

                    glue(
                        index = 2,
                        pulse = p1,
                        ads = Triple(
                            (2.5 * s * 100).toLong(),
                            (2.5 * s * 100).toLong(),
                            (p1 / (maxOf(pv1, pv2, pv3) * 2 / s) * 100).toLong()
                        )
                    )

                    glue(
                        index = 3,
                        pulse = pv1,
                        ads = Triple(
                            (s * s / 2 / pv1 * 100).toLong(),
                            (s * 100).toLong(),
                            (s * 100).toLong()
                        )
                    )

                    glue(
                        index = 4,
                        pulse = pv2,
                        ads = Triple(
                            (s * s / 2 / pv2 * 100).toLong(),
                            (s * 100).toLong(),
                            (s * 100).toLong()
                        )
                    )

                    glue(
                        index = 5,
                        pulse = pv3,
                        ads = Triple(
                            (s * s / 2 / pv3 * 100).toLong(),
                            (s * 100).toLong(),
                            (s * 100).toLong()
                        )
                    )

                    glue(
                        index = 6,
                        pulse = p5,
                        ads = Triple(
                            (s * 100).toLong(),
                            (s * s / 2 / pv1 * 100).toLong(),
                            (s * 100).toLong()
                        )
                    )

                    glue(
                        index = 7,
                        pulse = p6,
                        ads = Triple(
                            (s * 100).toLong(),
                            (s * s / 2 / pv2 * 100).toLong(),
                            (s * 100).toLong()
                        )
                    )

                    glue(
                        index = 8,
                        pulse = p7,
                        ads = Triple(
                            (s * 100).toLong(),
                            (s * s / 2 / pv3 * 100).toLong(),
                            (s * 100).toLong()
                        )
                    )
                }

                _loading.value = 1
                serial {
                    timeout = 1000L * 60L
                    start(index = 1, dv = ordinate)
                }

                serial {
                    timeout = 1000L * 60L
                    start(index = 0, dv = abscissa)
                }

                serial { gpio(2) }
                delay(300L)
                if (!getGpio(2)) {
                    serial { valve(2 to 0) }
                    delay(30L)
                    serial {
                        timeout = 1000L * 60
                        start(index = 2, pulse = Constants.ZT_0005 * -1)
                    }
                }

                // Set the loading state to 0 to indicate that the execution has stopped
                _loading.value = 0
            }
            _job.value?.invokeOnCompletion {
                _job.value = null
            }
        }
    }

    private fun stop() {
        viewModelScope.launch {
            // Cancel and join the current job
            _job.value?.cancel()
            _job.value = null

            // Reset the screen and stop the motors
            _loading.value = 1

            val abscissa = dataSaver.readData(Constants.ZT_0003, 0.0)
            val ordinate = dataSaver.readData(Constants.ZT_0004, 0.0)

            serial { stop(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) }
            delay(200L)

            serial {
                timeout = 1000L * 60L
                start(index = 1, dv = ordinate)
            }

            serial {
                timeout = 1000L * 60L
                start(index = 0, dv = abscissa)
            }

            serial { gpio(2) }
            delay(300L)
            if (!getGpio(2)) {
                serial { valve(2 to 0) }
                delay(30L)
                serial {
                    timeout = 1000L * 60
                    start(index = 2, pulse = Constants.ZT_0005 * -1)
                }
            }

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
                    start(index = 9, pulse = 3200L * 10000L)
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
                serial { stop(2) }
                delay(100L)
                serial {
                    timeout = 1000L * 60
                    start(index = 2, pulse = Constants.ZT_0005 * -1)
                }
            } else {
                _loading.value = 2 + index
                syringeJob = launch {
                    while (true) {
                        serial { valve(2 to if (index == 1) 0 else 1) }
                        delay(30L)
                        serial {
                            timeout = 1000L * 60
                            start(index = 2, pulse = Constants.ZT_0005)
                        }
                        serial { valve(2 to if (index == 1) 1 else 0) }
                        delay(30L)
                        serial {
                            timeout = 1000L * 60
                            start(index = 2, pulse = Constants.ZT_0005 * -1)
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
                _loading.value = 4 + index
                serial {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        start(index = it + 3, pulse = 3200L * 10000L * if (index == 1) 1 else -1)
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