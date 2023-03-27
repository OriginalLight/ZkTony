package com.zktony.www.ui.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.dialog.spannerDialog
import com.zktony.common.ext.getTimeFormat
import com.zktony.serialport.util.Serial
import com.zktony.www.common.ext.completeDialog
import com.zktony.www.data.local.room.dao.*
import com.zktony.www.data.local.room.entity.*
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel constructor(
    private val logDao: LogDao,
    private val containerDao: ContainerDao,
    private val programDao: ProgramDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao,
    private val serialManager: SerialManager,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                programDao.getAll().collect {
                    if (it.isEmpty()) {
                        _uiState.value = _uiState.value.copy(programList = it, program = null)
                    } else {
                        _uiState.value = _uiState.value.copy(programList = it, program = it[0])
                        loadPlate(it[0].id)
                    }
                }
            }
            launch {
                containerDao.getById(1L).collect {
                    _uiState.value = _uiState.value.copy(container = it)
                }
            }
        }
    }

    private fun loadPlate(id: Long) {
        viewModelScope.launch {
            plateDao.getBySubId(id).collect {
                _uiState.value = _uiState.value.copy(plateList = it)
                var size = 10
                if (it.isNotEmpty()) {
                    size = it[0].size
                }
                _uiState.value = _uiState.value.copy(
                    info = _uiState.value.info.copy(
                        plateSize = size
                    )
                )
                loadHole(it.map { hole -> hole.id })
            }
        }
    }

    private fun loadHole(idList: List<Long>) {
        viewModelScope.launch {
            launch {
                holeDao.getBySudIdList(idList).collect {
                    _uiState.value = _uiState.value.copy(holeList = it)
                }
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
                loadPlate(uiState.value.programList[index].id)
            }
        )
    }

    fun reset() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            if (!serialManager.pause.value) {
                if (serialManager.lock.value) {
                    PopTip.show("运动中禁止复位")
                } else {
                    serialManager.reset()
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
                    container = _uiState.value.container!!,
                    plateList = _uiState.value.plateList,
                    holeList = _uiState.value.holeList,
                    scope = this,
                )
                serialManager.reset(false)
                executor.event = {
                    when (it) {
                        is ExecutorEvent.CurrentHole -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    hole = it.hole
                                )
                            )
                        }
                        is ExecutorEvent.HoleList -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    holeList = it.hole
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
                                speed = "${String.format("%.2f", _uiState.value.info.speed)} 孔/分钟",
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
                    plateSize = if (_uiState.value.plateList.isNotEmpty()) {
                        _uiState.value.plateList[0].size
                    } else {
                        10
                    },
                    holeList = emptyList(),
                    process = 0
                )
            )
            serialManager.pause(false)
            serialManager.sendHex(
                serial = Serial.TTYS0,
                hex = V1(pa = "10").toHex()
            )
            delay(1000L)
            serialManager.lock(false)
            while (serialManager.lock.value) {
                delay(100L)
            }
            reset()
        }
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(pause = !_uiState.value.pause)
        this.serialManager.pause(_uiState.value.pause)
    }

    private fun updateLog(log: Log) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(log = log)
            logDao.insert(log)
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
                serialManager.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                delay(100L)
                reset()
            } else {
                if (serialManager.reset.value) {
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
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0301").toHex()
                            )
                            delay(7000L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0305").toHex()
                            )
                            delay(6500L)
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
                serialManager.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                delay(100L)
                reset()
            } else {
                if (serialManager.reset.value) {
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
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0303").toHex()
                            )
                            delay(6500L)
                        } else {
                            _uiState.value = _uiState.value.copy(upOrDown = true)
                            serialManager.sendHex(
                                serial = Serial.TTYS3,
                                hex = V1(pa = "0B", data = "0305").toHex()
                            )
                            delay(6500L)
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
            serialManager.sendHex(
                serial = Serial.TTYS3,
                hex = V1(pa = "0B", data = "0401").toHex()
            )
        }
    }

    /**
     * 回吸胶体
     */
    fun recaptureColloid() {
        viewModelScope.launch {
            serialManager.sendHex(
                serial = Serial.TTYS3,
                hex = V1(pa = "0B", data = "0402").toHex()
            )
        }
    }

    /**
     * 停止填充和回吸
     */
    fun stopFillAndRecapture() {
        viewModelScope.launch {
            serialManager.sendHex(
                serial = Serial.TTYS3,
                hex = V1(pa = "0B", data = "0400").toHex()
            )
        }
    }
}

data class HomeUiState(
    val programList: List<Program> = emptyList(),
    val plateList: List<Plate> = emptyList(),
    val holeList: List<Hole> = emptyList(),
    val container: Container? = null,
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
    val plateSize: Int = 10,
    val hole: Hole = Hole(),
    val holeList: List<Pair<Int, Boolean>> = emptyList(),
    val speed: Float = 0f,
    val lastTime: Long = 0L,
    val process: Int = 0,
)