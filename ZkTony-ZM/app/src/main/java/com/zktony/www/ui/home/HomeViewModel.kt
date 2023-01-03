package com.zktony.www.ui.home

import androidx.lifecycle.viewModelScope
import com.zktony.www.R
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.audio.AudioPlayer
import com.zktony.www.data.model.LogData
import com.zktony.www.data.model.LogRecord
import com.zktony.www.data.model.Program
import com.zktony.www.data.repository.LogDataRepository
import com.zktony.www.data.repository.LogRecordRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.serial.SerialManager
import com.zktony.www.serial.protocol.V1
import com.zktony.www.ui.home.Model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val logRecordRepository: LogRecordRepository,
    private val logDataRepository: LogDataRepository
) : BaseViewModel() {


    @Inject
    lateinit var appViewModel: AppViewModel

    private var logDisposableX: Disposable? = null
    private var logDisposableY: Disposable? = null


    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(200L)
            while (true) {
                delay(1000L)
                SerialManager.instance.send(V1.QUERY_HEX)
            }
        }
    }

    /**
     * 获取程序
     * @return [Flow]<[List]<[Program]>>
     */
    fun getAllProgram(): Flow<List<Program>> {
        return programRepository.getAll()
    }

    /**
     * 更新程序为默认
     * @param kind [Int]
     */
    fun updateProgramDefaultByKind(kind: Int) {
        viewModelScope.launch {
            programRepository.updateDefaultByKind(kind)
        }
    }

    /**
     * 更新程序
     * @param program [Program]
     */
    fun updateProgram(program: Program) {
        viewModelScope.launch {
            programRepository.update(program)
        }
    }

    /**
     * 开始收集log
     * @param module [Model] 模块
     * @param programId [String] 程序Id
     * @param state [ControlState] 控制状态
     */
    fun startRecordLog(module: Model, programId: String, state: ControlState) {
        viewModelScope.launch {
            var logRecord = LogRecord().copy(programId = programId)
            if (module == X) {
                logRecord = logRecord.copy(
                    model = if (state.modelX === A) 0 else 1,
                    motor = state.motorX,
                    voltage = state.voltageX,
                    time = state.timeX.toFloat(),
                )
                if (logDisposableX != null && !logDisposableX!!.isDisposed) {
                    logDisposableX!!.dispose()
                }
            }
            if (module == Y) {
                logRecord = logRecord.copy(
                    model = if (state.modelY === A) 0 else 1,
                    motor = state.motorY,
                    voltage = state.voltageY,
                    time = state.timeY.toFloat(),
                )
                if (logDisposableY != null && !logDisposableY!!.isDisposed) {
                    logDisposableY!!.dispose()
                }
            }
            logRecordRepository.insert(logRecord)
            Observable.interval(5, 5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long> {
                    override fun onSubscribe(d: Disposable) {
                        if (module == X) {
                            logDisposableX = d
                        }
                        if (module == Y) {
                            logDisposableY = d
                        }
                    }

                    override fun onNext(aLong: Long) {
                        var data = LogData().copy(logId = logRecord.id, time = (aLong * 5).toInt())
                        val recCmd = appViewModel.received.value
                        data = if (module == X) {
                            data.copy(
                                motor = recCmd.stepMotorX,
                                voltage = recCmd.getVoltageX,
                                current = recCmd.getCurrentX
                            )
                        } else {
                            data.copy(
                                motor = recCmd.stepMotorY,
                                voltage = recCmd.getVoltageY,
                                current = recCmd.getCurrentY
                            )
                        }
                        viewModelScope.launch {
                            logDataRepository.insert(data)
                        }
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}
                })
        }

    }

    /**
     * 停止收集log
     * @param module [Model] 模块
     */
    fun stopRecordLog(module: Model) {
        viewModelScope.launch {
            if (module == X && logDisposableX != null && !logDisposableX!!.isDisposed) {
                logDisposableX!!.dispose()
            }
            if (module == Y && logDisposableY != null && !logDisposableY!!.isDisposed) {
                logDisposableY!!.dispose()
            }
            logRecordRepository.deleteByDate()
            logDataRepository.deleteByDate()
            logDataRepository.deleteDataLessThanTen()
            logRecordRepository.deleteInvalidedLog()
        }
    }

    /**
     * 运行时电流哨兵
     */
    fun setSentinel() {
        viewModelScope.launch {
            delay(5500)
            val cmd = appViewModel.received.value
            if (cmd.powerENX == 1 && cmd.getCurrentX < 0.1 && cmd.powerENY == 0) {
                _errorMessage.emit("模块A异常，请检查！！！")
                playAudio(R.raw.error)
            }
            if (cmd.powerENY == 1 && cmd.getCurrentY < 0.1 && cmd.powerENX == 0) {
                _errorMessage.emit("模块B异常，请检查！！！")
                playAudio(R.raw.error)
            }
            if (cmd.powerENX == 1 && cmd.getCurrentX < 0.1 && cmd.powerENY == 1 && cmd.getCurrentY < 0.1) {
                _errorMessage.emit("模块A、B异常，请检查！！！")
                playAudio(R.raw.error)
            }
        }
    }

    /**
     * 播放音频
     */
    fun playAudio(id: Int) {
        viewModelScope.launch {
            if (appViewModel.setting.value.audio) {
                AudioPlayer.instance.play(id)
            }
        }
    }
}

enum class Model {
    X, Y, A, B
}

data class ControlState(
    val modelX: Model = A,
    val modelY: Model = A,
    val motorX: Int = 0,
    val motorY: Int = 0,
    val voltageX: Float = 0f,
    val voltageY: Float = 0f,
    val timeX: Int = 0,
    val timeY: Int = 0,
    val isRunX: Boolean = false,
    val isRunY: Boolean = false,
    val stepMotorX: Int = 0,
    val stepMotorY: Int = 0
) {
    fun isCanStartX(): Boolean {
        if (modelX === A) {
            return motorX > 0 && voltageX > 0 && timeX > 0
        }
        return if (modelX === B) {
            voltageX > 0 && timeX > 0
        } else false
    }

    fun isCanStartY(): Boolean {
        if (modelY === A) {
            return motorY > 0 && voltageY > 0 && timeY > 0
        }
        return if (modelY === B) {
            voltageY > 0 && timeY > 0
        } else false
    }
}