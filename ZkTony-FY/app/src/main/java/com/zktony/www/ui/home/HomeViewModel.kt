package com.zktony.www.ui.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.zktony.www.R
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppIntent
import com.zktony.www.common.app.AppState
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.extractTemp
import com.zktony.www.common.extension.removeZero
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

    // VIEW层操作
    private val intent = MutableSharedFlow<HomeIntent>()

    // 保存的UI状态
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> get() = _uiState

    init {
        viewModelScope.launch {
            launch {
                intent.collect {
                    when (it) {
                        is HomeIntent.OnSwitchProgram -> onSwitchProgram(it.index, it.module)
                        is HomeIntent.OnStart -> onStart(it.module)
                        is HomeIntent.OnStop -> onStop(it.module)
                        is HomeIntent.OnReset -> onReset()
                        is HomeIntent.OnPause -> onPause()
                        is HomeIntent.OnInsulating -> onInsulating()
                    }
                }
            }
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
                    appViewModel.dispatch(
                        AppIntent.SenderText(
                            SerialPortEnum.SERIAL_FOUR, Command.saveTemperature(i.toString(), "26")
                        )
                    )
                    delay(200L)
                    appViewModel.dispatch(
                        AppIntent.SenderText(
                            SerialPortEnum.SERIAL_FOUR, Command.setTemperature(i.toString(), "26")
                        )
                    )
                }
                // 每一分钟查询一次温度
                while (true) {
                    for (i in 0..3) {
                        delay(200L)
                        appViewModel.dispatch(
                            AppIntent.SenderText(
                                SerialPortEnum.SERIAL_FOUR, Command.queryTemperature(i.toString())
                            )
                        )
                    }
                    delay(60 * 1000L)
                }
            }
        }
    }

    /**
     * Intent处理器
     * @param intent [HomeIntent]
     */
    fun dispatch(intent: HomeIntent) {
        viewModelScope.launch {
            try {
                this@HomeViewModel.intent.emit(intent)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * 切换程序
     * @param index [Int] 程序索引
     * @param module [ModuleEnum] 模块
     */
    private fun onSwitchProgram(index: Int, module: ModuleEnum) {
        viewModelScope.launch {
            val uiState = uiState.value
            uiState.run {
                when (module) {
                    ModuleEnum.A -> {
                        moduleA.index = index
                        moduleA.btnStart.enable = programList[index].actionCount > 0
                    }
                    ModuleEnum.B -> {
                        moduleB.index = index
                        moduleB.btnStart.enable = programList[index].actionCount > 0
                    }
                    ModuleEnum.C -> {
                        moduleC.index = index
                        moduleC.btnStart.enable = programList[index].actionCount > 0
                    }
                    ModuleEnum.D -> {
                        moduleD.index = index
                        moduleD.btnStart.enable = programList[index].actionCount > 0
                    }
                }
                _uiState.update { this }
                _state.emit(HomeState.OnSwitchProgram(index, module))
                _state.emit(HomeState.OnButtonChange(module))
            }
        }
    }

    /**
     * 开始执行程序
     * @param module [ModuleEnum] 模块
     */
    private fun onStart(module: ModuleEnum) {
        viewModelScope.launch {
            val uiState = uiState.value
            uiState.run {
                when (module) {
                    ModuleEnum.A -> {
                        moduleA.isRunning = true
                        moduleA.btnStart.visibility = View.GONE
                        moduleA.btnStop.visibility = View.VISIBLE
                        moduleA.btnProgram.isClickable = false
                        runProgram(module, programList[moduleA.index])
                    }
                    ModuleEnum.B -> {
                        moduleB.isRunning = true
                        moduleB.btnStart.visibility = View.GONE
                        moduleB.btnStop.visibility = View.VISIBLE
                        moduleB.btnProgram.isClickable = false
                        runProgram(module, programList[moduleB.index])
                    }
                    ModuleEnum.C -> {
                        moduleC.isRunning = true
                        moduleC.btnStart.visibility = View.GONE
                        moduleC.btnStop.visibility = View.VISIBLE
                        moduleC.btnProgram.isClickable = false
                        runProgram(module, programList[moduleC.index])
                    }
                    ModuleEnum.D -> {
                        moduleD.isRunning = true
                        moduleD.btnStart.visibility = View.GONE
                        moduleD.btnStop.visibility = View.VISIBLE
                        moduleD.btnProgram.isClickable = false
                        runProgram(module, programList[moduleD.index])
                    }
                }
                _uiState.update { this }
                _state.emit(HomeState.OnButtonChange(module))
            }
        }
    }

    /**
     * 停止执行程序
     * @param module [ModuleEnum] 模块
     */
    private fun onStop(module: ModuleEnum) {
        viewModelScope.launch {
            val uiState = uiState.value
            uiState.run {
                when (module) {
                    ModuleEnum.A -> {
                        moduleA.isRunning = false
                        moduleA.btnStart.visibility = View.VISIBLE
                        moduleA.btnStop.visibility = View.GONE
                        moduleA.btnProgram.isClickable = true
                    }
                    ModuleEnum.B -> {
                        moduleB.isRunning = false
                        moduleB.btnStart.visibility = View.VISIBLE
                        moduleB.btnStop.visibility = View.GONE
                        moduleB.btnProgram.isClickable = true
                    }
                    ModuleEnum.C -> {
                        moduleC.isRunning = false
                        moduleC.btnStart.visibility = View.VISIBLE
                        moduleC.btnStop.visibility = View.GONE
                        moduleC.btnProgram.isClickable = true
                    }
                    ModuleEnum.D -> {
                        moduleD.isRunning = false
                        moduleD.btnStart.visibility = View.VISIBLE
                        moduleD.btnStop.visibility = View.GONE
                        moduleD.btnProgram.isClickable = true
                    }
                }
                stopProgram(module)
                _uiState.value = this
                _state.emit(HomeState.OnButtonChange(module))
            }
        }
    }

    /**
     * 复位
     */
    private fun onReset() {
        SerialPortManager.instance.commandQueue.clear()
        appViewModel.dispatch(
            AppIntent.Sender(
                SerialPortEnum.SERIAL_ONE,
                Command(function = "05", parameter = "01", data = "0101302C302C302C302C").toHex()
            )
        )
        appViewModel.dispatch(
            AppIntent.Sender(SerialPortEnum.SERIAL_ONE, Command().toHex())
        )
        stopProgram(ModuleEnum.A)
        stopProgram(ModuleEnum.B)
        stopProgram(ModuleEnum.C)
        stopProgram(ModuleEnum.D)
    }

    /**
     * 摇床暂停
     */
    private fun onPause() {
        viewModelScope.launch {
            val uiState = uiState.value
            uiState.run {
                btnPause.run {
                    if (isRunning) {
                        isRunning = false
                        text = "暂停摇床"
                        background = R.mipmap.btn_pause
                        textColor = R.color.dark_outline
                        appViewModel.dispatch(
                            AppIntent.Sender(
                                SerialPortEnum.SERIAL_ONE,
                                Command(parameter = "0B", data = "0101").toHex()
                            )
                        )
                    } else {
                        isRunning = true
                        text = "继  续"
                        background = R.mipmap.btn_continue
                        textColor = R.color.red
                        appViewModel.dispatch(
                            AppIntent.Sender(
                                SerialPortEnum.SERIAL_ONE,
                                Command(parameter = "0B", data = "0100").toHex()
                            )
                        )
                    }
                }
                _uiState.update { this }
                _state.emit(HomeState.OnPause)
            }
        }
    }

    /**
     * 抗体保温
     */
    private fun onInsulating() {
        viewModelScope.launch {
            val uiState = uiState.value
            uiState.run {
                btnInsulating.run {
                    if (isRunning) {
                        isRunning = false
                        text = "抗体保温"
                        textColor = R.color.dark_outline
                        appViewModel.dispatch(
                            AppIntent.SenderText(
                                SerialPortEnum.SERIAL_FOUR, Command.setTemperature(
                                    address = "4", temperature = "26"
                                )
                            )
                        )
                    } else {
                        val temp = appViewModel.settingState.value.temp.toString().removeZero()
                        isRunning = true
                        text = "抗体保温 $temp℃"
                        textColor = R.color.red
                        appViewModel.dispatch(
                            AppIntent.SenderText(
                                SerialPortEnum.SERIAL_FOUR, Command.setTemperature(
                                    address = "4", temperature = temp
                                )
                            )
                        )
                    }
                }
                _uiState.update { this }
                _state.emit(HomeState.OnInsulating)
            }
        }
    }

    /**
     * 串口一数据接收
     * @param hex [String] 数据
     */
    private fun receiverSerialOne(hex: String) {
        val command = Command(hex)
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
                    _uiState.update {
                        uiState.value.copy(
                            dashBoardA = uiState.value.dashBoardA.copy(
                                temperature = "$temp℃"
                            )
                        )
                    }
                }
                1 -> {
                    _uiState.update {
                        uiState.value.copy(
                            dashBoardB = uiState.value.dashBoardB.copy(
                                temperature = "$temp℃"
                            )
                        )
                    }
                }
                2 -> {
                    _uiState.update {
                        uiState.value.copy(
                            dashBoardC = uiState.value.dashBoardC.copy(
                                temperature = "$temp℃"
                            )
                        )
                    }
                }
                3 -> {
                    _uiState.update {
                        uiState.value.copy(
                            dashBoardD = uiState.value.dashBoardD.copy(
                                temperature = "$temp℃"
                            )
                        )
                    }
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
        val uiState = uiState.value
        uiState.run {
            this.programList = programList
            if (programList.isEmpty()) {
                moduleA.index = -1
                moduleA.btnStart.enable = false
                moduleB.index = -1
                moduleB.btnStart.enable = false
                moduleC.index = -1
                moduleC.btnStart.enable = false
                moduleD.index = -1
                moduleD.btnStart.enable = false
            } else {
                if (moduleA.index >= programList.size) {
                    moduleA.index = 0
                } else if (moduleA.index < 0) {
                    moduleA.index = 0
                }
                if (moduleB.index >= programList.size) {
                    moduleB.index = 0
                } else if (moduleB.index < 0) {
                    moduleB.index = 0
                }
                if (moduleC.index >= programList.size) {
                    moduleC.index = 0
                } else if (moduleC.index < 0) {
                    moduleC.index = 0
                }
                if (moduleD.index >= programList.size) {
                    moduleD.index = 0
                } else if (moduleD.index < 0) {
                    moduleD.index = 0
                }
                moduleA.btnStart.enable = programList[moduleA.index].actionCount > 0
                moduleB.btnStart.enable = programList[moduleB.index].actionCount > 0
                moduleC.btnStart.enable = programList[moduleC.index].actionCount > 0
                moduleD.btnStart.enable = programList[moduleD.index].actionCount > 0
            }
            _uiState.update { this }
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
                    _uiState.update {
                        it.apply {
                            moduleA = moduleA.copy(job = job, isRunning = true)
                        }
                    }
                }
                ModuleEnum.B -> {
                    uiState.value.moduleB.job?.let { if (it.isActive) it.cancel() }
                    _uiState.update {
                        it.apply {
                            moduleB = moduleB.copy(job = job, isRunning = true)
                        }
                    }
                }
                ModuleEnum.C -> {
                    uiState.value.moduleC.job?.let { if (it.isActive) it.cancel() }
                    _uiState.update {
                        it.apply {
                            moduleC = moduleC.copy(job = job, isRunning = true)
                        }
                    }
                }
                ModuleEnum.D -> {
                    uiState.value.moduleD.job?.let { if (it.isActive) it.cancel() }
                    _uiState.update {
                        it.apply {
                            moduleD = moduleD.copy(job = job, isRunning = true)
                        }
                    }
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
                    _uiState.update {
                        it.apply {
                            moduleA = it.moduleA.copy(job = null, isRunning = false)
                        }
                    }
                    _uiState.update { it.apply { dashBoardA = DashBoardState() } }
                }
                ModuleEnum.B -> {
                    uiState.value.moduleB.job?.let { if (it.isActive) it.cancel() }
                    _uiState.update {
                        it.apply {
                            moduleB = it.moduleB.copy(job = null, isRunning = false)
                        }
                    }
                    _uiState.update { it.apply { dashBoardB = DashBoardState() } }
                }
                ModuleEnum.C -> {
                    uiState.value.moduleC.job?.let { if (it.isActive) it.cancel() }
                    _uiState.update {
                        it.apply {
                            moduleC = it.moduleC.copy(job = null, isRunning = false)
                        }
                    }
                    _uiState.update { it.apply { dashBoardC = DashBoardState() } }
                }
                ModuleEnum.D -> {
                    uiState.value.moduleD.job?.let { if (it.isActive) it.cancel() }
                    _uiState.update {
                        it.apply {
                            moduleD = it.moduleD.copy(job = null, isRunning = false)
                        }
                    }
                    _uiState.update { it.apply { dashBoardD = DashBoardState() } }
                }
            }
            // 如果四个模块的job都是null的话暂停摇床
            if (uiState.value.moduleA.job == null && uiState.value.moduleB.job == null && uiState.value.moduleC.job == null && uiState.value.moduleD.job == null) {
                AppIntent.Sender(
                    SerialPortEnum.SERIAL_ONE, Command(parameter = "0B", data = "0101").toHex()
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
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardA = uiState.value.dashBoardA.copy(
                                            currentAction = action
                                        )
                                    )
                                }
                            }
                            ModuleEnum.B -> {
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardB = uiState.value.dashBoardB.copy(
                                            currentAction = action
                                        )
                                    )
                                }
                            }
                            ModuleEnum.C -> {
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardC = uiState.value.dashBoardC.copy(
                                            currentAction = action
                                        )
                                    )
                                }
                            }
                            ModuleEnum.D -> {
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardD = uiState.value.dashBoardD.copy(
                                            currentAction = action
                                        )
                                    )
                                }
                            }
                        }
                        _state.emit(HomeState.OnDashBoardChange(it.module))
                    }
                    is ActionState.CurrentActionTime -> {
                        val time = it.time
                        when (it.module) {
                            ModuleEnum.A -> {
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardA = uiState.value.dashBoardA.copy(
                                            time = time
                                        )
                                    )
                                }
                            }
                            ModuleEnum.B -> {
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardB = uiState.value.dashBoardB.copy(
                                            time = time
                                        )
                                    )
                                }
                            }
                            ModuleEnum.C -> {
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardC = uiState.value.dashBoardC.copy(
                                            time = time
                                        )
                                    )
                                }
                            }
                            ModuleEnum.D -> {
                                _uiState.update {
                                    uiState.value.copy(
                                        dashBoardD = uiState.value.dashBoardD.copy(
                                            time = time
                                        )
                                    )
                                }
                            }
                        }
                        _state.emit(HomeState.OnDashBoardChange(it.module))
                    }
                    is ActionState.Finish -> onStop(it.module)
                }
            }
        }
    }

}

sealed class HomeIntent {
    data class OnSwitchProgram(val index: Int, val module: ModuleEnum) : HomeIntent()
    data class OnStart(val module: ModuleEnum) : HomeIntent()
    data class OnStop(val module: ModuleEnum) : HomeIntent()
    object OnReset : HomeIntent()
    object OnPause : HomeIntent()
    object OnInsulating : HomeIntent()
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
    var programList: List<Program> = emptyList(),
    var moduleA: ModuleState = ModuleState(),
    var moduleB: ModuleState = ModuleState(),
    var moduleC: ModuleState = ModuleState(),
    var moduleD: ModuleState = ModuleState(),
    var btnReset: ButtonState = ButtonState(),
    var btnPause: ButtonState = ButtonState(
        text = "暂停摇床", background = R.mipmap.btn_pause, textColor = R.color.dark_outline
    ),
    var btnInsulating: ButtonState = ButtonState(text = "抗体保温", textColor = R.color.dark_outline),
    var dashBoardA: DashBoardState = DashBoardState(),
    var dashBoardB: DashBoardState = DashBoardState(),
    var dashBoardC: DashBoardState = DashBoardState(),
    var dashBoardD: DashBoardState = DashBoardState()
)

data class ModuleState(
    var index: Int = -1,
    var isRunning: Boolean = false,
    var btnStart: ButtonState = ButtonState(enable = false),
    var btnStop: ButtonState = ButtonState(visibility = View.GONE),
    var btnProgram: ButtonState = ButtonState(),
    var job: Job? = null,
)

data class ButtonState(
    var visibility: Int = View.VISIBLE,
    var isClickable: Boolean = true,
    var text: String = "",
    var enable: Boolean = true,
    var background: Int = 0,
    var isRunning: Boolean = false,
    var icon: Int = 0,
    var textColor: Int = 0,
)

data class DashBoardState(
    var currentAction: Action = Action(),
    var time: String = "00:00:00",
    var temperature: String = "0.0℃",
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
