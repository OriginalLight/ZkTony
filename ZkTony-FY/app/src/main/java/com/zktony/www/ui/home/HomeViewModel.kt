package com.zktony.www.ui.home

import android.view.View
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.serialport.util.Serial.TTYS0
import com.zktony.serialport.util.Serial.TTYS3
import com.zktony.www.R
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.extractTemp
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.model.Queue
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.Log
import com.zktony.www.common.room.entity.Program
import com.zktony.www.common.room.entity.getActionEnum
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.repository.ActionRepository
import com.zktony.www.data.repository.LogRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
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
    private val proRepo: ProgramRepository,
    private val actionRepo: ActionRepository,
    private val logRepo: LogRepository,
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val serial = SerialManager.instance

    private val _proFlow = MutableStateFlow<List<Program>>(emptyList())
    private val _aFlow = MutableStateFlow(ModuleUiState())
    private val _bFlow = MutableStateFlow(ModuleUiState())
    private val _cFlow = MutableStateFlow(ModuleUiState())
    private val _dFlow = MutableStateFlow(ModuleUiState())
    private val _uiFlow = MutableStateFlow(UiState())
    val proFlow = _proFlow.asStateFlow()
    val aFlow = _aFlow.asStateFlow()
    val bFlow = _bFlow.asStateFlow()
    val cFlow = _cFlow.asStateFlow()
    val dFlow = _dFlow.asStateFlow()
    val uiFlow = _uiFlow.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                proRepo.getAll().collect {
                    _proFlow.value = it
                    onProgramChange(it)
                }
            }
            launch {
                // 串口四flow
                serial.ttys3Flow.collect {
                    it?.let {
                        if (it.startsWith("TC1:TCACTUALTEMP=")) {
                            // 读取温度
                            val address = it.substring(it.length - 2, it.length - 1).toInt()
                            val temp = it.extractTemp()
                            when (address) {
                                1 -> _aFlow.value = _aFlow.value.copy(temp = "$temp ℃")
                                2 -> _bFlow.value = _bFlow.value.copy(temp = "$temp ℃")
                                3 -> _cFlow.value = _cFlow.value.copy(temp = "$temp ℃")
                                4 -> _dFlow.value = _dFlow.value.copy(temp = "$temp ℃")
                                0 -> _uiFlow.value = _uiFlow.value.copy(temp = "$temp ℃")
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
                        serial = TTYS3, text = V1.saveTemp(
                            addr = i.toString(), temp = if (i == 0) "3" else "26"
                        )
                    )
                    delay(200L)
                    serial.sendText(
                        serial = TTYS3, text = V1.setTemp(
                            addr = i.toString(), temp = if (i == 0) "3" else "26"
                        )
                    )
                }
                // 每十秒钟查询一次温度
                while (true) {
                    for (i in 0..4) {
                        delay(200L)
                        serial.sendText(
                            serial = TTYS3, text = V1.queryTemp(i.toString())
                        )
                    }
                    delay(2 * 1000L)
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
                        startEnable = programList[0].actionCount > 0,
                    )
                } else {
                    if (programList.find { it.id == state.value.program!!.id } == null) {
                        state.value = state.value.copy(
                            program = programList[0],
                            startEnable = programList[0].actionCount > 0,
                        )
                    } else {
                        state.value = state.value.copy(
                            program = programList.find { it.id == state.value.program!!.id },
                            startEnable = programList.find { it.id == state.value.program!!.id }!!.actionCount > 0
                        )
                    }
                }
            }
        } else {
            _aFlow.value = _aFlow.value.copy(program = null, startEnable = false)
            _bFlow.value = _bFlow.value.copy(program = null, startEnable = false)
            _cFlow.value = _cFlow.value.copy(program = null, startEnable = false)
            _dFlow.value = _dFlow.value.copy(program = null, startEnable = false)
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
                program = _proFlow.value[index],
                startEnable = _proFlow.value[index].actionCount > 0,
                status = "已就绪",
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
                startVisible = View.GONE,
                stopVisible = View.VISIBLE,
                selectEnable = false,
                status = "运行中",
            )
            // 创建job
            val job = launch {
                // 将程序中的所有步骤排序放入队列
                val actionQueue = Queue<Action>()
                actionRepo.getByProgramId(state.value.program!!.id).first().forEach {
                    actionQueue.enqueue(it)
                }
                // 创建程序执行者
                val runner = ProgramExecutor(
                    queue = actionQueue, module = module, settings = appViewModel.settings.value
                )
                // 收集执行者的状态
                launch {
                    runner.event.collect {
                        when (it) {
                            is ActionEvent.CurrentAction -> {
                                state.value = state.value.copy(
                                    action = getActionEnum(it.action.mode).value
                                )
                            }
                            is ActionEvent.Time -> {
                                state.value = state.value.copy(time = it.time)
                            }
                            is ActionEvent.Finish -> {
                                state.value.log?.let { log ->
                                    logRepo.update(log.copy(status = 1))
                                }
                                state.value = state.value.copy(
                                    status = "已完成", time = "已完成", log = null
                                )
                                stop(it.module)
                            }
                            is ActionEvent.Count -> {
                                state.value = state.value.copy(
                                    action = if (state.value.action.startsWith(
                                            "洗涤"
                                        )
                                    ) "洗涤 X${it.count}" else state.value.action
                                )
                            }
                            is ActionEvent.Wait -> {
                                state.value = state.value.copy(time = it.msg)
                            }
                        }
                    }
                }
                runner.run()
            }
            // 更新状态中的job
            launch {
                // 取消之前的job，防止多个job同时执行
                state.value.job?.cancel()
                // 创建日志
                val log = Log(
                    programName = state.value.program!!.name,
                    module = module,
                    actions = state.value.program!!.actions,
                )
                logRepo.insert(log)
                // 更新状态中的job和日志
                state.value = state.value.copy(
                    job = job, log = log
                )
                // 更新当前程序的运行次数
                proRepo.update(
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
    fun stop(module: Int) {
        viewModelScope.launch {
            // 获取对应模块的状态
            val state = flow(module)
            state.value.run {
                // 取消协程
                job?.cancel()
                // 非正常停止删除日志
                log?.let { log ->
                    logRepo.delete(log)
                }
            }
            // 更新状态
            state.value = state.value.copy(
                job = null,
                startVisible = View.VISIBLE,
                stopVisible = View.GONE,
                selectEnable = true,
                status = if (state.value.status != "已完成") "已就绪" else state.value.status,
                action = "/",
                time = if (state.value.status != "已完成") Constants.ZERO_TIME else state.value.time,
            )
            // 等待一下，当还有没有其他程序在运行中时
            // 暂停摇床
            // 恢复到室温
            // 复位
            delay(200L)
            if (serial.executing == 0) {
                // 暂停摇床
                serial.sendHex(
                    serial = TTYS0, hex = V1.pauseShakeBed()
                )
                // 恢复到室温
                for (i in 1..4) {
                    delay(200L)
                    serial.sendText(
                        serial = TTYS3, text = V1.setTemp(
                            addr = i.toString(), temp = "26"
                        )
                    )
                }
                serial.reset()
            }
        }
    }

    /**
     * 机构复位
     */
    fun reset() {
        viewModelScope.launch {
            // 如果有正在执行的程序，提示用户
            if (serial.executing == 0) {
                if (serial.runtimeLock.value) {
                    PopTip.show("运动中禁止复位")
                } else {
                    serial.reset()
                    PopTip.show(R.mipmap.ic_reset, "复位-已下发")
                }
            } else {
                PopTip.show("请中止所有运行中程序")
            }
        }
    }

    /**
     * 摇床暂停
     */
    fun shakeBed() {
        viewModelScope.launch {
            // pauseEnable false 未暂停 true 暂停
            // 发送指令 -> 更新状态
            // 发送指令 如果是未暂停，发送暂停命令，如果是暂停，发送继续命令
            serial.sendHex(
                serial = TTYS0, hex = if (_uiFlow.value.pause) V1.resumeShakeBed()
                else V1.pauseShakeBed()
            )
            // 更新状态
            _uiFlow.value = _uiFlow.value.copy(
                pause = !_uiFlow.value.pause
            )
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
            serial.sendText(
                serial = TTYS3, text = V1.setTemp(
                    addr = "0", temp = if (_uiFlow.value.insulating) "26"
                    else appViewModel.settings.value.temp.toString().removeZero()
                )
            )
            // 更改按钮状态
            _uiFlow.value = _uiFlow.value.copy(
                insulating = !_uiFlow.value.insulating
            )
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
    val selectEnable: Boolean = true,
    val startEnable: Boolean = false,
    val startVisible: Int = View.VISIBLE,
    val stopVisible: Int = View.GONE,
    val time: String = Constants.ZERO_TIME,
    val status: String = "已就绪",
    val action: String = "/",
    val temp: String = "0.0℃",
)

/**
 * 右侧按钮的状态
 */
data class UiState(
    val pause: Boolean = false,
    val insulating: Boolean = false,
    val temp: String = "0.0℃",
)
