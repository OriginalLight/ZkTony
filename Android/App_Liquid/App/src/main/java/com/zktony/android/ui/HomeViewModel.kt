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

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    private val _status = MutableStateFlow(0)
    private val _orificePlate = MutableStateFlow(OrificePlate())
    private val _finished = MutableStateFlow(emptyList<Triple<Int, Int, Color>>())

    private var job: Job? = null

    val selected = _selected.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val status = _status.asStateFlow()
    val orificePlate = _orificePlate.asStateFlow()
    val finished = _finished.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        reset()
    }

    fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.NavTo -> _page.value = intent.page
            is HomeIntent.Pause -> _status.value = 2
            is HomeIntent.Pipeline -> pipeline(intent.index)
            is HomeIntent.Reset -> reset()
            is HomeIntent.Resume -> _status.value = 1
            is HomeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is HomeIntent.Start -> start()
            is HomeIntent.Stop -> stop()
            is HomeIntent.Selected -> selected(intent.id)
        }
    }

    private fun reset() {
        viewModelScope.launch {
            try {
                _uiFlags.value = UiFlags.objects(1)
                withTimeout(30 * 1000L) {
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
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
        }
    }

    private fun start() {
        job = viewModelScope.launch {
            _status.value = 1
            val selected = dao.getById(_selected.value).firstOrNull()
            if (selected == null) {
                _uiFlags.value = UiFlags.message("不存在该程序，请检查！！！")
                _status.value = 0
                return@launch
            }
            if (selected.orificePlates.isEmpty()) {
                _uiFlags.value = UiFlags.message("该程序没有孔板，请检查！！！")
                _status.value = 0
                return@launch
            }

            val orificePlates = selected.orificePlates

            orificePlates.forEach { op ->
                delay(10)
                _orificePlate.value = op
                previousAlgorithm(op)

                if (op.type == 0) {
                    separationAlgorithm(op)
                } else {
                    hybridAlgorithm(op)
                }
            }
            delay(100L)
            _uiFlags.value = UiFlags.objects(4)
        }
        job?.invokeOnCompletion {
            _finished.value = emptyList()
            _status.value = 0
        }
    }

    private fun stop() {
        viewModelScope.launch {
            job?.cancel()
            stop(0, 1, 2, 3, 4, 5, 6, 7)
        }
    }

    private fun selected(id: Long) {
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
                _uiFlags.value = UiFlags.none()
                stop(2, 3, 4, 5, 6, 7)
            } else {
                _uiFlags.value = UiFlags.objects(index)
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
                while (_status.value == 2) {
                    delay(100)
                }

                // 移动到加液位置
                val coordinate = op.orifices[j][i * 6].point
                start {
                    with(index = 0, pdv = coordinate.x)
                    with(index = 1, pdv = coordinate.y)
                }

                delay(10L)
                while (_status.value == 2) {
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
                while (_status.value == 2) {
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
                while (_status.value == 2) {
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

sealed class HomeIntent {
    data class NavTo(val page: Int) : HomeIntent()
    data class Pipeline(val index: Int) : HomeIntent()
    data class Selected(val id: Long) : HomeIntent()
    data class Flags(val uiFlags: UiFlags) : HomeIntent()
    data object Pause : HomeIntent()
    data object Reset : HomeIntent()
    data object Resume : HomeIntent()
    data object Start : HomeIntent()
    data object Stop : HomeIntent()
}