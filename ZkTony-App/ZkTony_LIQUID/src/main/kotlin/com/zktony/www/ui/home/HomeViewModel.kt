package com.zktony.www.ui.home

import android.util.Log
import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.serialport.util.Serial
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.spannerDialog
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.control.serial.protocol.V1
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.data.local.room.entity.Work
import com.zktony.www.data.local.room.entity.WorkPlate
import com.zktony.www.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workRepository: WorkRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val serial = SerialManager.instance

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workRepository.getAllWork().collect {
                if (it.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(workList = it, work = it[0])
                    loadPlate(it[0].id)
                }
            }
        }
    }

    private fun loadPlate(id: String) {
        viewModelScope.launch {
            launch {
                workRepository.getWorkPlateByWorkId(id).collect {
                    _uiState.value = _uiState.value.copy(plateList = it)
                    var size: Pair<Int, Int> = Pair(8, 12)
                    if (it.isNotEmpty()) {
                        size = it[0].row to it[0].column
                    }
                    _uiState.value = _uiState.value.copy(
                        info = _uiState.value.info.copy(
                            plateSize = size
                        )
                    )
                }
            }
            launch {
                workRepository.getHoleByWorkId(id).collect {
                    _uiState.value = _uiState.value.copy(holeList = it)
                }
            }
        }
    }

    fun selectWork(view: View) {
        val list = uiState.value.workList.map { it.name }
        spannerDialog(
            view = view,
            menu = list,
            block = { _, index ->
                _uiState.value = _uiState.value.copy(work = uiState.value.workList[index])
                loadPlate(uiState.value.workList[index].id)
            }
        )
    }

    fun reset() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            if (!serial.work.value) {
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
                        if (!_uiState.value.suspend) {
                            _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                        }
                    }
                }
                val executor = WorkExecutor(
                    plateList = _uiState.value.plateList,
                    holeList = _uiState.value.holeList,
                    settings = appViewModel.settings.value,
                )
                executor.callBack = {
                    when (it) {
                        is ExecutorEvent.Plate -> {
                            Log.d("WorkViewModel", "plate: ${it.plate}")
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    plate = when(it.plate.sort) {
                                        0 -> "一号板"
                                        1 -> "二号板"
                                        2 -> "三号板"
                                        3 -> "四号板"
                                        else -> "未知板"
                                    },
                                    plateSize = it.plate.row to it.plate.column,
                                )
                            )
                        }
                        is ExecutorEvent.Liquid -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    liquid = it.liquid
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
                            val lastTime = time / it.progress - time
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    speed = it.progress,
                                    lastTime = lastTime.toLong(),
                                )
                            )

                        }
                        is ExecutorEvent.Finish -> {
                            stop()
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
            info = CurrentInfo().copy(
                plateSize = if (_uiState.value.plateList.isNotEmpty()) {
                    _uiState.value.plateList[0].row to _uiState.value.plateList[0].column
                } else {
                    Pair(8, 12)
                },
            )
        )
    }

    fun suspend() {
        _uiState.value = _uiState.value.copy(suspend = !_uiState.value.suspend)
    }

}

data class HomeUiState(
    val workList: List<Work> = emptyList(),
    val plateList: List<WorkPlate> = emptyList(),
    val holeList: List<Hole> = emptyList(),
    val work: Work? = null,
    val job: Job? = null,
    val washJob: Job? = null,
    val plate: Plate? = null,
    val holes: List<Hole>? = null,
    val suspend: Boolean = false,
    val time: Long = 0L,
    val info: CurrentInfo = CurrentInfo(),
)

data class CurrentInfo(
    val plate: String = "/",
    val plateSize: Pair<Int, Int> = Pair(8, 12),
    val holeList: List<Hole> = emptyList(),
    val liquid: String = "/",
    val speed: Float = 0f,
    val lastTime: Long = 0L,
)