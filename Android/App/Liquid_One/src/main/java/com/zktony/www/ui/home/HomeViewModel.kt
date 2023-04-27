package com.zktony.www.ui.home

import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.*
import com.zktony.datastore.ext.read
import com.zktony.serialport.protocol.V1
import com.zktony.www.common.ext.completeDialog
import com.zktony.www.common.ext.execute
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.Point
import com.zktony.www.room.entity.Program
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class HomeViewModel constructor(
    private val PD: PointDao,
    private val PGD: ProgramDao,
    private val SM: SerialManager,
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
                        wash = it
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
            if (_uiState.value.job == null) {
                SM.reset()
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.resetting))
            } else {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.stop_all))
            }
        }
    }

    fun waste() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            if (SM.lock.value) {
                PopTip.show("运动中")
            } else {
                execute {
                    step {
                        x = _uiState.value.wash.first
                        y = _uiState.value.wash.second
                    }
                }
            }
        }
    }

    fun wash(time: Int = 30, type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                SM.sendHex(hex = V1(pa = "0B", data = "0301").toHex())
            } else {
                SM.sendHex(hex = V1(pa = "0B", data = "0300").toHex())
            }
        }
    }

    fun fill(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                SM.sendHex(hex = V1(pa = "0B", data = "0301").toHex())
            } else {
                SM.sendHex(hex = V1(pa = "0B", data = "0300").toHex())
            }
        }
    }

    fun back(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                SM.sendHex(hex = V1(pa = "0B", data = "0302").toHex())
            } else {
                SM.sendHex(hex = V1(pa = "0B", data = "0300").toHex())
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
                            reset()
                            completeDialog(
                                name = _uiState.value.program?.name ?: "None",
                                time = _uiState.value.time.getTimeFormat(),
                            )
                            launch {
                                delay(500L)
                                while (SM.lock.value) {
                                    delay(100L)
                                }
                                waste()
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
        SM.pause(false)
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(pause = !_uiState.value.pause)
        SM.pause(_uiState.value.pause)
    }


}

data class HomeUiState(
    val wash: Pair<Float, Float> = Pair(0f, 0f),
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