package com.zktony.www.ui.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.R
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.extractTemp
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.toCommand
import com.zktony.www.common.model.Queue
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Log
import com.zktony.www.common.room.entity.Program
import com.zktony.www.common.room.entity.getActionEnum
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.repository.ActionRepository
import com.zktony.www.data.repository.LogRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.serialport.SerialPort.SERIAL_FOUR
import com.zktony.www.serialport.SerialPort.SERIAL_ONE
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.serialport.protocol.Command
import com.zktony.www.ui.home.ModuleEnum.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val programRepo: ProgramRepository,
    private val actionRepo: ActionRepository,
    private val logRepo: LogRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val serial = SerialPortManager.instance

    private val _programList = MutableStateFlow<List<Program>>(emptyList())
    private val _stateOne = MutableStateFlow(UiState())
    private val _stateTwo = MutableStateFlow(UiState())
    private val _stateThree = MutableStateFlow(UiState())
    private val _stateFour = MutableStateFlow(UiState())
    private val _stateButton = MutableStateFlow(ButtonState())
    val programList = _programList.asStateFlow()
    val stateOne = _stateOne.asStateFlow()
    val stateTwo = _stateTwo.asStateFlow()
    val stateThree = _stateThree.asStateFlow()
    val stateFour = _stateFour.asStateFlow()
    val stateButton = _stateButton.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                programRepo.getAll().collect {
                    _programList.value = it
                    programListFlow(it)
                }
            }
            launch {
                // 串口一flow
                serial.serialOneFlow.collect {
                    it?.let {
                        val command = it.toCommand()
                        when (command.function) {
                            "86" -> {
                                if (command.parameter == "0A") {
                                    if (command.data == "00") {
                                        PopTip.show("复位成功")
                                    } else {
                                        PopTip.show("复位失败")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            launch {
                // 串口四flow
                serial.serialFourFlow.collect {
                    it?.let {
                        if (it.startsWith("TC1:TCACTUALTEMP=")) {
                            // 读取温度
                            val address = it.substring(it.length - 2, it.length - 1).toInt()
                            val temp = it.extractTemp()
                            when (address) {
                                1 -> _stateOne.value = _stateOne.value.copy(tempText = "$temp ℃")
                                2 -> _stateTwo.value = _stateTwo.value.copy(tempText = "$temp ℃")
                                3 -> _stateThree.value = _stateThree.value.copy(tempText = "$temp ℃")
                                4 -> _stateFour.value = _stateFour.value.copy(tempText = "$temp ℃")
                                0 -> _stateButton.value =
                                    _stateButton.value.copy(insulatingTemp = "$temp ℃")
                            }
                        }
                    }
                }
            }
            launch {
                // 设置和定时查询温控
                for (i in 0..4) {
                    delay(200L)
                    serial.sendText(
                        SERIAL_FOUR, Command.saveTemperature(i.toString(), "26")
                    )
                    delay(200L)
                    serial.sendText(
                        SERIAL_FOUR, Command.setTemperature(i.toString(), "26")
                    )
                }
                // 每一分钟查询一次温度
                while (true) {
                    for (i in 0..4) {
                        delay(200L)
                        serial.sendText(
                            SERIAL_FOUR, Command.queryTemperature(i.toString())
                        )
                    }
                    delay(10 * 1000L)
                }
            }
        }
    }

    /**
     * 获取对应模块的状态
     * @param module 模块
     * @return 状态
     */
    private fun stateSelector(module: ModuleEnum) = when (module) {
        A -> _stateOne
        B -> _stateTwo
        C -> _stateThree
        D -> _stateFour
    }

    /**
     * 程序发生变化添加删除时候更新选中的index
     * @param programList [List]<[Program]> 程序列表
     */
    private fun programListFlow(programList: List<Program>) {
        if (programList.isNotEmpty()) {
            listOf(_stateOne, _stateTwo, _stateThree, _stateFour).forEach { state ->
                if (state.value.program == null) {
                    state.value = state.value.copy(
                        program = programList[0],
                        btnStartEnable = programList[0].actionCount > 0,
                    )
                } else {
                    if (programList.find { it.id == state.value.program!!.id } == null) {
                        state.value = state.value.copy(
                            program = programList[0],
                            btnStartEnable = programList[0].actionCount > 0,
                        )
                    } else {
                        state.value = state.value.copy(
                            program = programList.find { it.id == state.value.program!!.id },
                            btnStartEnable = programList.find { it.id == state.value.program!!.id }!!.actionCount > 0
                        )
                    }
                }
            }
        } else {
            _stateOne.value = _stateOne.value.copy(program = null, btnStartEnable = false)
            _stateTwo.value = _stateTwo.value.copy(program = null, btnStartEnable = false)
            _stateThree.value = _stateThree.value.copy(program = null, btnStartEnable = false)
            _stateFour.value = _stateFour.value.copy(program = null, btnStartEnable = false)
        }
    }

    /**
     * 切换程序
     * @param index [Int] 程序索引
     * @param module [ModuleEnum] 模块
     */
    fun switchProgram(index: Int, module: ModuleEnum) {
        viewModelScope.launch {
            val state = stateSelector(module)
            state.value = state.value.copy(
                program = _programList.value[index],
                btnStartEnable = _programList.value[index].actionCount > 0,
                runtimeText = "已就绪",
                countDownText = Constants.ZERO_TIME,
            )
        }
    }

    /**
     * 开始执行程序
     * @param module [ModuleEnum] 模块
     */
    fun start(module: ModuleEnum) {
        viewModelScope.launch {
            val state = stateSelector(module)
            state.value = state.value.copy(
                btnStartVisible = View.GONE,
                btnStopVisible = View.VISIBLE,
                btnSelectorEnable = false,
                runtimeText = "运行中",
            )
            val job = viewModelScope.launch {
                val actionQueue = Queue<Action>()
                actionRepo.getByProgramId(state.value.program!!.id).first().forEach {
                    actionQueue.enqueue(it)
                }
                val runner = ProgramExecutor.Builder()
                    .setModule(module)
                    .setActionQueue(actionQueue)
                    .setSettingState(appViewModel.settings.value)
                    .build()
                launch {
                    runner.event.collect {
                        when (it) {
                            is ActionEvent.CurrentAction -> {
                                state.value =
                                    state.value.copy(currentActionText = getActionEnum(it.action.mode).value)
                            }
                            is ActionEvent.CurrentActionTime -> {
                                state.value = state.value.copy(countDownText = it.time)
                            }
                            is ActionEvent.Finish -> {
                                state.value.log?.let { log ->
                                    logRepo.update(log.copy(status = 1))
                                }
                                state.value = state.value.copy(
                                    runtimeText = "已完成", countDownText = "已完成", log = null
                                )
                                stop(it.module)
                            }
                            is ActionEvent.Count -> {
                                state.value = state.value.copy(
                                    currentActionText = _stateOne.value.currentActionText.substring(
                                        0, 2
                                    ) + " X${it.count}"
                                )
                            }
                            is ActionEvent.Wait -> {
                                state.value = state.value.copy(countDownText = it.msg)
                            }
                        }
                    }
                }
                runner.run()
            }
            viewModelScope.launch {
                state.value.job?.cancel()
                val log = Log(
                    programName = state.value.program!!.name,
                    module = module.index,
                    actions = state.value.program!!.actions,
                )
                logRepo.insert(log)
                state.value = state.value.copy(job = job, log = log)
                programRepo.update(
                    state.value.program!!.copy(
                        runCount = state.value.program!!.runCount + 1
                    )
                )
            }
        }
    }

    /**
     * 停止执行程序
     * @param module [ModuleEnum] 模块
     */
    fun stop(module: ModuleEnum) {
        viewModelScope.launch {
            val state = stateSelector(module)
            state.value.run {
                job?.cancel()
                log?.let { log ->
                    logRepo.delete(log)
                }
            }
            state.value = state.value.copy(
                job = null,
                btnStartVisible = View.VISIBLE,
                btnStopVisible = View.GONE,
                btnSelectorEnable = true,
                runtimeText = if (state.value.runtimeText != "已完成") "已就绪" else state.value.runtimeText,
                currentActionText = "/",
                countDownText = if (state.value.runtimeText != "已完成") Constants.ZERO_TIME else state.value.countDownText,
            )
            delay(200L)
            if (serial.getExecuting() == 0) {
                serial.sendHex(SERIAL_ONE, Command.pauseShakeBed())
            }
        }
    }

    /**
     * 机构复位
     */
    fun reset() {
        if (serial.getExecuting() == 0) {
            serial.sendHex(
                SERIAL_ONE,
                Command(function = "05", parameter = "01", data = "0101302C302C302C302C").toHex()
            )
            serial.sendHex(SERIAL_ONE, Command().toHex())
            PopTip.show(R.mipmap.ic_reset, "复位-已下发")
        } else {
            PopTip.show("请中止所有运行中程序")
        }
    }

    /**
     * 摇床暂停
     */
    fun pauseShakeBed() {
        viewModelScope.launch {
            serial.sendHex(
                SERIAL_ONE,
                if (_stateButton.value.pauseEnable) Command.resumeShakeBed() else Command.pauseShakeBed()
            )
            _stateButton.value =
                _stateButton.value.copy(pauseEnable = !_stateButton.value.pauseEnable)
        }
    }

    /**
     * 抗体保温
     */
    fun insulating() {
        viewModelScope.launch {
            val temp = appViewModel.settings.value.temp.toString().removeZero()
            serial.sendText(
                SERIAL_FOUR, Command.setTemperature(
                    address = "0",
                    temperature = if (_stateButton.value.insulatingEnable) "26" else temp
                )
            )
            _stateButton.value =
                _stateButton.value.copy(insulatingEnable = !_stateButton.value.insulatingEnable)
        }
    }
}

data class UiState(
    val job: Job? = null,
    val program: Program? = null,
    val log: Log? = null,
    val btnSelectorEnable: Boolean = true,
    val btnStartEnable: Boolean = false,
    val btnStartVisible: Int = View.VISIBLE,
    val btnStopVisible: Int = View.GONE,
    val countDownText: String = Constants.ZERO_TIME,
    val runtimeText: String = "已就绪",
    val currentActionText: String = "/",
    val tempText: String = "0.0℃",
)

data class ButtonState(
    val pauseEnable: Boolean = false,
    val insulatingEnable: Boolean = false,
    val insulatingTemp: String = "0.0℃",
)

enum class ModuleEnum(val value: String, val index: Int) {
    A("模块A", 0), B("模块B", 1), C("模块C", 2), D("模块D", 3),
}

fun getModuleFromIndex(index: Int): ModuleEnum {
    return when (index) {
        0 -> A
        1 -> B
        2 -> C
        3 -> D
        else -> throw IllegalArgumentException("index must be 0-3")
    }
}
