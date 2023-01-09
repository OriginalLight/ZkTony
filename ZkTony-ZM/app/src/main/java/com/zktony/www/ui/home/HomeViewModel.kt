package com.zktony.www.ui.home

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.audio.AudioPlayer
import com.zktony.www.data.model.Program
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
    private val programRepository: ProgramRepository,
    private val logRecordRepository: LogRecordRepository,
    private val logDataRepository: LogDataRepository
) : BaseViewModel() {


    @Inject
    lateinit var appViewModel: AppViewModel

    private val _uiStateX = MutableStateFlow(HomeUiState())
    private val _uiStateY = MutableStateFlow(HomeUiState())
    private val _programList = MutableStateFlow<List<Program>>(emptyList())
    val uiStateX = _uiStateX.asStateFlow()
    val uiStateY = _uiStateY.asStateFlow()
    val programList = _programList.asStateFlow()


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
                programRepository.getAll().collect {
                    _programList.value = listOf(
                        Program(
                            name = "洗涤",
                            motor = 160,
                            time = 5f

                        )
                    ) + it
                    setCurrentProgram()
                }
            }
            launch {
                delay(100)
                appViewModel.received.collect {
                    _uiStateY.value = _uiStateX.value.copy(
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
            state.value = state.value.copy(model = model)
            setCurrentProgram()
        }
    }

    private fun setCurrentProgram() {
        val programList = programList.value
        val max1 = programList.filter { program -> program.model == 0 }
            .maxByOrNull { program1 -> program1.count }
        val max2 = programList.filter { program -> program.model == 1 }
            .maxByOrNull { program1 -> program1.count }
        val maxX = if (_uiStateX.value.model == 0) max1 else max2
        val maxY = if (_uiStateY.value.model == 0) max1 else max2
        _uiStateX.value = _uiStateX.value.copy(
            program = maxX,
            programName = maxX?.name ?: "",
            motor = maxX?.motor ?: 0,
            voltage = maxX?.voltage ?: 0f,
            time = maxX?.time ?: 0f,
        )
        _uiStateY.value = _uiStateY.value.copy(
            program = maxY,
            programName = maxY?.name ?: "",
            motor = maxY?.motor ?: 0,
            voltage = maxY?.voltage ?: 0f,
            time = maxY?.time ?: 0f,
        )
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
     * 更新程序
     * @param program [Program]
     */
    fun updateProgram(program: Program) {
        viewModelScope.launch {
            programRepository.update(program)
        }
    }

    /**
     * 播放音频
     */
    fun playAudio(id: Int) {
        viewModelScope.launch {
            if (appViewModel.setting.value.audio) {
                AudioPlayer.instance.play(id)
            }
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
            state.value = state.value.copy(job = null)
        }
        val job = viewModelScope.launch {
            delay(10000)
        }
        state.value = state.value.copy(job = job)
        job.invokeOnCompletion {
            state.value = state.value.copy(job = null)
        }
        job.start()
    }
}


data class HomeUiState(
    val job: Job? = null,
    val program: Program? = null,
    val model: Int = 0,
    val startEnable: Boolean = false,
    val programName: String = "",
    val motor: Int = 0,
    val voltage: Float = 0f,
    val time: Float = 0f,
    val currentStatus: Int = 0,
    val currentMotor: Int = 0,
    val currentVoltage: Float = 0f,
    val currentTime: Int = 0,
    val currentCurrent: Float = 0f,
    val motorCache: Int = 0,
)