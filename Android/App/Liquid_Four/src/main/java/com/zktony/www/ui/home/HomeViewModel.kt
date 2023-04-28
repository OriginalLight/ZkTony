package com.zktony.www.ui.home

import android.graphics.Color
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.*
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import com.zktony.www.R
import com.zktony.www.common.ext.*
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.Point
import com.zktony.www.room.entity.Program
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class HomeViewModel constructor(
    private val DS: DataStore<Preferences>,
    private val PD: PointDao,
    private val PGD: ProgramDao,
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                delay(1000L)
                waitLock { waste() }
            }
            launch {
                PGD.getAll().collect {
                    if (it.isEmpty()) {
                        _uiState.value = _uiState.value.copy(programList = it, program = null)
                    } else {
                        _uiState.value = _uiState.value.copy(programList = it, program = it[0])
                        loadPlate(it[0].id)
                    }
                }
            }
            launch {
                DS.read("WASH_X_AXIS", 0f).zip(
                    DS.read("WASH_Y_AXIS", 0f)
                ) { x, y ->
                    x to y
                }.collect {
                    _uiState.value = _uiState.value.copy(washTank = it)
                }
            }
            launch {
                DS.read(Constants.NEEDLE_SPACE, 12f).collect {
                    _uiState.value = _uiState.value.copy(needleSpace = it)
                }
            }
        }
    }

    private fun loadPlate(id: Long) {
        viewModelScope.launch {
            PD.getBySubId(id).collect {
                _uiState.value = _uiState.value.copy(pointList = it)
                var size: Pair<Int, Int> = Pair(8, 12)
                if (it.isNotEmpty()) {
                    val minIndex = it.minOf { point -> point.index }
                    val x = it.filter { point -> point.index == minIndex }
                        .maxOf { point -> point.x } + 1
                    val y = it.filter { point -> point.index == minIndex }
                        .maxOf { point -> point.y } + 1
                    size = x to y
                }
                _uiState.value = _uiState.value.copy(
                    info = _uiState.value.info.copy(
                        size = size
                    )
                )
            }
        }
    }


    fun select(view: View) {
        val list = uiState.value.programList.map { it.name }
        if (_uiState.value.job != null || list.isEmpty()) {
            return
        }
        spannerDialog(
            view = view,
            menu = list,
            block = { _, index ->
                _uiState.value = _uiState.value.copy(program = uiState.value.programList[index])
                loadPlate(uiState.value.programList[index].id)
            }
        )
    }

    fun reset() {
        viewModelScope.launch {
            decideLock {
                yes { PopTip.show(com.zktony.core.R.string.running) }
                no { asyncHex(0) {} }
            }
        }
    }

    fun waste() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            execute {
                step {
                    x = _uiState.value.washTank.first
                    y = _uiState.value.washTank.second
                }
            }
        }
    }

    fun wash(time: Int = 30, type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                val washJob = launch {
                    asyncHex(0) {
                        pa = "0B"
                        data = "0301"
                    }
                    asyncHex(3) {
                        pa = "0B"
                        data = "0401"
                    }
                    delay(time * 1000L)
                    wash(type = 1)
                }
                _uiState.value = _uiState.value.copy(washJob = washJob)
                washJob.start()
            } else {
                _uiState.value.washJob?.cancel()
                _uiState.value = _uiState.value.copy(washJob = null)
                asyncHex(0) {
                    pa = "0B"
                    data = "0300"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0400"
                }
            }
        }
    }

    fun fill(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                asyncHex(0) {
                    pa = "0B"
                    data = "0301"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0401"
                }
            } else {
                asyncHex(0) {
                    pa = "0B"
                    data = "0300"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0400"
                }
            }
        }
    }

    fun suckBack(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                asyncHex(0) {
                    pa = "0B"
                    data = "0302"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0402"
                }
            } else {
                asyncHex(0) {
                    pa = "0B"
                    data = "0300"
                }
                asyncHex(3) {
                    pa = "0B"
                    data = "0400"
                }
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            val job = launch(start = CoroutineStart.LAZY) {
                launch {
                    while (true) {
                        delay(1000L)
                        if (!_uiState.value.pause) {
                            _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                            val lastTime = _uiState.value.info.lastTime
                            if (lastTime > 0) {
                                _uiState.value = _uiState.value.copy(
                                    info = _uiState.value.info.copy(
                                        lastTime = lastTime - 1
                                    )
                                )
                            }
                        }
                    }
                }
                val executor = ProgramExecutor(
                    list = _uiState.value.pointList,
                    space = _uiState.value.needleSpace,
                    scope = this,
                )
                launch {
                    uiState.collect {
                        executor.pause = it.pause
                    }
                }
                executor.event = {
                    when (it) {
                        is ExecutorEvent.CurrentContainer -> {
                            val maxX = _uiState.value.pointList.filter { point ->
                                point.index == it.index
                            }.maxOf { point -> point.x } + 1
                            val maxY = _uiState.value.pointList.filter { point ->
                                point.index == it.index
                            }.maxOf { point -> point.y } + 1
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    index = when (it.index) {
                                        0 -> Ext.ctx.getString(R.string.plate_one)
                                        1 -> Ext.ctx.getString(R.string.plate_two)
                                        2 -> Ext.ctx.getString(R.string.plate_three)
                                        3 -> Ext.ctx.getString(R.string.plate_four)
                                        else -> "None"
                                    },
                                    size = maxX to maxY,
                                )
                            )
                        }

                        is ExecutorEvent.Liquid -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    liquid = when (it.liquid) {
                                        0 -> Ext.ctx.getString(R.string.pump_one)
                                        1 -> Ext.ctx.getString(R.string.pump_two)
                                        2 -> Ext.ctx.getString(R.string.pump_three)
                                        3 -> Ext.ctx.getString(R.string.pump_four)
                                        else -> "None"
                                    },
                                    color = when (it.liquid) {
                                        0 -> Color.BLUE
                                        1 -> Color.CYAN
                                        2 -> Color.YELLOW
                                        3 -> Color.GREEN
                                        else -> Color.RED
                                    }
                                )
                            )
                        }

                        is ExecutorEvent.PointList -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    tripleList = it.hole
                                )
                            )
                        }

                        is ExecutorEvent.Progress -> {
                            val time = _uiState.value.time + 1
                            val percent = it.complete.toFloat() / it.total.toFloat()
                            val lastTime = time.toFloat() / percent - time.toFloat()
                            val speed = it.complete / time.toFloat() * 60
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    speed = speed,
                                    lastTime = lastTime.toLong(),
                                    process = ((it.complete / it.total.toFloat()) * 100).toInt(),
                                )
                            )

                        }

                        is ExecutorEvent.Finish -> {
                            completeDialog(
                                name = _uiState.value.program?.name ?: "None",
                                time = _uiState.value.time.getTimeFormat(),
                                speed = String.format(
                                    "%.2f",
                                    _uiState.value.info.speed
                                ),
                            )
                            launch {
                                delay(500L)
                                waitLock {
                                    waste()
                                }
                                delay(500L)
                                stop()
                            }
                        }
                    }
                }
                executor.execute()
            }
            job.start()
            _uiState.value = _uiState.value.copy(job = job)
        }
    }

    fun stop() {
        _uiState.value.job?.cancel()
        _uiState.value = _uiState.value.copy(
            job = null,
            time = 0L,
            info = _uiState.value.info.copy(
                process = 0
            )
        )
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(pause = !_uiState.value.pause)
    }

}

data class HomeUiState(
    val washTank: Pair<Float, Float> = Pair(0f, 0f),
    val info: CurrentInfo = CurrentInfo(),
    val job: Job? = null,
    val pause: Boolean = false,
    val pointList: List<Point> = emptyList(),
    val program: Program? = null,
    val programList: List<Program> = emptyList(),
    val time: Long = 0L,
    val washJob: Job? = null,
    val needleSpace: Float = 12f,
)

data class CurrentInfo(
    val color: Int = Color.GREEN,
    val index: String = "/",
    val lastTime: Long = 0L,
    val liquid: String = "/",
    val process: Int = 0,
    val size: Pair<Int, Int> = Pair(8, 12),
    val speed: Float = 0f,
    val tripleList: List<Triple<Int, Int, Boolean>> = emptyList(),
)