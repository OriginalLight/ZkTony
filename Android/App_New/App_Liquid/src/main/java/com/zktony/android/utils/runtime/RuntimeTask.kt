package com.zktony.android.utils.runtime

import com.zktony.android.data.entities.OrificePlate
import com.zktony.android.data.entities.Program
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlin.math.ceil

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
                    if (program == null) throw Exception("Program is null")
                    if (program?.orificePlates.isNullOrEmpty()) throw Exception("OrificePlate is null")
                    val orificePlates = program!!.orificePlates

                    val total = orificePlates.sumOf {
                        it.orifices.flatten().filter { orifice -> orifice.selected }.size
                    }.toFloat()
                    var finished = 0

                    orificePlates.forEach { orificePlate ->
                        if (orificePlate.type == 0) {
                            separationAlgorithm(orificePlate) {
                                finished += it
                                _process.value = finished / total
                            }
                        } else {
                            hybridAlgorithm(orificePlate) {
                                finished += it
                                _process.value = finished / total
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (ex is CancellationException) {
                        _selected.value = emptyList()
                        _status.value = RuntimeStatus.STOPPED
                        _process.value = 0f
                    } else {
                        _status.value = RuntimeStatus.ERROR
                    }
                } finally {
                    _selected.value = emptyList()
                    _status.value = RuntimeStatus.STOPPED
                    _process.value = 0f
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
            job?.cancel()
        }
    }

    private suspend fun separationAlgorithm(
        orificePlate: OrificePlate,
        block: (Int) -> Unit
    ) {
        val row = orificePlate.row
        val column = orificePlate.column
        for (i in 0 until ceil(row / 6.0).toInt()) {
            for (j in if (i % 2 == 0) 0 until column else column - 1 downTo 0) {
                val coordinate = orificePlate.orifices[j][i * 6].coordinate
                tx {
                    move {
                        index = 0
                        dv = coordinate.abscissa
                    }
                    move {
                        index = 1
                        dv = coordinate.ordinate
                    }
                }

                val list = mutableListOf<Pair<Int, Int>>()

                tx {
                    repeat(6) {
                        if (i * 6 + it < row) {
                            val orifice = orificePlate.orifices[j][i * 6 + it]
                            if (orifice.selected) {
                                move {
                                    index = 2 + it
                                    dv = orifice.volume.getOrNull(0) ?: 0.0
                                }
                                list += Pair(j, i * 6 + it)
                            }
                        }
                    }
                }
                _selected.value += list
                block(list.size)

                if (orificePlate.delay > 0) {
                    delay(orificePlate.delay)
                }
            }
        }
        _selected.value = emptyList()
    }

    private suspend fun hybridAlgorithm(
        orificePlate: OrificePlate,
        block: (Int) -> Unit
    ) {
        val row = orificePlate.row
        val column = orificePlate.column
        val coordinate = orificePlate.coordinate
        val rowSpace = (coordinate[1].abscissa - coordinate[0].abscissa) / (row - 1)
        for (i in 0 until row + 5) {
            for (j in if (i % 2 == 0) 0 until column else column - 1 downTo 0) {
                val abscissa = if (i < 6) {
                    orificePlate.orifices[j][0].coordinate.abscissa - (5 - i) * rowSpace
                } else {
                    orificePlate.orifices[j][i - 5].coordinate.abscissa
                }

                tx {
                    move {
                        index = 0
                        dv = abscissa
                    }
                    move {
                        index = 1
                        dv = orificePlate.orifices[j][0].coordinate.ordinate
                    }
                }

                val list = mutableListOf<Pair<Int, Int>>()

                tx {
                    repeat(6) {
                        if (i - 5 + it in 0 until row) {
                            val orifice = orificePlate.orifices[j][i - 5 + it]
                            if (orifice.selected) {
                                move {
                                    index = 2 + it
                                    dv = orifice.volume.getOrNull(it) ?: 0.0
                                }
                                if (it == 0) {
                                    list += Pair(j, i - 5)
                                }
                            }
                        }
                    }
                }

                _selected.value += list
                block(list.size)

                if (orificePlate.delay > 0) {
                    delay(orificePlate.delay)
                }
            }
        }

    }

    companion object {
        val instance: RuntimeTask by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RuntimeTask()
        }
    }
}