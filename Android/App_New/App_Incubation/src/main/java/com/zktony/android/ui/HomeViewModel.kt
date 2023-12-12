package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.History
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.IncubationStage
import com.zktony.android.data.entities.internal.Log
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.readWithTemperature
import com.zktony.android.utils.SerialPortUtils.writeRegister
import com.zktony.android.utils.SerialPortUtils.writeWithPulse
import com.zktony.android.utils.SerialPortUtils.writeWithTemperature
import com.zktony.android.utils.SerialPortUtils.writeWithValve
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    private val historyDao: HistoryDao,
    private val dataStore: DataSaverDataStore
) : ViewModel() {

    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())
    private val _selected = MutableStateFlow(0)
    private val _insulation = MutableStateFlow(List(9) { 0.0 })
    private val _shaker = MutableStateFlow(1)
    private val _stateList = MutableStateFlow<List<IncubationState>>(emptyList())
    private val _cleanJob = MutableStateFlow(0)

    private val jobMap = hashMapOf<Int, Job>()
    private val lock = AtomicInteger(-1)
    private val logList = mutableListOf<Log>()
    private var job: Job? = null

    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val selected = _selected.asStateFlow()
    val insulation = _insulation.asStateFlow()
    val shaker = _shaker.asStateFlow()
    val stateList = _stateList.asStateFlow()
    val cleanJob = _cleanJob.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            // 根据模块数量配置
            val coll = dataStore.readData(Constants.ZT_0000, 4)
            repeat(coll / 2) {
                try {
                    writeWithValve(it, 1)
                } catch (ex: Exception) {
                    _uiFlags.value = UiFlags.message(ex.message ?: "Unknown")
                }
                delay(300L)
            }
            // 设置初始温度
            launch {
                writeWithTemperature(0, dataStore.readData(Constants.ZT_0001, 4.0))
            }
            // 设置定时查询温度
            while (true) {
                delay(3000L)
                repeat(coll + 1) {
                    readWithTemperature(it) { address, temp ->
                        _insulation.value = _insulation.value.toMutableList().apply {
                            this[address] = temp
                        }
                    }
                    delay(100L)
                }
            }
        }
    }

    fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is HomeIntent.NavTo -> _page.value = intent.page
            is HomeIntent.Selected -> _selected.value = intent.id
            is HomeIntent.Start -> start()
            is HomeIntent.Stop -> stop()
            is HomeIntent.Stages -> stages(intent.index, intent.program)
            is HomeIntent.Shaker -> shaker()
            is HomeIntent.AutoClean -> autoClean()
        }
    }

    private fun start() {
        viewModelScope.launch {
            // check
            val current = _selected.value
            val state = _stateList.value.find { it.index == current }
            if (state == null) {
                _uiFlags.value = UiFlags.message("WARN 请选择一个程序")
                return@launch
            } else {
                if (state.id == 0L) {
                    _uiFlags.value = UiFlags.message("WARN 请选择一个程序")
                    return@launch
                }
                if (state.stages.isEmpty()) {
                    _uiFlags.value = UiFlags.message("WARN 程序中不存在孵育流程")
                    return@launch
                }
                if (!state.isStopped()) {
                    _uiFlags.value = UiFlags.message("WARN 该程序正在运行中")
                    return@launch
                }
                updateState(state.copy(flags = 1, stages = state.stages.map { it.copy(flags = 2) }))
                jobMap[current] = viewModelScope.launch {
                    interpreter(current, state)
                }
            }
        }
    }

    private fun stop() {
        viewModelScope.launch {
            if (lock.get() == _selected.value) {
                lock.set(-1)
            }
            jobMap.remove(_selected.value)?.cancel()
            _stateList.value.find { it.index == _selected.value }?.let { state ->
                updateState(state.copy(flags = 2, stages = state.stages.map { it.copy(flags = 2) }))
            }
            if (jobMap.isEmpty()) {
                _shaker.value = 4
                // 停止摇床
                writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
                delay(300L)
                writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
                dataStore.readData(Constants.ZT_0005, 0.0).takeIf { it > 0.0 }?.let {
                    delay(3500L)
                    writeRegister(startAddr = 222, slaveAddr = 0, value = (it * 6400).toLong())
                }
                _shaker.value = 1
            }
        }
    }

    private fun stages(index: Int, program: Program) {
        viewModelScope.launch {
            updateState(
                IncubationState(
                    index = index,
                    id = program.id,
                    stages = program.stages
                )
            )
        }
    }

    private fun shaker() {
        viewModelScope.launch {
            try {
                when (_shaker.value) {
                    0 -> {
                        _shaker.value = 4
                        writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
                        delay(300L)
                        writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
                        dataStore.readData(Constants.ZT_0005, 0.0).takeIf { it > 0.0 }?.let {
                            delay(3500L)
                            writeRegister(
                                startAddr = 222,
                                slaveAddr = 0,
                                value = (it * 6400).toLong()
                            )
                        }
                        _shaker.value = 1
                    }

                    4 -> {
                        _uiFlags.value = UiFlags.message("WARN 请不要重复点击")
                    }

                    else -> {
                        writeRegister(slaveAddr = 0, startAddr = 200, value = 1)
                        _shaker.value = 0
                    }
                }
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message(ex.message ?: "Unknown")
            }
        }
    }

    /**
     * 解释器
     *
     * 解释每个进程
     */
    private suspend fun interpreter(index: Int, state: IncubationState) {
        try {
            state.stages.forEach { stage ->
                when (stage.type) {
                    0 -> blocking(index, stage)
                    1 -> primaryAntibody(index, stage)
                    2 -> secondaryAntibody(index, stage)
                    3 -> washing(index, stage)
                    4 -> phosphateBufferedSaline(index, stage)
                }
            }
            // 更新模块显示
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 2))
            }
            jobMap.remove(index)
        } catch (ex: Exception) {
            if (ex !is CancellationException) {
                _uiFlags.value = UiFlags.error(ex.message ?: "Unknown")
                logList.add(Log(index = index, message = ex.message ?: "Unknown", level = "ERROR"))
            }
            if (lock.get() == index) {
                lock.set(-1)
            }
            jobMap.remove(index)?.cancel()
            updateState(state.copy(flags = 0))
        } finally {
            storageLog()
        }
    }

    private suspend fun blocking(index: Int, stage: IncubationStage) {
        // check
        if (stage.dosage == 0.0) {
            throw Exception("ERROR 0X0003 - 加液量为零")
        }

        // 加液
        waitForLock(index) {
            // 更新模块状态
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 4))
            }
            // 加液
            liquid(index, stage, 9, (index - ((index / 4) * 4)) + 1)
            // 更新模块状态和进程状态
            _stateList.value.find { it.index == index }?.let { state ->
                updateState(
                    state.copy(
                        flags = 1,
                        stages = state.stages.map {
                            if (it.uuid == stage.uuid) it.copy(flags = 1) else it
                        })
                )
            }
            // 记录日志
            logList.add(
                Log(
                    index = index,
                    message = "INFO 添加封闭液完成 √ \n加液量：${stage.dosage} \n进液通道：9 \n出液通道：${(index - ((index / 4) * 4)) + 1}"
                )
            )
        }

        // 倒计时
        countDown(index, (stage.duration * 60 * 60).toLong())

        waitForLock(index) {
            // 更新模块状态
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 5))
            }
            // 清理废液
            clean(index, stage, 11, (index - ((index / 4) * 4)) + 1)
            // 更新模块状态和进程状态
            _stateList.value.find { it.index == index }?.let { state ->
                updateState(
                    state.copy(
                        flags = 1,
                        stages = state.stages.map {
                            if (it.uuid == stage.uuid) it.copy(flags = 0) else it
                        })
                )
            }
            // 记录日志
            logList.add(
                Log(
                    index = index,
                    message = "INFO 封闭液废液处理完成 √ \n进液通道：11 \n出液通道：${(index - ((index / 4) * 4)) + 1}"
                )
            )
        }
    }

    private suspend fun primaryAntibody(index: Int, stage: IncubationStage) {
        // check
        if (stage.dosage == 0.0) {
            throw Exception("ERROR 0X0003 - 加液量为零")
        }

        waitForLock(index) {
            // 更新模块状态
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 4))
            }
            // 加液
            val inChannel =
                if (stage.origin == 0) (index - ((index / 4) * 4)) + 1 else stage.origin
            liquid(index, stage, inChannel, (index - ((index / 4) * 4)) + 1)
            // 更新模块状态和进程状态
            _stateList.value.find { it.index == index }?.let { state ->
                updateState(
                    state.copy(
                        flags = 1,
                        stages = state.stages.map {
                            if (it.uuid == stage.uuid) it.copy(flags = 1) else it
                        })
                )
            }
            // 记录日志
            logList.add(
                Log(
                    index = index,
                    message = "INFO 添加一抗完成 √ \n加液量：${stage.dosage} \n进液通道：$inChannel \n出液通道：${(index - ((index / 4) * 4)) + 1}"
                )
            )
        }

        // 倒计时
        countDown(index, (stage.duration * 60 * 60).toLong())

        waitForLock(index) {
            // 更新模块状态
            val inChannel =
                if (stage.origin == 0) (index - (index / 4) * 4) + 1 else stage.origin
            if (stage.recycle) {
                _stateList.value.find { it.index == index }?.let {
                    updateState(it.copy(flags = 6))
                }
                clean(index, stage, inChannel, (index - ((index / 4) * 4)) + 1)
            } else {
                _stateList.value.find { it.index == index }?.let {
                    updateState(it.copy(flags = 5))
                }
                clean(index, stage, 11, (index - ((index / 4) * 4)) + 1)
            }
            // 更新模块状态和进程状态
            _stateList.value.find { it.index == index }?.let { state ->
                updateState(
                    state.copy(
                        flags = 1,
                        stages = state.stages.map {
                            if (it.uuid == stage.uuid) it.copy(flags = 0) else it
                        })
                )
            }
            // 记录日志
            logList.add(
                Log(
                    index = index,
                    message = "INFO 一抗废液${if (stage.recycle) "回收" else "清理"}完成 √ \n进液通道：${if (stage.recycle) inChannel else 11} \n出液通道：${(index - ((index / 4) * 4)) + 1}"
                )
            )
        }
    }

    private suspend fun secondaryAntibody(index: Int, stage: IncubationStage) {
        // check
        if (stage.dosage == 0.0) {
            throw Exception("ERROR 0X0003 - 加液量为零")
        }

        waitForLock(index) {
            // 更新模块状态
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 4))
            }
            // 加液
            val inChannel =
                if (stage.origin == 0) (index - ((index / 4) * 4)) + 5 else stage.origin
            liquid(index, stage, inChannel, (index - ((index / 4) * 4)) + 1)
            // 更新模块状态和进程状态
            _stateList.value.find { it.index == index }?.let { state ->
                updateState(
                    state.copy(
                        flags = 1,
                        stages = state.stages.map {
                            if (it.uuid == stage.uuid) it.copy(flags = 1) else it
                        })
                )
            }
            // 记录日志
            logList.add(
                Log(
                    index = index,
                    message = "INFO 添加二抗完成 √ \n加液量：${stage.dosage} \n进液通道：$inChannel \n出液通道：${(index - ((index / 4) * 4)) + 1}"
                )
            )
        }

        // 倒计时
        countDown(index, (stage.duration * 60 * 60).toLong())

        waitForLock(index) {
            // 更新模块状态
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 5))
            }
            // 清理废液
            clean(index, stage, 11, (index - ((index / 4) * 4)) + 1)
            // 更新模块状态和进程状态
            _stateList.value.find { it.index == index }?.let { state ->
                updateState(
                    state.copy(
                        flags = 1,
                        stages = state.stages.map {
                            if (it.uuid == stage.uuid) it.copy(flags = 0) else it
                        })
                )
            }
            // 记录日志
            logList.add(
                Log(
                    index = index,
                    message = "INFO 二抗废液清理完成 √ \n进液通道：11 \n出液通道：${(index - ((index / 4) * 4)) + 1}"
                )
            )
        }
    }

    private suspend fun washing(index: Int, stage: IncubationStage) {
        // check
        if (stage.dosage == 0.0) {
            throw Exception("ERROR 0X0003 - 加液量为零")
        }

        repeat(stage.times) {
            waitForLock(index) {
                // 更新模块状态
                _stateList.value.find { it.index == index }?.let {
                    updateState(it.copy(flags = 4))
                }
                // 加液
                liquid(index, stage, 10, (index - ((index / 4) * 4)) + 1)
                // 更新模块状态和进程状态
                _stateList.value.find { it.index == index }?.let { state ->
                    updateState(
                        state.copy(
                            flags = 1,
                            stages = state.stages.map {
                                if (it.uuid == stage.uuid) it.copy(flags = 1) else it
                            })
                    )
                }
            }

            // 倒计时
            countDown(index, (stage.duration * 60).toLong())

            waitForLock(index) {
                // 更新模块状态
                _stateList.value.find { it.index == index }?.let {
                    updateState(it.copy(flags = 5))
                }
                // 清理废液
                clean(index, stage, 11, (index - ((index / 4) * 4)) + 1)
                logList.add(
                    Log(
                        index = index,
                        message = "INFO 第${it + 1}次洗涤完成 √ \n进液通道：11 \n出液通道：${(index - ((index / 4) * 4)) + 1}"
                    )
                )
            }
        }

        // 更新模块状态
        _stateList.value.find { it.index == index }?.let { state ->
            updateState(
                state.copy(
                    flags = 1,
                    stages = state.stages.map {
                        if (it.uuid == stage.uuid) it.copy(flags = 0) else it
                    })
            )
        }
    }

    private suspend fun phosphateBufferedSaline(index: Int, stage: IncubationStage) {
        // check
        if (stage.dosage == 0.0) {
            throw Exception("ERROR 0X0003 - 加液量为零")
        }

        waitForLock(index) {
            // 更新模块状态
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 4))
            }
            // 加液
            liquid(index, stage, 10, (index - ((index / 4) * 4)) + 1)
            // 更新模块状态和进程状态
            _stateList.value.find { it.index == index }?.let { state ->
                updateState(
                    state.copy(
                        flags = 1,
                        stages = state.stages.map {
                            if (it.uuid == stage.uuid) stage.copy(flags = 0) else it
                        })
                )
            }
            logList.add(
                Log(
                    index = index,
                    message = "添加缓冲液完成"
                )
            )
        }
    }

    private suspend fun liquid(
        index: Int,
        stage: IncubationStage,
        inChannel: Int,
        outChannel: Int
    ) {
        val group = index / 4
        val pulse = (AppStateUtils.hpc[group + 1] ?: { x -> x * 100 }).invoke(stage.dosage)
        val inAddr = 2 * group
        val outAddr = 2 * group + 1
        val rIn = dataStore.readData(
            when (stage.type) {
                0 -> Constants.ZT_0003
                1, 2 -> Constants.ZT_0002
                3, 4 -> Constants.ZT_0004
                else -> Constants.ZT_0002
            }, 0.0
        )
        val rOut = dataStore.readData(Constants.ZT_0002, 0.0)
        // 设置温度
        viewModelScope.launch {
            writeWithTemperature(index + 1, stage.temperature)
        }
        // 打开摇床
        writeRegister(slaveAddr = 0, startAddr = 200, value = 1)
        _shaker.value = 0
        // 切阀加液
        delay(100L)
        writeWithValve(inAddr, inChannel)
        writeWithValve(outAddr, outChannel)
        writeWithPulse(group + 1, (pulse + (rIn * 6400)).toLong())
        // 回收残留液体
        if (rIn > 0.0) {
            // 后边段残留的液体
            delay(500L)
            writeWithValve(inAddr, 12)
            writeWithValve(outAddr, outChannel)
            writeWithPulse(group + 1, (rOut * 6400 * 9).toLong())
            delay(500L)
            // 退回前半段残留的液体
            writeWithValve(inAddr, inChannel)
            writeWithValve(outAddr, 6)
            writeWithPulse(group + 1, -(rIn * 6400 * 3).toLong())
        }
        // 清理管路避免污染
        if (stage.type == 1 || stage.type == 2) {
            val rx = dataStore.readData(Constants.ZT_0004, 0.0)
            if (rx > 0.0) {
                delay(500L)
                writeWithValve(inAddr, 10)
                writeWithValve(outAddr, 5)
                writeWithPulse(group + 1, (rx * 6400 * 3).toLong())
                delay(500L)
                writeWithValve(inAddr, 12)
                writeWithValve(outAddr, 5)
                writeWithPulse(group + 1, (rx * 6400 * 3).toLong())
                delay(500L)
                writeWithValve(inAddr, 10)
                writeWithValve(outAddr, 6)
                writeWithPulse(group + 1, -(rx * 6400 * 3).toLong())
            }
        }
    }

    private suspend fun clean(
        index: Int,
        stage: IncubationStage,
        inChannel: Int,
        outChannel: Int
    ) {
        val group = index / 4
        val pulse = (AppStateUtils.hpc[group + 1] ?: { x -> x * 100 }).invoke(stage.dosage)
        val inAddr = 2 * group
        val outAddr = 2 * group + 1
        val rIn = dataStore.readData(
            when (stage.type) {
                0 -> Constants.ZT_0003
                1, 2 -> Constants.ZT_0002
                3, 4 -> Constants.ZT_0004
                else -> Constants.ZT_0002
            }, 0.0
        )
        val rOut = dataStore.readData(Constants.ZT_0002, 0.0)
        // 停止摇床
        _shaker.value = 4
        writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
        delay(300L)
        writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
        dataStore.readData(Constants.ZT_0006, 0.0).takeIf { it > 0.0 }?.let {
            delay(3500L)
            writeRegister(startAddr = 222, slaveAddr = 0, value = (it * 6400).toLong())
        }
        delay(1000L)
        _shaker.value = 2
        // 切阀回收残留液体
        writeWithValve(inAddr, inChannel)
        writeWithValve(outAddr, outChannel)
        writeWithPulse(group + 1, -(pulse * 2 + ((rIn + rOut * 3) * 6400 * 2)).toLong())
        // 清理管路避免污染
        if (stage.type == 1 || stage.type == 2) {
            val rx = dataStore.readData(Constants.ZT_0004, 0.0)
            if (rx > 0.0) {
                delay(500L)
                writeWithValve(inAddr, 10)
                writeWithValve(outAddr, 5)
                writeWithPulse(group + 1, (rx * 6400 * 3).toLong())
                delay(500L)
                writeWithValve(inAddr, 12)
                writeWithValve(outAddr, 5)
                writeWithPulse(group + 1, (rx * 6400 * 3).toLong())
                delay(500L)
                writeWithValve(inAddr, 10)
                writeWithValve(outAddr, 6)
                writeWithPulse(group + 1, -(rx * 6400 * 3).toLong())
            }
        }
    }

    private suspend fun waitForLock(index: Int, block: suspend () -> Unit) {
        if (lock.get() >= 0) {
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(flags = 3))
            }
            while (lock.get() >= 0) {
                delay(1000L)
            }
        }
        lock.set(index)
        block()
        lock.set(-1)
    }

    private suspend fun countDown(index: Int, allTime: Long) {
        repeat(allTime.toInt()) { time ->
            _stateList.value.find { it.index == index }?.let {
                updateState(it.copy(time = allTime - time))
            }
            delay(1000L)
        }
    }

    private fun updateState(state: IncubationState) {
        val stateList = _stateList.value.toMutableList()
        stateList.indexOfFirst { it.index == state.index }.takeIf { it >= 0 }?.let {
            stateList.removeAt(it)
        }
        stateList.add(state)
        _stateList.value = stateList
    }

    private fun storageLog() {
        viewModelScope.launch {
            if (jobMap.isEmpty() && logList.isNotEmpty()) {
                val startTime =
                    (logList.firstOrNull() ?: Log(message = "None")).createTime
                val logs = logList.sortedBy { it.index }
                historyDao.insert(History(logs = logs, createTime = startTime))
                // 清空日志
                logList.clear()
            }
        }
    }

    private fun autoClean() {
        if (job == null) {
            job = viewModelScope.launch {
                _cleanJob.value = 1
                val r1 = dataStore.readData(Constants.ZT_0002, 0.0)
                val r2 = dataStore.readData(Constants.ZT_0003, 0.0)
                val r3 = dataStore.readData(Constants.ZT_0004, 0.0)
                val pulse = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(20000.0)

                try {
                    _shaker.value = 4
                    writeRegister(slaveAddr = 0, startAddr = 200, value = 0)
                    delay(300L)
                    writeRegister(slaveAddr = 0, startAddr = 201, value = 45610)
                    dataStore.readData(Constants.ZT_0006, 0.0).takeIf { it > 0.0 }?.let {
                        delay(3500L)
                        writeRegister(
                            startAddr = 222,
                            slaveAddr = 0,
                            value = (it * 6400).toLong()
                        )
                    }
                    _shaker.value = 2

                    delay(300L)
                    repeat(4) { index ->
                        // 切阀加液进孵育盒
                        writeWithValve(0, 10)
                        writeWithValve(1, index + 1)
                        writeWithPulse(1, (pulse + (r3 * 6400)).toLong())
                        delay(500L)
                        //切阀排空后半段
                        writeWithValve(0, 12)
                        writeWithValve(1, index + 1)
                        writeWithPulse(1, (r1 * 6400 * 4).toLong())
                        delay(500L)
                        // 切阀放到一抗容器
                        writeWithValve(0, index + 1)
                        writeWithValve(1, index + 1)
                        writeWithPulse(1, -(pulse * 1.5 + (r1 * 6400 * 4)).toLong())
                        delay(500L)
                        // 切阀加液进孵育盒
                        writeWithValve(0, 10)
                        writeWithValve(1, index + 1)
                        writeWithPulse(1, pulse.toLong())
                        delay(500L)
                        //切阀排空后半段
                        writeWithValve(0, 12)
                        writeWithValve(1, index + 1)
                        writeWithPulse(1, (r1 * 6400 * 4).toLong())
                        delay(500L)
                        //切阀排空前半段
                        writeWithValve(0, 10)
                        writeWithValve(1, 6)
                        writeWithPulse(1, -(r3 * 6400 * 3).toLong())
                        delay(500L)
                        // 切阀放到二抗容器
                        writeWithValve(0, index + 5)
                        writeWithValve(1, index + 1)
                        writeWithPulse(1, -(pulse * 1.5 + (r1 * 6400 * 4)).toLong())
                        delay(500L)
                        // 切阀排空一抗容器
                        writeWithValve(0, index + 1)
                        writeWithValve(1, 5)
                        writeWithPulse(1, (pulse * 1.5 + (r1 * 6400 * 4)).toLong())
                        delay(500L)
                        // 切阀排空二抗容器
                        writeWithValve(0, index + 5)
                        writeWithValve(1, 5)
                        writeWithPulse(1, (pulse * 1.5 + (r1 * 6400 * 4)).toLong())
                        delay(500L)
                    }
                    // 洗涤封闭液管路
                    writeWithValve(0, 9)
                    writeWithValve(1, 5)
                    writeWithPulse(1, (pulse + (r2 * 6400)).toLong())
                    delay(500L)
                    // 切阀排空后半段
                    writeWithValve(0, 12)
                    writeWithValve(1, 5)
                    writeWithPulse(1, (r2 * 6400 * 3).toLong())
                    delay(500L)
                    // 切阀排空前半段
                    writeWithValve(0, 9)
                    writeWithValve(1, 6)
                    writeWithPulse(1, -(r2 * 6400 * 3).toLong())
                    _cleanJob.value = 2
                } catch (ex: Exception) {
                    if (ex !is CancellationException) {
                        _uiFlags.value = UiFlags.error(ex.message ?: "Unknown")
                    }
                    writeRegister(slaveAddr = 1, startAddr = 200, value = 0)
                    _cleanJob.value = 0
                }
            }
        } else {
            job?.cancel()
            job = null
        }
    }
}

sealed class HomeIntent {
    data class NavTo(val page: Int) : HomeIntent()
    data class Flags(val uiFlags: UiFlags) : HomeIntent()
    data class Stages(val index: Int, val program: Program) : HomeIntent()
    data class Selected(val id: Int) : HomeIntent()
    data object Start : HomeIntent()
    data object Stop : HomeIntent()
    data object Shaker : HomeIntent()
    data object AutoClean : HomeIntent()
}

data class IncubationState(
    val id: Long = 0L,
    val index: Int = 0,
    val stages: List<IncubationStage> = listOf(),
    // 0 未开始 1 运行中 2 已结束 3 等待中 4 液位 5 废液 6 回收
    val flags: Int = 0,
    val time: Long = 0L
) {
    fun isStopped() = flags == 0 || flags == 2
}