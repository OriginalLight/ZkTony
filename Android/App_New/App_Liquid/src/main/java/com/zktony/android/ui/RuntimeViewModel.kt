package com.zktony.android.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.entities.OrificePlate
import com.zktony.android.data.entities.Program
import com.zktony.android.utils.extra.serial
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * @author 刘贺贺
 * @date 2023/8/25 13:04
 */
class RuntimeViewModel : ViewModel() {
    private val _status = MutableStateFlow(RuntimeStatus.STOPPED)
    private val _orificePlate = MutableStateFlow(OrificePlate())
    private val _process = MutableStateFlow(0f)
    private val _selected = MutableStateFlow(emptyList<Triple<Int, Int, Color>>())
    private val _state = MutableStateFlow(RuntimeState())
    private var program: Program? = null
    private var job: Job? = null

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
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
        viewModelScope.launch {
            _status.value = RuntimeStatus.RUNNING
            job = viewModelScope.launch {
                try {
                    if (program == null) throw Exception("Program is null")
                    if (program?.orificePlates.isNullOrEmpty()) throw Exception("OrificePlate is null")
                    val orificePlates = program!!.orificePlates

                    val total = orificePlates.sumOf {
                        it.orifices.flatten().filter { orifice -> orifice.selected }.size
                    }.toFloat()
                    var finished = 0

                    orificePlates.forEach { orificePlate ->
                        _orificePlate.value = orificePlate

                        if (orificePlate.type == 0) {
                            separationAlgorithm(orificePlate) {
                                finished += it
                                _process.value = finished / total
                            }
                        } else {
                            hybridAlgorithm(orificePlate) {
                                finished += it
                                _process.value = finished / (total * 6)
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
        viewModelScope.launch {
            _status.value = RuntimeStatus.PAUSED
        }
    }

    fun resume() {
        viewModelScope.launch {
            _status.value = RuntimeStatus.RUNNING
        }
    }

    fun stop() {
        viewModelScope.launch {
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
                while (_status.value == RuntimeStatus.PAUSED) {
                    delay(100)
                }
                val coordinate = orificePlate.orifices[j][i * 6].coordinate
                serial {
                    start(index = 0, pdv = coordinate.abscissa)
                    start(index = 1, pdv = coordinate.ordinate)
                }

                while (_status.value == RuntimeStatus.PAUSED) {
                    delay(100)
                }

                val list = mutableListOf<Triple<Int, Int, Color>>()

                serial {
                    timeout = 1000L * 30
                    repeat(6) {
                        if (i * 6 + it < row) {
                            val orifice = orificePlate.orifices[j][i * 6 + it]
                            if (orifice.selected) {
                                start(index = 2 + it, pdv = orifice.volume.getOrNull(0) ?: 0.0)
                                list += Triple(j, i * 6 + it, Color.Green)
                            }
                        }
                    }
                }
                _selected.value += list
                block(list.size)

                delay(orificePlate.delay)
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
                while (_status.value == RuntimeStatus.PAUSED) {
                    delay(100)
                }
                val abscissa = if (i < 6) {
                    orificePlate.orifices[j][0].coordinate.abscissa - (5 - i) * rowSpace
                } else {
                    orificePlate.orifices[j][i - 5].coordinate.abscissa
                }

                serial {
                    start(index = 0, pdv = abscissa)
                    start(index = 1, pdv = orificePlate.orifices[j][0].coordinate.ordinate)
                }

                while (_status.value == RuntimeStatus.PAUSED) {
                    delay(100)
                }

                val list = mutableListOf<Triple<Int, Int, Color>>()

                serial {
                    timeout = 1000L * 30
                    repeat(6) {
                        if (i - 5 + it in 0 until row) {
                            val orifice = orificePlate.orifices[j][i - 5 + it]
                            if (orifice.selected) {
                                start(index = 2 + it, pdv = orifice.volume.getOrNull(it) ?: 0.0)
                                list += Triple(
                                    j, i - 5 + it, when (it) {
                                        0 -> Color.Green
                                        1 -> Color.Blue
                                        2 -> Color.Red
                                        3 -> Color.Yellow
                                        4 -> Color.Cyan
                                        5 -> Color.Magenta
                                        else -> Color.Black
                                    }
                                )
                            }
                        }
                    }
                }

                list.forEach { triple ->
                    _selected.value -= _selected.value.filter { it.first == triple.first && it.second == triple.second }
                }
                _selected.value += list
                block(list.size)

                delay(orificePlate.delay)
            }
        }
        _selected.value = emptyList()
    }

}

data class RuntimeState(
    val status: RuntimeStatus = RuntimeStatus.STOPPED,
    val orificePlate: OrificePlate = OrificePlate(),
    val process: Float = 0f,
    val selected: List<Triple<Int, Int, Color>> = emptyList(),
)

enum class RuntimeStatus {
    RUNNING, STOPPED, PAUSED, ERROR
}