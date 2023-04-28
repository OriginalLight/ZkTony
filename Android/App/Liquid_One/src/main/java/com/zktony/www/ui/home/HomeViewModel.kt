package com.zktony.www.ui.home

import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.*
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
    private val PD: PointDao,
    private val PGD: ProgramDao,
    private val DS: DataStore<Preferences>
) : BaseViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                delay(1000L)
                waste()
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
                    Pair(x, y)
                }.collect {
                    _uiState.value = _uiState.value.copy(
                        washTank = it
                    )
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
            // 如果有正在执行的程序，提示用户
            decideLock {
                yes { PopTip.show(Ext.ctx.getString(R.string.in_operation)) }
                no { syncHex { } }
            }
        }
    }

    fun waste() {
        viewModelScope.launch {
            execute {
                step {
                    x = _uiState.value.washTank.first
                    y = _uiState.value.washTank.second
                }
            }
        }
    }

    fun fill(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                asyncHex {
                    pa = "0B"
                    data = "0301"
                }
            } else {
                asyncHex {
                    pa = "0B"
                    data = "0300"
                }
            }
        }
    }

    fun back(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                asyncHex {
                    pa = "0B"
                    data = "0302"
                }
            } else {
                asyncHex {
                    pa = "0B"
                    data = "0300"
                }
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            val job = launch {
                launch {
                    while (true) {
                        delay(1000L)
                        if (!_uiState.value.pause) {
                            _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                        }
                    }
                }
                val executor = ProgramExecutor(
                    list = _uiState.value.pointList,
                    scope = this,
                )
                launch {
                    uiState.collect {
                        executor.pause = it.pause
                    }
                }
                executor.event = {
                    when (it) {

                        is ExecutorEvent.PointList -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    tripleList = it.hole
                                )
                            )
                        }

                        is ExecutorEvent.Progress -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    process = ((it.complete / it.total.toFloat()) * 100).toInt(),
                                )
                            )
                        }

                        is ExecutorEvent.Finish -> {
                            completeDialog(
                                name = _uiState.value.program?.name ?: "None",
                                time = _uiState.value.time.getTimeFormat(),
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
            _uiState.value = _uiState.value.copy(job = job)
        }
    }

    fun stop() {
        _uiState.value.job?.cancel()
        _uiState.value = _uiState.value.copy(
            job = null,
            time = 0L,
            info = _uiState.value.info.copy(
                process = 0,
                tripleList = emptyList()
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
)

data class CurrentInfo(
    val process: Int = 0,
    val size: Pair<Int, Int> = Pair(8, 12),
    val tripleList: List<Triple<Int, Int, Boolean>> = emptyList(),
)