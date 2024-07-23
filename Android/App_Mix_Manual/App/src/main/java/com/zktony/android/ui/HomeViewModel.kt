package com.zktony.android.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.R
import com.zktony.android.data.dao.ErrorRecordDao
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.data.entities.SportsLog
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpe
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.SerialPortUtils.endStartFlashYellow
import com.zktony.android.utils.SerialPortUtils.getGpio
import com.zktony.android.utils.SerialPortUtils.gpio
import com.zktony.android.utils.SerialPortUtils.lightFlashYellow
import com.zktony.android.utils.SerialPortUtils.lightGreed
import com.zktony.android.utils.SerialPortUtils.lightRed
import com.zktony.android.utils.SerialPortUtils.lightYellow
import com.zktony.android.utils.SerialPortUtils.pulse
import com.zktony.android.utils.SerialPortUtils.sendPING
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.SerialPortUtils.stop
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.playAudio
import com.zktony.android.utils.internal.ExceptionPolicy
import com.zktony.serialport.utils.logInfo
import com.zktony.serialport.utils.writeThread
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    private val dataStore: DataSaverDataStore,
    private val erDao: ExperimentRecordDao,
    private val slDao: SettingDao,
    private val errorDao: ErrorRecordDao,
) : ViewModel() {

    private val _selected = MutableStateFlow(1L)
    private val _selectedER = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())
    private val _job = MutableStateFlow<Job?>(null)
    private val _job2 = MutableStateFlow<Job?>(null)
    private val _waitTimeRinseJob = MutableStateFlow<Job?>(null)
    private val _hintJob = MutableStateFlow<Job?>(null)
    private val _heartbeatJob = MutableStateFlow<Job?>(null)


    /**
     * 心跳异常
     */
    private val _heartbeatError = MutableStateFlow(false)

    /**
     * 等待5分钟后清洗的次数
     * 清洗6次后，自动停止
     */
    private val _waitTimeRinseNum = MutableStateFlow(0)

    /**
     * 加液次数
     */
    private val _complate = MutableStateFlow(0)

    /**
     * 柱塞泵加液计数
     */
    private val _stautsNum = MutableStateFlow(1)


    /**
     * 单次加液进度
     */
    private val _progress = MutableStateFlow(0f)

    /**
     * 计算出的预计制胶数量
     */
    private val _calculate = MutableStateFlow(0)

    /**
     * 计算出的高浓度母液液量mL
     */
    private val _higemother = MutableStateFlow(0f)


    /**
     * 计算出的低浓度母液液量mL
     */
    private val _lowmother = MutableStateFlow(0f)

    /**
     * 计算出的纯水母液液量mL
     */
    private val _watermother = MutableStateFlow(0f)

    /**
     * 计算出的促凝剂母液液量mL
     */
    private val _coagulantmother = MutableStateFlow(0f)

    /**
     * 废液进度
     */
    private val _wasteprogress = MutableStateFlow(0f)


    private val _first = MutableStateFlow(false)

    /**
     * 填充是否弹出弹窗
     */
    private val _pipelineDialogOpen = MutableStateFlow(false)

    /**
     * 清洗是否弹出弹窗
     */
    private val _cleanDialogOpen = MutableStateFlow(false)


    /**
     * 开机弹窗提示
     */
    private val _initHintDialog = MutableStateFlow(false)

    private val _erCount = MutableStateFlow(0)

    /**
     * 检测是否更换制胶架
     * true:更换过
     * false:未更换
     */
    private val _hint = MutableStateFlow(false)

    private val coagulantStart = MutableStateFlow(0L)


    private var syringeJob: Job? = null

    val selected = _selected.asStateFlow()
    val selectedER = _selectedER.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val job = _job.asStateFlow()
    val job2 = _job2.asStateFlow()
    val complate = _complate.asStateFlow()
    val progress = _progress.asStateFlow()
    val calculate = _calculate.asStateFlow()
    val wasteprogress = _wasteprogress.asStateFlow()
    val higemother = _higemother.asStateFlow()
    val lowmother = _lowmother.asStateFlow()
    val watermother = _watermother.asStateFlow()
    val coagulantmother = _coagulantmother.asStateFlow()
    val first = _first.asStateFlow()

    val hint = _hint.asStateFlow()

    val pipelineDialogOpen = _pipelineDialogOpen.asStateFlow()
    val cleanDialogOpen = _cleanDialogOpen.asStateFlow()
    val heartbeatError = _heartbeatError.asStateFlow()
    val initHintDialog = _initHintDialog.asStateFlow()
    val erCount = _erCount.asStateFlow()


    /**
     * 制胶程序dao
     */
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    /**
     * 实验记录dao
     */
    val erEntities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { erDao.getByPage() }.flow.cachedIn(viewModelScope)


    init {

        _initHintDialog.value = true
        dataStore.saveData("expectedMakenum", 0)
        val selectRudio = dataStore.readData("selectRudio", 1)
        if (selectRudio == 1) {
            //开机
            ApplicationUtils.ctx.playAudio(R.raw.power_buzz)
        } else if (selectRudio == 2) {
            ApplicationUtils.ctx.playAudio(R.raw.power_voice)
        }
        initreset()
        heartbeat()

    }

    fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Clean -> clean()
            is HomeIntent.NavTo -> _page.value = intent.page
            is HomeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is HomeIntent.Pipeline -> pipeline(intent.index)
            is HomeIntent.Reset -> reset()
            is HomeIntent.Start -> startJob(intent.count)
            is HomeIntent.Stop -> stopJob()
            is HomeIntent.Selected -> _selected.value = intent.id

            is HomeIntent.Update -> viewModelScope.launch {
                erDao.update(intent.entity)
            }


            is HomeIntent.Calculate -> calculate()
            is HomeIntent.HigeLowMotherVol -> higeLowMotherVol()

            is HomeIntent.MoveCom -> moveCom(intent.startNum)

            is HomeIntent.First -> viewModelScope.launch {
                _first.value = true
                val selectRudio = dataStore.readData("selectRudio", 1)
                if (selectRudio == 2) {
                    ApplicationUtils.ctx.playAudio(R.raw.first_voice)
                }
            }

            is HomeIntent.CleanWaste -> viewModelScope.launch {
                val selectRudio = dataStore.readData("selectRudio", 1)
                if (selectRudio == 2) {
                    ApplicationUtils.ctx.playAudio(R.raw.cleanwaste_voice)
                } else if (selectRudio == 1) {
                    ApplicationUtils.ctx.playAudio(R.raw.detection_buzz)
                }
            }

            is HomeIntent.CleanWasteState -> viewModelScope.launch {
                _wasteprogress.value = 0f
            }

            is HomeIntent.CleanDialog -> viewModelScope.launch {
                _cleanDialogOpen.value = intent.cleanState
            }

            is HomeIntent.PipelineDialog -> viewModelScope.launch {
                _pipelineDialogOpen.value = intent.pipelineState
            }

            is HomeIntent.MotherVolZero -> viewModelScope.launch {
                _higemother.value = 0f
                _lowmother.value = 0f
                _watermother.value = 0f
                _coagulantmother.value = 0f
            }

            is HomeIntent.Heartbeat -> viewModelScope.launch {
                _heartbeatError.value = false
                delay(100)
                heartbeat()
            }

            is HomeIntent.InitHintDialog -> viewModelScope.launch {
                _initHintDialog.value = false

            }

            is HomeIntent.StopHeartbeat -> viewModelScope.launch {
                _heartbeatJob.value?.cancel()
                _heartbeatJob.value = null
            }

            is HomeIntent.WaitTimeRinse -> waitTimeRinse()

            is HomeIntent.StopWaitTimeRinse -> viewModelScope.launch {
                _waitTimeRinseJob.value?.cancel()
                _waitTimeRinseJob.value = null
            }

            is HomeIntent.erCount -> viewModelScope.launch {
                _erCount.value = erDao.count()
                delay(500)
            }


        }
    }


    /**
     * 语音
     */
    private fun hintBuzz() {
        viewModelScope.launch {
            val selectRudio = dataStore.readData("selectRudio", 1)
            if (selectRudio == 1) {
                _job2.value = launch {
                    var num = 0f
                    while (num < 30) {
                        if (_job2.value == null) {
                            break
                        }
                        ApplicationUtils.ctx.playAudio(R.raw.hint_buzz)
                        num += 2
                        delay(2000L)
                    }
                }
            }

        }
    }

    /**
     * 心跳
     */
    fun heartbeat() {
        viewModelScope.launch {
            _heartbeatJob.value = launch {
                var num = 0
                val endNum = 30
                var queryState = false
                while (num < endNum) {
                    if (_heartbeatJob.value == null) {
                        break
                    }
                    num += 2
                    delay(2000L)
                }
                if (num >= endNum) {
                    hpe.remove(1)
                    sendPING()
                    delay(100)
                    logInfo("heartbeat", "30秒的心跳检测结果:${hpe[1]}")

                    if (hpe[1] == null) {
                        queryState = true
                    }
                    if (queryState) {
                        queryState = false
                        for (i in 1..2) {
                            hpe.remove(1)
                            sendPING()
                            delay(100)
                            if (hpe[1] == null) {
                                queryState = true
                            } else {
                                queryState = false
                            }
                        }
                    }
                    logInfo("heartbeat", "30秒的心跳检测结果queryState:$queryState")
                    if (queryState) {
                        _heartbeatError.value = true
                        _heartbeatJob.value?.cancel()
                        _heartbeatJob.value = null
                        errorDao.insert(ErrorRecord(detail = "上下位机失联"))
                        logInfo("heartbeat", "上下位机失联")
                    } else {
                        _heartbeatError.value = false
                        _heartbeatJob.value?.cancel()
                        _heartbeatJob.value = null
                        //继续执行心跳方法
                        heartbeat()
                        logInfo("heartbeat", "继续执行心跳方法")
                    }

                }

            }

        }
    }


    /**
     * 等待半小时后冲洗
     */
    private fun waitTimeRinse() {
        viewModelScope.launch {
            _waitTimeRinseJob.value = launch {
                var num = 0
                var waitTime = 60 * 5
                while (num < waitTime) {
//                    if (_waitTimeRinseNum.value == 6) {
//                        break
//                    }
                    if (_waitTimeRinseJob.value == null) {
                        break
                    }
                    num += 1
                    delay(1000L)
                }
                if (num >= waitTime) {
                    _uiFlags.value = UiFlags.objects(13)
                    val slEnetity = slDao.getById(1L).firstOrNull()
                    if (slEnetity != null) {
                        val rinseCleanVolume = slEnetity.rinseCleanVolume
                        val rinseCleanVolume2 = slEnetity.rinseCleanVolume2
                        _wasteprogress.value += (rinseCleanVolume / 150).toFloat()

                        /**
                         * 废液槽位置
                         */
                        val wastePosition = slEnetity.wastePosition

                        /**
                         * x轴转速
                         */
                        val xSpeed = dataStore.readData("xSpeed", 100L)

                        val rinse1 = rinseCleanVolume / 3
                        lightYellow()
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 0,
                                pdv = wastePosition,
                                ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                            )
                        }

                        /**
                         * 冲洗转速
                         */
                        val rinseSpeed = dataStore.readData("rinseSpeed", 600L)


                        if (_waitTimeRinseNum.value == 0) {
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 4,
                                    ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                                    pdv = rinse1 * 1000
                                )
                            }
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                                    pdv = slEnetity.coagulantRinse
                                )
                            }

                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 4,
                                    ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                                    pdv = rinse1 * 2 * 1000
                                )
                            }
                        } else if (_waitTimeRinseNum.value == 1) {
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 4,
                                    ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                                    pdv = rinseCleanVolume * 1000
                                )
                            }
                        } else {
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 4,
                                    ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                                    pdv = rinseCleanVolume2 * 1000
                                )
                            }
                        }

                        _waitTimeRinseNum.value += 1
                    }
                    lightGreed()
                    _waitTimeRinseJob.value?.cancel()
                    _waitTimeRinseJob.value = null
                    waitTimeRinse()
                    _uiFlags.value = UiFlags.none()


                }
            }

        }
    }

    /**
     * 检测是否更换制胶架
     */
    private fun hint() {
        viewModelScope.launch {
            _hintJob.value = launch {
                var state1 = 0
                var state2 = 0
                logInfo(
                    "hint",
                    "检测是否更换制胶架"
                )
                while (!_hint.value) {
                    if (_hintJob.value == null) {
                        break
                    }

                    if (state1 == 1) {
                        break
                    }
                    gpio(3)
                    delay(500)
                    logInfo(
                        "hint",
                        "制胶板是否离开制胶架:${getGpio(3)}"
                    )
                    if (!getGpio(3)) {
                        state1 = 1
                    }

                }

                while (!_hint.value) {
                    if (_hintJob.value == null) {
                        break
                    }
                    if (state2 == 1) {
                        break
                    }
                    gpio(3)
                    delay(500)
                    logInfo(
                        "hint",
                        "制胶板是否放入制胶架:${getGpio(3)}"
                    )
                    if (getGpio(3)) {

                        state2 = 1
                    }
                }

                if (state1 == 1 && state2 == 1) {
                    _hint.value = true
                }


            }
        }

    }


    //托盘检测

    private fun initreset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
                delay(500)

                val selectRudio = dataStore.readData("selectRudio", 1)
                if (selectRudio == 1) {

                } else if (selectRudio == 2) {
                    ApplicationUtils.ctx.playAudio(R.raw.startreset_voice)
                }
                lightFlashYellow()
                delay(100)

                _waitTimeRinseJob.value?.cancel()
                _waitTimeRinseJob.value = null
                _waitTimeRinseNum.value = 0

                /**
                 * 柱塞泵总行程
                 */
                val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                /**
                 * 复位等待时间
                 */
                val coagulantTime = dataStore.readData("coagulantTime", 800).toLong()

                /**
                 * 复位后预排步数
                 */
                val coagulantResetPulse = dataStore.readData("coagulantResetPulse", 1500).toLong()

                var resetBool = false


                delay(100)

                withTimeout(60 * 1000L) {
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */
                    //x轴复位===========================================
                    gpio(0, 1)
                    delay(500L)
                    logInfo(
                        "HomeViewModel",
                        "x轴光电状态====0号光电===" + getGpio(0) + "====1号光电===" + getGpio(1)
                    )
                    if (!getGpio(0) && !getGpio(1)) {
                        logInfo(
                            "HomeViewModel",
                            "x轴反转64000L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        logInfo(
                            "HomeViewModel",
                            "x轴正转6400L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        logInfo(
                            "HomeViewModel",
                            "x轴反转6500L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {

                            logInfo(
                                "HomeViewModel",
                                "x轴正转1600L"
                            )
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                            logInfo(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "HomeViewModel",
                                "复位失败"
                            )
                            resetBool = true
                            _uiFlags.value = UiFlags.objects(14)
                        }

                    } else if (!getGpio(0) && getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "HomeViewModel",
                                "复位失败"
                            )
                            resetBool = true
                            _uiFlags.value = UiFlags.objects(14)
                            //复位失败
                        }

                    } else if (getGpio(0) && !getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 6400L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            logInfo(
                                "HomeViewModel",
                                "复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                        } else {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -6500L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "HomeViewModel",
                                "复位完成"
                            )
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                        }
                    } else {
                        logInfo(
                            "HomeViewModel",
                            "复位失败"
                        )
                        resetBool = true
                        _uiFlags.value = UiFlags.objects(14)
                    }
                    //x轴复位===========================================


                    if (!resetBool) {
                        // 查询GPIO状态
                        //柱塞泵复位===========================================
                        gpio(2)
                        delay(500L)
                        logInfo(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + getGpio(2)
                        )
                        if (!getGpio(2)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = coagulantpulse + 20000L,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }
                            delay(300L)
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = -64000L,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }
                            delay(300L)
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = 64500L,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }
                            delay(coagulantTime)

                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = -coagulantpulse,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }

                            delay(300L)
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = coagulantResetPulse,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }
                            delay(300L)
                            gpio(2)
                            delay(1500L)
                            logInfo(
                                "HomeViewModel",
                                "注射泵光电状态====2号光电===" + getGpio(2)
                            )
                            delay(300L)
                            if (!getGpio(2)) {

                                delay(300L)
                                logInfo(
                                    "HomeViewModel",
                                    "柱塞泵复位成功"
                                )
                                //复位完成
                            } else {
                                logInfo(
                                    "HomeViewModel",
                                    "柱塞泵复位失败"
                                )
                                //复位失败
                                resetBool = true
                                _uiFlags.value = UiFlags.objects(14)
                            }
                        } else {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = -64000L,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),

                                    )
                            }
                            delay(300L)

                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = 64500L,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }
                            delay(coagulantTime)
                            logInfo(
                                "HomeViewModel",
                                "柱塞泵复位完成"
                            )
                            //复位完成
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = -coagulantpulse,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }
                            delay(300L)

                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = coagulantResetPulse,
                                    ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                )
                            }

                            delay(300L)
                            gpio(2)
                            delay(500L)
                            logInfo(
                                "HomeViewModel",
                                "注射泵光电状态====2号光电===" + getGpio(2)
                            )
                            if (getGpio(2)) {
                                logInfo(
                                    "HomeViewModel",
                                    "柱塞泵复位失败"
                                )
                                resetBool = true
                                _uiFlags.value = UiFlags.objects(14)
                                //复位失败
                            } else {
                                logInfo(
                                    "HomeViewModel",
                                    "柱塞泵复位完成"
                                )
                            }

                        }
                        //柱塞泵复位===========================================


                        delay(100)
                    }

                    if (!resetBool) {
                        //蠕动泵复位===========================================
                        /**
                         * 蠕动泵复位次数
                         */
                        val numberOfPumpResets = dataStore.readData("numberofpumpresets", 3)

                        val rinseSpeed = dataStore.readData("rinseSpeed", 600L)


                        for (i in 1..numberOfPumpResets) {
                            if (i == numberOfPumpResets) {
                                //转一整圈
                                if (i % 2 == 0) {
                                    start {
                                        timeOut = 1000L * 60L * 10
                                        with(
                                            index = 2,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = 51200L
                                        )
                                        with(
                                            index = 3,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = 51200L
                                        )
                                        with(
                                            index = 4,
                                            ads = Triple(
                                                rinseSpeed * 30,
                                                rinseSpeed * 30,
                                                rinseSpeed * 30
                                            ),
                                            pdv = 3200L
                                        )
                                    }
                                } else {
                                    start {
                                        timeOut = 1000L * 60L * 10
                                        with(
                                            index = 2,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = -51200L
                                        )
                                        with(
                                            index = 3,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = -51200L
                                        )
                                        with(
                                            index = 4,
                                            ads = Triple(
                                                rinseSpeed * 30,
                                                rinseSpeed * 30,
                                                rinseSpeed * 30
                                            ),
                                            pdv = -3200L
                                        )
                                    }
                                }
                            } else {
                                if (i % 2 == 0) {
                                    //正转
                                    start {
                                        timeOut = 1000L * 60L * 10
                                        with(
                                            index = 2,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = 25600L
                                        )
                                        with(
                                            index = 3,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = 25600L
                                        )
                                        with(
                                            index = 4,
                                            ads = Triple(
                                                rinseSpeed * 30,
                                                rinseSpeed * 30,
                                                rinseSpeed * 30
                                            ),
                                            pdv = 1600L
                                        )
                                    }
                                } else {
                                    //反转
                                    start {
                                        timeOut = 1000L * 60L * 10
                                        with(
                                            index = 2,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = -25600L
                                        )
                                        with(
                                            index = 3,
                                            ads = Triple(
                                                rinseSpeed * 13,
                                                rinseSpeed * 1193,
                                                rinseSpeed * 1193
                                            ),
                                            pdv = -25600L
                                        )
                                        with(
                                            index = 4,
                                            ads = Triple(
                                                rinseSpeed * 30,
                                                rinseSpeed * 30,
                                                rinseSpeed * 30
                                            ),
                                            pdv = -1600L
                                        )
                                    }
                                }
                            }

                        }
                        //蠕动泵复位===========================================

                        if (selectRudio == 1) {

                        } else if (selectRudio == 2) {
                            ApplicationUtils.ctx.playAudio(R.raw.endreset_voice)
                        }

                        lightGreed()
                    }

                }
                if (!resetBool) {
                    _uiFlags.value = UiFlags.none()
                }
            } catch (ex: Exception) {
                lightRed()
                delay(100)
                errorDao.insert(ErrorRecord(detail = "复位失败"))
                delay(100)
                ApplicationUtils.ctx.playAudio(R.raw.error_buzz)
//                _uiFlags.value = UiFlags.objects(14)
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
        }
    }

//    private fun reset() {
//        viewModelScope.launch {
//            try {
//                query(0)
//                delay(500L)
//                query(1)
////                delay(500L)
////                query(2)
////                delay(500L)
////                query(3)
////                withTimeout(3000L) {
////                    gpio(0, 1)
////                    delay(500L)
////                    if (!getGpio(0) && !getGpio(1)) {
////                        start {
////                            executeType=ExecuteType.ASYNC
////                            exceptionPolicy = ExceptionPolicy.QUERY
////                            timeOut = 1000L * 30
////                            with(
////                                index = 0,
////                                pdv = -64000L,
////                                ads = Triple(1600, 1600, 1600),
////                            )
////                        }
////                    }else{
////                        start {
////                            executeType=ExecuteType.ASYNC
////                            exceptionPolicy = ExceptionPolicy.QUERY
////                            timeOut = 1000L * 30
////                            with(
////                                index = 0,
////                                pdv = 6400L,
////                                ads = Triple(1600, 1600, 1600),
////                            )
////                        }
////                    }
////                    query(0)
//////                    delay(3000)
//////                    reset()
////                }
//            } catch (ex: Exception) {
//                println("复位超时请重试")
//            }
//
//
////            sendPING()
////            println("心跳====${hpe[1]}")
//////            delay(100)
//////            version()
//        }
//    }

    private fun reset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
                delay(500)
                val selectRudio = dataStore.readData("selectRudio", 1)
                if (selectRudio == 1) {

                } else if (selectRudio == 2) {
                    ApplicationUtils.ctx.playAudio(R.raw.startreset_voice)
                }
                lightFlashYellow()
                delay(100)

                _waitTimeRinseJob.value?.cancel()
                _waitTimeRinseJob.value = null
                _waitTimeRinseNum.value = 0

                /**
                 * 柱塞泵总行程
                 */
                val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                /**
                 * 复位等待时间
                 */
                val coagulantTime = dataStore.readData("coagulantTime", 800).toLong()

                /**
                 * 复位后预排步数
                 */
                val coagulantResetPulse = dataStore.readData("coagulantResetPulse", 1500).toLong()

                withTimeout(60 * 1000L) {
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */
                    //x轴复位===========================================
                    gpio(0, 1)
                    delay(500L)
                    logInfo(
                        "HomeViewModel",
                        "x轴光电状态====0号光电===" + getGpio(0) + "====1号光电===" + getGpio(1)
                    )
                    if (!getGpio(0) && !getGpio(1)) {
                        logInfo(
                            "HomeViewModel",
                            "x轴反转64000L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        logInfo(
                            "HomeViewModel",
                            "x轴正转6400L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        logInfo(
                            "HomeViewModel",
                            "x轴反转6500L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            logInfo(
                                "HomeViewModel",
                                "x轴正转1600L"
                            )
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                            logInfo(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "HomeViewModel",
                                "复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                            //复位失败
                        }

                    } else if (!getGpio(0) && getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "HomeViewModel",
                                "复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                            //复位失败
                        }

                    } else if (getGpio(0) && !getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 6400L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            logInfo(
                                "HomeViewModel",
                                "复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                        } else {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -6500L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "HomeViewModel",
                                "复位完成"
                            )
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                        }
                    } else {
                        logInfo(
                            "HomeViewModel",
                            "复位失败"
                        )
                        _uiFlags.value = UiFlags.objects(14)
                    }
                    //x轴复位===========================================


                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    gpio(2)
                    delay(500L)
                    logInfo(
                        "HomeViewModel",
                        "注射泵光电状态====2号光电===" + getGpio(2)
                    )
                    if (!getGpio(2)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantpulse + 20000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)

                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        gpio(2)
                        delay(1500L)
                        logInfo(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + getGpio(2)
                        )
                        delay(300L)
                        if (!getGpio(2)) {

                            delay(300L)
                            logInfo(
                                "HomeViewModel",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                            //复位失败
                        }
                    } else {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),

                                )
                        }
                        delay(300L)

                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)
                        logInfo(
                            "HomeViewModel",
                            "柱塞泵复位完成"
                        )
                        //复位完成
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)

                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        gpio(2)
                        delay(500L)
                        logInfo(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + getGpio(2)
                        )
                        if (getGpio(2)) {
                            logInfo(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                            _uiFlags.value = UiFlags.objects(14)
                        } else {
                            logInfo(
                                "HomeViewModel",
                                "柱塞泵复位完成"
                            )
                        }

                    }
                    //柱塞泵复位===========================================
                    delay(100)

                    if (selectRudio == 1) {

                    } else if (selectRudio == 2) {
                        ApplicationUtils.ctx.playAudio(R.raw.endreset_voice)
                    }

                    lightGreed()
                }
                waitTimeRinse()
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                lightRed()
                delay(100)
                errorDao.insert(ErrorRecord(detail = "复位超时请重试"))
                delay(100)
                ApplicationUtils.ctx.playAudio(R.raw.error_buzz)
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
        }
    }


    //预计制胶数量=INT（MIN（（高浓度母液量-高浓度泵填充液量）/（（预排高浓度泵步数+制胶高浓度步数）/高浓度泵校准数据（步/μL）/1000），（低浓度母液量-低浓度泵填充液量）/（（预排低浓度泵步数+制胶低浓度步数）/低浓度泵校准数据（步/μL）/1000）））
    private fun calculate() {
        viewModelScope.launch {
            try {
                _uiFlags.value = UiFlags.objects(10)
                val selected = dao.getById(_selected.value).firstOrNull()
                if (selected == null) {
                    _uiFlags.value = UiFlags.message("未选择程序")
                    return@launch
                }


                val setting = slDao.getById(1L).firstOrNull()
                if (setting == null) {
                    _uiFlags.value = UiFlags.message("系统参数无数据")
                    return@launch
                }

                logInfo(
                    "calculate",
                    "预计制胶数量=INT（MIN（（高浓度母液量-高浓度泵填充液量）/（（预排高浓度泵步数+制胶高浓度步数）/高浓度泵校准数据（步/μL）/1000），（低浓度母液量-低浓度泵填充液量）/（（预排低浓度泵步数+制胶低浓度步数）/低浓度泵校准数据（步/μL）/1000）））"
                )

                //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）
                //1.1   获取高低浓度的平均校准因子
                val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                val p2jz = (AppStateUtils.hpc[2] ?: { x -> x * 100 }).invoke(1.0)
                val p3jz = (AppStateUtils.hpc[3] ?: { x -> x * 100 }).invoke(1.0)
                val highLowAvg = (p2jz + p3jz) / 2
                logInfo(
                    "calculate",
                    "===获取高低浓度的平均校准因子===$highLowAvg"
                )

                //1.2   胶液总步数
                val volumePulseCount = selected.volume * 1000 * highLowAvg
                logInfo(
                    "calculate",
                    "===01胶液总步数===$volumePulseCount"
                )
                //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）

                //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）
                val coagulantVol = selected.coagulant
                logInfo(
                    "calculate",
                    "===促凝剂加液量===$coagulantVol"
                )
                //促凝剂总步数
                val coagulantPulseCount = coagulantVol * p1jz
                logInfo(
                    "calculate",
                    "===02促凝剂总步数===$coagulantPulseCount"
                )
                //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）

                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60
                //制胶速度，根据这个速度转换其他泵的速度
                val speed = dataStore.readData("speed", 180)
                logInfo(
                    "calculate",
                    "===制胶速度===$speed"
                )
                //制胶所需时间
                val guleTime = volumePulseCount / 51200 / speed * 60
                logInfo(
                    "calculate",
                    "===03制胶所需时间===$guleTime"
                )
                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60

                //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）
                val guleFlow = selected.volume * 1000 / guleTime
                logInfo(
                    "calculate",
                    "===03A制胶总流速===$guleFlow"
                )
                //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）

                //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
                //母液低浓度
                val lowCoagulant = dataStore.readData("lowCoagulant", 4.0)
                logInfo(
                    "calculate",
                    "===母液低浓度===$lowCoagulant"
                )
                //母液高浓度
                val highCoagulant = dataStore.readData("highCoagulant", 20.0)
                logInfo(
                    "calculate",
                    "===母液高浓度===$highCoagulant"
                )
                //高浓度泵启动速度
                val highStartSpeed =
                    guleFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
                logInfo(
                    "calculate",
                    "===04高浓度泵启动速度===$highStartSpeed"
                )
                //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

                //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
                val lowEndSpeed =
                    guleFlow * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant) * p3jz * 60 / 51200
                logInfo(
                    "calculate",
                    "===05低浓度泵结束速度===$lowEndSpeed"
                )
                //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

                //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
                val highEndSpeed =
                    guleFlow * (lowCoagulant - selected.startRange) / (lowCoagulant - highCoagulant) * p2jz * 60 / 51200
                logInfo(
                    "calculate",
                    "===06高浓度泵结束速度===$highEndSpeed"
                )
                //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

                //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
                val lowStartSpeed =
                    guleFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
                logInfo(
                    "calculate",
                    "===07低浓度泵启动速度===$lowStartSpeed"
                )
                //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

                //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比
                //促凝剂变速比-默认1
                val ratio = 1
                logInfo(
                    "calculate",
                    "===促凝剂变速比===$ratio"
                )
                //促凝剂泵启动速度
                val coagulantStartSpeed = coagulantPulseCount / 51200 / guleTime * 60 * ratio
                logInfo(
                    "calculate",
                    "===08促凝剂泵启动速度===$coagulantStartSpeed"
                )
                //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比

                //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）
                val coagulantEndSpeed = coagulantPulseCount / 51200 / guleTime * 60 * (2 - ratio)
                logInfo(
                    "calculate",
                    "===09促凝剂泵结束速度===$coagulantEndSpeed"
                )
                //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）

                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                logInfo(
                    "calculate",
                    "===高浓度泵启动速度===$highStartSpeed====高浓度泵结束速度===$highEndSpeed===制胶所需时间===$guleTime==="
                )
                val guleHighPulse = (highStartSpeed + highEndSpeed) / 2 * guleTime / 60 * 51200
                logInfo(
                    "calculate",
                    "===10制胶高浓度泵步数===$guleHighPulse"
                )
                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                val guleLowPulse = (lowStartSpeed + lowEndSpeed) / 2 * guleTime / 60 * 51200
                logInfo(
                    "calculate",
                    "===11制胶低浓度泵步数===$guleLowPulse"
                )
                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

                //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））
                val highAcc = abs(highStartSpeed - highEndSpeed) / guleTime
                logInfo(
                    "calculate",
                    "===12高浓度泵加速度===$highAcc"
                )
                //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））

                //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)
                val lowAcc = abs(lowStartSpeed - lowEndSpeed) / guleTime
                logInfo(
                    "calculate",
                    "===13低浓度泵加速度===$lowAcc"
                )
                //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)

                //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）
                val coagulantAcc = abs(coagulantStartSpeed - coagulantEndSpeed) / guleTime
                logInfo(
                    "calculate",
                    "===14促凝剂泵加速度===$coagulantAcc"
                )
                //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）

                logInfo(
                    "calculate",
                    "===制胶前期准备数据结束==="
                )

                logInfo(
                    "calculate",
                    "===预排前期准备数据开始==="
                )

                //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）
                /**
                 * 高浓度预排液量
                 */
                val higeRehearsalVolume = setting.higeRehearsalVolume * 1000
                logInfo(
                    "calculate",
                    "===高浓度预排液量===$higeRehearsalVolume"
                )

                val highExpectedPulseCount = higeRehearsalVolume * highLowAvg
                logInfo(
                    "calculate",
                    "===15预排总步数===$highExpectedPulseCount"
                )
                //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）

                /**
                 *冲洗液泵转速
                 */
                val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                logInfo(
                    "calculate",
                    "===冲洗液泵转速===$rinseSpeed"
                )

                //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60
                val expectedTime = highExpectedPulseCount / 51200 / rinseSpeed * 60
                logInfo(
                    "calculate",
                    "===16预排时间===$expectedTime"
                )
                //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60

                //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）
                val expectedFlow = higeRehearsalVolume / expectedTime
                logInfo(
                    "calculate",
                    "===17预排总流速===$expectedFlow"
                )
                //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）

                //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
                val highExpectedSpeed =
                    expectedFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
                logInfo(
                    "calculate",
                    "===18预排高浓度泵速度===$highExpectedSpeed"
                )
                //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

                //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
                val lowExpectedSpeed =
                    expectedFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
                logInfo(
                    "calculate",
                    "===19预排低浓度泵速度===$lowExpectedSpeed"
                )
                //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

                //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比
                val coagulantExpectedPulse =
                    setting.higeRehearsalVolume * selected.coagulant / selected.volume * p1jz * ratio
                logInfo(
                    "calculate",
                    "===20预排促凝剂步数===$coagulantExpectedPulse"
                )
                //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比


                //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
                val highExpectedPulse = highExpectedSpeed * expectedTime / 60 * 51200
                logInfo(
                    "calculate",
                    "===21预排高浓度泵步数===$highExpectedPulse"
                )
                //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

                //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
                val lowExpectedPulse = lowExpectedSpeed * expectedTime / 60 * 51200
                logInfo(
                    "calculate",
                    "===22预排低浓度泵步数===$lowExpectedPulse"
                )
                //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

                //23预排促凝剂泵速度=预排促凝剂泵步数/每圈步数/预排时间（s）×60×促凝剂转速比
                val coagulantExpectedSpeed =
                    coagulantExpectedPulse / 51200 / expectedTime * 60 * ratio
                logInfo(
                    "calculate",
                    "===23预排促凝剂泵速度===$coagulantExpectedSpeed"
                )
                //23预排促凝剂泵速度=预排促凝剂泵总步数/每圈步数/预排时间（s）×60×促凝剂转速比


                /**
                 * 高浓度母液量
                 */
                val highCoagulantVol = _higemother.value
                logInfo("calculate", "highCoagulantVol===$highCoagulantVol")

                /**
                 * 高浓度泵填充液量
                 */
                val higeFilling = setting.higeFilling
                logInfo("calculate", "higeFilling===$higeFilling")

                /**
                 * 低浓度泵填充液量
                 */
                val lowFilling = setting.lowFilling
                logInfo("calculate", "lowFilling===$lowFilling")


                /**
                 * 低浓度母液量
                 */
                val lowCoagulantVol = _lowmother.value
                logInfo("calculate", "lowCoagulantVol===$lowCoagulantVol")



                logInfo(
                    "calculate",
                    "计算1===${(highCoagulantVol / ((highExpectedPulse + guleHighPulse) / p2jz / 1000))}"
                )


                logInfo(
                    "calculate",
                    "计算2===${(lowCoagulantVol / ((lowExpectedPulse + guleLowPulse) / p3jz / 1000))}"
                )

                /**
                 *  预计制胶数量=INT（MIN
                 *  （高浓度母液量/（（预排高浓度泵步数+制胶高浓度步数）/高浓度泵校准数据（步/μL）/1000），
                 *  低浓度母液量/（（预排低浓度泵步数+制胶低浓度步数）/低浓度泵校准数据（步/μL）/1000）））
                 *
                 */
                _calculate.value =
                    (highCoagulantVol / ((highExpectedPulse + guleHighPulse) / p2jz / 1000)).coerceAtMost(
                        (lowCoagulantVol / ((lowExpectedPulse + guleLowPulse) / p3jz / 1000))
                    ).toInt()
                logInfo(
                    "calculate",
                    " _calculate.value===${_calculate.value}"
                )
                delay(500)
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                delay(100)
                logInfo(
                    "calculate",
                    "预计制胶数量计算失败$ex"
                )
                _uiFlags.value = UiFlags.message("预计制胶数量计算失败$ex")
            }

        }
    }

    //高浓度预计所需母液体积（mL）=（预排高浓度泵步数+制胶高浓度步数）/高浓度泵校准数据（步/μL）/1000×预计制胶数量+高浓度泵填充液量×2
    //低浓度预计所需母液体积（mL）=（预排低浓度泵步数+制胶低浓度步数）/低浓度泵校准数据（步/μL）/1000×预计制胶数量+低浓度泵填充液量×2
    private fun higeLowMotherVol() {
        viewModelScope.launch {
            try {
                _uiFlags.value = UiFlags.objects(11)

                val selected = dao.getById(_selected.value).firstOrNull()
                logInfo("", "selected===$selected")
                if (selected == null) {
                    _uiFlags.value = UiFlags.message("未选择程序")
                    return@launch
                }


                val setting = slDao.getById(1L).firstOrNull()
                if (setting == null) {
                    _uiFlags.value = UiFlags.message("系统参数无数据")
                    return@launch
                }


                //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）
                //1.1   获取高低浓度的平均校准因子
                val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                val p2jz = (AppStateUtils.hpc[2] ?: { x -> x * 100 }).invoke(1.0)
                val p3jz = (AppStateUtils.hpc[3] ?: { x -> x * 100 }).invoke(1.0)
                val highLowAvg = (p2jz + p3jz) / 2
                logInfo(
                    "higeLowMotherVol",
                    "===p1jz===$p1jz"
                )
                logInfo(
                    "higeLowMotherVol",
                    "===获取高低浓度的平均校准因子===$highLowAvg"
                )
                //1.2   胶液总步数
                val volumePulseCount = selected.volume * 1000 * highLowAvg
                logInfo(
                    "higeLowMotherVol",
                    "===01胶液总步数===$volumePulseCount"
                )
                //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）


                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60
                //制胶速度，根据这个速度转换其他泵的速度
                val speed = dataStore.readData("speed", 180)
                logInfo(
                    "higeLowMotherVol",
                    "===制胶速度===$speed"
                )
                //制胶所需时间
                val guleTime = volumePulseCount / 51200 / speed * 60
                logInfo(
                    "higeLowMotherVol",
                    "===03制胶所需时间===$guleTime"
                )
                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60

                //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）
                val guleFlow = selected.volume * 1000 / guleTime
                logInfo(
                    "higeLowMotherVol",
                    "===03A制胶总流速===$guleFlow"
                )
                //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）

                //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
                //母液低浓度
                val lowCoagulant = dataStore.readData("lowCoagulant", 4.0)
                logInfo(
                    "higeLowMotherVol",
                    "===母液低浓度===$lowCoagulant"
                )
                //母液高浓度
                val highCoagulant = dataStore.readData("highCoagulant", 20.0)
                logInfo(
                    "higeLowMotherVol",
                    "===母液高浓度===$highCoagulant"
                )
                //高浓度泵启动速度
                val highStartSpeed =
                    guleFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
                logInfo(
                    "higeLowMotherVol",
                    "===04高浓度泵启动速度===$highStartSpeed"
                )
                //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

                //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
                val lowEndSpeed =
                    guleFlow * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant) * p3jz * 60 / 51200
                logInfo(
                    "higeLowMotherVol",
                    "===05低浓度泵结束速度===$lowEndSpeed"
                )
                //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

                //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
                val highEndSpeed =
                    guleFlow * (lowCoagulant - selected.startRange) / (lowCoagulant - highCoagulant) * p2jz * 60 / 51200
                logInfo(
                    "higeLowMotherVol",
                    "===06高浓度泵结束速度===$highEndSpeed"
                )
                //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

                //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
                val lowStartSpeed =
                    guleFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
                logInfo(
                    "higeLowMotherVol",
                    "===07低浓度泵启动速度===$lowStartSpeed"
                )
                //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

                //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比
                //促凝剂变速比-默认1
                val ratio = 1
                logInfo(
                    "higeLowMotherVol",
                    "===促凝剂变速比===$ratio"
                )


                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                logInfo(
                    "higeLowMotherVol",
                    "===高浓度泵启动速度===$highStartSpeed====高浓度泵结束速度===$highEndSpeed===制胶所需时间===$guleTime==="
                )
                val guleHighPulse = (highStartSpeed + highEndSpeed) / 2 * guleTime / 60 * 51200
                logInfo(
                    "higeLowMotherVol",
                    "===10制胶高浓度泵步数===$guleHighPulse"
                )
                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                val guleLowPulse = (lowStartSpeed + lowEndSpeed) / 2 * guleTime / 60 * 51200
                logInfo(
                    "higeLowMotherVol",
                    "===11制胶低浓度泵步数===$guleLowPulse"
                )
                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

                //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））
                val highAcc = abs(highStartSpeed - highEndSpeed) / guleTime
                logInfo(
                    "higeLowMotherVol",
                    "===12高浓度泵加速度===$highAcc"
                )
                //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））

                //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)
                val lowAcc = abs(lowStartSpeed - lowEndSpeed) / guleTime
                logInfo(
                    "higeLowMotherVol",
                    "===13低浓度泵加速度===$lowAcc"
                )
                //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)


                //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）
                /**
                 * 高浓度预排液量
                 */
                val higeRehearsalVolume = setting.higeRehearsalVolume * 1000
                logInfo(
                    "HomeViewModel_startJob",
                    "===高浓度预排液量===$higeRehearsalVolume"
                )

                val highExpectedPulseCount = higeRehearsalVolume * highLowAvg
                logInfo(
                    "higeLowMotherVol",
                    "===15预排总步数===$highExpectedPulseCount"
                )
                //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）

                /**
                 *冲洗液泵转速
                 */
                val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                logInfo(
                    "higeLowMotherVol",
                    "===冲洗液泵转速===$rinseSpeed"
                )

                //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60
                val expectedTime = highExpectedPulseCount / 51200 / rinseSpeed * 60
                logInfo(
                    "higeLowMotherVol",
                    "===16预排时间===$expectedTime"
                )
                //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60

                //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）
                val expectedFlow = higeRehearsalVolume / expectedTime
                logInfo(
                    "higeLowMotherVol",
                    "===17预排总流速===$expectedFlow"
                )
                //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）

                //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
                val highExpectedSpeed =
                    expectedFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
                logInfo(
                    "higeLowMotherVol",
                    "===18预排高浓度泵速度===$highExpectedSpeed"
                )
                //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

                //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
                val lowExpectedSpeed =
                    expectedFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
                logInfo(
                    "higeLowMotherVol",
                    "===19预排低浓度泵速度===$lowExpectedSpeed"
                )
                //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数


                //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比
                val coagulantExpectedPulse =
                    setting.higeRehearsalVolume * selected.coagulant / selected.volume * p1jz * ratio
                logInfo(
                    "higeLowMotherVol",
                    "===20预排促凝剂步数===$coagulantExpectedPulse"
                )


                //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
                val highExpectedPulse = highExpectedSpeed * expectedTime / 60 * 51200
                logInfo(
                    "higeLowMotherVol",
                    "===21预排高浓度泵步数===$highExpectedPulse"
                )
                //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

                //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
                val lowExpectedPulse = lowExpectedSpeed * expectedTime / 60 * 51200
                logInfo(
                    "higeLowMotherVol",
                    "===22预排低浓度泵步数===$lowExpectedPulse"
                )
                //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

                /**
                 * 高浓度母液量
                 */

                val highCoagulantVol = _higemother.value
                logInfo("higeLowMotherVol", "highCoagulantVol===$highCoagulantVol")

                /**
                 * 高浓度泵填充液量
                 */
                val higeFilling = setting.higeFilling
                logInfo("higeLowMotherVol", "higeFilling===$higeFilling")

                /**
                 * 低浓度泵填充液量
                 */
                val lowFilling = setting.lowFilling
                logInfo("higeLowMotherVol", "lowFilling===$lowFilling")


                /**
                 * 低浓度母液量
                 */
                val lowCoagulantVol = _lowmother.value
                logInfo("higeLowMotherVol", "lowCoagulantVol===$lowCoagulantVol")

                val expectedMakenum = dataStore.readData("expectedMakenum", 0)
                //高浓度预计所需母液体积（mL）=（预排高浓度泵步数+制胶高浓度步数）/高浓度泵校准数据（步/μL）/1000×预计制胶数量+高浓度泵填充液量×2
                val higeMother =
                    (highExpectedPulse + guleHighPulse) / p2jz / 1000 * expectedMakenum + higeFilling * 2
                logInfo("higeLowMotherVol", "higeMother=========$higeMother")
                //高浓度预计所需母液体积（mL）=（预排高浓度泵步数+制胶高浓度步数）/高浓度泵校准数据（步/μL）/1000×预计制胶数量+高浓度泵填充液量×2


                val higeSolution: Float = higeMother.toFormattedFloatUsingBigDecimal()


                //低浓度预计所需母液体积（mL）=（预排低浓度泵步数+制胶低浓度步数）/低浓度泵校准数据（步/μL）/1000×预计制胶数量+低浓度泵填充液量×2
                val lowMother =
                    (lowExpectedPulse + guleLowPulse) / p3jz / 1000 * expectedMakenum + lowFilling * 2
                //低浓度预计所需母液体积（mL）=（预排低浓度泵步数+制胶低浓度步数）/低浓度泵校准数据（步/μL）/1000×预计制胶数量+低浓度泵填充液量×2
                logInfo("higeLowMotherVol", "lowMother=========$lowMother")
                val lowSolution: Float = lowMother.toFormattedFloatUsingBigDecimal()


                //促凝剂推荐液量（mL）=IF(((预排促凝剂步数/促凝剂泵校准数据（步/μL）+促凝剂体积)/1000×预计制胶数量+促凝剂泵填充液量+2）＜10，10，((预排促凝剂步数/促凝剂泵校准数据（步/μL）+促凝剂体积)/1000×预计制胶数量+促凝剂泵填充液量+2））
                val coagulantMother =
                    (coagulantExpectedPulse / p1jz + selected.coagulant) / 1000 * expectedMakenum + setting.coagulantFilling + 2
                logInfo("higeLowMotherVol", "coagulantMother=========$coagulantMother")

                var coagulantSolution: Float
                if (coagulantMother < 10) {
                    coagulantSolution = 10f
                } else {
                    coagulantSolution = coagulantMother.toFormattedFloatUsingBigDecimal()
                    logInfo("higeLowMotherVol", "coagulantSolution=========$coagulantMother")

                }

                val a =
                    (setting.rinseCleanVolume * (expectedMakenum + 1) + setting.rinseFilling * 2) / 50
                val b = ((floor(a).toInt()) + 1) * 50

                _higemother.value = higeSolution
                _lowmother.value = lowSolution
                _watermother.value = b.toFloat()
                _coagulantmother.value = coagulantSolution

                logInfo("higeLowMotherVol", "_higemother=========${_higemother.value}")
                logInfo("higeLowMotherVol", "_lowmother=========${_lowmother.value}")
                logInfo("higeLowMotherVol", "__watermother=========${_watermother.value}")
                logInfo("higeLowMotherVol", "_coagulantmother=========${_coagulantmother.value}")

                delay(200)
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                delay(100)
                _uiFlags.value = UiFlags.message("母液量设置失败$ex")
            }

        }
    }


    private fun Double.toFormattedFloatUsingBigDecimal(): Float {
        return if (this.isNaN()) {
            0.0f
        } else {
            BigDecimal(this).setScale(1, RoundingMode.HALF_UP).toFloat()
        }
    }

    private fun startJob(status: Int) {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(0)
            val selected = dao.getById(_selected.value).firstOrNull()
            if (selected == null) {
                _uiFlags.value = UiFlags.message("未选择程序")
                return@launch
            }


            val setting = slDao.getById(1L).firstOrNull()
            if (setting == null) {
                _uiFlags.value = UiFlags.message("系统参数无数据")
                return@launch
            }

            if (_heartbeatError.value) {
                _uiFlags.value = UiFlags.message("上下位机通讯失联")
                return@launch
            }

            /**
             * 第一次进入方法检测是否有制胶架
             */
            val selectRudio = dataStore.readData("selectRudio", 1)
            if (status == 0) {
                gpio(3)
                delay(500)
                if (!getGpio(3)) {
                    if (selectRudio == 2) {
                        ApplicationUtils.ctx.playAudio(R.raw.hint_voice)
                    } else if (selectRudio == 1) {
                        ApplicationUtils.ctx.playAudio(R.raw.hint_buzz)
                    }
                    _uiFlags.value = UiFlags.message("制胶架没有正确放置")
                    return@launch
                }
            }



            _waitTimeRinseJob.value?.cancel()
            _waitTimeRinseJob.value = null
            _waitTimeRinseNum.value = 0
            _hintJob.value?.cancel()
            _hintJob.value = null

            lightYellow()
            delay(100)
            val xSpeed = dataStore.readData("xSpeed", 600L)

            logInfo(
                "HomeViewModel_startJob",
                "===制胶前期准备数据开始==="
            )


            //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）
            //1.1   获取高低浓度的平均校准因子
            val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
            val p2jz = (AppStateUtils.hpc[2] ?: { x -> x * 100 }).invoke(1.0)
            val p3jz = (AppStateUtils.hpc[3] ?: { x -> x * 100 }).invoke(1.0)
            val highLowAvg = (p2jz + p3jz) / 2
            logInfo(
                "HomeViewModel_startJob",
                "===获取高低浓度的平均校准因子===$highLowAvg"
            )

            //3.0.2新增测试促凝剂预排增加5微升
            val p1jz5 = (p1jz * 5).toLong()

            //1.2   胶液总步数
            val volumePulseCount = selected.volume * 1000 * highLowAvg
            logInfo(
                "HomeViewModel_startJob",
                "===01胶液总步数===$volumePulseCount"
            )
            //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）

            //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）
            val coagulantVol = selected.coagulant
            logInfo(
                "HomeViewModel_startJob",
                "===促凝剂加液量===$coagulantVol"
            )
            //促凝剂总步数
            val coagulantPulseCount = coagulantVol * p1jz
            logInfo(
                "HomeViewModel_startJob",
                "===02促凝剂总步数===$coagulantPulseCount"
            )
            //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）

            //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60
            //制胶速度，根据这个速度转换其他泵的速度
            val speed = dataStore.readData("speed", 180)
            logInfo(
                "HomeViewModel_startJob",
                "===制胶速度===$speed"
            )
            //制胶所需时间
            val guleTime = volumePulseCount / 51200 / speed * 60
            logInfo(
                "HomeViewModel_startJob",
                "===03制胶所需时间===$guleTime"
            )
            //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60

            //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）
            val guleFlow = selected.volume * 1000 / guleTime
            logInfo(
                "HomeViewModel_startJob",
                "===03A制胶总流速===$guleFlow"
            )
            //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）

            //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            //母液低浓度
            val lowCoagulant = dataStore.readData("lowCoagulant", 4.0)
            logInfo(
                "HomeViewModel_startJob",
                "===母液低浓度===$lowCoagulant"
            )
            //母液高浓度
            val highCoagulant = dataStore.readData("highCoagulant", 20.0)
            logInfo(
                "HomeViewModel_startJob",
                "===母液高浓度===$highCoagulant"
            )
            //高浓度泵启动速度
            val highStartSpeed =
                guleFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
            logInfo(
                "HomeViewModel_startJob",
                "===04高浓度泵启动速度===$highStartSpeed"
            )
            //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowEndSpeed =
                guleFlow * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant) * p3jz * 60 / 51200
            logInfo(
                "HomeViewModel_startJob",
                "===05低浓度泵结束速度===$lowEndSpeed"
            )
            //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            val highEndSpeed =
                guleFlow * (lowCoagulant - selected.startRange) / (lowCoagulant - highCoagulant) * p2jz * 60 / 51200
            logInfo(
                "HomeViewModel_startJob",
                "===06高浓度泵结束速度===$highEndSpeed"
            )
            //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowStartSpeed =
                guleFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
            logInfo(
                "HomeViewModel_startJob",
                "===07低浓度泵启动速度===$lowStartSpeed"
            )
            //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比
            //促凝剂变速比-默认1
            val ratio = 1
            logInfo(
                "HomeViewModel_startJob",
                "===促凝剂变速比===$ratio"
            )
            //促凝剂泵启动速度
            val coagulantStartSpeed = coagulantPulseCount / 51200 / guleTime * 60 * ratio
            logInfo(
                "HomeViewModel_startJob",
                "===08促凝剂泵启动速度===$coagulantStartSpeed"
            )
            //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比

            //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）
            val coagulantEndSpeed = coagulantPulseCount / 51200 / guleTime * 60 * (2 - ratio)
            logInfo(
                "HomeViewModel_startJob",
                "===09促凝剂泵结束速度===$coagulantEndSpeed"
            )
            //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）

            //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
            logInfo(
                "HomeViewModel_startJob",
                "===高浓度泵启动速度===$highStartSpeed====高浓度泵结束速度===$highEndSpeed===制胶所需时间===$guleTime==="
            )
            val guleHighPulse = (highStartSpeed + highEndSpeed) / 2 * guleTime / 60 * 51200
            logInfo(
                "HomeViewModel_startJob",
                "===10制胶高浓度泵步数===$guleHighPulse"
            )
            //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

            //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
            val guleLowPulse = (lowStartSpeed + lowEndSpeed) / 2 * guleTime / 60 * 51200
            logInfo(
                "HomeViewModel_startJob",
                "===11制胶低浓度泵步数===$guleLowPulse"
            )
            //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

            //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））
            val highAcc = abs(highStartSpeed - highEndSpeed) / guleTime
            logInfo(
                "HomeViewModel_startJob",
                "===12高浓度泵加速度===$highAcc"
            )
            //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））

            //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)
            val lowAcc = abs(lowStartSpeed - lowEndSpeed) / guleTime
            logInfo(
                "HomeViewModel_startJob",
                "===13低浓度泵加速度===$lowAcc"
            )
            //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)

            //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）
            val coagulantAcc = abs(coagulantStartSpeed - coagulantEndSpeed) / guleTime
            logInfo(
                "HomeViewModel_startJob",
                "===14促凝剂泵加速度===$coagulantAcc"
            )
            //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）

            logInfo(
                "HomeViewModel_startJob",
                "===制胶前期准备数据结束==="
            )

            logInfo(
                "HomeViewModel_startJob",
                "===预排前期准备数据开始==="
            )

            //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）
            /**
             * 高浓度预排液量
             */
            val higeRehearsalVolume = setting.higeRehearsalVolume * 1000
            logInfo(
                "HomeViewModel_startJob",
                "===高浓度预排液量===$higeRehearsalVolume"
            )

            val highExpectedPulseCount = higeRehearsalVolume * highLowAvg
            logInfo(
                "HomeViewModel_startJob",
                "===15预排总步数===$highExpectedPulseCount"
            )
            //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）

            /**
             *冲洗液泵转速
             */
            val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
            logInfo(
                "HomeViewModel_startJob",
                "===冲洗液泵转速===$rinseSpeed"
            )

            //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60
            val expectedTime = highExpectedPulseCount / 51200 / rinseSpeed * 60
            logInfo(
                "HomeViewModel_startJob",
                "===16预排时间===$expectedTime"
            )
            //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60

            //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）
            val expectedFlow = higeRehearsalVolume / expectedTime
            logInfo(
                "HomeViewModel_startJob",
                "===17预排总流速===$expectedFlow"
            )
            //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）

            //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            val highExpectedSpeed =
                expectedFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
            logInfo(
                "HomeViewModel_startJob",
                "===18预排高浓度泵速度===$highExpectedSpeed"
            )
            //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowExpectedSpeed =
                expectedFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
            logInfo(
                "HomeViewModel_startJob",
                "===19预排低浓度泵速度===$lowExpectedSpeed"
            )
            //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比
            val coagulantExpectedPulse =
                setting.higeRehearsalVolume * selected.coagulant / selected.volume * p1jz * ratio
            logInfo(
                "HomeViewModel_startJob",
                "===20预排促凝剂步数===$coagulantExpectedPulse"
            )
            //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比


            //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
            val highExpectedPulse = highExpectedSpeed * expectedTime / 60 * 51200
            logInfo(
                "HomeViewModel_startJob",
                "===21预排高浓度泵步数===$highExpectedPulse"
            )
            //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

            //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
            val lowExpectedPulse = lowExpectedSpeed * expectedTime / 60 * 51200
            logInfo(
                "HomeViewModel_startJob",
                "===22预排低浓度泵步数===$lowExpectedPulse"
            )
            //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

            //23预排促凝剂泵速度=预排促凝剂泵步数/每圈步数/预排时间（s）×60×促凝剂转速比
            val coagulantExpectedSpeed = coagulantExpectedPulse / 51200 / expectedTime * 60 * ratio
            logInfo(
                "HomeViewModel_startJob",
                "===23预排促凝剂泵速度===$coagulantExpectedSpeed"
            )
            //23预排促凝剂泵速度=预排促凝剂泵总步数/每圈步数/预排时间（s）×60×促凝剂转速比


            logInfo(
                "HomeViewModel_startJob",
                "===预排前期准备数据结束==="
            )

            //制胶使用液量

            _higemother.value -= ((guleHighPulse + highExpectedPulse) / p2jz / 1000).toFloat()
            _lowmother.value -= ((guleLowPulse + lowExpectedPulse) / p3jz / 1000).toFloat()
            _coagulantmother.value -= ((coagulantPulseCount + coagulantExpectedPulse) / p1jz / 1000).toFloat()
            if (_coagulantmother.value < 0) {
                _coagulantmother.value = 0f
            }
            _watermother.value -= setting.rinseCleanVolume.toFloat()


            _higemother.value = String.format("%.1f", _higemother.value).toFloat()
            _lowmother.value = String.format("%.1f", _lowmother.value).toFloat()
            _coagulantmother.value = String.format("%.1f", _coagulantmother.value).toFloat()
            _watermother.value = String.format("%.1f", _watermother.value).toFloat()

            delay(100)
            //制胶使用液量

            var coagulantBool = false


            logInfo(
                "HomeViewModel_startJob",
                "===运动次数status===$status"
            )

            val selectedER = erDao.getById(_selectedER.value).firstOrNull()

            val countER = erDao.count()

            val modelsThickness = dataStore.readData("modelsThickness", "G1520")
            println("modelsThickness====$modelsThickness")
            println("countER====$countER")

            if (status == 0) {
                _stautsNum.value = 1
                _complate.value = 0

                coagulantStart.value =
                    (coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong() + p1jz5) * _stautsNum.value
                logInfo(
                    "HomeViewModel_startJob",
                    "===已经运动${_stautsNum.value}次的柱塞泵步数===${coagulantStart.value}"
                )
                if (modelsThickness == "G1500") {
                    if (countER < 10) {
                        _selectedER.value = erDao.insert(
                            ExperimentRecord(
                                startRange = selected.startRange,
                                endRange = selected.endRange,
                                thickness = selected.thickness,
                                coagulant = selected.coagulant,
                                volume = selected.volume,
                                number = 0,
                                status = EPStatus.RUNNING,
                                detail = "",
                            )
                        )
                    }
                } else if (modelsThickness == "G1510") {
                    if (countER < 20) {
                        _selectedER.value = erDao.insert(
                            ExperimentRecord(
                                startRange = selected.startRange,
                                endRange = selected.endRange,
                                thickness = selected.thickness,
                                coagulant = selected.coagulant,
                                volume = selected.volume,
                                number = 0,
                                status = EPStatus.RUNNING,
                                detail = "",
                            )
                        )
                    }
                } else {
                    if (countER < 30) {
                        _selectedER.value = erDao.insert(
                            ExperimentRecord(
                                startRange = selected.startRange,
                                endRange = selected.endRange,
                                thickness = selected.thickness,
                                coagulant = selected.coagulant,
                                volume = selected.volume,
                                number = 0,
                                status = EPStatus.RUNNING,
                                detail = "",
                            )
                        )
                    }
                }


            } else {

                if (modelsThickness == "G1500") {
                    if (countER < 10) {
                        if (selectedER != null) {
                            selectedER.number = _complate.value
                            erDao.update(selectedER)
                        }
                    }
                } else if (modelsThickness == "G1510") {
                    if (countER < 20) {
                        if (selectedER != null) {
                            selectedER.number = _complate.value
                            erDao.update(selectedER)
                        }
                    }
                } else {
                    if (countER < 30) {
                        if (selectedER != null) {
                            selectedER.number = _complate.value
                            erDao.update(selectedER)
                        }
                    }

                }


                //计算柱塞泵是否够下一次运动
                /**
                 * 柱塞泵总行程
                 */
                val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()


                /**
                 * 柱塞泵剩余步数
                 */
                val coagulantSy = coagulantpulse - coagulantStart.value
                logInfo(
                    "HomeViewModel_startJob",
                    "===已经运动${_stautsNum.value}次的柱塞泵剩余步数===${coagulantSy}"
                )
                logInfo(
                    "HomeViewModel_startJob",
                    "===柱塞泵剩余步数===$coagulantSy"
                )

                if (coagulantSy < coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong() + p1jz5) {
                    /**
                     * 柱塞泵剩余步数不够加液
                     */
                    _stautsNum.value = 1
                    coagulantBool = true
                    logInfo(
                        "HomeViewModel_startJob",
                        "===柱塞泵剩余步数的_stautsNum.value===${_stautsNum.value}"
                    )
                } else {
                    _stautsNum.value += 1
                    /**
                     * 已经运动的柱塞泵步数
                     */
                    coagulantStart.value =
                        (coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong() + p1jz5) * _stautsNum.value

                }

                logInfo(
                    "HomeViewModel_startJob",
                    "===已经运动${_stautsNum.value}次的柱塞泵步数===${coagulantStart.value}"
                )

            }
            /**
             * 计算废液槽
             */
            logInfo(
                "HomeViewModel_startJob",
                "===计算前的废液槽加液量===${_wasteprogress.value}"
            )

            _wasteprogress.value +=
                (setting.higeRehearsalVolume / 150).toFloat()

            logInfo(
                "HomeViewModel_startJob",
                "===计算后的废液槽加液量===${_wasteprogress.value}"
            )

            _job2.value?.cancel()
            _job2.value = null
            _job.value?.cancel()
            _job.value = launch {
                try {

                    launch {
                        var startTime = 0
                        val guleTimeToInt = ceil(guleTime) + 30
                        while (startTime < guleTimeToInt) {
                            delay(1000L)
                            startTime += 1
                            if (_heartbeatError.value) {
                                var experimentRecord =
                                    erDao.getById(_selectedER.value).firstOrNull()
                                val countER = erDao.count()
                                val modelsThickness = dataStore.readData("modelsThickness", "G1520")
                                println("modelsThickness====$modelsThickness")
                                println("countER====$countER")
                                if (modelsThickness == "G1500") {
                                    if (countER < 10) {
                                        if (experimentRecord != null) {
                                            experimentRecord.status = EPStatus.FAULT
                                            experimentRecord.detail = "上下位机断开连接"
                                            erDao.update(experimentRecord)
                                        }
                                    }
                                } else if (modelsThickness == "G1510") {
                                    if (countER < 20) {
                                        if (experimentRecord != null) {
                                            experimentRecord.status = EPStatus.FAULT
                                            experimentRecord.detail = "上下位机断开连接"
                                            erDao.update(experimentRecord)
                                        }
                                    }
                                } else {
                                    if (countER < 30) {
                                        if (experimentRecord != null) {
                                            experimentRecord.status = EPStatus.FAULT
                                            experimentRecord.detail = "上下位机断开连接"
                                            erDao.update(experimentRecord)
                                        }
                                    }

                                }

                                delay(100)
                                logInfo(
                                    "HomeViewModel_startJob",
                                    "===上下位机断开连接==="
                                )
                                errorDao.insert(ErrorRecord(detail = "上下位机断开连接"))
                                delay(100)
                                _job.value?.cancel()
                                _job.value = null
                                break
                            }
                        }
                    }

                    if (selectRudio == 2) {
                        ApplicationUtils.ctx.playAudio(R.raw.startjob_voice)
                    } else if (selectRudio == 1) {
                        ApplicationUtils.ctx.playAudio(R.raw.start_buzz)
                    }
                    if (coagulantBool) {
                        logInfo(
                            "HomeViewModel_startJob",
                            "===柱塞泵回到下拉到底==="
                        )
                        start {
                            exceptionPolicy = ExceptionPolicy.SKIP
                            timeOut = 1000L * 60 * 1
                            with(
                                index = 1,
                                pdv = -coagulantStart.value,
                                ads = Triple(
                                    (600 * 13).toLong(),
                                    (600 * 1193).toLong(),
                                    (600 * 1193).toLong()
                                )
                            )
                        }
                        logInfo(
                            "HomeViewModel_startJob",
                            "===柱塞泵回到下拉到底==="
                        )


                        coagulantStart.value =
                            (coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong() + p1jz5) * _stautsNum.value

                    }


                    //===================废液槽运动开始=====================
                    _uiFlags.value = UiFlags.objects(101)
                    logInfo(
                        "HomeViewModel_startJob",
                        "===废液槽运动开始==="
                    )
                    //废液槽位置
                    start {
                        exceptionPolicy = ExceptionPolicy.SKIP
                        timeOut = 1000L * 60L
                        with(
                            index = 0,
                            pdv = setting.wastePosition,
                            ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                        )
                    }
                    logInfo(
                        "HomeViewModel_startJob",
                        "===废液槽运动结束==="
                    )
//===================废液槽运动结束=====================

                    /**
                     * 预排液
                     */
                    //===================预排液开始=====================
                    logInfo(
                        "HomeViewModel_startJob",
                        "===预排液开始==="
                    )
                    //测算使用时间
                    var startTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel_startJob",
                        "===预排液开始时间===$startTime"
                    )

                    logInfo(
                        "HomeViewModel_startJob",
                        "===柱塞泵的加液步数===${coagulantExpectedPulse.toLong() + p1jz5}====启动速度===$coagulantExpectedSpeed===结束速度===$coagulantExpectedSpeed"
                    )


                    logInfo(
                        "HomeViewModel_startJob",
                        "===高浓度泵的加液步数===${highExpectedPulse.toLong()}====启动速度===$highExpectedSpeed===结束速度===$highExpectedSpeed"
                    )


                    logInfo(
                        "HomeViewModel_startJob",
                        "===低浓度泵的加液步数===${lowExpectedPulse.toLong()}====启动速度===$lowExpectedSpeed===结束速度===$lowExpectedSpeed"
                    )

                    start {
                        exceptionPolicy = ExceptionPolicy.SKIP
                        timeOut = 1000L * 60 * 1
                        with(
                            index = 1,
                            pdv = coagulantExpectedPulse.toLong() + p1jz5,
                            ads = Triple(
                                0L,
                                (coagulantExpectedSpeed * 1193).toLong(),
                                (coagulantExpectedSpeed * 1193).toLong()
                            )
                        )

                        with(
                            index = 2,
                            pdv = highExpectedPulse.toLong(),
                            ads = Triple(
                                0L,
                                (highExpectedSpeed * 1193).toLong(),
                                (highExpectedSpeed * 1193).toLong()
                            )
                        )

                        with(
                            index = 3,
                            pdv = lowExpectedPulse.toLong(),
                            ads = Triple(
                                0L,
                                (lowExpectedSpeed * 1193).toLong(),
                                (lowExpectedSpeed * 1193).toLong()
                            )
                        )
                    }
                    var endTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel_startJob",
                        "===预排液结束时间===$startTime"
                    )
                    //秒
                    var dTime = (endTime - startTime) / 1000
                    logInfo(
                        "HomeViewModel_startJob",
                        "===预排液结束时间差===$dTime"
                    )
                    var time = (dTime / 3600.0)
                    logInfo(
                        "HomeViewModel_startJob",
                        "===预排液结束时间1小时的百分比未精确小数点===$time"
                    )

                    setting.highTime += time
                    setting.lowLife += time


                    setting.highTime = String.format("%.4f", setting.highTime).toDouble()
                    setting.lowLife = String.format("%.4f", setting.lowLife).toDouble()

                    slDao.update(setting)

                    //===================预排液结束=====================
                    logInfo(
                        "HomeViewModel_startJob",
                        "===预排液结束==="
                    )


                    //===================制胶位置移动开始=====================
                    logInfo(
                        "HomeViewModel_startJob",
                        "===制胶位置移动开始==="
                    )
                    //制胶位置
                    _uiFlags.value = UiFlags.objects(102)
                    start {
                        timeOut = 1000L * 60L
//                        with(index = 0, pdv = glueBoardPosition)
                        with(
                            index = 0,
                            pdv = setting.glueBoardPosition,
                            ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                        )

                    }
                    logInfo(
                        "HomeViewModel_startJob",
                        "===制胶位置移动结束==="
                    )
                    delay(100)

                    logInfo(
                        "HomeViewModel_startJob",
                        "===制胶运动开始==="
                    )

                    logInfo(
                        "HomeViewModel_startJob",
                        "===柱塞泵参数===步数===$coagulantPulseCount===加速度===${(coagulantAcc * 13).toLong()}===开始速度===${(coagulantStartSpeed * 1193).toLong()}===结束速度==${(coagulantEndSpeed * 1193).toLong()}"
                    )
                    logInfo(
                        "HomeViewModel_startJob",
                        "===高浓度泵参数===步数===${guleHighPulse.toLong()}===加速度===${(highAcc * 13).toLong()}===开始速度===${(highEndSpeed * 1193).toLong()}===结束速度==${(highStartSpeed * 1193).toLong()}"
                    )
                    logInfo(
                        "HomeViewModel_startJob",
                        "===低浓度泵参数===步数===${guleLowPulse.toLong()}===加速度===${(lowAcc * 13).toLong()}===开始速度===${(lowStartSpeed * 1193).toLong()}===结束速度==${(lowEndSpeed * 1193).toLong()}"
                    )


                    launch {
                        var startTime = 0
                        val guleTimeToInt = ceil(guleTime)
                        while (startTime < guleTimeToInt) {
                            delay(1000L)
                            startTime += 1
                            var pro = (startTime / guleTimeToInt).toFloat()
                            _progress.value = pro
                        }
                    }


                    startTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel_startJob",
                        "===制胶开始时间===$startTime"
                    )
                    start {
                        exceptionPolicy = ExceptionPolicy.SKIP
                        timeOut = (ceil(guleTime) + 30).toLong() * 1000L
                        with(
                            index = 1,
                            pdv = coagulantPulseCount.toLong(),
                            ads = Triple(
                                if ((coagulantAcc * 13).toLong() == 0L) 1 else (coagulantAcc * 13).toLong(),
                                (coagulantStartSpeed * 1193).toLong(),
                                (coagulantEndSpeed * 1193).toLong()
                            )
                        )
                        with(
                            index = 2,
                            pdv = guleHighPulse.toLong(),
                            ads = Triple(
                                (highAcc * 13).toLong(),
                                (highEndSpeed * 1193).toLong(),
                                (highStartSpeed * 1193).toLong()
                            )
                        )
                        with(
                            index = 3,
                            pdv = guleLowPulse.toLong(),
                            ads = Triple(
                                (lowAcc * 13).toLong(),
                                (lowStartSpeed * 1193).toLong(),
                                (lowEndSpeed * 1193).toLong()
                            )
                        )
                    }

                    endTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel_startJob",
                        "===制胶结束时间===$startTime"
                    )

                    //秒
                    dTime = (endTime - startTime) / 1000
                    time = (dTime / 3600.0)
                    setting.highTime += time
                    setting.lowLife += time

                    setting.highTime = String.format("%.4f", setting.highTime).toDouble()
                    setting.lowLife = String.format("%.4f", setting.lowLife).toDouble()
                    slDao.update(setting)

                    logInfo(
                        "HomeViewModel_startJob",
                        "===制胶运动结束==="
                    )
                    //===================制胶运动结束=====================

                    //===================x轴复位，防止x轴运动偏移位置=====================
                    logInfo(
                        "HomeViewModel_startJob",
                        "x轴复位，防止x轴运动偏移位置，复位开始"
                    )
                    _uiFlags.value = UiFlags.objects(103)
                    start {
                        timeOut = 1000L * 30
                        with(
                            index = 0,
                            pdv = -64000L,
                            ads = Triple(1600, 1600, 1600),

                            )
                    }
                    logInfo(
                        "HomeViewModel_startJob",
                        "x轴复位，防止x轴运动偏移位置，复位结束"
                    )
                    //===================x轴复位，防止x轴运动偏移位置=====================


                    val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                    logInfo(
                        "HomeViewModel_clean",
                        "冲洗转速===$rinseSpeed"
                    )
                    delay(2000)
                    //制胶完成，清洗运动
                    /**
                     * 冲洗液泵清洗液量
                     */
                    val rinseP = pulse(index = 4, dvp = setting.rinseCleanVolume * 1000)
                    //制胶进度归0
                    _progress.value = 0f
                    start {
                        exceptionPolicy = ExceptionPolicy.SKIP
                        timeOut = 1000L * 60L
                        with(
                            index = 0,
                            ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                            pdv = setting.wastePosition
                        )
                    }

                    //测算使用时间
                    startTime = Calendar.getInstance().timeInMillis
                    delay(100)
                    start {
                        exceptionPolicy = ExceptionPolicy.SKIP
                        timeOut = 1000L * 60L
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                            pdv = rinseP
                        )
                    }
                    _wasteprogress.value += (setting.rinseCleanVolume / 150).toFloat()
                    endTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel_startJob",
                        "===冲洗结束时间===$startTime"
                    )
                    //秒
                    dTime = (endTime - startTime) / 1000
                    time = (dTime / 3600.0)
                    setting.rinseTime += time
                    setting.rinseTime = String.format("%.4f", setting.rinseTime).toDouble()
                    slDao.update(setting)


                    delay(100)

                    _complate.value += 1
                    val expectedMakenum = dataStore.readData("expectedMakenum", 0)
                    waitTimeRinse()
                    if (_complate.value == expectedMakenum) {
                        delay(100)
                        //运动结束
                        if (selectRudio == 2) {
                            ApplicationUtils.ctx.playAudio(R.raw.startend_voice)
                        } else if (selectRudio == 1) {
                            ApplicationUtils.ctx.playAudio(R.raw.startend_buzz)
                        }

                        _uiFlags.value = UiFlags.objects(6)
                    } else {
                        //更换制胶架
                        endStartFlashYellow()
                        delay(100)
                        if (selectRudio == 2) {
                            ApplicationUtils.ctx.playAudio(R.raw.replace_voice)
                        } else if (selectRudio == 1) {
//                            ApplicationUtils.ctx.playAudio(R.raw.hint_buzz)
                            hintBuzz()
                        }
                        _hint.value = false
                        hint()
                        _uiFlags.value = UiFlags.objects(4)
                    }

                } catch (ex: Exception) {
                    _uiFlags.value = UiFlags.none()
                    _job.value?.cancel()
                    _job.value = null
                    delay(100)
                    var experimentRecord = erDao.getById(_selectedER.value).firstOrNull()
                    val countER = erDao.count()
                    val modelsThickness = dataStore.readData("modelsThickness", "G1520")
                    println("modelsThickness====$modelsThickness")
                    println("countER====$countER")
                    if (modelsThickness == "G1500") {
                        if (countER < 10) {
                            if (experimentRecord != null) {
                                experimentRecord.status = EPStatus.FAULT
                                experimentRecord.detail = "系统故障"
                                HomeIntent.Update(experimentRecord)
                            }
                        }
                    } else if (modelsThickness == "G1510") {
                        if (countER < 20) {
                            if (experimentRecord != null) {
                                experimentRecord.status = EPStatus.FAULT
                                experimentRecord.detail = "系统故障"
                                HomeIntent.Update(experimentRecord)
                            }
                        }
                    } else {
                        if (countER < 30) {
                            if (experimentRecord != null) {
                                experimentRecord.status = EPStatus.FAULT
                                experimentRecord.detail = "系统故障"
                                HomeIntent.Update(experimentRecord)
                            }
                        }

                    }

                    delay(100)
                    errorDao.insert(ErrorRecord(detail = "上下位机断开连接"))
                    delay(100)
                    ApplicationUtils.ctx.playAudio(R.raw.error_buzz)
                    ex.printStackTrace()
                } finally {
                    _job.value?.cancel()
                    _job.value = null
                }
            }

        }
    }

    /**
     * 全部制胶运动完成
     */
    private fun moveCom(startNum: Int) {

        /**
         * 制胶运动全部完成
         */
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(12)
            _job2.value?.cancel()
            _job2.value = null
            _hintJob.value?.cancel()
            _hintJob.value = null

            val setting = slDao.getById(1L).firstOrNull()
            if (setting == null) {
                _uiFlags.value = UiFlags.message("系统参数无数据")
                return@launch
            }

            val selectedER = erDao.getById(_selectedER.value).firstOrNull()
            val selectRudio = dataStore.readData("selectRudio", 1)
            val countER = erDao.count()
            val modelsThickness = dataStore.readData("modelsThickness", "G1520")
            println("modelsThickness====$modelsThickness")
            println("countER====$countER")
            if (selectedER != null) {
                if (startNum == 1) {
                    if (modelsThickness == "G1500") {
                        if (countER < 10) {
                            selectedER.number = _complate.value
                            selectedER.status = EPStatus.COMPLETED
                            erDao.update(selectedER)
                        }
                    } else if (modelsThickness == "G1510") {
                        if (countER < 20) {
                            selectedER.number = _complate.value
                            selectedER.status = EPStatus.COMPLETED
                            erDao.update(selectedER)
                        }
                    } else {
                        if (countER < 30) {
                            selectedER.number = _complate.value
                            selectedER.status = EPStatus.COMPLETED
                            erDao.update(selectedER)
                        }
                    }

                } else {
                    if (modelsThickness == "G1500") {
                        if (countER < 10) {
                            selectedER.number = _complate.value
                            selectedER.status = EPStatus.ABORT
                            selectedER.detail = "手动停止制胶"
                            erDao.update(selectedER)
                        }
                    } else if (modelsThickness == "G1510") {
                        if (countER < 20) {
                            selectedER.number = _complate.value
                            selectedER.status = EPStatus.ABORT
                            selectedER.detail = "手动停止制胶"
                            erDao.update(selectedER)
                        }
                    } else {
                        if (countER < 30) {
                            selectedER.number = _complate.value
                            selectedER.status = EPStatus.ABORT
                            selectedER.detail = "手动停止制胶"
                            erDao.update(selectedER)
                        }
                    }


                    if (selectRudio == 1) {

                    } else if (selectRudio == 2) {
                        ApplicationUtils.ctx.playAudio(R.raw.startstop_voice)
                    }
                }

            }


            val rinseSpeed = dataStore.readData("rinseSpeed", 600L)

            logInfo(
                "moveCom",
                "===停止运动的柱塞泵步数===${coagulantStart.value}"
            )

            start {
                timeOut = 1000L * 60L
                with(
                    index = 1,
                    ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
                    pdv = -coagulantStart.value
                )
            }

            start {
                timeOut = 1000L * 60L
                with(
                    index = 4,
                    ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                    pdv = setting.rinseCleanVolume * 1000
                )
            }
            lightGreed()
            _uiFlags.value = UiFlags.none()
            delay(200)


        }

    }

    private fun stopJob() {
        viewModelScope.launch {
            _waitTimeRinseJob.value?.cancel()
            _waitTimeRinseJob.value = null
            _waitTimeRinseNum.value = 0

            _job2.value?.cancel()
            _job2.value = null


            _hintJob.value?.cancel()
            _hintJob.value = null

            _job.value?.cancel()
            _job.value = null




            lightFlashYellow()


            val selectedER = erDao.getById(_selectedER.value).firstOrNull()
            val countER = erDao.count()
            val modelsThickness = dataStore.readData("modelsThickness", "G1520")
            println("modelsThickness====$modelsThickness")
            println("countER====$countER")
            if (modelsThickness == "G1500") {
                if (countER < 10) {
                    if (selectedER != null) {
                        selectedER.number = _complate.value
                        selectedER.status = EPStatus.ABORT
                        selectedER.detail = "手动停止制胶"
                        erDao.update(selectedER)
                    }
                }
            } else if (modelsThickness == "G1510") {
                if (countER < 20) {
                    if (selectedER != null) {
                        selectedER.number = _complate.value
                        selectedER.status = EPStatus.ABORT
                        selectedER.detail = "手动停止制胶"
                        erDao.update(selectedER)
                    }
                }
            } else {
                if (countER < 30) {
                    if (selectedER != null) {
                        selectedER.number = _complate.value
                        selectedER.status = EPStatus.ABORT
                        selectedER.detail = "手动停止制胶"
                        erDao.update(selectedER)
                    }
                }

            }


            val selectRudio = dataStore.readData("selectRudio", 1)

            if (selectRudio == 1) {

            } else if (selectRudio == 2) {
                ApplicationUtils.ctx.playAudio(R.raw.startstop_voice)
            }
            _uiFlags.value = UiFlags.objects(1)

            delay(200L)
            stop(0, 1, 2, 3, 4)
            delay(200L)


            try {
                delay(500)
                val selectRudio = dataStore.readData("selectRudio", 1)
                if (selectRudio == 1) {

                } else if (selectRudio == 2) {
                    ApplicationUtils.ctx.playAudio(R.raw.startreset_voice)
                }
                lightFlashYellow()
                delay(100)

                _waitTimeRinseJob.value?.cancel()
                _waitTimeRinseJob.value = null
                _waitTimeRinseNum.value = 0

                /**
                 * 柱塞泵总行程
                 */
                val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                /**
                 * 复位等待时间
                 */
                val coagulantTime = dataStore.readData("coagulantTime", 800).toLong()

                /**
                 * 复位后预排步数
                 */
                val coagulantResetPulse = dataStore.readData("coagulantResetPulse", 1500).toLong()


                withTimeout(60 * 1000L) {
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */
                    //x轴复位===========================================
                    gpio(0, 1)
                    delay(500L)
                    logInfo(
                        "stopJob",
                        "x轴光电状态====0号光电===" + getGpio(0) + "====1号光电===" + getGpio(1)
                    )
                    if (!getGpio(0) && !getGpio(1)) {
                        logInfo(
                            "stopJob",
                            "x轴反转64000L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        logInfo(
                            "stopJob",
                            "x轴正转6400L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        logInfo(
                            "stopJob",
                            "x轴反转6500L"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            logInfo(
                                "stopJob",
                                "x轴正转1600L"
                            )
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                            logInfo(
                                "stopJob",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "stopJob",
                                "复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                            //复位失败
                        }

                    } else if (!getGpio(0) && getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "stopJob",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "stopJob",
                                "复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                            //复位失败
                        }

                    } else if (getGpio(0) && !getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 6400L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            logInfo(
                                "stopJob",
                                "复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                        } else {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -6500L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "stopJob",
                                "复位完成"
                            )
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                        }
                    } else {
                        logInfo(
                            "stopJob",
                            "复位失败"
                        )
                        _uiFlags.value = UiFlags.objects(14)
                    }
                    //x轴复位===========================================


                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    gpio(2)
                    delay(500L)
                    logInfo(
                        "stopJob",
                        "注射泵光电状态====2号光电===" + getGpio(2)
                    )
                    if (!getGpio(2)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantpulse + 20000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)

                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        gpio(2)
                        delay(1500L)
                        logInfo(
                            "stopJob",
                            "注射泵光电状态====2号光电===" + getGpio(2)
                        )
                        delay(300L)
                        if (!getGpio(2)) {

                            delay(300L)
                            logInfo(
                                "stopJob",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "stopJob",
                                "柱塞泵复位失败"
                            )
                            _uiFlags.value = UiFlags.objects(14)
                            //复位失败
                        }
                    } else {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),

                                )
                        }
                        delay(300L)

                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)
                        logInfo(
                            "stopJob",
                            "柱塞泵复位完成"
                        )
                        //复位完成
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)

                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        gpio(2)
                        delay(500L)
                        logInfo(
                            "stopJob",
                            "注射泵光电状态====2号光电===" + getGpio(2)
                        )
                        if (getGpio(2)) {
                            logInfo(
                                "stopJob",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                            _uiFlags.value = UiFlags.objects(14)
                        } else {
                            logInfo(
                                "stopJob",
                                "柱塞泵复位完成"
                            )
                        }

                    }
                    //柱塞泵复位===========================================
                    delay(100)

                    if (selectRudio == 1) {

                    } else if (selectRudio == 2) {
                        ApplicationUtils.ctx.playAudio(R.raw.endreset_voice)
                    }

                }
                delay(200L)
                val slEnetity = slDao.getById(1L).firstOrNull()
                if (slEnetity != null) {
                    val rinseCleanVolume = slEnetity.rinseCleanVolume
                    _wasteprogress.value += (rinseCleanVolume / 150).toFloat()

                    /**
                     * 冲洗转速
                     */
                    val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                            pdv = rinseCleanVolume * 1000
                        )
                    }
                }
                delay(100)
                lightGreed()
                waitTimeRinse()
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                lightRed()
                delay(100)
                errorDao.insert(ErrorRecord(detail = "复位超时请重试"))
                delay(100)
                ApplicationUtils.ctx.playAudio(R.raw.error_buzz)
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }

        }
    }

//    private fun clean() {
//        viewModelScope.launch {
//            version()
//        }
//    }

    private fun clean() {
        viewModelScope.launch {
            if (_uiFlags.value is UiFlags.Objects && (_uiFlags.value as UiFlags.Objects).objects == 2) {
                _uiFlags.value = UiFlags.none()
                stop(1, 2, 3, 4)
            } else {
                _uiFlags.value = UiFlags.objects(2)
                val selectRudio = dataStore.readData("selectRudio", 1)
                if (selectRudio == 2) {
                    ApplicationUtils.ctx.playAudio(R.raw.cleanstart_voice)
                }

                val slEnetity = slDao.getById(1L).firstOrNull()
                logInfo(
                    "HomeViewModel_clean",
                    "计时前的slEnetity===$slEnetity"
                )
                if (slEnetity != null) {

                    lightYellow()

                    _waitTimeRinseJob.value?.cancel()
                    _waitTimeRinseJob.value = null
                    _waitTimeRinseNum.value = 0


                    /**
                     * 高浓度清洗液量
                     */
                    val higeCleanVolume = slEnetity.higeCleanVolume
                    logInfo(
                        "HomeViewModel_clean",
                        "高浓度清洗液量===$higeCleanVolume"
                    )
                    /**
                     * 低浓度清洗液量
                     */
                    val lowCleanVolume = slEnetity.lowCleanVolume
                    logInfo(
                        "HomeViewModel_clean",
                        "低浓度清洗液量===$lowCleanVolume"
                    )

                    /**
                     * 冲洗液泵清洗液量
                     */
                    val rinseCleanVolume = slEnetity.rinseCleanVolume
                    logInfo(
                        "HomeViewModel_clean",
                        "冲洗液泵清洗液量===$rinseCleanVolume"
                    )

                    /**
                     * 促凝剂泵清洗液量
                     */
                    val coagulantCleanVolume = slEnetity.coagulantCleanVolume


                    logInfo(
                        "HomeViewModel_clean",
                        "促凝剂泵清洗液量===$coagulantCleanVolume"
                    )

                    val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂泵校准因子===$p1jz"
                    )
//                    val p1 = pulse(index = 1, dvp = coagulantFilling * 1000)
                    val p1 = (coagulantCleanVolume * 1000 * p1jz).toLong()
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂泵管路填充加液步数===$p1"
                    )
                    /**
                     * 复位后预排步数
                     */
                    val coagulantResetPulse =
                        dataStore.readData("coagulantResetPulse", 1500).toLong()
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂复位后预排步数===$coagulantResetPulse"
                    )


                    /**
                     * 促凝剂总长度
                     */
                    val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()
                    logInfo(
                        "HomeViewModel_clean",
                        "促凝剂总长度===$coagulantpulse"
                    )

                    /**
                     * 促凝剂转速
                     */
                    val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)
                    logInfo(
                        "HomeViewModel_clean",
                        "促凝剂转速===$coagulantSpeed"
                    )

                    /**
                     * 冲洗转速
                     */
                    val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                    logInfo(
                        "HomeViewModel_clean",
                        "冲洗转速===$rinseSpeed"
                    )

                    /**
                     * x轴转速
                     */
                    val xSpeed = dataStore.readData("xSpeed", 100L)
                    logInfo(
                        "HomeViewModel_clean",
                        "x轴转速===$xSpeed"
                    )

                    val p1Count = p1.toDouble() / (coagulantpulse - 50000)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count"
                    )

                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count"
                    )
                    //向下取整
                    val count = floor(p1Count).toInt()
                    logInfo(
                        "HomeViewModel_pipeline",
                        "循环向下取整===$count"
                    )
                    val qyu = p1 % (coagulantpulse - 50000)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "取余===$qyu"
                    )

                    _wasteprogress.value +=
                        ((higeCleanVolume + lowCleanVolume + rinseCleanVolume + coagulantCleanVolume) / 150).toFloat()

                    /**
                     * 废液槽位置
                     */
                    val wastePosition = slEnetity.wastePosition
                    logInfo(
                        "HomeViewModel_pipeline",
                        "废液槽位置===$wastePosition"
                    )

                    delay(100)


                    start {
                        timeOut = 1000L * 60L * 10
                        with(

                            index = 0,
                            pdv = wastePosition,
                            ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                        )
                    }
                    delay(100L)


                    var startTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel",
                        "===预排液开始时间===$startTime"
                    )
                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 2,
                            ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
                            pdv = higeCleanVolume * 1000
                        )
                        with(
                            index = 3,
                            ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
                            pdv = lowCleanVolume * 1000
                        )
                    }
                    var endTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel",
                        "===预排液结束时间===$endTime"
                    )
                    //秒
                    val dTime = (endTime - startTime) / 1000
                    logInfo(
                        "HomeViewModel",
                        "===预排液结束时间差===$dTime"
                    )
                    val time = (dTime / 3600.0)
                    logInfo(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比未精确小数点===$time"
                    )

                    slEnetity.highTime += time
                    slEnetity.lowLife += time
                    slEnetity.rinseTime += time

                    slEnetity.highTime = String.format("%.4f", slEnetity.highTime).toDouble()
                    slEnetity.lowLife = String.format("%.4f", slEnetity.lowLife).toDouble()
                    slEnetity.rinseTime = String.format("%.4f", slEnetity.rinseTime).toDouble()

                    slDao.update(slEnetity)
                    logInfo(
                        "HomeViewModel_clean",
                        "计时后的slEnetity===$slEnetity"
                    )

                    for (i in 1..count) {
                        if (i == 1) {
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        rinseSpeed * 13,
                                        rinseSpeed * 1193,
                                        rinseSpeed * 1193
                                    ),
                                    pdv = coagulantpulse - 50000
                                )
                            }
                            delay(100L)
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        rinseSpeed * 13,
                                        rinseSpeed * 1193,
                                        rinseSpeed * 1193
                                    ),
                                    pdv = -(coagulantpulse - 50000 + coagulantResetPulse)
                                )
                            }
                            delay(100L)
                        } else {
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        rinseSpeed * 13,
                                        rinseSpeed * 1193,
                                        rinseSpeed * 1193
                                    ),
                                    pdv = (coagulantpulse - 50000)
                                )
                            }
                            delay(100L)
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        rinseSpeed * 13,
                                        rinseSpeed * 1193,
                                        rinseSpeed * 1193
                                    ),
                                    pdv = -(coagulantpulse - 50000)
                                )
                            }
                            delay(100L)
                        }
                    }

                    if (count == 0) {
                        //没有进上面的循环
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    rinseSpeed * 13,
                                    rinseSpeed * 1193,
                                    rinseSpeed * 1193
                                ),
                                pdv = qyu
                            )
                        }
                        delay(100L)
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    rinseSpeed * 13,
                                    rinseSpeed * 1193,
                                    rinseSpeed * 1193
                                ),
                                pdv = -(qyu + coagulantResetPulse)
                            )
                        }
                        delay(100L)

                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    rinseSpeed * 13,
                                    rinseSpeed * 1193,
                                    rinseSpeed * 1193
                                ),
                                pdv = coagulantResetPulse
                            )
                        }
                        delay(100L)

                    } else {
                        //进入上面的循环
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    rinseSpeed * 13,
                                    rinseSpeed * 1193,
                                    rinseSpeed * 1193
                                ),
                                pdv = qyu
                            )
                        }
                        delay(100L)
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    rinseSpeed * 13,
                                    rinseSpeed * 1193,
                                    rinseSpeed * 1193
                                ),
                                pdv = -qyu
                            )
                        }
                        delay(100L)

                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    rinseSpeed * 13,
                                    rinseSpeed * 1193,
                                    rinseSpeed * 1193
                                ),
                                pdv = coagulantResetPulse
                            )
                        }
                        delay(100L)

                    }


                    delay(100)
                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                            pdv = rinseCleanVolume * 1000
                        )
                    }
                    delay(100)
                    if (selectRudio == 2) {
                        ApplicationUtils.ctx.playAudio(R.raw.cleanend_voice)
                        delay(500)
                    }
                    reset()
                } else {
                    _uiFlags.value = UiFlags.message("没有清洗液量数据!")
                    return@launch
                }

            }
        }
    }


    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _uiFlags.value = UiFlags.none()
                stop(1, 2, 3, 4)
                delay(100L)
                reset()
            } else {
                _uiFlags.value = UiFlags.objects(5)

                _waitTimeRinseJob.value?.cancel()
                _waitTimeRinseJob.value = null
                _waitTimeRinseNum.value = 0
                /**
                 * 高浓度管路填充
                 */
                val slEnetity = slDao.getById(1L).firstOrNull()
                if (slEnetity != null) {
                    lightYellow()
                    val higeFilling = slEnetity.higeFilling
                    logInfo(
                        "HomeViewModel_pipeline",
                        "高浓度管路填充液量===$higeFilling"
                    )

                    /**
                     * 低浓度管路填充
                     */
                    val lowFilling = slEnetity.lowFilling
                    logInfo(
                        "HomeViewModel_pipeline",
                        "低浓度管路填充液量===$lowFilling"
                    )

                    /**
                     * 冲洗液泵管路填充
                     */
                    val rinseFilling = slEnetity.rinseFilling

                    logInfo(
                        "HomeViewModel_pipeline",
                        "冲洗液泵管路填充液量===$rinseFilling"
                    )

                    /**
                     * 促凝剂泵管路填充
                     */
                    val coagulantFilling = slEnetity.coagulantFilling
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂泵管路填充液量===$coagulantFilling"
                    )
                    val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂泵校准因子===$p1jz"
                    )

                    val coagulantpipeline = dataStore.readData("coagulantpipeline", 50)

                    logInfo(
                        "HomeViewModel_pipeline",
                        "coagulantpipeline===$coagulantpipeline"
                    )

                    var p1 = (coagulantFilling * 1000 * p1jz).toLong()
                    val p2 = (coagulantpipeline * p1jz).toLong()
                    logInfo(
                        "HomeViewModel_pipeline",
                        "p2===$p2"
                    )
                    p1 -= p2
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂泵管路填充加液步数===$p1"
                    )
                    /**
                     * 促凝剂总行程
                     */
                    val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂总行程===$coagulantpulse"
                    )
                    /**
                     * 复位后预排步数
                     */
                    val coagulantResetPulse =
                        dataStore.readData("coagulantResetPulse", 1500).toLong()
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂复位后预排步数===$coagulantResetPulse"
                    )

                    /**
                     * 促凝剂转速
                     */
                    val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂转速===$coagulantSpeed"
                    )

                    /**
                     * 冲洗转速
                     */
                    val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "冲洗转速===$rinseSpeed"
                    )

                    /**
                     * x轴转速
                     */
                    val xSpeed = dataStore.readData("xSpeed", 100L)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "x轴转速===$xSpeed"
                    )

                    /**
                     * 废液槽位置
                     */
                    val wastePosition = slEnetity.wastePosition
                    logInfo(
                        "HomeViewModel_pipeline",
                        "废液槽位置===$wastePosition"
                    )
                    val expectedMakenum = dataStore.readData("expectedMakenum", 0)
                    if (expectedMakenum > 0) {
                        _higemother.value -= higeFilling.toFloat()
                        _lowmother.value -= lowFilling.toFloat()
                        _coagulantmother.value -= coagulantFilling.toFloat()
                        _watermother.value -= rinseFilling.toFloat()

                        if (_coagulantmother.value < 0) {
                            _coagulantmother.value = 0f
                        }

                        if (_lowmother.value < 0) {
                            _lowmother.value = 0f
                        }
                        if (_higemother.value < 0) {
                            _higemother.value = 0f
                        }
                        if (_watermother.value < 0) {
                            _watermother.value = 0f
                        }
                        _higemother.value = String.format("%.1f", _higemother.value).toFloat()
                        _lowmother.value = String.format("%.1f", _lowmother.value).toFloat()
                        _coagulantmother.value =
                            String.format("%.1f", _coagulantmother.value).toFloat()
                        _watermother.value = String.format("%.1f", _watermother.value).toFloat()
                        delay(100)
                    }



                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 0,
                            pdv = wastePosition,
                            ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                        )
                    }

                    val p1Count = p1.toDouble() / (coagulantpulse - 50000)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count"
                    )
                    //向下取整
                    val count = floor(p1Count).toInt()
                    logInfo(
                        "HomeViewModel_pipeline",
                        "循环向下取整===$count"
                    )
                    val qyu = p1 % (coagulantpulse - 50000)
                    logInfo(
                        "HomeViewModel_pipeline",
                        "取余===$qyu"
                    )
                    for (i in 1..count) {
                        if (i == 1) {
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
                                    ),
                                    pdv = coagulantpulse - 50000
                                )
                            }
                            delay(1000L)
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
                                    ),
                                    pdv = -(coagulantpulse - 50000 + coagulantResetPulse)
                                )
                            }
                            delay(1000L)
                        } else {
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
                                    ),
                                    pdv = (coagulantpulse - 50000)
                                )
                            }
                            delay(1000L)
                            start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
                                    ),
                                    pdv = -(coagulantpulse - 50000)
                                )
                            }
                            delay(1000L)
                        }
                    }

                    if (count == 0) {
                        //没有进上面的循环
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = qyu
                            )
                        }
                        delay(1000L)
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = -(qyu + coagulantResetPulse)
                            )
                        }
                        delay(1000L)

                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = coagulantResetPulse
                            )
                        }
                        delay(1000L)

                    } else {
                        //进入上面的循环
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = qyu
                            )
                        }
                        delay(1000L)
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = -qyu
                            )
                        }
                        delay(1000L)

                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = coagulantResetPulse
                            )
                        }
                        delay(1000L)

                    }

                    delay(100L)
                    var startTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel",
                        "===填充开始时间===$startTime"
                    )
                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 2,
                            ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
                            pdv = higeFilling * 1000
                        )
                        with(
                            index = 3,
                            ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
                            pdv = lowFilling * 1000
                        )
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                            pdv = rinseFilling * 1000
                        )
                    }

                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 1,
                            ads = Triple(
                                coagulantSpeed * 13,
                                coagulantSpeed * 1193,
                                coagulantSpeed * 1193
                            ),
                            pdv = p2
                        )
                    }

                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 1,
                            ads = Triple(
                                coagulantSpeed * 13,
                                coagulantSpeed * 1193,
                                coagulantSpeed * 1193
                            ),
                            pdv = -(p2 + coagulantResetPulse)
                        )
                    }

                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 1,
                            ads = Triple(
                                coagulantSpeed * 13,
                                coagulantSpeed * 1193,
                                coagulantSpeed * 1193
                            ),
                            pdv = coagulantResetPulse
                        )
                    }

                    /**
                     *  1.柱塞泵加200微升
                     *  2.冲洗加1毫升
                     */


                    var endTime = Calendar.getInstance().timeInMillis
                    logInfo(
                        "HomeViewModel",
                        "===填充结束时间===$endTime"
                    )
                    //秒
                    val dTime = (endTime - startTime) / 1000
                    logInfo(
                        "HomeViewModel",
                        "===填充结束时间差===$dTime"
                    )
                    val time = (dTime / 3600.0)
                    logInfo(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比未精确小数点===$time"
                    )
                    slEnetity.highTime += time
                    slEnetity.lowLife += time
                    slEnetity.rinseTime += time


                    slEnetity.highTime = String.format("%.4f", slEnetity.highTime).toDouble()
                    slEnetity.lowLife = String.format("%.4f", slEnetity.lowLife).toDouble()
                    slEnetity.rinseTime = String.format("%.4f", slEnetity.rinseTime).toDouble()

                    slDao.update(slEnetity)
                    delay(100L)

                    val selectRudio = dataStore.readData("selectRudio", 1)
                    if (selectRudio == 2) {
                        ApplicationUtils.ctx.playAudio(R.raw.pipeline_voice)
                    }
                    lightGreed()
                    waitTimeRinse()
                    _uiFlags.value = UiFlags.none()
                }


            }
        }
    }
}

/**
 * 状态；0-运行中；1-已完成；2-中止；3-故障
 */
object EPStatus {
    /**
     * 运行中
     */
    const val RUNNING = "运行中"

    /**
     * 已完成
     */
    const val COMPLETED = "已完成"

    /**
     * 中止
     */
    const val ABORT = "中止"

    /**
     * 故障
     */
    const val FAULT = "故障"
}

sealed class HomeIntent {
    data class NavTo(val page: Int) : HomeIntent()
    data class Flags(val uiFlags: UiFlags) : HomeIntent()
    data class Pipeline(val index: Int) : HomeIntent()
    data class Selected(val id: Long) : HomeIntent()
    data object Clean : HomeIntent()
    data object Reset : HomeIntent()
    data class Start(val count: Int) : HomeIntent()
    data object Stop : HomeIntent()

    data class Update(val entity: ExperimentRecord) : HomeIntent()

    data object Calculate : HomeIntent()
    data object HigeLowMotherVol : HomeIntent()

    data class MoveCom(val startNum: Int) : HomeIntent()

    data object First : HomeIntent()

    data object CleanWaste : HomeIntent()

    data object CleanWasteState : HomeIntent()

    data class CleanDialog(val cleanState: Boolean) : HomeIntent()
    data class PipelineDialog(val pipelineState: Boolean) : HomeIntent()


    data object MotherVolZero : HomeIntent()
    data object Heartbeat : HomeIntent()

    data object InitHintDialog : HomeIntent()
    data object StopHeartbeat : HomeIntent()
    data object WaitTimeRinse : HomeIntent()
    data object StopWaitTimeRinse : HomeIntent()

    data object erCount : HomeIntent()


}