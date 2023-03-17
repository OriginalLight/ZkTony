package com.zktony.www.ui.home

import android.graphics.Color
import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.dialog.spannerDialog
import com.zktony.common.ext.getTimeFormat
import com.zktony.serialport.util.Serial
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.completeDialog
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.LogDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.dao.ProgramDao
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Log
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.data.local.room.entity.Program
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val programDao: ProgramDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao,
    private val logDao: LogDao
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val serial = SerialManager.instance

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                programDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(programList = it, program = it[0])
                        loadPlate(it[0].id)
                    }
                }
            }
        }
    }

    private fun loadPlate(id: Long) {
        viewModelScope.launch {
            launch {
                plateDao.getBySubId(id).collect {
                    _uiState.value = _uiState.value.copy(plateList = it)
                    var size: Pair<Int, Int> = Pair(8, 12)
                    if (it.isNotEmpty()) {
                        size = it[0].x to it[0].y
                    }
                    _uiState.value = _uiState.value.copy(
                        info = _uiState.value.info.copy(
                            plateSize = size
                        )
                    )
                    loadHole(it.map { it.id })
                }
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
            if (!serial.pause.value) {
                if (serial.lock.value) {
                    PopTip.show("运动中禁止复位")
                } else {
                    serial.reset()
                    PopTip.show("复位-已下发")
                }
            } else {
                PopTip.show("请中止所有运行中程序")
            }
        }
    }

    fun wash(time: Int = 30, type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                val washJob = launch {
                    serial.sendHex(
                        serial = Serial.TTYS0,
                        hex = V1(pa = "0B", data = "0301").toHex()
                    )
                    serial.sendHex(
                        serial = Serial.TTYS3,
                        hex = V1(pa = "0B", data = "0401").toHex()
                    )
                    delay(time * 1000L)
                    wash(type = 1)
                }
                _uiState.value = _uiState.value.copy(washJob = washJob)
                washJob.start()
            } else {
                _uiState.value.washJob?.cancel()
                _uiState.value = _uiState.value.copy(washJob = null)
                serial.sendHex(
                    serial = Serial.TTYS0,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                serial.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0400").toHex()
                )
            }
        }
    }

    fun fill(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                serial.sendHex(
                    serial = Serial.TTYS0,
                    hex = V1(pa = "0B", data = "0301").toHex()
                )
                serial.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0401").toHex()
                )
            } else {
                serial.sendHex(
                    serial = Serial.TTYS0,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                serial.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0400").toHex()
                )
            }
        }
    }

    fun suckBack(type: Int) {
        viewModelScope.launch {
            if (type == 0) {
                serial.sendHex(
                    serial = Serial.TTYS0,
                    hex = V1(pa = "0B", data = "0302").toHex()
                )
                serial.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0402").toHex()
                )
            } else {
                serial.sendHex(
                    serial = Serial.TTYS0,
                    hex = V1(pa = "0B", data = "0300").toHex()
                )
                serial.sendHex(
                    serial = Serial.TTYS3,
                    hex = V1(pa = "0B", data = "0400").toHex()
                )
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
                    updateLog(Log(workName = _uiState.value.program?.name ?: "未知程序"))
                }
                val executor = ProgramExecutor(
                    plateList = _uiState.value.plateList,
                    holeList = _uiState.value.holeList,
                    settings = appViewModel.settings.value,
                    scope = this,
                )
                executor.event = {
                    when (it) {
                        is ExecutorEvent.CurrentPlate -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    plate = when (it.plate.index) {
                                        0 -> "一号板"
                                        1 -> "二号板"
                                        2 -> "三号板"
                                        3 -> "四号板"
                                        else -> "未知板"
                                    },
                                    plateSize = it.plate.x to it.plate.y,
                                )
                            )
                        }
                        is ExecutorEvent.Liquid -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    liquid = when (it.liquid) {
                                        0 -> "一号泵"
                                        1 -> "二号泵"
                                        2 -> "三号泵"
                                        3 -> "四号泵"
                                        else -> "一号泵"
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
                                )
                            )

                        }
                        is ExecutorEvent.Log -> {
                            _uiState.value.log?.let { l ->
                                updateLog(l.copy(content = l.content + it.log))
                            }
                        }
                        is ExecutorEvent.Finish -> {
                            reset()
                            completeDialog(
                                name = _uiState.value.program?.name ?: "错误",
                                time = _uiState.value.time.getTimeFormat(),
                                speed = "${String.format("%.2f", _uiState.value.info.speed)} 孔/分钟",
                            )
                            launch {
                                _uiState.value.log?.let { l ->
                                    updateLog(l.copy(status = 1))
                                }
                                delay(1000L)
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
            log = null,
            time = 0L,
            info = CurrentInfo().copy(
                plateSize = if (_uiState.value.plateList.isNotEmpty()) {
                    _uiState.value.plateList[0].x to _uiState.value.plateList[0].y
                } else {
                    Pair(8, 12)
                }
            )
        )
        serial.pause(false)
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(pause = !_uiState.value.pause)
        serial.pause(_uiState.value.pause)
    }

    private fun updateLog(log: Log) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(log = log)
            logDao.insert(log)
        }
    }

}

data class HomeUiState(
    val programList: List<Program> = emptyList(),
    val plateList: List<Plate> = emptyList(),
    val holeList: List<Hole> = emptyList(),
    val log: Log? = null,
    val program: Program? = null,
    val job: Job? = null,
    val washJob: Job? = null,
    val pause: Boolean = false,
    val time: Long = 0L,
    val info: CurrentInfo = CurrentInfo(),
)

data class CurrentInfo(
    val plate: String = "/",
    val plateSize: Pair<Int, Int> = Pair(8, 12),
    val holeList: List<Triple<Int, Int, Boolean>> = emptyList(),
    val liquid: String = "/",
    val speed: Float = 0f,
    val lastTime: Long = 0L,
    val color: Int = Color.GREEN
)