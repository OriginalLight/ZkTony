package com.zktony.www.ui.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.dialog.spannerDialog
import com.zktony.core.ext.getTimeFormat
import com.zktony.www.common.ext.completeDialog
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import com.zktony.www.room.dao.*
import com.zktony.www.room.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel constructor(
    private val LD: LogDao,
    private val PGD: ProgramDao,
    private val PD: PointDao,
    private val SM: SerialManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                PGD.getAll().collect {
                    if (it.isEmpty()) {
                        _uiState.value = _uiState.value.copy(programList = it, program = null)
                    } else {
                        _uiState.value = _uiState.value.copy(programList = it, program = it[0])
                        loadPoint(it[0].id)
                    }
                }
            }
        }
    }

    private fun loadPoint(id: Long) {
        viewModelScope.launch {
            PD.getBySubId(id).collect {
                _uiState.value = _uiState.value.copy(pointList = it)
            }
        }
    }


    fun select(view: View) {
        val list = uiState.value.programList.map { it.name }
        if (_uiState.value.job != null) {
            PopTip.show("请先停止当前程序")
            return
        }
        if (list.isEmpty()) {
            PopTip.show("请先添加程序")
            return
        }
        spannerDialog(
            view = view,
            menu = list,
            block = { _, index ->
                _uiState.value = _uiState.value.copy(program = uiState.value.programList[index])
                loadPoint(uiState.value.programList[index].id)
            }
        )
    }

    fun reset() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            if (_uiState.value.job == null) {
                if (SM.lock.value) {
                    PopTip.show("运动中禁止复位")
                } else {
                    SM.reset()
                    PopTip.show("复位-已下发")
                }
            } else {
                PopTip.show("请中止所有运行中程序")
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
                launch {
                    updateLog(Log(name = _uiState.value.program?.name ?: "未知程序"))
                }
                val executor = ProgramExecutor(
                    list = _uiState.value.pointList,
                    scope = this,
                )
                SM.reset(false)
                executor.event = {
                    when (it) {
                        is ExecutorEvent.CurrentPoint -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    point = it.point
                                )
                            )
                        }

                        is ExecutorEvent.FinishList -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    pairs = it.list
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

                        is ExecutorEvent.Log -> {
                            _uiState.value.log?.let { l ->
                                updateLog(l.copy(content = l.content + it.log))
                            }
                        }

                        is ExecutorEvent.Finish -> {
                            completeDialog(
                                name = _uiState.value.program?.name ?: "错误",
                                time = _uiState.value.time.getTimeFormat(),
                                speed = "${
                                    String.format(
                                        "%.2f",
                                        _uiState.value.info.speed
                                    )
                                } 孔/分钟",
                            )
                            launch {
                                _uiState.value.log?.let { l ->
                                    updateLog(l.copy(status = 1))
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
        viewModelScope.launch {
            _uiState.value.job?.cancel()
            _uiState.value = _uiState.value.copy(
                job = null,
                log = null,
                time = 0L,
                info = CurrentInfo().copy(
                    pairs = emptyList(),
                    process = 0
                )
            )
            SM.pause(false)
            SM.sendHex(
                index = 0,
                hex = V1(pa = "10").toHex()
            )
            delay(1000L)
            SM.lock(false)
            while (SM.lock.value) {
                delay(100L)
            }
            reset()
        }
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(pause = !_uiState.value.pause)
        this.SM.pause(_uiState.value.pause)
    }

    private fun updateLog(log: Log) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(log = log)
            LD.insert(log)
        }
    }

    /**
     * 填充促凝剂
     */
    fun fillCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.fillCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    fillCoagulant = false,
                )
                SM.sendHex(
                    index = 3,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                delay(100L)
                reset()
            } else {
                if (SM.reset.value) {
                    if (_uiState.value.recaptureCoagulant) {
                        PopTip.show("请先停止回吸")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        upOrDown = true,
                        fillCoagulant = true,
                    )
                    delay(100L)
                    while (_uiState.value.fillCoagulant) {
                        if (_uiState.value.upOrDown) {
                            _uiState.value = _uiState.value.copy(upOrDown = false)
                            SM.sendHex(
                                index = 3,
                                hex = V1(pa = "0B", data = "0301").toHex()
                            )
                            delay(8500L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            SM.sendHex(
                                index = 3,
                                hex = V1(pa = "0B", data = "0305").toHex()
                            )
                            delay(9000L)
                        }
                    }

                } else {
                    PopTip.show("请先复位")
                }
            }
        }
    }

    /**
     * 回吸促凝剂
     */
    fun recaptureCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.recaptureCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    recaptureCoagulant = false,
                )
                SM.sendHex(
                    index = 3,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                delay(100L)
                reset()
            } else {
                if (SM.reset.value) {
                    if (_uiState.value.fillCoagulant) {
                        PopTip.show("请先停止填充")
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        upOrDown = true,
                        recaptureCoagulant = true,
                    )
                    delay(100L)
                    while (_uiState.value.recaptureCoagulant) {
                        if (_uiState.value.upOrDown) {
                            _uiState.value = _uiState.value.copy(upOrDown = false)
                            SM.sendHex(
                                index = 3,
                                hex = V1(pa = "0B", data = "0303").toHex()
                            )
                            delay(8500L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            SM.sendHex(
                                index = 3,
                                hex = V1(pa = "0B", data = "0304").toHex()
                            )
                            delay(9000L)
                        }
                    }

                } else {
                    PopTip.show("请先复位")
                }
            }
        }
    }

    /**
     * 填充胶体
     */
    fun fillColloid() {
        viewModelScope.launch {
            SM.sendHex(
                index = 3,
                hex = V1(pa = "0B", data = "0401").toHex()
            )
        }
    }

    /**
     * 回吸胶体
     */
    fun recaptureColloid() {
        viewModelScope.launch {
            SM.sendHex(
                index = 3,
                hex = V1(pa = "0B", data = "0402").toHex()
            )
        }
    }

    /**
     * 停止填充和回吸
     */
    fun stopFillAndRecapture() {
        viewModelScope.launch {
            SM.sendHex(
                index = 3,
                hex = V1(pa = "0B", data = "0400").toHex()
            )
        }
    }
}

data class HomeUiState(
    val programList: List<Program> = emptyList(),
    val pointList: List<Point> = emptyList(),
    val log: Log? = null,
    val program: Program? = null,
    val job: Job? = null,
    val pause: Boolean = false,
    val time: Long = 0L,
    val info: CurrentInfo = CurrentInfo(),
    val fillCoagulant: Boolean = false,
    val recaptureCoagulant: Boolean = false,
    val upOrDown: Boolean = true,
)

data class CurrentInfo(
    val point: Point = Point(),
    val pairs: List<Pair<Int, Boolean>> = emptyList(),
    val speed: Float = 0f,
    val lastTime: Long = 0L,
    val process: Int = 0,
)