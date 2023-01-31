package com.zktony.www.ui.home

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.MessageDialog
import com.zktony.www.R
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.audio.AudioPlayer
import com.zktony.www.common.extension.getTimeFormat
import com.zktony.www.common.room.entity.LogData
import com.zktony.www.common.room.entity.LogRecord
import com.zktony.www.common.room.entity.Program
import com.zktony.www.data.repository.LogDataRepository
import com.zktony.www.data.repository.LogRecordRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val programRepo: ProgramRepository,
    private val logRecordRepo: LogRecordRepository,
    private val logDataRepo: LogDataRepository
) : BaseViewModel() {


    @Inject
    lateinit var appViewModel: AppViewModel

    private val _uiStateX = MutableStateFlow(HomeUiState())
    private val _uiStateY = MutableStateFlow(HomeUiState())
    private val _programList = MutableStateFlow<List<Program>>(emptyList())
    val uiStateX = _uiStateX.asStateFlow()
    val uiStateY = _uiStateY.asStateFlow()
    val programList = _programList.asStateFlow()
    private var cleanJob: Job? = null
    private var sentinelJob: Job? = null
    private var first = true


    init {
        viewModelScope.launch {
            launch {
                delay(200L)
                while (true) {
                    delay(1000L)
                    SerialManager.instance.send(V1.QUERY_HEX)
                }
            }
            launch {
                programRepo.getAll().collect {
                    _programList.value = listOf(
                        Program(
                            name = "洗涤",
                            motor = 160,
                            time = 5f

                        )
                    ) + it
                    if (first) {
                        setCurrentProgram(3)
                        first = false
                    }
                }
            }
            launch {
                delay(100)
                appViewModel.received.collect {
                    _uiStateX.value = _uiStateX.value.copy(
                        currentStatus = it.inputSensorX,
                        currentMotor = it.stepMotorX,
                        currentVoltage = it.getVoltageX,
                        currentCurrent = it.getCurrentX
                    )
                    _uiStateY.value = _uiStateY.value.copy(
                        currentStatus = it.inputSensorY,
                        currentMotor = it.stepMotorY,
                        currentVoltage = it.getVoltageY,
                        currentCurrent = it.getCurrentY
                    )
                }
            }
        }
    }

    private fun getUiState(xy: Int): MutableStateFlow<HomeUiState> {
        return if (xy == 0) {
            _uiStateX
        } else {
            _uiStateY
        }
    }

    fun setModel(model: Int, xy: Int) {
        viewModelScope.launch {
            val state = getUiState(xy)
            state.value = state.value.copy(model = model, currentTime = "00:00")
            setCurrentProgram(xy)
        }
    }

    private fun setCurrentProgram(xy: Int) {
        val programList = programList.value
        val max1 = programList.filter { program -> program.model == 0 }
            .maxByOrNull { program1 -> program1.count }
        val max2 = programList.filter { program -> program.model == 1 }
            .maxByOrNull { program1 -> program1.count }
        val maxX = if (_uiStateX.value.model == 0) max1 else max2
        val maxY = if (_uiStateY.value.model == 0) max1 else max2
        if (xy == 0 || xy == 3) {
            _uiStateX.value = _uiStateX.value.copy(
                program = maxX,
                programName = maxX?.name ?: "",
                motor = maxX?.motor ?: 0,
                voltage = maxX?.voltage ?: 0f,
                time = maxX?.time ?: 0f,
            )
        }
        if (xy == 1 || xy == 3) {
            _uiStateY.value = _uiStateY.value.copy(
                program = maxY,
                programName = maxY?.name ?: "",
                motor = maxY?.motor ?: 0,
                voltage = maxY?.voltage ?: 0f,
                time = maxY?.time ?: 0f,
            )
        }
    }

    /**
     * @param motor 泵速度
     * @param xy 模块
     */
    fun setMotor(motor: Int, xy: Int) {
        val state = getUiState(xy)
        state.value = state.value.copy(motor = motor)
    }

    /**
     * @param voltage 电压
     * @param xy 模块
     */
    fun setVoltage(voltage: Float, xy: Int) {
        val state = getUiState(xy)
        state.value = state.value.copy(voltage = voltage)
    }

    /**
     * @param time 时间
     * @param xy 模块
     */
    fun setTime(time: Float, xy: Int) {
        val state = getUiState(xy)
        state.value = state.value.copy(time = time)
    }

    /**
     * 选择程序
     * @param program [Program]
     * @param xy 模块
     */
    fun selectProgram(program: Program?, xy: Int) {
        val state = getUiState(xy)
        state.value = state.value.copy(
            program = program,
            programName = program?.name ?: "",
            motor = program?.motor ?: 0,
            voltage = program?.voltage ?: 0f,
            time = program?.time ?: 0f,
        )
    }

    /**
     * 填充或者回吸
     * @param upOrBack 区分
     * @param start 开始/停止
     * @param xy 模块
     */
    fun pumpUpOrBack(upOrBack: Int, start: Int, xy: Int) {
        val latest = appViewModel.send.value
        var speed = appViewModel.setting.value.motorSpeed
        if (upOrBack == 1) speed = -speed
        if (xy == 0) {
            if (start == 0) {
                _uiStateX.value = _uiStateX.value.copy(motorCache = latest.stepMotorX)
            } else {
                speed = _uiStateX.value.motorCache
            }
            appViewModel.send(latest.apply { stepMotorX = speed })
        } else {
            if (start == 0) {
                _uiStateY.value = _uiStateY.value.copy(motorCache = latest.stepMotorY)
            } else {
                speed = _uiStateY.value.motorCache
            }
            appViewModel.send(latest.apply { stepMotorY = speed })
        }
    }

    /**
     * 程序开始
     *  @param xy 模块
     */
    fun start(xy: Int) {
        val state = getUiState(xy)
        if (state.value.job != null) {
            state.value.job?.cancel()
            state.value.log?.cancel()
            state.value = state.value.copy(job = null, log = null)
        }
        // 开始计时
        val job = viewModelScope.launch {
            if (state.value.programName == "洗涤") {
                val latest = appViewModel.send.value
                appViewModel.send(latest.apply {
                    stepMotorX = if (xy == 0) state.value.motor else latest.stepMotorX
                    stepMotorY = if (xy == 1) state.value.motor else latest.stepMotorY
                })
            } else {
                // 发送开始命令
                startOrStop(true, xy)
                if (state.value.model == 0) {
                    autoClean()
                }
                sentinel()
                updateProgram(xy)
            }
            for (i in (state.value.time * 60).toInt() downTo 0) {
                delay(1000)
                state.value = state.value.copy(currentTime = i.getTimeFormat())
                if (i == 20 && state.value.programName == "洗涤") {
                    val latest = appViewModel.send.value
                    appViewModel.send(latest.apply {
                        stepMotorX = if (xy == 0) -state.value.motor else latest.stepMotorX
                        stepMotorY = if (xy == 1) -state.value.motor else latest.stepMotorY
                    })
                }
                if (i == 0) {
                    stop(xy)
                    state.value = state.value.copy(currentTime = "已完成")
                    playAudio(R.raw.finish)
                }
            }
        }
        // 开始记录
        val log = viewModelScope.launch {
            if (state.value.programName != "洗涤") {
                val log = LogRecord().copy(
                    programId = state.value.program?.id ?: "None",
                    model = if (xy == 0) state.value.model else state.value.model + 2,
                    motor = state.value.motor,
                    voltage = state.value.voltage,
                    time = state.value.time,
                )
                logRecordRepo.insert(log)
                for (i in 0..(state.value.time * 60).toInt() step 5) {
                    val rec = appViewModel.received.value
                    logDataRepo.insert(
                        LogData().copy(
                            logId = log.id,
                            time = i,
                            motor = if (xy == 0) rec.stepMotorX else rec.stepMotorY,
                            voltage = if (xy == 0) rec.getVoltageX else rec.getVoltageY,
                            current = if (xy == 0) rec.getCurrentX else rec.getCurrentY,
                        )
                    )
                    delay(5000)
                }
            }
        }
        log.invokeOnCompletion {
            state.value = state.value.copy(log = null)
        }
        state.value = state.value.copy(job = job, log = log)
        job.start()
        log.start()

    }

    /**
     * 程序停止
     *  @param xy 模块
     */
    fun stop(xy: Int) {
        val state = getUiState(xy)
        state.value.job?.cancel()
        state.value.log?.cancel()
        state.value = state.value.copy(job = null, log = null, currentTime = "00:00")
        if (state.value.programName != "洗涤") {
            startOrStop(false, xy)
        } else {
            val latest = appViewModel.send.value
            appViewModel.send(latest.apply {
                stepMotorX = if (xy == 0) 0 else latest.stepMotorX
                stepMotorY = if (xy == 1) 0 else latest.stepMotorY
            })
        }
        if (_uiStateX.value.job == null && _uiStateY.value.job == null) {
            if (cleanJob != null) {
                cleanJob?.cancel()
                cleanJob = null
            }
            if (sentinelJob != null) {
                sentinelJob?.cancel()
                sentinelJob = null
            }
        } else if (_uiStateX.value.job != null && _uiStateY.value.job == null) {
            if (_uiStateX.value.model == 1 && cleanJob != null) {
                cleanJob?.cancel()
                cleanJob = null
            }
        } else if (_uiStateX.value.job == null && _uiStateY.value.job != null) {
            if (_uiStateY.value.model == 1 && cleanJob != null) {
                cleanJob?.cancel()
                cleanJob = null
            }
        }
    }

    /**
     * 更新时间
     * @param xy 模块
     */
    fun setCurrentTime(xy: Int) {
        val state = getUiState(xy)
        state.value = state.value.copy(currentTime = "00:00")
    }

    /**
     * 开始或停止命令发送
     * @param start 开始/停止
     * @param xy 模块
     */
    private fun startOrStop(start: Boolean, xy: Int) {
        val latest = appViewModel.send.value
        val state = getUiState(xy)
        if (start) {
            appViewModel.send(latest.apply {
                powerENX = if (xy == 0) 1 else latest.powerENX
                powerENY = if (xy == 1) 1 else latest.powerENY
                autoX = if (xy == 0) 1 else latest.autoX
                autoY = if (xy == 1) 1 else latest.autoY
                stepMotorX = if (xy == 0) state.value.motor else latest.stepMotorX
                stepMotorY = if (xy == 1) state.value.motor else latest.stepMotorY
                targetVoltageX = if (xy == 0) state.value.voltage else latest.targetVoltageX
                targetVoltageY = if (xy == 1) state.value.voltage else latest.targetVoltageY
            })
        } else {
            appViewModel.send(latest.apply {
                powerENX = if (xy == 0) 0 else latest.powerENX
                powerENY = if (xy == 1) 0 else latest.powerENY
                autoX = if (xy == 0) 0 else latest.autoX
                autoY = if (xy == 1) 0 else latest.autoY
                stepMotorX = if (xy == 0) 0 else latest.stepMotorX
                stepMotorY = if (xy == 1) 0 else latest.stepMotorY
                targetVoltageX = if (xy == 0) 0f else latest.targetVoltageX
                targetVoltageY = if (xy == 1) 0f else latest.targetVoltageY
            })
        }
    }

    /**
     * 设置哨兵
     */
    private fun sentinel() {
        if (sentinelJob == null && appViewModel.setting.value.detect) {
            val job = viewModelScope.launch {
                while (true) {
                    delay(5000)
                    val rec = appViewModel.received.value
                    var msg = ""
                    if (rec.powerENX == 1 && rec.powerENY == 0) {
                        if (rec.getCurrentX < 0.05f) {
                            stop(0)
                            playAudio(R.raw.error)
                            msg = "模块A异常，请检查！！！"
                        }
                    } else if (rec.powerENX == 0 && rec.powerENY == 1) {
                        if (rec.getCurrentY < 0.05f) {
                            stop(1)
                            playAudio(R.raw.error)
                            msg = "模块B异常，请检查！！！"
                        }
                    } else if (rec.powerENX == 1 && rec.powerENY == 1) {
                        if (rec.getCurrentX < 0.05f && rec.getCurrentY > 0.05f) {
                            stop(0)
                            playAudio(R.raw.error)
                            msg = "模块A异常，请检查！！！"
                        } else if (rec.getCurrentX > 0.05f && rec.getCurrentY < 0.05f) {
                            stop(1)
                            playAudio(R.raw.error)
                            msg = "模块B异常，请检查！！！"
                        } else if (rec.getCurrentX < 0.05f && rec.getCurrentY < 0.05f) {
                            stop(0)
                            stop(1)
                            playAudio(R.raw.error)
                            msg = "模块A、B异常，请检查！！！"
                        }
                    }
                    if (msg.isNotEmpty()) {
                        MessageDialog.build()
                            .setTitle("模块异常")
                            .setMessage(msg)
                            .setOkButton("确定") { dialog, _ ->
                                dialog.dismiss()
                                msg = ""
                                true
                            }
                            .show()
                    }
                }
            }
            sentinelJob = job
            job.start()
        }
    }

    /**
     * 设置自动清理
     */
    private fun autoClean() {
        if (cleanJob == null) {
            val job = viewModelScope.launch {
                val interval = appViewModel.setting.value.interval
                val duration = appViewModel.setting.value.duration
                while (true) {
                    delay(interval * 60 * 1000L)
                    viewModelScope.launch {
                        // 开启直流泵
                        appViewModel.send(appViewModel.send.value.apply {
                            motorX = 1
                            motorY = 1
                        })
                        delay(duration * 1000L)
                        // 关闭直流泵
                        appViewModel.send(appViewModel.send.value.apply {
                            motorX = 0
                            motorY = 0
                        })
                    }
                }
            }
            cleanJob = job
            job.start()
        }
    }

    /**
     * 更新程序
     * @param xy 模块
     */
    private fun updateProgram(xy: Int) {
        viewModelScope.launch {
            val state = getUiState(xy = xy)
            state.value.program?.let {
                programRepo.update(
                    it.copy(
                        count = it.count + 1,
                        upload = 0
                    )
                )
            }
        }
    }

    /**
     * 播放音频
     */
    private fun playAudio(id: Int) {
        viewModelScope.launch {
            if (appViewModel.setting.value.audio) {
                AudioPlayer.instance.play(id)
            }
        }
    }
}


data class HomeUiState(
    val job: Job? = null,
    val log: Job? = null,
    val program: Program? = null,
    val model: Int = 0,
    val programName: String = "",
    val motor: Int = 0,
    val voltage: Float = 0f,
    val time: Float = 0f,
    val currentStatus: Int = 0,
    val currentMotor: Int = 0,
    val currentVoltage: Float = 0f,
    val currentTime: String = "00:00",
    val currentCurrent: Float = 0f,
    val motorCache: Int = 0,
)