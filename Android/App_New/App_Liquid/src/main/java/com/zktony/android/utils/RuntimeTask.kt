package com.zktony.android.utils

import com.zktony.android.data.entities.OrificePlate
import com.zktony.android.data.entities.Program
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

/**
 * @author 刘贺贺
 * @date 2023/8/10 14:21
 */
class RuntimeTask {

    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _status = MutableStateFlow(RuntimeStatus.STOPPED)
    private val _orificePlate = MutableStateFlow(OrificePlate())
    private val _process = MutableStateFlow(0f)
    private val _selected = MutableStateFlow(emptyList<Pair<Int, Int>>())
    private val _state = MutableStateFlow(RuntimeState())
    private var program: Program? = null
    private var job: Job? = null

    val state = _state.asStateFlow()

    init {
        scope.launch {
            combine(
                _status,
                _orificePlate,
                _process,
                _selected,
            ) { status, orificePlate, process, selected ->
                RuntimeState(
                    status = status,
                    orificePlate = orificePlate,
                    process = process,
                    selected = selected
                )
            }.catch {
                it.printStackTrace()
            }.collect {
                _state.value = it
            }
        }
    }

    fun toggleProgram(program: Program) {
        this.program = program
        _orificePlate.value = program.orificePlates.getOrNull(0) ?: OrificePlate()
    }

    fun start() {
        scope.launch {
            _status.value = RuntimeStatus.RUNNING
            job = scope.launch {
                try {
                    if (program == null) {
                        throw Exception("Program is null")
                    }
                    if (program?.orificePlates.isNullOrEmpty()) {
                        throw Exception("OrificePlate is null")
                    }
                    val orificePlates = program!!.orificePlates
                    val total = orificePlates.sumOf {
                        it.orifices.flatten().filter { orifice -> orifice.selected }.size
                    }.toFloat()
                    var finished = 0

                    orificePlates.forEach { orificePlate ->
                        _orificePlate.value = orificePlate
                        for (i in 0 until orificePlate.column) {
                            for (j in 0 until orificePlate.row) {
                                while (_status.value == RuntimeStatus.PAUSED) {
                                    delay(100)
                                }
                                if (orificePlate.orifices[i][j].selected) {
                                    delay(1000)
                                    _selected.value += Pair(i, j)
                                    finished += 1
                                    _process.value = finished / total
                                }
                            }
                        }
                        _selected.value = emptyList()
                    }
                    _process.value = 0f
                    _status.value = RuntimeStatus.STOPPED
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (ex is CancellationException) {
                        return@launch
                    }
                    _status.value = RuntimeStatus.ERROR
                }
            }
        }
    }

    fun pause() {
        scope.launch {
            _status.value = RuntimeStatus.PAUSED
        }
    }

    fun resume() {
        scope.launch {
            _status.value = RuntimeStatus.RUNNING
        }
    }

    fun stop() {
        scope.launch {
            _status.value = RuntimeStatus.STOPPED
            _process.value = 0f
            job?.cancel()
        }
    }


    companion object {
        val instance: RuntimeTask by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RuntimeTask()
        }
    }

}

data class RuntimeState(
    val status: RuntimeStatus = RuntimeStatus.STOPPED,
    val orificePlate: OrificePlate = OrificePlate(),
    val process: Float = 0f,
    val selected: List<Pair<Int, Int>> = emptyList(),
)

enum class RuntimeStatus {
    RUNNING, STOPPED, PAUSED, ERROR
}