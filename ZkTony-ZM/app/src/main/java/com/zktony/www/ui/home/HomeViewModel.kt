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
                    _programList.value = it
                    setCurrentProgram()
                }
            }
        }
    }

    fun setModel(model: Int, xy: Int) {
        viewModelScope.launch {
            if (xy == 0) {
                _uiStateX.value = _uiStateX.value.copy(model = model)
            } else {
                _uiStateY.value = _uiStateY.value.copy(model = model)
            }
            setCurrentProgram()
        }
    }

    private fun setCurrentProgram() {
        val programList = programList.value
        if (programList.isEmpty()) {
            _uiStateX.value = _uiStateX.value.copy(
                program = null,
                programName = "",
                motor = 0,
                voltage = 0f,
                time = 0f,
            )
            _uiStateY.value = _uiStateY.value.copy(
                program = null,
                programName = "",
                motor = 0,
                voltage = 0f,
                time = 0f,
            )
        } else {
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
}


data class HomeUiState(
    val program: Program? = null,
    val model: Int = 0,
    val isRun: Boolean = false,
    val startEnable: Boolean = false,
    val programName: String = "",
    val motor: Int = 0,
    val voltage: Float = 0f,
    val time: Float = 0f,
    val currentStatus: Int = 0,
    val currentMotor: Int = 0,
    val currentVoltage: Float = 0f,
    val currentTime: Float = 0f,
    val currentCurrent: Float = 0f,
)