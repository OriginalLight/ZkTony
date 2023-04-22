package com.zktony.www.ui.home

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.getTimeFormat
import com.zktony.core.ext.playAudio
import com.zktony.core.utils.Constants
import com.zktony.core.utils.Constants.ERROR_CURRENT
import com.zktony.core.utils.Constants.MAX_MOTOR
import com.zktony.core.utils.Constants.MAX_TIME
import com.zktony.core.utils.Constants.MAX_VOLTAGE_RS
import com.zktony.core.utils.Constants.MAX_VOLTAGE_ZM
import com.zktony.datastore.ext.read
import com.zktony.www.R
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.dao.LogDataDao
import com.zktony.www.room.dao.LogRecordDao
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.LogData
import com.zktony.www.room.entity.LogRecord
import com.zktony.www.room.entity.Program
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel constructor(
    private val PD: ProgramDao,
    private val LRD: LogRecordDao,
    private val LDD: LogDataDao,
    private val SM: SerialManager,
    private val DS: DataStore<Preferences>
) : BaseViewModel() {

    private val _uiStateX = MutableStateFlow(HomeUiState())
    private val _uiStateY = MutableStateFlow(HomeUiState())
    private val _setting = MutableStateFlow(Setting())
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
                PD.getAll().collect {
                    _programList.value = listOf(
                        Program(
                            name = Ext.ctx.getString(R.string.wash),
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
                SM.received.collect {
                    _uiStateX.value = _uiStateX.value.copy(
                        currentMotor = it.stepMotorX,
                        currentVoltage = it.getVoltageX,
                        currentCurrent = it.getCurrentX
                    )
                    _uiStateY.value = _uiStateY.value.copy(
                        currentMotor = it.stepMotorY,
                        currentVoltage = it.getVoltageY,
                        currentCurrent = it.getCurrentY
                    )
                }
            }
            launch {
                launch {
                    DS.read(Constants.AUDIO, true).collect {
                        _setting.value = _setting.value.copy(audio = it)
                    }
                }
                launch {
                    DS.read(Constants.DETECT, true).collect {
                        _setting.value = _setting.value.copy(detect = it)
                    }
                }
                launch {
                    DS.read(Constants.INTERVAL, 1).collect {
                        _setting.value = _setting.value.copy(interval = it)
                    }
                }
                launch {
                    DS.read(Constants.DURATION, 10).collect {
                        _setting.value = _setting.value.copy(duration = it)
                    }
                }
                launch {
                    DS.read(Constants.MOTOR_SPEED, 160).collect {
                        _setting.value = _setting.value.copy(motorSpeed = it)
                    }
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
    fun setMotor(motor: Int, xy: Int, block: () -> Unit) {
        val state = getUiState(xy)
        state.value = state.value.copy(motor = minOf(motor, MAX_MOTOR))
        if (motor > MAX_MOTOR) {
            block()
        }
    }

    /**
     * @param voltage 电压
     * @param xy 模块
     */
    fun setVoltage(voltage: Float, xy: Int, block: (Float) -> Unit) {
        val state = getUiState(xy)
        val max = when (state.value.model) {
            0 -> MAX_VOLTAGE_ZM
            1 -> MAX_VOLTAGE_RS
            else -> MAX_VOLTAGE_ZM
        }
        state.value = state.value.copy(voltage = minOf(voltage, max))
        if (voltage > max) {
            block(max)
        }
    }

    /**
     * @param time 时间
     * @param xy 模块
     */
    fun setTime(time: Float, xy: Int, block: () -> Unit) {
        val state = getUiState(xy)
        state.value = state.value.copy(time = minOf(time, MAX_TIME))
        if (time > MAX_TIME) {
            block()
        }
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
        val latest = SM.send.value
        var speed = _setting.value.motorSpeed
        if (upOrBack == 1) speed = -speed
        if (xy == 0) {
            if (start == 0) {
                _uiStateX.value = _uiStateX.value.copy(motorCache = latest.stepMotorX)
            } else {
                speed = _uiStateX.value.motorCache
            }
            SM.send(latest.apply { stepMotorX = speed })
        } else {
            if (start == 0) {
                _uiStateY.value = _uiStateY.value.copy(motorCache = latest.stepMotorY)
            } else {
                speed = _uiStateY.value.motorCache
            }
            SM.send(latest.apply { stepMotorY = speed })
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
            if (state.value.programName == Ext.ctx.getString(R.string.wash)) {
                val latest = SM.send.value
                SM.send(latest.apply {
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
                if (i == 20 && state.value.programName == Ext.ctx.getString(R.string.wash)) {
                    val latest = SM.send.value
                    SM.send(latest.apply {
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
            if (state.value.programName != Ext.ctx.getString(R.string.wash)) {
                val log = LogRecord().copy(
                    programId = state.value.program?.id ?: "None",
                    model = if (xy == 0) state.value.model else state.value.model + 2,
                    motor = state.value.motor,
                    voltage = state.value.voltage,
                    time = state.value.time,
                )
                LRD.insert(log)
                for (i in 0..(state.value.time * 60).toInt() step 5) {
                    val rec = SM.received.value
                    LDD.insert(
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
        if (state.value.programName != Ext.ctx.getString(R.string.wash)) {
            startOrStop(false, xy)
        } else {
            val latest = SM.send.value
            SM.send(latest.apply {
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
     * 开始或停止命令发送
     * @param start 开始/停止
     * @param xy 模块
     */
    private fun startOrStop(start: Boolean, xy: Int) {
        val latest = SM.send.value
        val state = getUiState(xy)
        if (start) {
            SM.send(latest.apply {
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
            SM.send(latest.apply {
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
        if (sentinelJob == null && _setting.value.detect) {
            val job = viewModelScope.launch {
                while (true) {
                    delay(5000L)
                    val rec = SM.received.value
                    var msg = ""
                    if (rec.powerENX == 1 && rec.powerENY == 0) {
                        if (rec.getCurrentX < ERROR_CURRENT) {
                            stop(0)
                            playAudio(R.raw.error)
                            msg = Ext.ctx.getString(R.string.module_a_exception)
                        }
                    } else if (rec.powerENX == 0 && rec.powerENY == 1) {
                        if (rec.getCurrentY < ERROR_CURRENT) {
                            stop(1)
                            playAudio(R.raw.error)
                            msg = Ext.ctx.getString(R.string.module_b_exception)
                        }
                    } else if (rec.powerENX == 1 && rec.powerENY == 1) {
                        if (rec.getCurrentX < ERROR_CURRENT && rec.getCurrentY > ERROR_CURRENT) {
                            stop(0)
                            playAudio(R.raw.error)
                            msg = Ext.ctx.getString(R.string.module_a_exception)
                        } else if (rec.getCurrentX > ERROR_CURRENT && rec.getCurrentY < ERROR_CURRENT) {
                            stop(1)
                            playAudio(R.raw.error)
                            msg = Ext.ctx.getString(R.string.module_b_exception)
                        } else if (rec.getCurrentX < ERROR_CURRENT && rec.getCurrentY < ERROR_CURRENT) {
                            stop(0)
                            delay(300L)
                            stop(1)
                            playAudio(R.raw.error)
                            msg = Ext.ctx.getString(R.string.module_a_b_exception)
                        }
                    }
                    if (msg.isNotEmpty()) {
                        CustomDialog.build()
                            .setCustomView(object :
                                OnBindView<CustomDialog>(R.layout.layout_error) {
                                override fun onBind(dialog: CustomDialog, v: View) {
                                    val tvTitle =
                                        v.findViewById<TextView>(com.zktony.core.R.id.title)
                                    val tvMessage =
                                        v.findViewById<TextView>(com.zktony.core.R.id.message)
                                    val btnOk =
                                        v.findViewById<MaterialButton>(com.zktony.core.R.id.ok)
                                    tvTitle.text = Ext.ctx.getString(R.string.module_exception)
                                    tvMessage.text = msg
                                    btnOk.clickNoRepeat {
                                        dialog.dismiss()
                                    }
                                }
                            })
                            .setCancelable(false)
                            .setMaskColor(Color.parseColor("#4D000000"))
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
                val interval = _setting.value.interval
                val duration = _setting.value.duration
                while (true) {
                    delay(interval * 60 * 1000L)
                    viewModelScope.launch {
                        // 开启直流泵
                        SM.send(SM.send.value.apply {
                            motorX = 1
                            motorY = 1
                        })
                        delay(duration * 1000L)
                        // 关闭直流泵
                        SM.send(SM.send.value.apply {
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
                PD.update(
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
            if (_setting.value.audio) {
                Ext.ctx.playAudio(id)
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
    val currentMotor: Int = 0,
    val currentVoltage: Float = 0f,
    val currentTime: String = "00:00",
    val currentCurrent: Float = 0f,
    val motorCache: Int = 0,
)

data class Setting(
    val audio: Boolean = true,
    val bar: Boolean = false,
    val detect: Boolean = true,
    val duration: Int = 10,
    val interval: Int = 1,
    val motorSpeed: Int = 160,
)