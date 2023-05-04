package com.zktony.www.ui.home

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.common.ext.*
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.Point
import com.zktony.www.room.entity.Program
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel constructor(
    private val PGD: ProgramDao,
    private val PD: PointDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                PGD.getAll().collect {
                    if (it.isEmpty()) {
                        _uiState.value = _uiState.value.copy(programList = it, program = null)
                    } else {
                        _uiState.value = _uiState.value.copy(programList = it, program = it[0])
                        loadPoint(it[0].id)
                    }
                }
            }
        }
    }

    private fun loadPoint(id: Long) {
        viewModelScope.launch {
            PD.getBySubId(id).collect {
                _uiState.value = _uiState.value.copy(pointList = it)
                if (it.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        info = _uiState.value.info.copy(
                            volume = it.last().v1 to it.last().v2,
                        )
                    )
                }
            }
        }
    }


    fun select(view: View) {
        val list = uiState.value.programList.map { it.name }
        if (_uiState.value.job != null) {
            return
        }
        if (list.isEmpty()) {
            return
        }
        spannerDialog(
            view = view,
            menu = list,
            block = { _, index ->
                _uiState.value = _uiState.value.copy(program = uiState.value.programList[index])
                loadPoint(uiState.value.programList[index].id)
            }
        )
    }

    fun reset() {
        viewModelScope.launch {
            decideLock {
                yes { PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.running)) }
                no {
                    asyncHex(0) { }
                    asyncHex(3) {
                        pa = "0B"
                        data = "0305"
                    }
                }
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            val job = launch {
                launch {
                    while (true) {
                        delay(1000L)
                        if (!_uiState.value.pause) {
                            _uiState.value = _uiState.value.copy(time = _uiState.value.time + 1)
                            val lastTime = _uiState.value.info.lastTime
                            if (lastTime > 0) {
                                _uiState.value = _uiState.value.copy(
                                    info = _uiState.value.info.copy(
                                        lastTime = lastTime - 1
                                    )
                                )
                            }
                        }
                    }
                }
                val executor = ProgramExecutor(
                    list = _uiState.value.pointList,
                    scope = this,
                )
                launch {
                    uiState.collect {
                        executor.pause = it.pause
                    }
                }
                executor.event = {
                    when (it) {
                        is ExecutorEvent.Volume -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    volume = it.volume
                                )
                            )
                        }

                        is ExecutorEvent.FinishList -> {
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    pairs = it.list
                                )
                            )
                        }

                        is ExecutorEvent.Progress -> {
                            val time = _uiState.value.time + 1
                            val percent = it.complete.toFloat() / it.total.toFloat()
                            val lastTime = time.toFloat() / percent - time.toFloat()
                            val speed = it.complete / time.toFloat() * 60
                            _uiState.value = _uiState.value.copy(
                                info = _uiState.value.info.copy(
                                    speed = speed,
                                    lastTime = lastTime.toLong(),
                                    process = ((it.complete / it.total.toFloat()) * 100).toInt(),
                                )
                            )

                        }

                        is ExecutorEvent.Finish -> {
                            CustomDialog.build()
                                .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_complete) {
                                    override fun onBind(dialog: CustomDialog, v: View) {
                                        val tvName = v.findViewById<TextView>(R.id.name)
                                        val tvTime = v.findViewById<TextView>(R.id.time)
                                        val tvSpeed = v.findViewById<TextView>(R.id.speed)
                                        tvSpeed.text = _uiState.value.program?.name ?: "None"
                                        tvTime.text = _uiState.value.time.getTimeFormat()
                                        tvName.text = String.format(
                                            "%.2f",
                                            _uiState.value.info.speed
                                        )
                                    }
                                })
                                .setMaskColor(Color.parseColor("#4D000000"))
                                .show()
                            stop()
                        }
                    }
                }
                executor.execute()
            }
            _uiState.value = _uiState.value.copy(job = job)
        }
    }

    fun stop() {
        viewModelScope.launch {
            _uiState.value.job?.cancel()
            _uiState.value = _uiState.value.copy(
                job = null,
                pause = false,
                time = 0L,
                info = _uiState.value.info.copy(
                    speed = 0f,
                    lastTime = 0L,
                    process = 0,
                    pairs = emptyList(),
                )
            )
        }
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(pause = !_uiState.value.pause)
    }

    /**
     * 填充促凝剂
     */
    fun fillCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.fillCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    fillCoagulant = false,
                )
                asyncHex(3) {
                    pa = "0B"
                    data = "0300"
                }
                delay(100L)
                syncHex(3) {
                    pa = "0B"
                    data = "0305"
                }
            } else {
                if (_uiState.value.recaptureCoagulant) {
                    PopTip.show(Ext.ctx.getString(R.string.stop_back))
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    fillCoagulant = true,
                )
                delay(100L)
                while (_uiState.value.fillCoagulant) {
                    if (_uiState.value.upOrDown) {
                        _uiState.value = _uiState.value.copy(upOrDown = false)
                        asyncHex(3) {
                            pa = "0B"
                            data = "0301"
                        }
                        delay(8500L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex(3) {
                            pa = "0B"
                            data = "0305"
                        }
                        delay(9000L)
                    }
                }
            }
        }
    }

    /**
     * 回吸促凝剂
     */
    fun recaptureCoagulant() {
        viewModelScope.launch {
            if (_uiState.value.recaptureCoagulant) {
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    recaptureCoagulant = false,
                )
                asyncHex(3) {
                    pa = "0B"
                    data = "0300"
                }
                delay(100L)
                syncHex(3) {
                    pa = "0B"
                    data = "0305"
                }
            } else {
                if (_uiState.value.fillCoagulant) {
                    PopTip.show(Ext.ctx.getString(R.string.stop_fill))
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    upOrDown = true,
                    recaptureCoagulant = true,
                )
                delay(100L)
                while (_uiState.value.recaptureCoagulant) {
                    if (_uiState.value.upOrDown) {
                        _uiState.value = _uiState.value.copy(upOrDown = false)
                        asyncHex(3) {
                            pa = "0B"
                            data = "0303"
                        }
                        delay(8500L)
                    } else {
                        _uiState.value = _uiState.value.copy(upOrDown = true)
                        asyncHex(3) {
                            pa = "0B"
                            data = "0304"
                        }
                        delay(9000L)
                    }
                }
            }
        }
    }

    /**
     * 填充胶体
     */
    fun fillColloid() {
        asyncHex(3) {
            pa = "0B"
            data = "0401"
        }
    }

    /**
     * 回吸胶体
     */
    fun recaptureColloid() {
        asyncHex(3) {
            pa = "0B"
            data = "0402"
        }
    }

    /**
     * 停止填充和回吸
     */
    fun stopFillAndRecapture() {
        asyncHex(3) {
            pa = "0B"
            data = "0400"
        }
    }
}

data class HomeUiState(
    val programList: List<Program> = emptyList(),
    val pointList: List<Point> = emptyList(),
    val program: Program? = null,
    val job: Job? = null,
    val pause: Boolean = false,
    val time: Long = 0L,
    val info: CurrentInfo = CurrentInfo(),
    val fillCoagulant: Boolean = false,
    val recaptureCoagulant: Boolean = false,
    val upOrDown: Boolean = true,
)

data class CurrentInfo(
    val volume: Pair<Int, Int> = Pair(0, 0),
    val pairs: List<Pair<Int, Boolean>> = emptyList(),
    val speed: Float = 0f,
    val lastTime: Long = 0L,
    val process: Int = 0,
)