package com.zktony.www.ui.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.currentTime
import com.zktony.core.ext.removeZero
import com.zktony.core.utils.Constants
import com.zktony.core.utils.Queue
import com.zktony.datastore.ext.read
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import com.zktony.www.room.dao.ActionDao
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.LogDao
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.Action
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Log
import com.zktony.www.room.entity.Program
import com.zktony.www.room.entity.getActionEnum
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel constructor(
    private val PD: ProgramDao,
    private val AD: ActionDao,
    private val LD: LogDao,
    private val CD: ContainerDao,
    private val SM: SerialManager,
    private val DS: DataStore<Preferences>,
) : BaseViewModel() {


    private val _programFlow = MutableStateFlow<List<Program>>(emptyList())
    private val _aFlow = MutableStateFlow(ModuleUiState(status = "A Active"))
    private val _bFlow = MutableStateFlow(ModuleUiState(status = "B Active"))
    private val _cFlow = MutableStateFlow(ModuleUiState(status = "C Active"))
    private val _dFlow = MutableStateFlow(ModuleUiState(status = "D Active"))
    private val _buttonFlow = MutableStateFlow(UiState())
    private val _settings = MutableStateFlow(Settings())
    val programFlow = _programFlow.asStateFlow()
    val aFlow = _aFlow.asStateFlow()
    val bFlow = _bFlow.asStateFlow()
    val cFlow = _cFlow.asStateFlow()
    val dFlow = _dFlow.asStateFlow()
    val buttonFlow = _buttonFlow.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                PD.getAll().collect {
                    _programFlow.value = it
                    onProgramChange(it)
                }
            }
            launch {
                CD.getAll().collect {
                    if (it.isNotEmpty()) {
                        _buttonFlow.value = _buttonFlow.value.copy(container = it[0])
                    }
                }
            }
            launch {
                // 串口四flow
                SM.ttys3Flow.collect {
                    it?.let {
                        if (it.startsWith("TC1:TCACTUALTEMP=")) {
                            // 读取温度
                            val address = it.substring(it.length - 2, it.length - 1).toInt()
                            val temp =
                                it.replace("TC1:TCACTUALTEMP=", "").split("@")[0].removeZero()
                            when (address) {
                                1 -> _aFlow.value = _aFlow.value.copy(temp = "$temp ℃")
                                2 -> _bFlow.value = _bFlow.value.copy(temp = "$temp ℃")
                                3 -> _cFlow.value = _cFlow.value.copy(temp = "$temp ℃")
                                4 -> _dFlow.value = _dFlow.value.copy(temp = "$temp ℃")
                                0 -> _buttonFlow.value = _buttonFlow.value.copy(temp = "$temp ℃")
                            }
                        }
                    }
                }
            }
            launch {
                // 设置和定时查询温控
                for (i in 0..4) {
                    delay(500L)
                    SM.setTemp(
                        addr = i,
                        temp = if (i == 0) _settings.value.temp.toString()
                            .removeZero() else "26"
                    )
                }
                // 每十秒钟查询一次温度
                while (true) {
                    for (i in 0..4) {
                        delay(300L)
                        SM.sendText(
                            index = 3, text = V1.queryTemp(i.toString())
                        )
                    }
                    delay(4 * 1000L)
                }
            }
            launch {
                launch {
                    DS.read(Constants.TEMP, 3.0f).collect {
                        _settings.value = _settings.value.copy(temp = it)
                    }
                }
                launch {
                    DS.read(Constants.RECYCLE, true).collect {
                        _settings.value = _settings.value.copy(recycle = it)
                    }
                }
            }
        }
    }

    /**
     * 获取对应模块的状态
     * @param module 模块
     * @return 状态
     */
    private fun flow(module: Int) = when (module) {
        0 -> _aFlow
        1 -> _bFlow
        2 -> _cFlow
        3 -> _dFlow
        else -> _aFlow
    }

    /**
     * 程序发生变化添加删除时候更新选中的index
     * @param programList [List]<[Program]> 程序列表
     */
    private fun onProgramChange(programList: List<Program>) {
        if (programList.isNotEmpty()) {
            listOf(_aFlow, _bFlow, _cFlow, _dFlow).forEach { state ->
                if (state.value.program == null) {
                    state.value = state.value.copy(
                        program = programList[0],
                    )
                } else {
                    if (programList.find { it.id == state.value.program!!.id } == null) {
                        state.value = state.value.copy(
                            program = programList[0],
                        )
                    } else {
                        state.value = state.value.copy(
                            program = programList.find { it.id == state.value.program!!.id },
                        )
                    }
                }
            }
        } else {
            _aFlow.value = _aFlow.value.copy(program = null)
            _bFlow.value = _bFlow.value.copy(program = null)
            _cFlow.value = _cFlow.value.copy(program = null)
            _dFlow.value = _dFlow.value.copy(program = null)
        }
    }

    /**
     * 切换程序
     * @param index [Int] 程序索引
     * @param module [Int] 模块
     */
    fun selectProgram(index: Int, module: Int) {
        viewModelScope.launch {
            val state = flow(module)
            state.value = state.value.copy(
                program = _programFlow.value[index],
                status = "Active",
                time = Constants.ZERO_TIME,
            )
        }
    }

    /**
     * 开始执行程序
     * @param module [Int] 模块
     */
    fun start(module: Int) {
        viewModelScope.launch {
            // 获取模块对应的状态
            val state = flow(module)
            // 更新状态
            state.value = state.value.copy(
                status = "Running",
            )
            // 创建job
            val job = launch {
                // 将程序中的所有步骤排序放入队列
                val actionQueue = Queue<Action>()
                AD.getByProgramId(state.value.program!!.id).first().forEach {
                    actionQueue.enqueue(it)
                }
                // 创建程序执行者
                val executor = ProgramExecutor(
                    queue = actionQueue,
                    module = module,
                    container = _buttonFlow.value.container,
                    settings = _settings.value,
                )
                // 收集执行者的状态
                executor.event = {
                    when (it) {
                        is ExecutorEvent.CurrentAction -> {
                            state.value = state.value.copy(
                                action = getActionEnum(it.action.mode).value
                            )
                        }

                        is ExecutorEvent.Time -> {
                            state.value = state.value.copy(time = it.time)
                        }

                        is ExecutorEvent.Finish -> {
                            state.value.log?.let { log ->
                                updateLog(module, log.copy(status = 1))
                            }
                            launch {
                                delay(500L)
                                state.value = state.value.copy(
                                    status = "已完成", time = "已完成", log = null
                                )
                                delay(100L)
                                stop(
                                    it.module,
                                    _settings.value.temp.toString().removeZero()
                                )
                            }
                        }

                        is ExecutorEvent.Count -> {
                            state.value = state.value.copy(
                                action = if (state.value.action.startsWith(
                                        "洗涤"
                                    )
                                ) "洗涤 X${it.count}" else state.value.action
                            )
                        }

                        is ExecutorEvent.Wait -> {
                            state.value = state.value.copy(time = it.msg)
                        }

                        is ExecutorEvent.Log -> {
                            val log = state.value.log
                            log?.let { l ->
                                updateLog(
                                    module,
                                    l.copy(content = l.content + "[ ${currentTime()} ]\t" + "${state.value.temp} \t" + it.msg + "\n")
                                )
                            }
                        }
                    }
                }
                executor.executor()
            }
            // 更新状态中的job
            launch {
                // 取消之前的job，防止多个job同时执行
                state.value.job?.cancel()
                // 创建日志
                val log = Log(
                    programName = state.value.program?.name ?: "None",
                    module = module,
                )
                LD.insert(log)
                // 更新状态中的job和日志
                state.value = state.value.copy(
                    job = job, log = log
                )
                // 更新当前程序的运行次数
                PD.update(
                    state.value.program!!.copy(
                        runCount = state.value.program!!.runCount + 1
                    )
                )
            }
        }
    }

    /**
     * 停止执行程序
     * @param module [Int] 模块
     */
    fun stop(module: Int, temp: String = "26") {
        viewModelScope.launch {
            // 获取对应模块的状态
            val state = flow(module)
            state.value.job?.cancel()
            // 更新状态
            state.value = state.value.copy(
                job = null,
                status = if (state.value.status != "已完成") "已就绪" else state.value.status,
                action = "/",
                time = if (state.value.status != "已完成") Constants.ZERO_TIME else state.value.time,
            )
            // 恢复到室温
            delay(200L)
            SM.setTemp(addr = module + 1, temp = temp)
        }
    }

    /**
     * 机构复位
     */
    fun reset() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            if (!SM.run.get()) {
                if (SM.lock.value) {
                    PopTip.show("运动中禁止复位")
                } else {
                    SM.reset()
                    PopTip.show("复位-已下发")
                }
            } else {
                PopTip.show("请中止所有运行中程序")
            }
        }
    }

    fun fill(flag: Int) {
        viewModelScope.launch {
            if (flag == 0) {
                SM.sendHex(
                    index = 2, hex = V1(pa = "0B", data = "0201").toHex()
                )
            } else {
                SM.sendHex(
                    index = 2, hex = V1(pa = "0B", data = "0200").toHex()
                )
            }
        }
    }

    /**
     * 摇床暂停
     */
    fun shakeBed() {
        viewModelScope.launch {
            val swing = SM.swing.value
            PopTip.show(
                if (swing) "摇床-已暂停" else "摇床-已恢复"
            )
            SM.swing(!swing)
        }
    }

    /**
     * 抗体保温
     */
    fun antibodyWarm() {
        viewModelScope.launch {
            // insulatingEnable false 未保温状态 true 保温状态
            // 发送设置温度命令 -> 更改按钮状态
            // 发送设置温度命令 如果当前是未保温状态发送设置中的温度，否则发送室温26度
            SM.setTemp(
                addr = 0, temp = if (_buttonFlow.value.insulating) "26"
                else _settings.value.temp.toString().removeZero()
            )
            // 更改按钮状态
            _buttonFlow.value = _buttonFlow.value.copy(
                insulating = !_buttonFlow.value.insulating
            )
        }
    }

    fun unlock() {
        viewModelScope.launch {
            _buttonFlow.value = _buttonFlow.value.copy(
                lock = false
            )
            SM.sendHex(
                index = 0, hex = V1.openLock()
            )
            delay(10 * 1000L)
            _buttonFlow.value = _buttonFlow.value.copy(
                lock = true
            )
            SM.sendHex(
                index = 0, hex = V1.closeLock()
            )
        }
    }

    private fun updateLog(module: Int, log: Log) {
        viewModelScope.launch {
            val state = flow(module)
            state.value = state.value.copy(log = log)
            LD.insert(log)
        }
    }
}

/**
 * 每个模块的状态
 */
data class ModuleUiState(
    val job: Job? = null,
    val program: Program? = null,
    val log: Log? = null,
    val time: String = Constants.ZERO_TIME,
    val status: String = "Active",
    val action: String = "/",
    val temp: String = "0.0℃",
)

/**
 * 右侧按钮的状态
 */
data class UiState(
    val insulating: Boolean = true,
    val temp: String = "0.0℃",
    val lock: Boolean = true,
    val container: Container = Container(),
)

data class Settings(
    val temp: Float = 5f,
    val recycle: Boolean = true,
)
