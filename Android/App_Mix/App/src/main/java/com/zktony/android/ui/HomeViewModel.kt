package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.getGpio
import com.zktony.android.utils.SerialPortUtils.glue
import com.zktony.android.utils.SerialPortUtils.gpio
import com.zktony.android.utils.SerialPortUtils.pulse
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.SerialPortUtils.stop
import com.zktony.android.utils.SerialPortUtils.valve
import com.zktony.android.utils.internal.ExecuteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    private val dataStore: DataSaverDataStore
) : ViewModel() {

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())
    private val _job = MutableStateFlow<Job?>(null)

    private var syringeJob: Job? = null

    val selected = _selected.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val job = _job.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        reset()
    }

    fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Clean -> clean()
            is HomeIntent.NavTo -> _page.value = intent.page
            is HomeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is HomeIntent.Pipeline -> pipeline(intent.index)
            is HomeIntent.Reset -> reset()
            is HomeIntent.Start -> startJob()
            is HomeIntent.Stop -> stopJob()
            is HomeIntent.Syringe -> syringe(intent.index)
            is HomeIntent.Selected -> _selected.value = intent.id
        }
    }

    private fun reset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
                withTimeout(60 * 1000L) {
                    gpio(0, 1, 2)
                    delay(500L)
                    val job = launch {
                        if (!getGpio(2)) {
                            valve(2 to 1)
                            delay(30L)
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 2,
                                    pdv = Constants.ZT_0005 * -1,
                                    ads = Triple(300, 400, 600)
                                )
                            }
                        }
                    }

                    listOf(1, 0).forEach {
                        if (!getGpio(it)) {
                            start {
                                timeOut = 1000L * 30
                                with(index = it, pdv = 3200L * -30, ads = Triple(300, 400, 600))
                            }
                        }

                        start {
                            timeOut = 1000L * 10
                            with(index = it, pdv = 3200L * 2, ads = Triple(300, 400, 600))
                        }

                        start {
                            timeOut = 1000L * 15
                            with(index = it, pdv = 3200L * -3, ads = Triple(300, 400, 600))
                        }
                    }
                    job.join()
                }
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
        }
    }

    private fun startJob() {
        viewModelScope.launch {
            val selected = dao.getById(_selected.value).firstOrNull()
            if (selected == null) {
                _uiFlags.value = UiFlags.message("未选择程序")
                return@launch
            }
            val abscissa = dataStore.readData(Constants.ZT_0003, 0.0)
            val ordinate = dataStore.readData(Constants.ZT_0004, 0.0)
            _job.value?.cancel()
            _job.value = launch {
                try {
                    start {
                        timeOut = 1000L * 60L
                        with(index = 1, pdv = ordinate)
                    }
                    start {
                        timeOut = 1000L * 60L
                        with(index = 0, pdv = abscissa)
                    }
                    valve(2 to 0)
                    delay(30L)
                    glue {
                        timeOut = 1000L * 60 * 1

                        val s = selected.speed.pre
                        val p1 = pulse(index = 2, dvp = selected.dosage.preCoagulant * 6)
                        val p2 = pulse(index = 3, dvp = selected.dosage.preColloid * 2)
                        val p3 = pulse(index = 4, dvp = selected.dosage.preColloid * 2)
                        val p4 = pulse(index = 5, dvp = selected.dosage.preColloid * 2)

                        val ad = (2 * s * 100).toLong()
                        val s1 = (p1 / (maxOf(p2, p3, p4) / s) * 100).toLong()
                        val s2 = (s * 100).toLong()

                        with(index = 2, pdv = p1, ads = Triple(ad, ad, s1))
                        with(index = 3, pdv = p2, ads = Triple(ad, ad, s2))
                        with(index = 4, pdv = p3, ads = Triple(ad, ad, s2))
                        with(index = 5, pdv = p4, ads = Triple(ad, ad, s2))
                    }
                    start {
                        timeOut = 1000L * 60L
                        with(index = 0, pdv = selected.point.x)
                    }
                    start {
                        timeOut = 1000L * 60L
                        with(index = 1, pdv = selected.point.y)
                    }
                    glue {
                        timeOut = 1000L * 60 * 10
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

                        with(
                            index = 2,
                            pdv = p1,
                            ads = Triple(
                                (2.5 * s * 100).toLong(),
                                (2.5 * s * 100).toLong(),
                                (p1 / (maxOf(pv1, pv2, pv3) * 2 / s) * 100).toLong()
                            )
                        )

                        with(
                            index = 3,
                            pdv = pv1,
                            ads = Triple(
                                (s * 100).toLong(),
                                (s * s / 2 / pv1 * 100).toLong(),
                                (s * 100).toLong()
                            )
                        )

                        with(
                            index = 4,
                            pdv = pv2,
                            ads = Triple(
                                (s * 100).toLong(),
                                (s * s / 2 / pv2 * 100).toLong(),
                                (s * 100).toLong()
                            )
                        )

                        with(
                            index = 5,
                            pdv = pv3,
                            ads = Triple(
                                (s * 100).toLong(),
                                (s * s / 2 / pv2 * 100).toLong(),
                                (s * 100).toLong()
                            )
                        )

                        with(
                            index = 6,
                            pdv = p5,
                            ads = Triple(
                                (s * s / 2 / pv1 * 100).toLong(),
                                (s * 100).toLong(),
                                (s * 100).toLong()
                            )
                        )

                        with(
                            index = 7,
                            pdv = p6,
                            ads = Triple(
                                (s * s / 2 / pv2 * 100).toLong(),
                                (s * 100).toLong(),
                                (s * 100).toLong()
                            )
                        )

                        with(
                            index = 8,
                            pdv = p7,
                            ads = Triple(
                                (s * s / 2 / pv3 * 100).toLong(),
                                (s * 100).toLong(),
                                (s * 100).toLong()
                            )
                        )
                    }
                    _uiFlags.value = UiFlags.objects(1)
                    start {
                        timeOut = 1000L * 60L
                        with(index = 1, pdv = ordinate)
                    }
                    start {
                        timeOut = 1000L * 60L
                        with(index = 0, pdv = abscissa)
                    }
                    gpio(2)
                    delay(300L)
                    if (!getGpio(2)) {
                        valve(2 to 1)
                        delay(30L)
                        start {
                            timeOut = 1000L * 60
                            with(index = 2, pdv = Constants.ZT_0005 * -1)
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    _uiFlags.value = UiFlags.none()
                    _job.value?.cancel()
                    _job.value = null
                }
            }
        }
    }

    private fun stopJob() {
        viewModelScope.launch {
            _job.value?.cancel()
            _job.value = null
            delay(200L)
            stop(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            delay(200L)
            reset()
        }
    }

    private fun clean() {
        viewModelScope.launch {
            if (_uiFlags.value is UiFlags.Objects && (_uiFlags.value as UiFlags.Objects).objects == 2) {
                _uiFlags.value = UiFlags.none()
                stop(9)
            } else {
                _uiFlags.value = UiFlags.objects(2)
                start {
                    executeType = ExecuteType.ASYNC
                    with(index = 9, pdv = 3200L * 10000L)
                }
            }
        }
    }

    private fun syringe(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                syringeJob?.cancel()
                syringeJob = null
                stop(2)
                delay(100L)
                valve(2 to if (_uiFlags.value is UiFlags.Objects && (_uiFlags.value as UiFlags.Objects).objects == 3) 1 else 0)
                delay(30L)
                _uiFlags.value = UiFlags.objects(1)
                start {
                    timeOut = 1000L * 30
                    with(index = 2, pdv = Constants.ZT_0005 * -1)
                }
                _uiFlags.value = UiFlags.none()
            } else {
                _uiFlags.value = UiFlags.objects(2 + index)
                syringeJob = launch {
                    while (true) {
                        valve(2 to if (index == 1) 0 else 1)
                        delay(30L)
                        start {
                            timeOut = 1000L * 60
                            with(index = 2, pdv = Constants.ZT_0005)
                        }
                        valve(2 to if (index == 1) 1 else 0)
                        delay(30L)
                        start {
                            timeOut = 1000L * 60
                            with(index = 2, pdv = Constants.ZT_0005 * -1)
                        }
                    }
                }
            }
        }
    }

    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _uiFlags.value = UiFlags.none()
                stop(3, 4, 5, 6, 7, 8)
            } else {
                _uiFlags.value = UiFlags.objects(4 + index)
                start {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        with(index = it + 3, pdv = 3200L * 10000L * if (index == 1) 1 else -1)
                    }
                }
            }
        }
    }
}

sealed class HomeIntent {
    data class NavTo(val page: Int) : HomeIntent()
    data class Flags(val uiFlags: UiFlags) : HomeIntent()
    data class Pipeline(val index: Int) : HomeIntent()
    data class Syringe(val index: Int) : HomeIntent()
    data class Selected(val id: Long) : HomeIntent()
    data object Clean : HomeIntent()
    data object Reset : HomeIntent()
    data object Start : HomeIntent()
    data object Stop : HomeIntent()
}