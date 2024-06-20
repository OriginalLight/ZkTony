package com.zktony.android.ui

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Orifice
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

    private val _selected = MutableStateFlow<Program?>(null)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    private val _status = MutableStateFlow(0)

    private var job: Job? = null

    val selected = _selected.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val status = _status.asStateFlow()
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
            is HomeIntent.Selected -> _selected.value = intent.program
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
            val selected = _selected.value
            if (selected == null) {
                _uiFlags.value = UiFlags.message("不存在该程序，请检查！！！")
                _status.value = 0
                return@launch
            }
            delay(10)

            if (selected.orificePlates.isEmpty()) {
                _uiFlags.value = UiFlags.message("该程序没有孔板，请检查！！！")
                _status.value = 0
                return@launch
            }
            delay(10)

            if (selected.orificePlates.first().orifices.all { it.status == 0 }) {
                _uiFlags.value = UiFlags.message("请至少选择一个孔位进行加液！！！")
                _status.value = 0
                return@launch
            }
            delay(10)

            if (selected.orificePlates.first().getVolume().first() == 0.0) {
                _uiFlags.value = UiFlags.message("加液量为 0 ！！！")
                _status.value = 0
                return@launch
            }
            delay(10)

            var orificePlates = selected.orificePlates.first()

            orificePlates = orificePlates.copy(
                orifices = orificePlates.orifices.map {
                    if (it.status == 2) {
                        it.copy(status = 1)
                    } else {
                        it
                    }
                }
            )

            // 更新状态 status = 2 的孔位
            _selected.value = selected.copy(
                orificePlates = listOf(orificePlates)
            )

            previousAlgorithm(orificePlates)

            delay(10)

            separationAlgorithm(orificePlates)

            delay(10)
            _uiFlags.value = UiFlags.objects(4)
        }
        job?.invokeOnCompletion {
            _status.value = 0
        }
    }

    private fun stop() {
        viewModelScope.launch {
            job?.cancel()
            stop(0, 1, 2, 3, 4, 5, 6, 7)
            _status.value = 0
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
        var oes = op.orifices.toMutableList()
        for (i in 0 until ceil(row / 6.0).toInt()) {
            for (j in if (i % 2 == 0) 0 until column else column - 1 downTo 0) {
                // 检查该步是否有需要加液的
                var next = false
                repeat(6) { t ->
                    if (i * 6 + t < row) {
                        val orifice = op.orifices.find { it.row == (i * 6 + t) && it.column == j }
                        orifice?.let { o ->
                            val pvd = o.volume.getOrNull(0) ?: 0.0
                            if (pvd > 0.0) {
                                next = true
                            }
                        }
                    }
                }
                if (!next) continue
                Log.d("separationAlgorithm", "i = $i, j = $j")

                delay(10L)
                while (_status.value == 2) {
                    delay(100)
                }

                // 移动到加液位置
                val coordinate = op.orifices.find { it.row == i && it.column == j }?.point
                coordinate?.let {
                    start {
                        with(index = 0, pdv = it.x)
                        with(index = 1, pdv = it.y)
                    }
                }

                delay(10L)
                while (_status.value == 2) {
                    delay(100)
                }

                // 加液
                start {
                    timeOut = 1000L * 60
                    repeat(6) { t ->
                        if (i * 6 + t < row) {
                            val orifice = op.orifices.find { it.row == i * 6 + t && it.column == j }
                            orifice?.let { o ->
                                if (o.status > 0) {
                                    with(index = 2 + t, pdv = o.volume.getOrNull(0) ?: 0.0)
                                    // op.orifices
                                    oes = oes.map {
                                        if (it.row == o.row && it.column == o.column) {
                                            it.copy(status = 2)
                                        } else {
                                            it
                                        }
                                    }.toMutableList()
                                }
                            }
                        }
                    }
                }

                // 更新状态
                _selected.value = _selected.value?.copy(
                    orificePlates = _selected.value?.orificePlates?.map {
                        it.copy(orifices = oes)
                    } ?: emptyList()
                )

                // 延时
                delay((op.delay * 1000L).toLong())
            }
        }
    }

}

sealed class HomeIntent {
    data class NavTo(val page: Int) : HomeIntent()
    data class Pipeline(val index: Int) : HomeIntent()
    data class Selected(val program: Program) : HomeIntent()
    data class Flags(val uiFlags: UiFlags) : HomeIntent()
    data object Pause : HomeIntent()
    data object Reset : HomeIntent()
    data object Resume : HomeIntent()
    data object Start : HomeIntent()
    data object Stop : HomeIntent()
}