package com.zktony.www.ui.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppEvent
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.extractTemp
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.toCommand
import com.zktony.www.common.model.Queue
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Log
import com.zktony.www.common.room.entity.Program
import com.zktony.www.common.room.entity.getActionEnum
import com.zktony.www.data.repository.ActionRepository
import com.zktony.www.data.repository.LogRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.SerialPortEnum.*
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
    private val programRepository: ProgramRepository,
    private val actionRepository: ActionRepository,
    private val logRepository: LogRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _programList = MutableStateFlow<List<Program>>(emptyList())
    private val _aState = MutableStateFlow(ModuleState())
    private val _bState = MutableStateFlow(ModuleState())
    private val _cState = MutableStateFlow(ModuleState())
    private val _dState = MutableStateFlow(ModuleState())
    private val _eState = MutableStateFlow(OperationState())
    val programList = _programList.asStateFlow()
    val aState = _aState.asStateFlow()
    val bState = _bState.asStateFlow()
    val cState = _cState.asStateFlow()
    val dState = _dState.asStateFlow()
    val eState = _eState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                programRepository.getAll().collect {
                    _programList.value = it
                    onProgramChange(it)
                }
            }
            launch {
                appViewModel.event.collect {
                    when (it) {
                        is AppEvent.ReceiverSerialOne -> receiverSerialOne(it.command)
                        is AppEvent.ReceiverSerialFour -> receiverSerialFour(it.command)
                        else -> {}
                    }
                }
            }
            launch {
                // 设置和定时查询温控
                for (i in 0..4) {
                    delay(200L)
                    appViewModel.senderText(SERIAL_FOUR, Command.saveTemperature(i.toString(), "26"))
                    delay(200L)
                    appViewModel.senderText(SERIAL_FOUR, Command.setTemperature(i.toString(), "26"))
                }
                // 每一分钟查询一次温度
                while (true) {
                    for (i in 0..3) {
                        delay(200L)
                        appViewModel.senderText(SERIAL_FOUR, Command.queryTemperature(i.toString()))
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
                A -> {
                    _aState.value = _aState.value.copy(
                        program = _programList.value[index],
                        btnStartEnable = _programList.value[index].actionCount > 0,
                    )
                }
                B -> {
                    _bState.value = _bState.value.copy(
                        program = _programList.value[index],
                        btnStartEnable = _programList.value[index].actionCount > 0,
                    )
                }
                C -> {
                    _cState.value = _cState.value.copy(
                        program = _programList.value[index],
                        btnStartEnable = _programList.value[index].actionCount > 0,
                    )
                }
                D -> {
                    _dState.value = _dState.value.copy(
                        program = _programList.value[index],
                        btnStartEnable = _programList.value[index].actionCount > 0,
                    )
                }
            }
        }
    }

    /**
     * 开始执行程序
     * @param module [ModuleEnum] 模块
     */
    fun start(module: ModuleEnum) {
        viewModelScope.launch {
            when (module) {
                A -> {
                    _aState.value = _aState.value.copy(
                        btnStartVisible = View.GONE,
                        btnStopVisible = View.VISIBLE,
                        btnSelectorEnable = false,
                        runtimeText = "运行中",
                    )
                    runProgram(module, _aState.value.program!!)
                }
                B -> {
                    _bState.value = _bState.value.copy(
                        btnStartVisible = View.GONE,
                        btnStopVisible = View.VISIBLE,
                        btnSelectorEnable = false,
                        runtimeText = "运行中",
                    )
                    runProgram(module, _bState.value.program!!)
                }
                C -> {
                    _cState.value = _cState.value.copy(
                        btnStartVisible = View.GONE,
                        btnStopVisible = View.VISIBLE,
                        btnSelectorEnable = false,
                        runtimeText = "运行中",
                    )
                    runProgram(module, _cState.value.program!!)
                }
                D -> {
                    _dState.value = _dState.value.copy(
                        btnStartVisible = View.GONE,
                        btnStopVisible = View.VISIBLE,
                        btnSelectorEnable = false,
                        runtimeText = "运行中",
                    )
                    runProgram(module, _dState.value.program!!)
                }
            }
        }
    }

    /**
     * 停止执行程序
     * @param module [ModuleEnum] 模块
     */
    fun stop(module: ModuleEnum) {
        viewModelScope.launch {
            SerialPortManager.instance.setModuleRunning(module, false)
            when (module) {
                A -> {
                    _aState.value.job?.let { if (it.isActive) it.cancel() }
                    _aState.value.log?.let { log ->
                        logRepository.delete(log)
                    }
                    _aState.value = _aState.value.copy(
                        job = null,
                        btnStartVisible = View.VISIBLE,
                        btnStopVisible = View.GONE,
                        btnSelectorEnable = true,
                        currentActionText = "/",
                        tempText = "0.0℃",
                        countDownText = "00:00:00",
                    )
                }
                B -> {
                    _bState.value.job?.let { if (it.isActive) it.cancel() }
                    _bState.value.log?.let { log ->
                        logRepository.delete(log)
                    }
                    _bState.value = _bState.value.copy(
                        job = null,
                        btnStartVisible = View.VISIBLE,
                        btnStopVisible = View.GONE,
                        btnSelectorEnable = true,
                        currentActionText = "/",
                        tempText = "0.0℃",
                        countDownText = "00:00:00",
                    )
                }
                C -> {
                    _cState.value.job?.let { if (it.isActive) it.cancel() }
                    _cState.value.log?.let { log ->
                        logRepository.delete(log)
                    }
                    _cState.value = _cState.value.copy(
                        job = null,
                        btnStartVisible = View.VISIBLE,
                        btnStopVisible = View.GONE,
                        btnSelectorEnable = true,
                        currentActionText = "/",
                        tempText = "0.0℃",
                        countDownText = "00:00:00",
                    )
                }
                D -> {
                    _dState.value.job?.let { if (it.isActive) it.cancel() }
                    _dState.value.log?.let { log ->
                        logRepository.delete(log)
                    }
                    _dState.value = _dState.value.copy(
                        job = null,
                        btnStartVisible = View.VISIBLE,
                        btnStopVisible = View.GONE,
                        btnSelectorEnable = true,
                        currentActionText = "/",
                        tempText = "0.0℃",
                        countDownText = "00:00:00",
                    )
                }
            }
            if (_aState.value.job == null && _bState.value.job == null && _cState.value.job == null && _dState.value.job == null) {
                appViewModel.sender(SERIAL_ONE, Command.pauseShakeBed())
                SerialPortManager.instance.commandQueue.clear()
            }
        }
    }

    /**
     * 复位
     */
    fun reset() {
        SerialPortManager.instance.commandQueue.clear()
        appViewModel.sender(
            SERIAL_ONE,
            Command(function = "05", parameter = "01", data = "0101302C302C302C302C").toHex()
        )
        appViewModel.sender(SERIAL_ONE, Command().toHex())
        stop(A)
        stop(B)
        stop(C)
        stop(D)
    }

    /**
     * 摇床暂停
     */
    fun pause() {
        viewModelScope.launch {
            appViewModel.sender(
                SERIAL_ONE,
                if (_eState.value.pauseEnable) Command.resumeShakeBed() else Command.pauseShakeBed()
            )
            _eState.value = _eState.value.copy(pauseEnable = !_eState.value.pauseEnable)
        }
    }

    /**
     * 抗体保温
     */
    fun insulating() {
        viewModelScope.launch {
            val temp = appViewModel.settingState.value.temp.toString().removeZero()
            appViewModel.senderText(
                SERIAL_FOUR,
                Command.setTemperature(
                    address = "4",
                    temperature = if (_eState.value.insulatingEnable) "26" else temp
                )
            )
            _eState.value = _eState.value.copy(insulatingEnable = !_eState.value.insulatingEnable)
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
                0 -> _aState.value = _aState.value.copy(tempText = "$temp ℃")
                1 -> _bState.value = _bState.value.copy(tempText = "$temp ℃")
                2 -> _cState.value = _cState.value.copy(tempText = "$temp ℃")
                3 -> _dState.value = _dState.value.copy(tempText = "$temp ℃")
            }
        }
    }


    /**
     * 复位反馈
     * @param command [Command] 命令
     */
    private fun resetCallBack(command: Command) {
        viewModelScope.launch {
            if (command.parameter == "0A" && command.data == "00") {
                PopTip.show("复位成功")
            } else {
                PopTip.show("复位失败")
            }
        }
    }

    /**
     * 程序发生变化添加删除时候更新选中的index
     * @param programList [List]<[Program]> 程序列表
     */
    private fun onProgramChange(programList: List<Program>) {
        if (programList.isNotEmpty()) {
            if (_aState.value.program == null) {
                _aState.value = _aState.value.copy(
                    program = programList.first(),
                    btnStartEnable = programList.first().actionCount > 0
                )
            } else {
                if (!programList.contains(_aState.value.program)) {
                    _aState.value = _aState.value.copy(
                        program = programList.first(),
                        btnStartEnable = programList.first().actionCount > 0
                    )
                } else {
                    _aState.value = _aState.value.copy(
                        btnStartEnable = programList.find { it.id == _aState.value.program!!.id }!!.actionCount > 0
                    )
                }
            }
            if (_bState.value.program == null) {
                _bState.value = _bState.value.copy(
                    program = programList.first(),
                    btnStartEnable = programList.first().actionCount > 0
                )
            } else {
                if (!programList.contains(_bState.value.program)) {
                    _bState.value = _bState.value.copy(
                        program = programList.first(),
                        btnStartEnable = programList.first().actionCount > 0
                    )
                } else {
                    _bState.value = _bState.value.copy(
                        btnStartEnable = programList.find { it.id == _bState.value.program!!.id }!!.actionCount > 0
                    )
                }
            }
            if (_cState.value.program == null) {
                _cState.value = _cState.value.copy(
                    program = programList.first(),
                    btnStartEnable = programList.first().actionCount > 0
                )
            } else {
                if (!programList.contains(_cState.value.program)) {
                    _cState.value = _cState.value.copy(
                        program = programList.first(),
                        btnStartEnable = programList.first().actionCount > 0
                    )
                } else {
                    _cState.value = _cState.value.copy(
                        btnStartEnable = programList.find { it.id == _cState.value.program!!.id }!!.actionCount > 0
                    )
                }
            }
            if (_dState.value.program == null) {
                _dState.value = _dState.value.copy(
                    program = programList.first(),
                    btnStartEnable = programList.first().actionCount > 0
                )
            } else {
                if (!programList.contains(_dState.value.program)) {
                    _dState.value = _dState.value.copy(
                        program = programList.first(),
                        btnStartEnable = programList.first().actionCount > 0
                    )
                } else {
                    _dState.value = _dState.value.copy(
                        btnStartEnable = programList.find { it.id == _dState.value.program!!.id }!!.actionCount > 0
                    )
                }
            }
        } else {
            _aState.value = _aState.value.copy(program = null, btnStartEnable = false)
            _bState.value = _bState.value.copy(program = null, btnStartEnable = false)
            _cState.value = _cState.value.copy(program = null, btnStartEnable = false)
            _dState.value = _dState.value.copy(program = null, btnStartEnable = false)
        }
    }

    /**
     * 运行程序
     * @param module [ModuleEnum] 模块
     * @param program [Program] 程序
     */
    private fun runProgram(module: ModuleEnum, program: Program) {
        SerialPortManager.instance.setModuleRunning(module, true)

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
        viewModelScope.launch {
            when (module) {
                A -> {
                    _aState.value.job?.let { if (it.isActive) it.cancel() }
                    val log = Log(
                        programName = _aState.value.program!!.name,
                        module = module.index,
                        actions = _aState.value.program!!.actions,
                    )
                    _aState.value = _aState.value.copy(job = job, log = log)
                    logRepository.insert(log)
                    programRepository.update(
                        _aState.value.program!!.copy(
                            runCount = _aState.value.program!!.runCount + 1
                        )
                    )
                }
                B -> {
                    _bState.value.job?.let { if (it.isActive) it.cancel() }
                    val log = Log(
                        programName = _bState.value.program!!.name,
                        module = module.index,
                        actions = _bState.value.program!!.actions,
                    )
                    _bState.value = _bState.value.copy(job = job, log = log)
                    logRepository.insert(log)
                    programRepository.update(
                        _bState.value.program!!.copy(
                            runCount = _bState.value.program!!.runCount + 1
                        )
                    )
                }
                C -> {
                    _cState.value.job?.let { if (it.isActive) it.cancel() }
                    val log = Log(
                        programName = _cState.value.program!!.name,
                        module = module.index,
                        actions = _cState.value.program!!.actions,
                    )
                    _cState.value = _cState.value.copy(job = job, log = log)
                    logRepository.insert(log)
                    programRepository.update(
                        _cState.value.program!!.copy(
                            runCount = _cState.value.program!!.runCount + 1
                        )
                    )
                }
                D -> {
                    _dState.value.job?.let { if (it.isActive) it.cancel() }
                    val log = Log(
                        programName = _dState.value.program!!.name,
                        module = module.index,
                        actions = _dState.value.program!!.actions,
                    )
                    _dState.value = _dState.value.copy(job = job, log = log)
                    logRepository.insert(log)
                    programRepository.update(
                        _dState.value.program!!.copy(
                            runCount = _dState.value.program!!.runCount + 1
                        )
                    )
                }
            }
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
                    is ActionEvent.CurrentAction -> {
                        when (it.module) {
                            A -> _aState.value =
                                _aState.value.copy(currentActionText = getActionEnum(it.action.mode).value)
                            B -> _bState.value =
                                _bState.value.copy(currentActionText = getActionEnum(it.action.mode).value)
                            C -> _cState.value =
                                _cState.value.copy(currentActionText = getActionEnum(it.action.mode).value)
                            D -> _dState.value =
                                _dState.value.copy(currentActionText = getActionEnum(it.action.mode).value)
                        }
                    }
                    is ActionEvent.CurrentActionTime -> {
                        when (it.module) {
                            A -> _aState.value =
                                _aState.value.copy(countDownText = it.time)
                            B -> _bState.value =
                                _bState.value.copy(countDownText = it.time)
                            C -> _cState.value =
                                _cState.value.copy(countDownText = it.time)
                            D -> _dState.value =
                                _dState.value.copy(countDownText = it.time)
                        }
                    }
                    is ActionEvent.Finish -> {
                        when (it.module) {
                            A -> {
                                _aState.value.log?.let { log ->
                                    logRepository.update(log.copy(status = 1))
                                }
                                _aState.value = _aState.value.copy(
                                    runtimeText = "已完成",
                                    log = null
                                )
                            }
                            B -> {
                                _bState.value.log?.let { log ->
                                    logRepository.update(log.copy(status = 1))
                                }
                                _bState.value = _bState.value.copy(
                                    runtimeText = "已完成",
                                    log = null
                                )
                            }
                            C -> {
                                _cState.value.log?.let { log ->
                                    logRepository.update(log.copy(status = 1))
                                }
                                _cState.value = _cState.value.copy(
                                    runtimeText = "已完成",
                                    log = null
                                )
                            }
                            D -> {
                                _dState.value.log?.let { log ->
                                    logRepository.update(log.copy(status = 1))
                                }
                                _dState.value = _dState.value.copy(
                                    runtimeText = "已完成",
                                    log = null
                                )
                            }
                        }
                        stop(it.module)
                    }
                    is ActionEvent.Count -> {
                        when (it.module) {
                            A -> _aState.value =
                                _aState.value.copy(
                                    currentActionText = _aState.value.currentActionText.substring(
                                        0,
                                        2
                                    )
                                            + " X${it.count}"
                                )
                            B -> _bState.value =
                                _bState.value.copy(
                                    currentActionText = _bState.value.currentActionText.substring(
                                        0,
                                        2
                                    )
                                            + " X${it.count}"
                                )
                            C -> _cState.value =
                                _cState.value.copy(
                                    currentActionText = _cState.value.currentActionText.substring(
                                        0,
                                        2
                                    )
                                            + " X${it.count}"
                                )
                            D -> _dState.value =
                                _dState.value.copy(
                                    currentActionText = _dState.value.currentActionText.substring(
                                        0,
                                        2
                                    )
                                            + " X${it.count}"
                                )
                        }
                    }
                }
            }
        }
    }

}


data class ModuleState(
    val job: Job? = null,
    val program: Program? = null,
    val log: Log? = null,
    val btnSelectorEnable: Boolean = true,
    val btnStartEnable: Boolean = false,
    val btnStartVisible: Int = View.VISIBLE,
    val btnStopVisible: Int = View.GONE,
    val countDownText: String = "00:00:00",
    val runtimeText: String = "已就绪",
    val currentActionText: String = "/",
    val tempText: String = "0.0℃",
)

data class OperationState(
    val pauseEnable: Boolean = false,
    val insulatingEnable: Boolean = false,
)

enum class ModuleEnum(val value: String, val index: Int) {
    A("模块A", 0),
    B("模块B", 1),
    C("模块C", 2),
    D("模块D", 3),
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
