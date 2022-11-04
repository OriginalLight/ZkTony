package com.zktony.www.ui.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.viewModelScope
import com.zktony.serialport.COMSerial
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.LogData
import com.zktony.www.common.room.entity.LogRecord
import com.zktony.www.common.room.entity.Program
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.model.Event
import com.zktony.www.data.model.SerialPort
import com.zktony.www.data.repository.LogDataRepository
import com.zktony.www.data.repository.LogRecordRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.ui.home.model.Cmd
import com.zktony.www.ui.home.model.ControlState
import com.zktony.www.ui.home.model.Model
import com.zktony.www.ui.home.model.Model.*
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val programRepository: ProgramRepository,
    private val logRecordRepository: LogRecordRepository,
    private val logDataRepository: LogDataRepository
) : BaseViewModel() {


    @Inject
    lateinit var appViewModel: AppViewModel

    private var logDisposableX: Disposable? = null
    private var logDisposableY: Disposable? = null
    var interval = 1
    var duration = 10
    var detect = true


    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            launch {
                dataStore.data.map { preferences ->
                    preferences[intPreferencesKey(Constants.INTERVAL)] ?: 1
                }.collect {
                    interval = it
                }
            }
            launch {
                dataStore.data.map { preferences ->
                    preferences[intPreferencesKey(Constants.DURATION)] ?: 10
                }.collect {
                    duration = it
                }
            }
            launch {
                dataStore.data.map { preferences ->
                    preferences[booleanPreferencesKey(Constants.DETECT)] ?: true
                }.collect {
                    detect = it
                }
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
                        val recCmd = appViewModel.latestReceiveCmd
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
            Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(object : Observer<Long> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(aLong: Long) {
                        EventBus.getDefault().post(Event(Constants.BLANK, Constants.UPDATE_LOG))
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}
                })

        }
    }

    /**
     * 运行时电流哨兵
     */
    fun setSentinel() {
        viewModelScope.launch {
            delay(5500)
            val cmd = appViewModel.latestReceiveCmd
            if (cmd.powerENX == 1 && cmd.getCurrentX < 0.1 && cmd.powerENY == 0) {
                _errorMessage.emit("模块A异常，请检查！！！")
            }
            if (cmd.powerENY == 1 && cmd.getCurrentY < 0.1 && cmd.powerENX == 0) {
                _errorMessage.emit("模块B异常，请检查！！！")
            }
            if (cmd.powerENX == 1 && cmd.getCurrentX < 0.1 && cmd.powerENY == 1 && cmd.getCurrentY < 0.1) {
                _errorMessage.emit("模块A、B异常，请检查！！！")
            }
        }
    }

    /**
     * 设置定时查询
     */
    fun initQueryWork() {
        viewModelScope.launch {
            Observable.interval(200, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(aLong: Long) {
                        COMSerial.instance.sendHex(SerialPort.TTYS4.device, Cmd.QUERY_HEX)
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}
                })
        }
    }


}