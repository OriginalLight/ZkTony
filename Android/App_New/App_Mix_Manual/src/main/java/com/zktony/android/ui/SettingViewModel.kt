package com.zktony.android.ui

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.dao.ErrorRecordDao
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.dao.NewCalibrationDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.dao.SportsLogDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Setting
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.extra.Application
import com.zktony.android.utils.extra.DownloadState
import com.zktony.android.utils.extra.download
import com.zktony.android.utils.extra.httpCall
import com.zktony.android.utils.extra.playAudio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dao: MotorDao,
    private val proDao: ProgramDao,
    private val slDao: SettingDao,
    private val ncDao: NewCalibrationDao,
    private val errorDao: ErrorRecordDao,
    private val dataStore: DataSaverDataStore,
    private val sportsLogDao: SportsLogDao,
    private val erDao: ExperimentRecordDao,
) : ViewModel() {

    private val _application = MutableStateFlow<Application?>(null)
    private val _selected = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.SETTINGS)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    private val _job = MutableStateFlow<Job?>(null)

    private val _currentpwd = MutableStateFlow("")

    /**
     * 加液次数
     */
    private val _complate = MutableStateFlow(0)

    /**
     * 当前加液次数
     */
    private val _current = MutableStateFlow(0)


    val application = _application.asStateFlow()
    val selected = _selected.asStateFlow()
    val progress = _progress.asStateFlow()
    val page = _page.asStateFlow()
    val currentpwd = _currentpwd.asStateFlow()

    val uiFlags = _uiFlags.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    val proEntities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { proDao.getByPage() }.flow.cachedIn(viewModelScope)

    val errorEntities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { errorDao.getByPage() }.flow.cachedIn(viewModelScope)

    val sportsLogEntitiesDis = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { sportsLogDao.getByPageDis() }.flow.cachedIn(viewModelScope)

    val sportsLogEntities = Pager(
        config = PagingConfig(pageSize = 50, initialLoadSize = 100),
    ) { sportsLogDao.getByPage() }.flow.cachedIn(viewModelScope)


    val slEntitiy = slDao.getById(1L)

    val ncEntitiy = ncDao.getById(1L)
    val job = _job.asStateFlow()
    val complate = _complate.asStateFlow()

    init {
        viewModelScope.launch {
            if (ApplicationUtils.isNetworkAvailable()) {
                httpCall { _application.value = it }
            }
        }
    }


    fun dispatch(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.CheckUpdate -> checkUpdate()
            is SettingIntent.Delete -> viewModelScope.launch { dao.deleteById(intent.id) }
            is SettingIntent.Insert -> viewModelScope.launch { dao.insert(Motor(displayText = "None")) }
            is SettingIntent.Flags -> _uiFlags.value = intent.uiFlags
            is SettingIntent.Navigation -> navigation(intent.navigation)
            is SettingIntent.NavTo -> _page.value = intent.page
            is SettingIntent.Network -> network()
            is SettingIntent.Selected -> _selected.value = intent.id
            is SettingIntent.Update -> viewModelScope.launch { dao.update(intent.entity) }
            is SettingIntent.InsertSet -> viewModelScope.launch {
                slDao.insert(
                    Setting(
                        highTime = intent.highTime,
                        lowLife = intent.lowLife,
                        rinseTime = intent.rinseTime,
                        highTimeExpected = intent.highTimeExpected,
                        lowTimeExpected = intent.lowTimeExpected,
                        rinseTimeExpected = intent.rinseTimeExpected,
                        wastePosition = intent.wastePosition,
                        glueBoardPosition = intent.glueBoardPosition,
                        higeCleanVolume = intent.higeCleanVolume,
                        higeRehearsalVolume = intent.higeRehearsalVolume,
                        higeFilling = intent.higeFilling,
                        lowCleanVolume = intent.lowCleanVolume,
                        lowFilling = intent.lowFilling,
                        rinseCleanVolume = intent.rinseCleanVolume,
                        rinseFilling = intent.rinseFilling,
                        coagulantCleanVolume = intent.coagulantCleanVolume,
                        coagulantFilling = intent.coagulantFilling
                    )
                )
            }

            is SettingIntent.UpdateSet -> viewModelScope.launch { slDao.update(intent.entity) }
            is SettingIntent.InsertNC -> viewModelScope.launch {
                ncDao.insert(
                    NewCalibration(
                        higeLiquidVolume1 = intent.higeLiquidVolume1,
                        higeLiquidVolume2 = intent.higeLiquidVolume2,
                        higeLiquidVolume3 = intent.higeLiquidVolume3,
                        higeAvg = intent.higeAvg,
                        lowLiquidVolume1 = intent.lowLiquidVolume1,
                        lowLiquidVolume2 = intent.lowLiquidVolume2,
                        lowLiquidVolume3 = intent.lowLiquidVolume3,
                        lowAvg = intent.lowAvg,
                        coagulantLiquidVolume1 = intent.coagulantLiquidVolume1,
                        coagulantLiquidVolume2 = intent.coagulantLiquidVolume2,
                        coagulantLiquidVolume3 = intent.coagulantLiquidVolume3,
                        coagulantAvg = intent.coagulantAvg,
                        rinseLiquidVolume1 = intent.rinseLiquidVolume1,
                        rinseLiquidVolume2 = intent.rinseLiquidVolume2,
                        rinseLiquidVolume3 = intent.rinseLiquidVolume3,
                        rinseAvg = intent.rinseAvg
                    )


                )
            }

            is SettingIntent.UpdateNC -> viewModelScope.launch { ncDao.update(intent.entity) }
            is SettingIntent.Start -> startJob(intent.count)

            is SettingIntent.Stop -> stopJob()
            is SettingIntent.Reset -> reset()
            is SettingIntent.XReset -> xreset()
            is SettingIntent.ZSReset -> zsreset()
            is SettingIntent.Clean -> clean()
            is SettingIntent.Pipeline -> pipeline()

            is SettingIntent.Sound -> viewModelScope.launch {
                delay(100)
                if (intent.state == 1) {
                    //蜂鸣
                    ApplicationUtils.ctx.playAudio(R.raw.setting_buzz)
                } else if (intent.state == 2) {
                    //语音
                    ApplicationUtils.ctx.playAudio(R.raw.power_voice)
                }


            }

            is SettingIntent.ClearAll -> viewModelScope.launch {
                proDao.deleteAll()
                errorDao.deleteAll()
                sportsLogDao.deleteAll()
                erDao.deleteAll()
            }

            is SettingIntent.Login -> viewModelScope.launch {
                _currentpwd.value = intent.pwd
            }


        }
    }

    private fun pipeline() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(5)

            /**
             * 高浓度管路填充
             */
            val slEnetity = slDao.getById(1L).firstOrNull()
            if (slEnetity != null) {
                val higeFilling = slEnetity.higeFilling

                /**
                 * 低浓度管路填充
                 */
                val lowFilling = slEnetity.lowFilling

                /**
                 * 冲洗液泵管路填充
                 */
                val rinseFilling = slEnetity.rinseFilling


                /**
                 * 促凝剂泵管路填充
                 */
                val coagulantFilling = slEnetity.coagulantFilling

                val p1 = SerialPortUtils.pulse(index = 1, dvp = coagulantFilling * 1000)

                /**
                 * 促凝剂总行程
                 */
                val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                /**
                 * 促凝剂转速
                 */
                val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)

                /**
                 * 冲洗转速
                 */
                val rinseSpeed = dataStore.readData("rinseSpeed", 600L)

                /**
                 * x轴转速
                 */
                val xSpeed = dataStore.readData("xSpeed", 100L)

                val p1Count = p1.toDouble() / (coagulantpulse - 51200).toDouble()

                /**
                 * 废液槽位置
                 */
                val wastePosition = dataStore.readData("wastePosition", 0.0)

                SerialPortUtils.start {
                    timeOut = 1000L * 60L * 10
                    with(
                        index = 0,
                        pdv = wastePosition,
                        ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                    )
                }
                delay(100L)
                if (p1Count > 1) {
                    for (i in 0 until Math.ceil(p1Count).toInt()) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = coagulantpulse
                            )
                        }
                        delay(100L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = -coagulantpulse
                            )
                        }
                        delay(100L)
                    }

                } else {
                    SerialPortUtils.start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 1,
                            ads = Triple(
                                coagulantSpeed * 13,
                                coagulantSpeed * 1193,
                                coagulantSpeed * 1193
                            ),
                            pdv = coagulantFilling * 1000
                        )
                    }
                }


                delay(100L)

                SerialPortUtils.start {
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
                        ads = Triple(rinseSpeed * 40, rinseSpeed * 40, rinseSpeed * 40),
                        pdv = rinseFilling * 1000
                    )
                }

                reset()
            }


        }
    }

    private fun clean() {
        viewModelScope.launch {
            if (_uiFlags.value is UiFlags.Objects && (_uiFlags.value as UiFlags.Objects).objects == 2) {
                _uiFlags.value = UiFlags.none()
                SerialPortUtils.stop(1, 2, 3, 4)
            } else {
                _uiFlags.value = UiFlags.objects(2)


                val slEnetity = slDao.getById(1L).firstOrNull()
                if (slEnetity != null) {

                    /**
                     * 高浓度清洗液量
                     */
                    val higeCleanVolume = slEnetity.higeCleanVolume

                    /**
                     * 低浓度清洗液量
                     */
                    val lowCleanVolume = slEnetity.lowCleanVolume

                    /**
                     * 冲洗液泵清洗液量
                     */
                    val rinseCleanVolume = slEnetity.rinseCleanVolume

                    /**
                     * 促凝剂泵清洗液量
                     */
                    val coagulantCleanVolume = slEnetity.coagulantCleanVolume

                    val p1 = SerialPortUtils.pulse(index = 1, dvp = coagulantCleanVolume * 1000)

                    /**
                     * 促凝剂总长度
                     */
                    val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                    /**
                     * 促凝剂转速
                     */
                    val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)

                    /**
                     * 冲洗转速
                     */
                    val rinseSpeed = dataStore.readData("rinseSpeed", 600L)

                    /**
                     * x轴转速
                     */
                    val xSpeed = dataStore.readData("xSpeed", 100L)

                    val p1Count = p1.toDouble() / coagulantpulse.toDouble()

                    /**
                     * 废液槽位置
                     */
                    val wastePosition = slEnetity.wastePosition

                    SerialPortUtils.start {
                        timeOut = 1000L * 60L * 10
                        with(

                            index = 0,
                            pdv = wastePosition,
                            ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                        )
                    }
                    delay(100L)
                    if (p1Count > 1) {
                        for (i in 0 until Math.ceil(p1Count).toInt()) {
                            SerialPortUtils.start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
                                    ),
                                    pdv = coagulantpulse
                                )
                            }
                            delay(100L)
                            SerialPortUtils.start {
                                timeOut = 1000L * 60L * 10
                                with(
                                    index = 1,
                                    ads = Triple(
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
                                    ),
                                    pdv = -coagulantpulse
                                )
                            }
                            delay(100L)
                        }

                    } else {
                        SerialPortUtils.start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = coagulantCleanVolume * 1000
                            )
                        }
                    }

                    delay(100L)
                    SerialPortUtils.start {
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
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 40, rinseSpeed * 40, rinseSpeed * 40),
                            pdv = rinseCleanVolume * 1000
                        )
                    }

                    reset()
                } else {
                    _uiFlags.value = UiFlags.message("没有清洗液量数据!")
                    return@launch
                }

            }
        }
    }

    private fun reset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
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
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */

                    //x轴复位===========================================
                    SerialPortUtils.gpio(0, 1)
                    delay(500L)
                    Log.d(
                        "HomeViewModel",
                        "x轴光电状态====0号光电===" + SerialPortUtils.getGpio(0) + "====1号光电===" + SerialPortUtils.getGpio(
                            1
                        )
                    )
                    if (!SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        Log.d(
                            "HomeViewModel",
                            "x轴反转64000L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        Log.d(
                            "HomeViewModel",
                            "x轴正转6400L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        Log.d(
                            "HomeViewModel",
                            "x轴反转6500L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {

                            Log.d(
                                "HomeViewModel",
                                "x轴正转1600L"
                            )
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (!SerialPortUtils.getGpio(0) && SerialPortUtils.getGpio(1)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                        } else {
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -3300L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                        }
                    } else {
                        Log.d(
                            "HomeViewModel",
                            "复位失败"
                        )
                    }
                    //x轴复位===========================================


                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    SerialPortUtils.gpio(2)
                    delay(500L)
                    Log.d(
                        "HomeViewModel",
                        "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                    )
                    if (!SerialPortUtils.getGpio(2)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantpulse + 20000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.gpio(2)
                        delay(1500L)
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        delay(300L)
                        if (!SerialPortUtils.getGpio(2)) {

                            delay(300L)
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        }
                    } else {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),

                                )
                        }
                        delay(300L)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)
                        Log.d(
                            "HomeViewModel",
                            "柱塞泵复位完成"
                        )
                        //复位完成
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        SerialPortUtils.gpio(2)
                        delay(500L)
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        if (SerialPortUtils.getGpio(2)) {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位完成"
                            )
                        }

                    }
                    //柱塞泵复位===========================================

                }
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
        }
    }

    private fun xreset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
                withTimeout(60 * 1000L) {
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */

                    //x轴复位===========================================
                    SerialPortUtils.gpio(0, 1)
                    delay(500L)
                    Log.d(
                        "HomeViewModel",
                        "x轴光电状态====0号光电===" + SerialPortUtils.getGpio(0) + "====1号光电===" + SerialPortUtils.getGpio(
                            1
                        )
                    )
                    if (!SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        Log.d(
                            "HomeViewModel",
                            "x轴反转64000L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        Log.d(
                            "HomeViewModel",
                            "x轴正转6400L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        Log.d(
                            "HomeViewModel",
                            "x轴反转6500L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {

                            Log.d(
                                "HomeViewModel",
                                "x轴正转1600L"
                            )
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (!SerialPortUtils.getGpio(0) && SerialPortUtils.getGpio(1)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                        } else {
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -3300L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                        }
                    } else {
                        Log.d(
                            "HomeViewModel",
                            "复位失败"
                        )
                    }
                    //x轴复位===========================================

                }
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
        }
    }

    private fun zsreset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
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
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */

                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    SerialPortUtils.gpio(2)
                    delay(500L)
                    Log.d(
                        "HomeViewModel",
                        "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                    )
                    if (!SerialPortUtils.getGpio(2)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantpulse + 20000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.gpio(2)
                        delay(1500L)
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        delay(300L)
                        if (!SerialPortUtils.getGpio(2)) {

                            delay(300L)
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        }
                    } else {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),

                                )
                        }
                        delay(300L)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)
                        Log.d(
                            "HomeViewModel",
                            "柱塞泵复位完成"
                        )
                        //复位完成
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        SerialPortUtils.gpio(2)
                        delay(500L)
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        if (SerialPortUtils.getGpio(2)) {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位完成"
                            )
                        }

                    }
                    //柱塞泵复位===========================================

                }
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
        }
    }


    private fun stopJob() {
        viewModelScope.launch {
            SerialPortUtils.cleanLight()
            _uiFlags.value = UiFlags.objects(1)
            _job.value?.cancel()
            _job.value = null
            delay(200L)
            SerialPortUtils.stop(0, 1, 2, 3, 4)
            delay(200L)
            try {
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
                    /**
                     * 0-x轴    3200/圈    0号光电-复位光电；1号光电-限位光电
                     * 1-柱塞泵 12800/圈    2号光电
                     * 2-高浓度
                     * 3-低浓度
                     * 4-清洗泵
                     */

                    //x轴复位===========================================
                    SerialPortUtils.gpio(0, 1)
                    delay(500L)
                    Log.d(
                        "HomeViewModel",
                        "x轴光电状态====0号光电===" + SerialPortUtils.getGpio(0) + "====1号光电===" + SerialPortUtils.getGpio(
                            1
                        )
                    )
                    if (!SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        Log.d(
                            "HomeViewModel",
                            "x轴反转64000L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        Log.d(
                            "HomeViewModel",
                            "x轴正转6400L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        Log.d(
                            "HomeViewModel",
                            "x轴反转6500L"
                        )
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {

                            Log.d(
                                "HomeViewModel",
                                "x轴正转1600L"
                            )
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (!SerialPortUtils.getGpio(0) && SerialPortUtils.getGpio(1)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -3300L,
                                ads = Triple(1600, 1600, 1600),

                                )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),
                            )
                        }
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                        } else {
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -3300L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1200L,
                                    ads = Triple(1600, 1600, 1600),
                                )
                            }
                        }
                    } else {
                        Log.d(
                            "HomeViewModel",
                            "复位失败"
                        )
                    }
                    //x轴复位===========================================


                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    SerialPortUtils.gpio(2)
                    delay(500L)
                    Log.d(
                        "HomeViewModel",
                        "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                    )
                    if (!SerialPortUtils.getGpio(2)) {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantpulse + 20000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)
                        SerialPortUtils.gpio(2)
                        delay(1500L)
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        delay(300L)
                        if (!SerialPortUtils.getGpio(2)) {

                            delay(300L)
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        }
                    } else {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -64000L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),

                                )
                        }
                        delay(300L)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 64500L,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(coagulantTime)
                        Log.d(
                            "HomeViewModel",
                            "柱塞泵复位完成"
                        )
                        //复位完成
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -coagulantpulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }
                        delay(300L)

                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = coagulantResetPulse,
                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                            )
                        }

                        delay(300L)
                        SerialPortUtils.gpio(2)
                        delay(500L)
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        if (SerialPortUtils.getGpio(2)) {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        } else {
                            Log.d(
                                "HomeViewModel",
                                "柱塞泵复位完成"
                            )
                        }

                    }
                    //柱塞泵复位===========================================

                }
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message("复位超时请重试")
            }
            delay(200L)
            val slEnetity = slDao.getById(1L).firstOrNull()
            if (slEnetity != null) {
                val rinseCleanVolume = slEnetity.rinseCleanVolume

                /**
                 * 冲洗转速
                 */
                val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                SerialPortUtils.start {
                    timeOut = 1000L * 60L * 10
                    with(
                        index = 4,
                        ads = Triple(rinseSpeed * 40, rinseSpeed * 40, rinseSpeed * 40),
                        pdv = rinseCleanVolume * 1000
                    )
                }
            }
        }
    }

    private fun startJob(status: Int) {
        viewModelScope.launch {
            println("进入startJob")
            _uiFlags.value = UiFlags.objects(0)
            val selected = proDao.getById(_selected.value).firstOrNull()
            println("selected===$selected")
            if (selected == null) {
                _uiFlags.value = UiFlags.message("未选择程序")
                return@launch
            }


            val setting = slDao.getById(1L).firstOrNull()
            println("setting===$setting")
            if (setting == null) {
                _uiFlags.value = UiFlags.message("系统参数无数据")
                return@launch
            }


            val xSpeed = dataStore.readData("xSpeed", 600L)



            Log.d(
                "HomeViewModel_startJob",
                "===制胶前期准备数据开始==="
            )
            //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）
            //1.1   获取高低浓度的平均校准因子
            val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
            val p2jz = (AppStateUtils.hpc[2] ?: { x -> x * 100 }).invoke(1.0)
            val p3jz = (AppStateUtils.hpc[3] ?: { x -> x * 100 }).invoke(1.0)
            val highLowAvg = (p2jz + p3jz) / 2
            Log.d(
                "HomeViewModel_startJob",
                "===获取高低浓度的平均校准因子===$highLowAvg"
            )
            //1.2   胶液总步数
            val volumePulseCount = selected.volume * 1000 * highLowAvg
            Log.d(
                "HomeViewModel_startJob",
                "===01胶液总步数===$volumePulseCount"
            )
            //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）

            //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）
            val coagulantVol = selected.coagulant
            Log.d(
                "HomeViewModel_startJob",
                "===促凝剂加液量===$coagulantVol"
            )
            //促凝剂总步数
            val coagulantPulseCount = coagulantVol * p1jz
            Log.d(
                "HomeViewModel_startJob",
                "===02促凝剂总步数===$coagulantPulseCount"
            )
            //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）

            //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60
            //制胶速度，根据这个速度转换其他泵的速度
            val speed = dataStore.readData("speed", 180)
            Log.d(
                "HomeViewModel_startJob",
                "===制胶速度===$speed"
            )
            //制胶所需时间
            val guleTime = volumePulseCount / 51200 / speed * 60
            Log.d(
                "HomeViewModel_startJob",
                "===03制胶所需时间===$guleTime"
            )
            //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60

            //04高浓度泵启动速度（rpm）=制胶速度（rpm）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）
            //高浓度泵启动速度
            val highStartSpeed =
                speed * (selected.endRange - selected.startRange) / (selected.endRange - selected.startRange)
            Log.d(
                "HomeViewModel_startJob",
                "===04高浓度泵启动速度===$highStartSpeed"
            )
            //04高浓度泵启动速度（rpm）=制胶速度（rpm）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）

            //05低浓度泵结束速度（rpm）=制胶速度（rpm）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）
            val lowEndSpeed =
                speed * (selected.startRange - selected.endRange) / (selected.startRange - selected.endRange)
            Log.d(
                "HomeViewModel_startJob",
                "===05低浓度泵结束速度===$lowEndSpeed"
            )
            //05低浓度泵结束速度（rpm）=制胶速度（rpm）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）

            //06高浓度泵结束速度（rpm）=制胶速度-低浓度泵结束速度
            val highEndSpeed = speed - lowEndSpeed
            Log.d(
                "HomeViewModel_startJob",
                "===06高浓度泵结束速度===$highEndSpeed"
            )
            //06高浓度泵结束速度（rpm）=制胶速度-低浓度泵结束速度

            //07低浓度泵启动速度（rpm）=制胶速度-高浓度泵启动速度
            val lowStartSpeed = speed - highStartSpeed
            Log.d(
                "HomeViewModel_startJob",
                "===07低浓度泵启动速度===$lowStartSpeed"
            )
            //07低浓度泵启动速度（rpm）=制胶速度-高浓度泵启动速度

            //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比
            //促凝剂变速比-默认1
            val ratio = 1
            Log.d(
                "HomeViewModel_startJob",
                "===促凝剂变速比===$ratio"
            )
            //促凝剂泵启动速度
            val coagulantStartSpeed = coagulantPulseCount / 51200 / guleTime * 60 * ratio
            Log.d(
                "HomeViewModel_startJob",
                "===08促凝剂泵启动速度===$coagulantStartSpeed"
            )
            //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比

            //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）
            val coagulantEndSpeed = coagulantPulseCount / 51200 / guleTime * 60 * (2 - ratio)
            Log.d(
                "HomeViewModel_startJob",
                "===09促凝剂泵结束速度===$coagulantEndSpeed"
            )
            //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）

            //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
            Log.d(
                "HomeViewModel_startJob",
                "===高浓度泵启动速度===$highStartSpeed====高浓度泵结束速度===$highEndSpeed===制胶所需时间===$guleTime==="
            )
            val guleHighPulse = (highStartSpeed + highEndSpeed) / 2 * guleTime / 60 * 51200
            Log.d(
                "HomeViewModel_startJob",
                "===10制胶高浓度泵步数===$guleHighPulse"
            )
            //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

            //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
            val guleLowPulse = (lowStartSpeed + lowEndSpeed) / 2 * guleTime / 60 * 51200
            Log.d(
                "HomeViewModel_startJob",
                "===11制胶低浓度泵步数===$guleLowPulse"
            )
            //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

            //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））
            val highAcc = abs(highStartSpeed - highEndSpeed) / guleTime
            Log.d(
                "HomeViewModel_startJob",
                "===12高浓度泵加速度===$highAcc"
            )
            //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））

            //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)
            val lowAcc = abs(lowStartSpeed - lowEndSpeed) / guleTime
            Log.d(
                "HomeViewModel_startJob",
                "===13低浓度泵加速度===$lowAcc"
            )
            //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)

            //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）
            val coagulantAcc = abs(coagulantStartSpeed - coagulantEndSpeed) / guleTime
            Log.d(
                "HomeViewModel_startJob",
                "===14促凝剂泵加速度===$coagulantAcc"
            )
            //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）

            Log.d(
                "HomeViewModel_startJob",
                "===制胶前期准备数据结束==="
            )

            Log.d(
                "HomeViewModel_startJob",
                "===预排前期准备数据开始==="
            )

            //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）
            /**
             * 高浓度预排液量
             */
            val higeRehearsalVolume = setting.higeRehearsalVolume * 1000
            Log.d(
                "HomeViewModel_startJob",
                "===高浓度预排液量===$higeRehearsalVolume"
            )

            val highExpectedPulseCount = higeRehearsalVolume * highLowAvg
            Log.d(
                "HomeViewModel_startJob",
                "===15预排总步数===$highExpectedPulseCount"
            )
            //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）

            //16预排高浓度步数=预排总步数×高浓度泵启动速度（rpm）/制胶速度（rpm）
            val highExpectedPulse = highExpectedPulseCount * highStartSpeed / speed
            Log.d(
                "HomeViewModel_startJob",
                "===16预排高浓度步数===$highExpectedPulse"
            )
            //16预排高浓度步数=预排总步数×高浓度泵启动速度（rpm）/制胶速度（rpm）

            //17预排低浓度步数=预排总步数×低浓度泵启动速度（rpm）/制胶速度（rpm）
            val lowExpectedPulse = highExpectedPulseCount * lowStartSpeed / speed
            Log.d(
                "HomeViewModel_startJob",
                "===17预排低浓度步数===$lowExpectedPulse"
            )
            //17预排低浓度步数=预排总步数×低浓度泵启动速度（rpm）/制胶速度（rpm）

            //18预排促凝剂步数=高浓度预排液量（mL）×促凝剂体积（μL）/胶液体积（mL）×促凝剂校准数据（步/μL）×促凝剂变速比
            val coagulantExpectedPulse =
                setting.higeRehearsalVolume * coagulantVol / selected.volume * p1jz * ratio
            Log.d(
                "HomeViewModel_startJob",
                "===18预排促凝剂步数===$coagulantExpectedPulse"
            )
            //18预排促凝剂步数=高浓度预排液量（mL）×促凝剂体积（μL）/胶液体积（mL）×促凝剂校准数据（步/μL）×促凝剂变速比

            //19预排高浓度泵速度（rpm）=高浓度泵启动速度（rpm）/制胶速度（rpm）×冲洗液泵速度（rpm）
            /**
             *冲洗液泵转速
             */
            val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
            Log.d(
                "HomeViewModel_startJob",
                "===冲洗液泵转速===$rinseSpeed"
            )
            val highExpectedSpeed = highStartSpeed / speed * rinseSpeed
            Log.d(
                "HomeViewModel_startJob",
                "===19预排高浓度泵速度===$highExpectedSpeed"
            )
            //19预排高浓度泵速度（rpm）=高浓度泵启动速度（rpm）/制胶速度（rpm）×冲洗液泵速度（rpm）

            //20预排低浓度泵速度（rpm）=低浓度泵启动速度（rpm）/制胶速度（rpm）×冲洗液泵速度（rpm）
            val lowExpectedSpeed = lowStartSpeed / speed * rinseSpeed
            Log.d(
                "HomeViewModel_startJob",
                "===20预排低浓度泵速度===$lowExpectedSpeed"
            )
            //20预排低浓度泵速度（rpm）=低浓度泵启动速度（rpm）/制胶速度（rpm）×冲洗液泵速度（rpm）

            //21预排时间s
            val expectedTime = highExpectedPulseCount / 51200 / rinseSpeed * 60
            Log.d(
                "HomeViewModel_startJob",
                "===21预排时间s===$expectedTime"
            )
            //21预排时间s

            //22预排促凝剂泵速度
            val expectedCoagulantSpeed = coagulantExpectedPulse / 51200 / expectedTime * 60 * ratio
            Log.d(
                "HomeViewModel_startJob",
                "===22预排促凝剂泵速度===$expectedCoagulantSpeed"
            )
            //22预排促凝剂泵速度


            Log.d(
                "HomeViewModel_startJob",
                "===预排前期准备数据结束==="
            )





            _job.value?.cancel()
            _job.value = launch {
                try {
                    for (i in 1..8) {


                        var coagulantBool = false
                        var coagulantStart = 0L
                        var currurStatus = status
                        Log.d(
                            "HomeViewModel_startJob",
                            "===运动次数status===$status"
                        )
                        if (currurStatus == 0) {
                            _complate.value = 0
                        } else {
                            //计算柱塞泵是否够下一次运动
                            /**
                             * 柱塞泵总行程
                             */
                            val coagulantpulse =
                                dataStore.readData("coagulantpulse", 550000).toLong()

                            /**
                             * 已经运动的柱塞泵步数
                             */
                            coagulantStart =
                                coagulantExpectedPulse.toLong() * currurStatus + coagulantPulseCount.toLong() * currurStatus
                            Log.d(
                                "HomeViewModel_startJob",
                                "===已经运动的柱塞泵步数===$coagulantStart"
                            )
                            /**
                             * 柱塞泵剩余步数
                             */
                            val coagulantSy = coagulantpulse - coagulantStart
                            Log.d(
                                "HomeViewModel_startJob",
                                "===柱塞泵剩余步数===$coagulantSy"
                            )

                            if (coagulantSy < coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong()) {
                                /**
                                 * 柱塞泵剩余步数不够加液
                                 */
                                coagulantBool = true
                            }

                        }


                        SerialPortUtils.cleanLight()

                        if (currurStatus == 0) {
                            /**
                             * 高浓度管路填充
                             */
                            val slEnetity = slDao.getById(1L).firstOrNull()
                            if (slEnetity != null) {
                                val higeFilling = slEnetity.higeFilling

                                /**
                                 * 低浓度管路填充
                                 */
                                val lowFilling = slEnetity.lowFilling

                                /**
                                 * 冲洗液泵管路填充
                                 */
                                val rinseFilling = slEnetity.rinseFilling


                                /**
                                 * 促凝剂泵管路填充
                                 */
                                val coagulantFilling = slEnetity.coagulantFilling

                                val p1 =
                                    SerialPortUtils.pulse(index = 1, dvp = coagulantFilling * 1000)

                                /**
                                 * 促凝剂总行程
                                 */
                                val coagulantpulse =
                                    dataStore.readData("coagulantpulse", 550000).toLong()

                                /**
                                 * 促凝剂转速
                                 */
                                val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)

                                /**
                                 * 冲洗转速
                                 */
                                val rinseSpeed = dataStore.readData("rinseSpeed", 600L)

                                /**
                                 * x轴转速
                                 */
                                val xSpeed = dataStore.readData("xSpeed", 100L)

                                val p1Count = p1.toDouble() / (coagulantpulse - 51200).toDouble()

                                /**
                                 * 废液槽位置
                                 */
                                val wastePosition = dataStore.readData("wastePosition", 0.0)

                                SerialPortUtils.start {
                                    timeOut = 1000L * 60L * 10
                                    with(
                                        index = 0,
                                        pdv = wastePosition,
                                        ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                                    )
                                }
                                delay(100L)
                                if (p1Count > 1) {
                                    for (i in 0 until Math.ceil(p1Count).toInt()) {
                                        SerialPortUtils.start {
                                            timeOut = 1000L * 60L * 10
                                            with(
                                                index = 1,
                                                ads = Triple(
                                                    coagulantSpeed * 13,
                                                    coagulantSpeed * 1193,
                                                    coagulantSpeed * 1193
                                                ),
                                                pdv = coagulantpulse
                                            )
                                        }
                                        delay(100L)
                                        SerialPortUtils.start {
                                            timeOut = 1000L * 60L * 10
                                            with(
                                                index = 1,
                                                ads = Triple(
                                                    coagulantSpeed * 13,
                                                    coagulantSpeed * 1193,
                                                    coagulantSpeed * 1193
                                                ),
                                                pdv = -coagulantpulse
                                            )
                                        }
                                        delay(100L)
                                    }

                                } else {
                                    SerialPortUtils.start {
                                        timeOut = 1000L * 60L * 10
                                        with(
                                            index = 1,
                                            ads = Triple(
                                                coagulantSpeed * 13,
                                                coagulantSpeed * 1193,
                                                coagulantSpeed * 1193
                                            ),
                                            pdv = coagulantFilling * 1000
                                        )
                                    }
                                }


                                delay(100L)

                                SerialPortUtils.start {
                                    timeOut = 1000L * 60L * 10
                                    with(
                                        index = 2,
                                        ads = Triple(
                                            rinseSpeed * 13,
                                            rinseSpeed * 1193,
                                            rinseSpeed * 1193
                                        ),
                                        pdv = higeFilling * 1000
                                    )
                                    with(
                                        index = 3,
                                        ads = Triple(
                                            rinseSpeed * 13,
                                            rinseSpeed * 1193,
                                            rinseSpeed * 1193
                                        ),
                                        pdv = lowFilling * 1000
                                    )
                                    with(
                                        index = 4,
                                        ads = Triple(
                                            rinseSpeed * 40,
                                            rinseSpeed * 40,
                                            rinseSpeed * 40
                                        ),
                                        pdv = rinseFilling * 1000
                                    )
                                }

                            }
                            delay(100L)
                        }

                        if (coagulantBool) {
                            Log.d(
                                "HomeViewModel_startJob",
                                "===柱塞泵回到下拉到底==="
                            )
                            SerialPortUtils.start {
                                timeOut = 1000L * 60 * 1

                                with(
                                    index = 1,
                                    pdv = -coagulantStart,
                                    ads = Triple(
                                        (600 * 13).toLong(),
                                        (600 * 1193).toLong(),
                                        (600 * 1193).toLong()
                                    )
                                )
                            }
                            Log.d(
                                "HomeViewModel_startJob",
                                "===柱塞泵回到下拉到底==="
                            )
                        }


//===================废液槽运动开始=====================
                        Log.d(
                            "HomeViewModel_startJob",
                            "===废液槽运动开始==="
                        )
                        //废液槽位置
                        SerialPortUtils.start {
                            timeOut = 1000L * 60L
                            with(
                                index = 0,
                                pdv = setting.wastePosition,
                                ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                            )
                        }
                        Log.d(
                            "HomeViewModel_startJob",
                            "===废液槽运动结束==="
                        )
//===================废液槽运动结束=====================

                        /**
                         * 预排液
                         */
                        //===================预排液开始=====================
                        Log.d(
                            "HomeViewModel",
                            "===预排液开始==="
                        )

                        SerialPortUtils.start {
                            timeOut = 1000L * 60 * 1

                            with(
                                index = 1,
                                pdv = coagulantExpectedPulse.toLong(),
                                ads = Triple(
                                    0L,
                                    (expectedCoagulantSpeed * 1193).toLong(),
                                    (expectedCoagulantSpeed * 1193).toLong()
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

                        //===================预排液结束=====================
                        Log.d(
                            "HomeViewModel",
                            "===预排液结束==="
                        )


                        //===================制胶位置移动开始=====================
                        Log.d(
                            "HomeViewModel",
                            "===制胶位置移动开始==="
                        )
                        //制胶位置
                        SerialPortUtils.start {
                            timeOut = 1000L * 60L
//                        with(index = 0, pdv = glueBoardPosition)
                            with(
                                index = 0,
                                pdv = setting.glueBoardPosition,
                                ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                            )

                        }
                        Log.d(
                            "HomeViewModel",
                            "===制胶位置移动结束==="
                        )
                        delay(100)

                        Log.d(
                            "HomeViewModel",
                            "===制胶运动开始==="
                        )

                        Log.d(
                            "HomeViewModel",
                            "===柱塞泵参数===步数===$coagulantPulseCount===加速度===${(coagulantAcc * 13).toLong()}===开始速度===${(coagulantStartSpeed * 1193).toLong()}===结束速度==${(coagulantEndSpeed * 1193).toLong()}"
                        )
                        Log.d(
                            "HomeViewModel",
                            "===高浓度泵参数===步数===${guleHighPulse.toLong()}===加速度===${(highAcc * 13).toLong()}===开始速度===${(highEndSpeed * 1193).toLong()}===结束速度==${(highStartSpeed * 1193).toLong()}"
                        )
                        Log.d(
                            "HomeViewModel",
                            "===低浓度泵参数===步数===${guleLowPulse.toLong()}===加速度===${(lowAcc * 13).toLong()}===开始速度===${(lowStartSpeed * 1193).toLong()}===结束速度==${(lowEndSpeed * 1193).toLong()}"
                        )


                        SerialPortUtils.start {
                            timeOut = 1000L * 60 * 10
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


                        Log.d(
                            "HomeViewModel",
                            "===制胶运动结束==="
                        )


                        //===================制胶运动结束=====================
                        val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                        Log.d(
                            "HomeViewModel_clean",
                            "冲洗转速===$rinseSpeed"
                        )
                        delay(2000)
                        //制胶完成，清洗运动
                        /**
                         * 冲洗液泵清洗液量
                         */
                        val rinseP =
                            SerialPortUtils.pulse(index = 4, dvp = setting.rinseCleanVolume * 1000)

                        SerialPortUtils.start {
                            timeOut = 1000L * 60L
                            with(
                                index = 0,
                                ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                                pdv = setting.wastePosition
                            )
                        }


                        SerialPortUtils.start {
                            timeOut = 1000L * 60L
                            with(
                                index = 4,
                                ads = Triple(rinseSpeed * 40, rinseSpeed * 40, rinseSpeed * 40),
                                pdv = rinseP
                            )
                        }

                        delay(100)
                    }
                    val glueNum = dataStore.readData("glueNum", 1)
                    if (_complate.value == glueNum) {
                        _uiFlags.value = UiFlags.objects(6)
                    } else {
                        _uiFlags.value = UiFlags.objects(4)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    _job.value?.cancel()
                    _job.value = null
                }
            }

        }
    }

    private fun navigation(nav: Boolean) {
        viewModelScope.launch {
            val intent = Intent().apply {
                action = "ACTION_SHOW_NAVBAR"
                putExtra("cmd", if (nav) "show" else "hide")
            }
            ApplicationUtils.ctx.sendBroadcast(intent)
        }
    }

    private fun network() {
        // Create a new intent to launch the Wi-Fi settings screen
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Set the extra preferences to show the button bar and custom text
            putExtra("extra_prefs_show_button_bar", true)
            putExtra("extra_prefs_set_next_text", ApplicationUtils.ctx.getString(R.string.finish))
            putExtra("extra_prefs_set_back_text", ApplicationUtils.ctx.getString(R.string.cancel))
        }
        // Launch the Wi-Fi settings screen
        ApplicationUtils.ctx.startActivity(intent)
    }

    private fun checkUpdate() {
        viewModelScope.launch {
            val application = _application.value
            if (application != null) {
                if (application.versionCode > BuildConfig.VERSION_CODE
                    && application.downloadUrl.isNotEmpty()
                    && _progress.value == 0
                ) {
                    _progress.value = 1
                    application.downloadUrl.download(
                        File(
                            ApplicationUtils.ctx.getExternalFilesDir(null),
                            "update.apk"
                        )
                    ).collect {
                        when (it) {
                            is DownloadState.Success -> {
                                _progress.value = 0
                                ApplicationUtils.installApp(it.file)
                            }

                            is DownloadState.Err -> {
                                _progress.value = 0
                                _uiFlags.value = UiFlags.message("下载失败: ${it.t.message}")
                            }

                            is DownloadState.Progress -> {
                                _progress.value = maxOf(it.progress, 1)
                            }
                        }
                    }
                }
            } else {
                httpCall(exception = {
                    _uiFlags.value = UiFlags.message(it.message ?: "Unknown")
                }) { app ->
                    if (app != null) {
                        _application.value = app
                    } else {
                        _uiFlags.value = UiFlags.message("未找到升级信息")
                    }
                }
            }
        }
    }
}

sealed class SettingIntent {
    data class Navigation(val navigation: Boolean) : SettingIntent()
    data class NavTo(val page: Int) : SettingIntent()
    data class Flags(val uiFlags: UiFlags) : SettingIntent()
    data class Selected(val id: Long) : SettingIntent()
    data object Insert : SettingIntent()
    data class Update(val entity: Motor) : SettingIntent()
    data class Delete(val id: Long) : SettingIntent()
    data object CheckUpdate : SettingIntent()
    data object Network : SettingIntent()

    data class InsertSet(
        val highTime: Double,
        val lowLife: Double,
        val rinseTime: Double,
        val highTimeExpected: Double,
        val lowTimeExpected: Double,
        val rinseTimeExpected: Double,
        val wastePosition: Double,
        val glueBoardPosition: Double,
        val higeCleanVolume: Double,
        val higeRehearsalVolume: Double,
        val higeFilling: Double,
        val lowCleanVolume: Double,
        val lowFilling: Double,
        val rinseCleanVolume: Double,
        val rinseFilling: Double,
        val coagulantCleanVolume: Double,
        val coagulantFilling: Double

    ) : SettingIntent()

    data class UpdateSet(val entity: Setting) : SettingIntent()

    data class InsertNC(
        val higeLiquidVolume1: Double,
        val higeLiquidVolume2: Double,
        val higeLiquidVolume3: Double,
        val higeAvg: Double,
        val lowLiquidVolume1: Double,
        val lowLiquidVolume2: Double,
        val lowLiquidVolume3: Double,
        val lowAvg: Double,
        val coagulantLiquidVolume1: Double,
        val coagulantLiquidVolume2: Double,
        val coagulantLiquidVolume3: Double,
        val coagulantAvg: Double,
        val rinseLiquidVolume1: Double,
        val rinseLiquidVolume2: Double,
        val rinseLiquidVolume3: Double,
        val rinseAvg: Double
    ) : SettingIntent()

    data class UpdateNC(val entity: NewCalibration) : SettingIntent()

    data class Start(val count: Int) : SettingIntent()
    data object Stop : SettingIntent()

    data object Reset : SettingIntent()
    data object XReset : SettingIntent()
    data object ZSReset : SettingIntent()

    data object Clean : SettingIntent()

    data object Pipeline : SettingIntent()

    data class Sound(val state: Int) : SettingIntent()

    data object ClearAll : SettingIntent()

    data class Login(val pwd: String) : SettingIntent()

}