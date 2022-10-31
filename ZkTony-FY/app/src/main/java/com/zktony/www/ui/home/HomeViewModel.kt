package com.zktony.www.ui.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.zktony.www.R
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppState
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.extractTemp
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.toCommand
import com.zktony.www.common.model.Queue
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Program
import com.zktony.www.data.repository.ActionRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.serialport.protocol.Command
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val actionRepository: ActionRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    // 返回的状态
    private val _state = MutableSharedFlow<HomeState>()
    val state: SharedFlow<HomeState> get() = _state

    // 保存的UI状态
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> get() = _uiState

    init {
        viewModelScope.launch {
            launch {
                programRepository.getAll().collect {
                    val uiState = uiState.value
                    uiState.run {
                        onProgramChange(it)
                        _state.emit(HomeState.OnLoadProgram(this))
                    }
                }
            }
            launch {
                appViewModel.state.collect {
                    when (it) {
                        is AppState.ReceiverSerialOne -> receiverSerialOne(it.command)
                        is AppState.ReceiverSerialFour -> receiverSerialFour(it.command)
                        else -> {}
                    }
                }
            }
            launch {
                // 设置和定时查询温控
                for (i in 0..4) {
                    delay(200L)
                    appViewModel.senderText(
                        SerialPortEnum.SERIAL_FOUR,
                        Command.saveTemperature(i.toString(), "26")
                    )
                    delay(200L)
                    appViewModel.senderText(
                        SerialPortEnum.SERIAL_FOUR,
                        Command.setTemperature(i.toString(), "26")
                    )
                }
                // 每一分钟查询一次温度
                while (true) {
                    for (i in 0..3) {
                        delay(200L)
                        appViewModel.senderText(
                            SerialPortEnum.SERIAL_FOUR,
                            Command.queryTemperature(i.toString())
                        )
                    }
                    delay(60 * 1000L)
                }
            }
        }
    }

    /**
     * 切换程序
     * @param index [Int] 程序索引
     * @param module [ModuleEnum] 模块
     */
    fun onSwitchProgram(index: Int, module: ModuleEnum) {
        viewModelScope.launch {
            when (module) {
                ModuleEnum.A -> {
                    _uiState.value = _uiState.value.copy(
                        moduleA = _uiState.value.moduleA.copy(
                            index = index,
                            btnStart = _uiState.value.moduleA.btnStart.copy(
                                enable = _uiState.value.programList[index].actionCount > 0,
                            ),
                        )
                    )
                }
                ModuleEnum.B -> {
                    _uiState.value = _uiState.value.copy(
                        moduleB = _uiState.value.moduleB.copy(
                            index = index,
                            btnStart = _uiState.value.moduleB.btnStart.copy(
                                enable = _uiState.value.programList[index].actionCount > 0,
                            ),
                        )
                    )
                }
                ModuleEnum.C -> {
                    _uiState.value = _uiState.value.copy(
                        moduleC = _uiState.value.moduleC.copy(
                            index = index,
                            btnStart = _uiState.value.moduleC.btnStart.copy(
                                enable = _uiState.value.programList[index].actionCount > 0,
                            ),
                        )
                    )
                }
                ModuleEnum.D -> {
                    _uiState.value = _uiState.value.copy(
                        moduleD = _uiState.value.moduleD.copy(
                            index = index,
                            btnStart = _uiState.value.moduleD.btnStart.copy(
                                enable = _uiState.value.programList[index].actionCount > 0,
                            ),
                        )
                    )
                }
            }
            _state.emit(HomeState.OnSwitchProgram(index, module))
            _state.emit(HomeState.OnButtonChange(module))
        }
    }

    /**
     * 开始执行程序
     * @param module [ModuleEnum] 模块
     */
    fun start(module: ModuleEnum) {
        viewModelScope.launch {
            when (module) {
                ModuleEnum.A -> {
                    _uiState.value = _uiState.value.copy(
                        moduleA = _uiState.value.moduleA.copy(
                            isRunning = true,
                            btnStart = _uiState.value.moduleA.btnStart.copy(
                                visibility = View.GONE,
                            ),
                            btnStop = _uiState.value.moduleA.btnStop.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnProgram = _uiState.value.moduleA.btnProgram.copy(
                                isClickable = false,
                            ),
                        )
                    )
                    runProgram(module, _uiState.value.programList[_uiState.value.moduleA.index])
                }
                ModuleEnum.B -> {
                    _uiState.value = _uiState.value.copy(
                        moduleB = _uiState.value.moduleB.copy(
                            isRunning = true,
                            btnStart = _uiState.value.moduleB.btnStart.copy(
                                visibility = View.GONE,
                            ),
                            btnStop = _uiState.value.moduleB.btnStop.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnProgram = _uiState.value.moduleB.btnProgram.copy(
                                isClickable = false,
                            ),
                        )
                    )
                    runProgram(module, _uiState.value.programList[_uiState.value.moduleB.index])
                }
                ModuleEnum.C -> {
                    _uiState.value = _uiState.value.copy(
                        moduleC = _uiState.value.moduleC.copy(
                            isRunning = true,
                            btnStart = _uiState.value.moduleC.btnStart.copy(
                                visibility = View.GONE,
                            ),
                            btnStop = _uiState.value.moduleC.btnStop.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnProgram = _uiState.value.moduleC.btnProgram.copy(
                                isClickable = false,
                            ),
                        )
                    )
                    runProgram(module, _uiState.value.programList[_uiState.value.moduleC.index])
                }
                ModuleEnum.D -> {
                    _uiState.value = _uiState.value.copy(
                        moduleD = _uiState.value.moduleD.copy(
                            isRunning = true,
                            btnStart = _uiState.value.moduleD.btnStart.copy(
                                visibility = View.GONE,
                            ),
                            btnStop = _uiState.value.moduleD.btnStop.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnProgram = _uiState.value.moduleD.btnProgram.copy(
                                isClickable = false,
                            ),
                        )
                    )
                    runProgram(module, _uiState.value.programList[_uiState.value.moduleD.index])
                }
            }
            _state.emit(HomeState.OnButtonChange(module))
        }
    }

    /**
     * 停止执行程序
     * @param module [ModuleEnum] 模块
     */
    fun stop(module: ModuleEnum) {
        viewModelScope.launch {
            when (module) {
                ModuleEnum.A -> {
                    _uiState.value = _uiState.value.copy(
                        moduleA = _uiState.value.moduleA.copy(
                            isRunning = false,
                            btnStart = _uiState.value.moduleA.btnStart.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnStop = _uiState.value.moduleA.btnStop.copy(
                                visibility = View.GONE,
                            ),
                            btnProgram = _uiState.value.moduleA.btnProgram.copy(
                                isClickable = true,
                            ),
                        )
                    )
                }
                ModuleEnum.B -> {
                    _uiState.value = _uiState.value.copy(
                        moduleB = _uiState.value.moduleB.copy(
                            isRunning = false,
                            btnStart = _uiState.value.moduleB.btnStart.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnStop = _uiState.value.moduleB.btnStop.copy(
                                visibility = View.GONE,
                            ),
                            btnProgram = _uiState.value.moduleB.btnProgram.copy(
                                isClickable = true,
                            ),
                        )
                    )
                }
                ModuleEnum.C -> {
                    _uiState.value = _uiState.value.copy(
                        moduleC = _uiState.value.moduleC.copy(
                            isRunning = false,
                            btnStart = _uiState.value.moduleC.btnStart.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnStop = _uiState.value.moduleC.btnStop.copy(
                                visibility = View.GONE,
                            ),
                            btnProgram = _uiState.value.moduleC.btnProgram.copy(
                                isClickable = true,
                            ),
                        )
                    )
                }
                ModuleEnum.D -> {
                    _uiState.value = _uiState.value.copy(
                        moduleD = _uiState.value.moduleD.copy(
                            isRunning = false,
                            btnStart = _uiState.value.moduleD.btnStart.copy(
                                visibility = View.VISIBLE,
                            ),
                            btnStop = _uiState.value.moduleD.btnStop.copy(
                                visibility = View.GONE,
                            ),
                            btnProgram = _uiState.value.moduleD.btnProgram.copy(
                                isClickable = true,
                            ),
                        )
                    )
                }
            }
            stopProgram(module)
            _state.emit(HomeState.OnButtonChange(module))
        }
    }

    /**
     * 复位
     */
    fun reset() {
        SerialPortManager.instance.commandQueue.clear()
        appViewModel.sender(
            SerialPortEnum.SERIAL_ONE,
            Command(function = "05", parameter = "01", data = "0101302C302C302C302C").toHex()
        )
        appViewModel.sender(SerialPortEnum.SERIAL_ONE, Command().toHex())
        stopProgram(ModuleEnum.A)
        stopProgram(ModuleEnum.B)
        stopProgram(ModuleEnum.C)
        stopProgram(ModuleEnum.D)
    }

    /**
     * 摇床暂停
     */
    fun pause() {
        viewModelScope.launch {
            appViewModel.sender(
                SerialPortEnum.SERIAL_ONE,
                Command(
                    parameter = "0B",
                    data = if (_uiState.value.btnPause.isRunning) "0101" else "0100"
                ).toHex()
            )
            _uiState.value = _uiState.value.copy(
                btnPause = _uiState.value.btnPause.copy(
                    text = if (_uiState.value.btnPause.isRunning) "暂停摇床" else "继  续",
                    background = if (_uiState.value.btnPause.isRunning) R.mipmap.btn_pause else R.mipmap.btn_continue,
                    textColor = if (_uiState.value.btnPause.isRunning) R.color.dark_outline else R.color.red,
                    isRunning = !_uiState.value.btnPause.isRunning,
                )
            )
            _state.emit(HomeState.OnPause)
        }
    }

    /**
     * 抗体保温
     */
    fun insulating() {
        viewModelScope.launch {
            val temp = appViewModel.settingState.value.temp.toString().removeZero()
            appViewModel.senderText(
                SerialPortEnum.SERIAL_FOUR,
                Command.setTemperature(
                    address = "4",
                    temperature = if (_uiState.value.btnInsulating.isRunning) "26" else temp
                )
            )
            _uiState.value = _uiState.value.copy(
                btnInsulating = _uiState.value.btnInsulating.copy(
                    text = if (_uiState.value.btnInsulating.isRunning) "抗体保温" else "保温中 $temp℃",
                    textColor = if (_uiState.value.btnInsulating.isRunning) R.color.dark_outline else R.color.red,
                    isRunning = !_uiState.value.btnInsulating.isRunning,
                )
            )
            _state.emit(HomeState.OnInsulating)
        }
    }

    /**
     * 串口一数据接收
     * @param hex [String] 数据
     */
    private fun receiverSerialOne(hex: String) {
        val command = hex.toCommand()
        when (command.function) {
            "86" -> resetCallBack(command)
        }
    }

    /**
     * 串口四数据接收
     * @param hex [String] 数据
     */
    private fun receiverSerialFour(hex: String) {
        viewModelScope.launch {
            // 读取温度
            val address = hex.last().toString().toInt()
            val temp = hex.extractTemp()
            when (address) {
                0 -> {
                    _uiState.value = _uiState.value.copy(
                        dashBoardA = _uiState.value.dashBoardA.copy(
                            temperature = "$temp ℃",
                        )
                    )
                }
                1 -> {
                    _uiState.value = _uiState.value.copy(
                        dashBoardB = _uiState.value.dashBoardB.copy(
                            temperature = "$temp ℃",
                        )
                    )
                }
                2 -> {
                    _uiState.value = _uiState.value.copy(
                        dashBoardC = _uiState.value.dashBoardC.copy(
                            temperature = "$temp ℃",
                        )
                    )
                }
                3 -> {
                    _uiState.value = _uiState.value.copy(
                        dashBoardD = _uiState.value.dashBoardD.copy(
                            temperature = "$temp ℃"
                        )
                    )
                }
            }
            _state.emit(HomeState.OnDashBoardChange(getModuleEnum(address)))
        }
    }


    /**
     * 复位反馈
     * @param command [Command] 命令
     */
    private fun resetCallBack(command: Command) {
        viewModelScope.launch {
            command.run {
                _state.emit(HomeState.OnRestCallBack(parameter == "0A" && data == "00"))
            }
        }
    }

    /**
     * 程序发生变化添加删除时候更新选中的index
     * @param programList [List]<[Program]> 程序列表
     */
    private fun onProgramChange(programList: List<Program>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                programList = programList,
                moduleA = _uiState.value.moduleA.copy(
                    index = if (programList.isEmpty()) -1 else if (_uiState.value.moduleA.index >= programList.size) 0 else _uiState.value.moduleA.index,
                    btnStart = _uiState.value.moduleA.btnStart.copy(
                        enable = if (programList.isEmpty()) false else programList[_uiState.value.moduleA.index].actionCount > 0
                    )
                ),
                moduleB = _uiState.value.moduleB.copy(
                    index = if (programList.isEmpty()) -1 else if (_uiState.value.moduleB.index >= programList.size) 0 else _uiState.value.moduleB.index,
                    btnStart = _uiState.value.moduleB.btnStart.copy(
                        enable = if (programList.isEmpty()) false else programList[_uiState.value.moduleB.index].actionCount > 0
                    )
                ),
                moduleC = _uiState.value.moduleC.copy(
                    index = if (programList.isEmpty()) -1 else if (_uiState.value.moduleC.index >= programList.size) 0 else _uiState.value.moduleC.index,
                    btnStart = _uiState.value.moduleC.btnStart.copy(
                        enable = if (programList.isEmpty()) false else programList[_uiState.value.moduleC.index].actionCount > 0
                    )
                ),
                moduleD = _uiState.value.moduleD.copy(
                    index = if (programList.isEmpty()) -1 else if (_uiState.value.moduleD.index >= programList.size) 0 else _uiState.value.moduleD.index,
                    btnStart = _uiState.value.moduleD.btnStart.copy(
                        enable = if (programList.isEmpty()) false else programList[_uiState.value.moduleD.index].actionCount > 0
                    )
                ),
            )
        }
    }

    /**
     * 运行程序
     * @param module [ModuleEnum] 模块
     * @param program [Program] 程序
     */
    private fun runProgram(module: ModuleEnum, program: Program) {
        val job = viewModelScope.launch {
            val actionQueue = Queue<Action>()
            actionRepository.getByProgramId(program.id).first().forEach {
                actionQueue.enqueue(it)
            }
            val runner = ActionActuator.Builder().setModule(module).setActionQueue(actionQueue)
                .setSettingState(appViewModel.settingState.value).build()
            programStateCollector(runner)
            runner.run()
        }
        setJobWhenProgramStart(module, job)
    }

    /**
     * 停止程序
     * @param module [ModuleEnum] 模块
     */
    private fun stopProgram(module: ModuleEnum) {
        clearJobWhenProgramStop(module)
    }

    /**
     * 设置Job当程序启动时
     * @param module [ModuleEnum] 模块
     * @param job [Job] Job
     */
    private fun setJobWhenProgramStart(module: ModuleEnum, job: Job) {
        viewModelScope.launch {
            when (module) {
                ModuleEnum.A -> {
                    uiState.value.moduleA.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleA = _uiState.value.moduleA.copy(
                            job = job
                        )
                    )
                }
                ModuleEnum.B -> {
                    uiState.value.moduleB.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleB = _uiState.value.moduleB.copy(
                            job = job
                        )
                    )
                }
                ModuleEnum.C -> {
                    uiState.value.moduleC.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleC = _uiState.value.moduleC.copy(
                            job = job
                        )
                    )
                }
                ModuleEnum.D -> {
                    uiState.value.moduleD.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleD = _uiState.value.moduleD.copy(
                            job = job
                        )
                    )
                }
            }
            _state.emit(HomeState.OnDashBoardChange(module))
        }
    }

    /**
     * 清除Job当程序停止时
     * @param module [ModuleEnum] 模块
     */
    private fun clearJobWhenProgramStop(module: ModuleEnum) {
        viewModelScope.launch {
            when (module) {
                ModuleEnum.A -> {
                    uiState.value.moduleA.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleA = _uiState.value.moduleA.copy(
                            job = null,
                            isRunning = false
                        ),
                        dashBoardA = DashBoardState()
                    )
                }
                ModuleEnum.B -> {
                    uiState.value.moduleB.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleB = _uiState.value.moduleB.copy(
                            job = null,
                            isRunning = false
                        ),
                        dashBoardB = DashBoardState()
                    )
                }
                ModuleEnum.C -> {
                    uiState.value.moduleC.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleC = _uiState.value.moduleC.copy(
                            job = null,
                            isRunning = false
                        ),
                        dashBoardC = DashBoardState()
                    )
                }
                ModuleEnum.D -> {
                    uiState.value.moduleD.job?.let { if (it.isActive) it.cancel() }
                    _uiState.value = _uiState.value.copy(
                        moduleD = _uiState.value.moduleD.copy(
                            job = null,
                            isRunning = false
                        ),
                        dashBoardD = DashBoardState()
                    )
                }
            }
            // 如果四个模块的job都是null的话暂停摇床
            if (uiState.value.moduleA.job == null && uiState.value.moduleB.job == null && uiState.value.moduleC.job == null && uiState.value.moduleD.job == null) {
                appViewModel.sender(
                    SerialPortEnum.SERIAL_ONE,
                    Command(parameter = "0B", data = "0101").toHex()
                )
            }
            _state.emit(HomeState.OnDashBoardChange(module))
        }
    }

    /**
     * 程序运行状态收集器
     * @param job [ActionActuator] 运行器
     */
    private fun programStateCollector(job: ActionActuator) {
        viewModelScope.launch {
            job.state.collect {
                when (it) {
                    is ActionState.CurrentAction -> {
                        val action = it.action
                        when (it.module) {
                            ModuleEnum.A -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardA = _uiState.value.dashBoardA.copy(
                                        currentAction = action
                                    )
                                )
                            }
                            ModuleEnum.B -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardB = _uiState.value.dashBoardB.copy(
                                        currentAction = action
                                    )
                                )
                            }
                            ModuleEnum.C -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardC = _uiState.value.dashBoardC.copy(
                                        currentAction = action
                                    )
                                )
                            }
                            ModuleEnum.D -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardD = _uiState.value.dashBoardD.copy(
                                        currentAction = action
                                    )
                                )
                            }
                        }
                        _state.emit(HomeState.OnDashBoardChange(it.module))
                    }
                    is ActionState.CurrentActionTime -> {
                        val time = it.time
                        when (it.module) {
                            ModuleEnum.A -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardA = _uiState.value.dashBoardA.copy(
                                        time = time
                                    )
                                )
                            }
                            ModuleEnum.B -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardB = _uiState.value.dashBoardB.copy(
                                        time = time
                                    )
                                )
                            }
                            ModuleEnum.C -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardC = _uiState.value.dashBoardC.copy(
                                        time = time
                                    )
                                )
                            }
                            ModuleEnum.D -> {
                                _uiState.value = _uiState.value.copy(
                                    dashBoardD = _uiState.value.dashBoardD.copy(
                                        time = time
                                    )
                                )
                            }
                        }
                        _state.emit(HomeState.OnDashBoardChange(it.module))
                    }
                    is ActionState.Finish -> stop(it.module)
                }
            }
        }
    }

}

sealed class HomeState {
    data class OnSwitchProgram(val index: Int, val module: ModuleEnum) : HomeState()
    data class OnLoadProgram(val uiState: HomeUiState) : HomeState()
    data class OnButtonChange(val module: ModuleEnum) : HomeState()
    data class OnRestCallBack(val success: Boolean) : HomeState()
    data class OnDashBoardChange(val module: ModuleEnum) : HomeState()
    object OnPause : HomeState()
    object OnInsulating : HomeState()
}

data class HomeUiState(
    val programList: List<Program> = emptyList(),
    val moduleA: ModuleState = ModuleState(),
    val moduleB: ModuleState = ModuleState(),
    val moduleC: ModuleState = ModuleState(),
    val moduleD: ModuleState = ModuleState(),
    val btnReset: ButtonState = ButtonState(),
    val btnPause: ButtonState = ButtonState(
        text = "暂停摇床", background = R.mipmap.btn_pause, textColor = R.color.dark_outline
    ),
    val btnInsulating: ButtonState = ButtonState(text = "抗体保温", textColor = R.color.dark_outline),
    val dashBoardA: DashBoardState = DashBoardState(),
    val dashBoardB: DashBoardState = DashBoardState(),
    val dashBoardC: DashBoardState = DashBoardState(),
    val dashBoardD: DashBoardState = DashBoardState()
)

data class ModuleState(
    val index: Int = -1,
    val isRunning: Boolean = false,
    val btnStart: ButtonState = ButtonState(enable = false),
    val btnStop: ButtonState = ButtonState(visibility = View.GONE),
    val btnProgram: ButtonState = ButtonState(),
    val job: Job? = null,
)

data class ButtonState(
    val visibility: Int = View.VISIBLE,
    val isClickable: Boolean = true,
    val text: String = "",
    val enable: Boolean = true,
    val background: Int = 0,
    val isRunning: Boolean = false,
    val icon: Int = 0,
    val textColor: Int = 0,
)

data class DashBoardState(
    val currentAction: Action = Action(),
    val time: String = "00:00:00",
    val temperature: String = "0.0℃",
)

enum class ModuleEnum(val value: String, val index: Int) {
    A("模块A", 0), B("模块B", 1), C("模块C", 2), D("模块D", 3),
}

fun getModuleEnum(index: Int): ModuleEnum {
    return when (index) {
        0 -> ModuleEnum.A
        1 -> ModuleEnum.B
        2 -> ModuleEnum.C
        3 -> ModuleEnum.D
        else -> ModuleEnum.A
    }
}
