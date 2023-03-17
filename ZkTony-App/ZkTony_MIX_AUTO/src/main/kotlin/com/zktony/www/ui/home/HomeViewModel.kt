package com.zktony.www.ui.home

import android.graphics.Color
import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.dialog.spannerDialog
import com.zktony.serialport.util.Serial
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.data.local.room.dao.HoleDao
import com.zktony.www.data.local.room.dao.LogDao
import com.zktony.www.data.local.room.dao.PlateDao
import com.zktony.www.data.local.room.dao.ProgramDao
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Log
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.data.local.room.entity.Program
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logDao: LogDao,
    private val programDao: ProgramDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao,
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
                    if (it.isEmpty()) {
                        _uiState.value = _uiState.value.copy(programList = it, program = null)
                    } else {
                        _uiState.value = _uiState.value.copy(programList = it, program = it[0])
                        loadPlate(it[0].id)
                    }
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
                    size = it[0].x
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

    }

    fun stop() {
        _uiState.value.job?.cancel()
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
    val plateSize: Int = 10,
    val holeList: List<Pair<Int, Boolean>> = emptyList(),
    val liquid: String = "/",
    val speed: Float = 0f,
    val lastTime: Long = 0L,
    val color: Int = Color.GREEN
)