package com.zktony.android.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Logger
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpd
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.cleanLight
import com.zktony.android.utils.SerialPortUtils.getGpio
import com.zktony.android.utils.SerialPortUtils.glue
import com.zktony.android.utils.SerialPortUtils.glueNew
import com.zktony.android.utils.SerialPortUtils.gpio
import com.zktony.android.utils.SerialPortUtils.lightFlashYellow
import com.zktony.android.utils.SerialPortUtils.lightGreed
import com.zktony.android.utils.SerialPortUtils.lightRed
import com.zktony.android.utils.SerialPortUtils.lightYellow
import com.zktony.android.utils.SerialPortUtils.pulse
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.SerialPortUtils.stop
import com.zktony.android.utils.SerialPortUtils.valve
import com.zktony.android.utils.extra.playAudio
import com.zktony.android.utils.internal.ExecuteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Date
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    private val dataStore: DataSaverDataStore,
    private val erDao: ExperimentRecordDao,
    private val slDao: SettingDao
) : ViewModel() {

    private val _selected = MutableStateFlow(1L)
    private val _selectedER = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())
    private val _job = MutableStateFlow<Job?>(null)

    /**
     * 加液次数
     */
    private val _complate = MutableStateFlow(0)

    /**
     * 单次加液进度
     */
    private val _progress = MutableStateFlow(0f)


    private var syringeJob: Job? = null

    val selected = _selected.asStateFlow()
    val selectedER = _selectedER.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val job = _job.asStateFlow()
    val complate = _complate.asStateFlow()
    val progress = _progress.asStateFlow()


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
        reset()
    }

    fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Clean -> clean()
            is HomeIntent.NavTo -> _page.value = intent.page
            is HomeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is HomeIntent.Pipeline -> pipeline(intent.index)
            is HomeIntent.Reset -> reset()
            is HomeIntent.deng -> dengguang()
            is HomeIntent.tuopan -> guangdian3()
            is HomeIntent.Start -> startJob(intent.count)
            is HomeIntent.Stop -> stopJob()
            is HomeIntent.Syringe -> syringe(intent.index)
            is HomeIntent.Selected -> _selected.value = intent.id
            is HomeIntent.Insert -> viewModelScope.launch {
                _selectedER.value = erDao.insert(
                    ExperimentRecord(
                        startRange = intent.startRange,
                        endRange = intent.endRange,
                        thickness = intent.thickness,
                        coagulant = intent.coagulant,
                        volume = intent.volume,
                        number = intent.number,
                        status = intent.status,
                        detail = intent.detail
                    )
                )
            }

            is HomeIntent.Update -> viewModelScope.launch { erDao.update(intent.entity) }
        }
    }

    private fun dengguang() {
        viewModelScope.launch {
            lightRed()
            delay(3000L)
            cleanLight()
        }
    }

    //托盘检测
    private fun guangdian3() {
        viewModelScope.launch {
            gpio(3)
            delay(500L)
        }
    }


    private fun reset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
                hpd[0] = -1
                val coagulantpulse = dataStore.readData("coagulantpulse", 67500).toLong()
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
                    Log.d(
                        "HomeViewModel",
                        "x轴光电状态====0号光电===" + getGpio(0) + "====1号光电===" + getGpio(1)
                    )
                    if (!getGpio(0) && !getGpio(1)) {
                        Log.d(
                            "HomeViewModel",
                            "x轴反转"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(600 * 100, 600 * 101, 600 * 100),

                                )
                        }
                        Log.d(
                            "HomeViewModel",
                            "x轴正转"
                        )
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 6400L,
                                ads = Triple(600 * 100, 600 * 101, 600 * 100),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -6500L,
                                ads = Triple(200 * 100, 200 * 101, 200 * 100),
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
                                    ads = Triple(600 * 100, 600 * 101, 600 * 100),

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

                    } else if (!getGpio(0) && getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(600 * 100, 600 * 101, 600 * 100),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 6400L,
                                ads = Triple(600 * 100, 600 * 101, 600 * 100),

                                )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -6500L,
                                ads = Triple(200 * 100, 200 * 101, 200 * 100),

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
                                    ads = Triple(600 * 100, 600 * 101, 600 * 100),

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

                    } else if (getGpio(0) && !getGpio(1)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = 6400L,
                                ads = Triple(600 * 100, 600 * 101, 600 * 100),
                            )
                        }
                        gpio(0)
                        delay(500L)
                        if (getGpio(0)) {
                            Log.d(
                                "HomeViewModel",
                                "复位失败"
                            )
                        } else {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -6500L,
                                    ads = Triple(200 * 100, 200 * 101, 200 * 100),

                                    )
                            }
                            Log.d(
                                "HomeViewModel",
                                "复位完成"
                            )
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(600 * 100, 600 * 101, 600 * 100),
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
                    gpio(2)
                    delay(500L)
                    Log.d(
                        "HomeViewModel",
                        "注射泵光电状态====2号光电===" + getGpio(2)
                    )
                    if (!getGpio(2)) {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 310000L,
                                ads = Triple(1200 * 100, 1200 * 100 + 5, 1200 * 100),
                            )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -6400L,
                                ads = Triple(1200 * 100, 1200 * 100 + 5, 1200 * 100),
                            )
                        }
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = 9600L,
                                ads = Triple(600 * 100, 600 * 100 + 5, 600 * 100),
                            )
                        }

                        gpio(2)
                        delay(500L)
                        if (getGpio(2)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = -coagulantpulse,
                                    ads = Triple(1200 * 100, 1200 * 100 + 5, 1200 * 100),
                                )
                            }

                            //复位完成
                        } else {
                            //复位失败
                        }
                    } else {
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 1,
                                pdv = -6400L,
                                ads = Triple(1200 * 100, 1200 * 100 + 5, 1200 * 100),

                                )
                        }
                        gpio(2)
                        delay(500L)
                        if (getGpio(2)) {
                            //复位失败
                        } else {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = 9600L,
                                    ads = Triple(600 * 100, 600 * 100 + 5, 600 * 100),
                                )
                            }
                            //复位完成
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 1,
                                    pdv = -coagulantpulse,
                                    ads = Triple(1200 * 100, 1200 * 100 + 5, 1200 * 100),
                                )
                            }


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

    private fun startJob(status: Int) {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(0)
            Log.d(
                "HomeViewModel",
                "已选择程序的id=========" + _selected.value
            )
            val selected = dao.getById(_selected.value).firstOrNull()

            Log.d(
                "HomeViewModel",
                "已选择的程序=========" + selected
            )
            if (selected == null) {
                _uiFlags.value = UiFlags.message("未选择程序")
                return@launch
            }


            val setting = slDao.getById(1L).firstOrNull()
            if (setting == null) {
                _uiFlags.value = UiFlags.message("系统参数无数据")
                return@launch
            }

            if (status == 0) {
                _complate.value = 0
            }
            /**
             * 要完成的制胶数量
             */
            var expectedMakeNum = dataStore.readData("expectedMakenum", 1)




            hpd[0] = -1
            /**
             * 总步数
             */
            val coagulantpulse = dataStore.readData("coagulantpulse", 67500).toLong()
            Log.d(
                "HomeViewModel",
                "总步数=========$coagulantpulse"
            )

            /**
             * 促凝剂体积
             */
            val coagulantVol = selected.coagulant
            var coagulantPulse = pulse(index = 1, dvp = coagulantVol)

            /**
             * 制胶进度总长度
             */
            val barCount = Math.floor(coagulantPulse / 160.0).toLong() - 2
            var barBool = false

            /**
             * 完成步数
             */
            if (coagulantpulse - coagulantPulse <= coagulantPulse) {
                stopJob()
            } else {
                Log.d(
                    "HomeViewModel",
                    "===开始制胶==="
                )


                /**
                 * 高浓度预排液量
                 */
                val higeRehearsalVolume = setting.higeRehearsalVolume * 1000
                Log.d(
                    "HomeViewModel",
                    "高浓度预排液量===$higeRehearsalVolume"
                )

                /**
                 * 制胶速度，根据这个速度转换其他泵的速度
                 */
                val speed = dataStore.readData("speed", 0)

                /**
                 *冲洗液泵转速
                 */
                val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                println("冲洗液泵转速===$rinseSpeed")


                /**
                 * 胶液体积
                 */
                val volumeVol = selected.volume * 1000
                Log.d(
                    "HomeViewModel",
                    "胶液体积===$volumeVol"
                )

                /**
                 * 促凝剂预排液量
                 */
                val coagulantRehearsal = coagulantVol / volumeVol * higeRehearsalVolume
                Log.d(
                    "HomeViewModel",
                    "促凝剂预排液量===$coagulantRehearsal"
                )

                _job.value?.cancel()
                _job.value = launch {
                    try {
                        cleanLight()
//===================废液槽运动开始=====================
                        Log.d(
                            "HomeViewModel",
                            "===废液槽运动开始==="
                        )
                        //废液槽位置
                        start {
                            timeOut = 1000L * 60L
                            with(
                                index = 0,
                                pdv = setting.wastePosition,
                                ads = Triple(300 * 100, 400 * 100, 600 * 100),
                            )
                        }
                        Log.d(
                            "HomeViewModel",
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
                        start {
                            timeOut = 1000L * 60 * 1

                            val s = speed.toDouble()


                            //柱塞泵-促凝剂  促凝剂体积/胶液体积*higeRehearsalVolume
                            var p1 = pulse(index = 1, dvp = coagulantRehearsal)
//                            _usedCoagulant.value += p1 / 1000f
                            Log.d(
                                "HomeViewModel",
                                "预排液p1步数=====$p1"
                            )
                            //高浓度
                            val p2 = pulse(index = 2, dvp = higeRehearsalVolume)
//                            _usedHigh.value += p2 / 1000f
                            Log.d(
                                "HomeViewModel",
                                "预排液p2步数=====$p2"
                            )

                            val ad = (2 * rinseSpeed * 100)


                            val s1 = (p1.toDouble() / p2 * rinseSpeed * 100).toLong()
                            Log.d(
                                "HomeViewModel",
                                "预排液s1=====$s1"
                            )
                            Log.d(
                                "HomeViewModel",
                                "预排液ad=====$ad"
                            )

                            with(index = 1, pdv = p1, ads = Triple(s1 * 2, s1 * 2, s1))

                            with(index = 2, pdv = p2, ads = Triple(ad, ad, rinseSpeed * 100))
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
                        start {
                            timeOut = 1000L * 60L
//                        with(index = 0, pdv = glueBoardPosition)
                            with(
                                index = 0,
                                pdv = setting.glueBoardPosition,
                                ads = Triple(300 * 100, 400 * 100, 600 * 100),
                            )

                        }
                        Log.d(
                            "HomeViewModel",
                            "===制胶位置移动结束==="
                        )
                        delay(100)
                        //回复=柱塞泵总步数/160
                        Log.d(
                            "HomeViewModel",
                            "===制胶运动开始==="
                        )
                        start {
                            timeOut = 1000L * 60 * 10
                            executeType = ExecuteType.ASYNC
                            val s = speed.toDouble()
                            var p1 = pulse(index = 1, dvp = coagulantVol)
                            val p2 = pulse(index = 2, dvp = volumeVol)
                            val p3 = pulse(index = 3, dvp = volumeVol)


                            Log.d(
                                "HomeViewModel",
                                "加液p1步数=====$p1"
                            )
                            Log.d(
                                "HomeViewModel",
                                "加液p2步数=====$p2"
                            )
                            Log.d(
                                "HomeViewModel",
                                "加液p3步数=====$p3"
                            )

                            val pv1 = (p2 + p3) / 2

                            Log.d(
                                "HomeViewModel",
                                "index===1===所有速度===" + ((p1 / (pv1 * 2 / s)) * 100).toLong()
                            )

                            Log.d(
                                "HomeViewModel",
                                "index===2===加速===" + (s * 100).toLong() + "===+减速===" + (s * s / 2 / pv1 * 100).toLong() + "===速度===" + (s * 100).toLong()
                            )


                            Log.d(
                                "HomeViewModel",
                                "index===3===加速===" + (s * s / 2 / pv1 * 100).toLong() + "===+减速===" + (s * 100).toLong() + "===速度===" + (s * 100).toLong()
                            )



                            with(
                                index = 1, pdv = p1, ads = Triple(
                                    ((p1 / (pv1 * 2 / s)) * 100).toLong(),
                                    ((p1 / (pv1 * 2 / s)) * 100).toLong(),
//                                (2.5 * s * 100).toLong(),
//                                (2.5 * s * 100).toLong(),
                                    ((p1 / (pv1 * 2 / s)) * 100).toLong()
                                )
                            )
                            with(
                                index = 2, pdv = pv1, ads = Triple(
//                                1, 1,
//                                s.toLong()
                                    (s * 100).toLong(),
                                    (s * s / 2 / pv1 * 100).toLong(),
                                    (s * 100).toLong()
                                )
                            )
                            with(
                                index = 3, pdv = pv1, ads = Triple(
//                                1, 1,
//                                s.toLong()
                                    (s * s / 2 / pv1 * 100).toLong(),
                                    (s * 100).toLong(),
                                    (s * 100).toLong()
                                )
                            )
                        }

                        delay(100)


                        if (hpd[0] != null && barCount > 0) {
                            while (hpd[0]!! <= barCount) {
                                delay(100L)
                                var bars = hpd[0]?.div(barCount.toFloat())
                                if (bars != null) {
                                    _progress.value = bars
                                }
                                Log.d(
                                    "HomeViewModel",
                                    "hdp[0]====" + hpd[0] + "===barCount===" + barCount + "===_progress.value===" + _progress.value
                                )
                                if (bars == 1.0f) {
                                    barBool = true
                                    _progress.value = bars
                                    break
                                }

                            }
                        }

                        Log.d(
                            "HomeViewModel",
                            "===制胶运动结束==="
                        )

                        //===================制胶运动结束=====================

                        if (barBool) {
                            delay(2000)
                            //制胶完成，清洗运动
                            /**
                             * 冲洗液泵清洗液量
                             */
                            val rinseP = pulse(index = 4, dvp = setting.rinseCleanVolume * 1000)

                            start {
                                timeOut = 1000L * 60L
                                with(
                                    index = 0,
                                    ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                    pdv = setting.wastePosition
                                )
                            }

                            start {
                                timeOut = 1000L * 60L
                                with(
                                    index = 4,
                                    ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                    pdv = rinseP
                                )
                            }

                            delay(100)
                            _complate.value += 1
//                            _usedCoagulant.value += (coagulantRehearsal/1000).toFloat()

                            if (_complate.value == expectedMakeNum) {
                                _uiFlags.value = UiFlags.objects(6)
                            } else {
                                lightFlashYellow()
                                _uiFlags.value = UiFlags.objects(4)
                            }
                            ApplicationUtils.ctx.playAudio(R.raw.error)
                        }

                    } catch (ex: Exception) {
                        var experimentRecord = erDao.getById(_selectedER.value).firstOrNull()
                        if (experimentRecord != null) {
                            experimentRecord.status = EPStatus.FAULT
                            experimentRecord.detail = "系统故障"
                            HomeIntent.Update(experimentRecord)
                        }
                        ex.printStackTrace()
                    } finally {

                        _job.value?.cancel()
                        _job.value = null
                    }
                }


            }
        }
    }


    private fun stopJob() {
        viewModelScope.launch {
            cleanLight()
            _uiFlags.value = UiFlags.objects(1)
            _job.value?.cancel()
            _job.value = null
            delay(200L)
//            stop(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            stop(0, 1, 2, 3, 4)
            delay(200L)
            reset()
        }
    }

    private fun clean() {
        viewModelScope.launch {
            if (_uiFlags.value is UiFlags.Objects && (_uiFlags.value as UiFlags.Objects).objects == 2) {
                _uiFlags.value = UiFlags.none()
                stop(1, 2, 3, 4)
            } else {
                _uiFlags.value = UiFlags.objects(2)

                val coagulantpulse = dataStore.readData("coagulantpulse", 67500).toLong()

                /**
                 * 高浓度清洗液量
                 */
                val higeCleanVolume = dataStore.readData("higeCleanVolume", 0.0)
                println("higeCleanVolume===$higeCleanVolume * 1000")
                /**
                 * 低浓度清洗液量
                 */
                val lowCleanVolume = dataStore.readData("lowCleanVolume", 0.0)
                println("lowCleanVolume===$lowCleanVolume * 1000")

                /**
                 * 冲洗液泵清洗液量
                 */
                val rinseCleanVolume = dataStore.readData("rinseCleanVolume", 0.0)
                println("rinseCleanVolume===$rinseCleanVolume * 1000")

                /**
                 * 促凝剂泵清洗液量
                 */
                val coagulantCleanVolume = dataStore.readData("coagulantCleanVolume", 0.0)

                val p1 = pulse(index = 1, dvp = coagulantCleanVolume)

                val p1Count = p1.toDouble() / coagulantpulse.toDouble()


                /**
                 * 废液槽位置
                 */
                val wastePosition = dataStore.readData("wastePosition", 0.0)

                start {
                    timeOut = 1000L * 60L * 10
                    with(

                        index = 0,
                        pdv = wastePosition,
                        ads = Triple(300 * 100, 400 * 100, 600 * 100),
                    )
                }

                if (p1Count > 1) {
                    for (i in 0 until Math.ceil(p1Count).toInt()) {
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = coagulantpulse
                            )
                        }
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = -coagulantpulse
                            )
                        }
                    }

                } else {
                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 1,
                            ads = Triple(600 * 100, 600 * 100, 600 * 100),
                            pdv = coagulantCleanVolume * 1000
                        )
                    }
                }


                start {
                    timeOut = 1000L * 60L * 10
                    with(
                        index = 2,
                        ads = Triple(600 * 100 + 5, 600 * 100 + 5, 600 * 100),
                        pdv = higeCleanVolume * 1000
                    )
                    with(
                        index = 3,
                        ads = Triple(600 * 100 + 5, 600 * 100 + 5, 600 * 100),
                        pdv = lowCleanVolume * 1000
                    )
                    with(
                        index = 4,
                        ads = Triple(600 * 100, 600 * 100, 600 * 100),
                        pdv = rinseCleanVolume * 1000
                    )
                }

                reset()
            }
        }
    }

    private fun syringe(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                syringeJob?.cancel()
                syringeJob = null
                stop(2)
                delay(100L)
                valve(2 to if (_uiFlags.value is UiFlags.Objects && (_uiFlags.value as UiFlags.Objects).objects == 3) 1 else 0)
                delay(30L)
                _uiFlags.value = UiFlags.objects(1)
                start {
                    timeOut = 1000L * 30
                    with(index = 2, pdv = Constants.ZT_0005 * -1)
                }
                _uiFlags.value = UiFlags.none()
            } else {
                _uiFlags.value = UiFlags.objects(2 + index)
                syringeJob = launch {
                    while (true) {
                        valve(2 to if (index == 1) 0 else 1)
                        delay(30L)
                        start {
                            timeOut = 1000L * 60
                            with(index = 2, pdv = Constants.ZT_0005)
                        }
                        valve(2 to if (index == 1) 1 else 0)
                        delay(30L)
                        start {
                            timeOut = 1000L * 60
                            with(index = 2, pdv = Constants.ZT_0005 * -1)
                        }
                    }
                }
            }
        }
    }


    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                _uiFlags.value = UiFlags.none()
                stop(1, 2, 3, 4)
            } else {
                _uiFlags.value = UiFlags.objects(4 + index)

                /**
                 * 高浓度管路填充
                 */
                val higeFilling = dataStore.readData("higeFilling", 0.0)
                println("higeFilling===$higeFilling")

                /**
                 * 低浓度管路填充
                 */
                val lowFilling = dataStore.readData("lowFilling", 0.0)
                println("lowFilling===$lowFilling")

                /**
                 * 冲洗液泵管路填充
                 */
                val rinseFilling = dataStore.readData("rinseFilling", 0.0)
                println("rinseFilling===$rinseFilling")

                /**
                 * 促凝剂泵管路填充
                 */
                val coagulantFilling = dataStore.readData("coagulantFilling", 0.0)

                val p1 = pulse(index = 1, dvp = coagulantFilling)
                val coagulantpulse = dataStore.readData("coagulantpulse", 67500).toLong()
                val p1Count = p1.toDouble() / coagulantpulse.toDouble()

                /**
                 * 废液槽位置
                 */
                val wastePosition = dataStore.readData("wastePosition", 0.0)

                start {
                    timeOut = 1000L * 60L * 10
                    with(

                        index = 0,
                        pdv = wastePosition,
                        ads = Triple(300 * 100, 400 * 100, 600 * 100),
                    )
                }

                if (p1Count > 1) {
                    for (i in 0 until Math.ceil(p1Count).toInt()) {
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = coagulantpulse
                            )
                        }
                        start {
                            timeOut = 1000L * 60L * 10
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = -coagulantpulse
                            )
                        }
                    }

                } else {
                    start {
                        timeOut = 1000L * 60L * 10
                        with(
                            index = 1,
                            ads = Triple(600 * 100, 600 * 100, 600 * 100),
                            pdv = coagulantFilling * 1000
                        )
                    }
                }




                start {
                    timeOut = 1000L * 60L * 10
                    with(
                        index = 2,
                        ads = Triple(600 * 100 + 5, 600 * 100 + 5, 600 * 100),
                        pdv = higeFilling * 1000
                    )
                    with(
                        index = 3,
                        ads = Triple(600 * 100 + 5, 600 * 100 + 5, 600 * 100),
                        pdv = lowFilling * 1000
                    )
                    with(
                        index = 4,
                        ads = Triple(600 * 100, 600 * 100, 600 * 100),
                        pdv = rinseFilling * 1000
                    )
                }

                reset()

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
    data class Syringe(val index: Int) : HomeIntent()
    data class Selected(val id: Long) : HomeIntent()
    data object tuopan : HomeIntent()
    data object Clean : HomeIntent()
    data object Reset : HomeIntent()
    data object deng : HomeIntent()
    data class Start(val count: Int) : HomeIntent()
    data object Stop : HomeIntent()

    data class Insert(
        val startRange: Double,
        val endRange: Double,
        val thickness: String,
        val coagulant: Double,
        val volume: Double,
        var number: Int,
        var status: String,
        var detail: String
    ) : HomeIntent()

    data class Update(val entity: ExperimentRecord) : HomeIntent()
}