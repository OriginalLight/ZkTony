package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.R
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils.hpd
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.cleanLight
import com.zktony.android.utils.SerialPortUtils.getGpio
import com.zktony.android.utils.SerialPortUtils.glue
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao,
    private val dataStore: DataSaverDataStore
) : ViewModel() {

    private val _selected = MutableStateFlow(1L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())
    private val _job = MutableStateFlow<Job?>(null)

    private var syringeJob: Job? = null

    val selected = _selected.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val job = _job.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)


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
            println("getGpio(3)====" + getGpio(3))
        }
    }

    private fun reset() {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(1)
            try {
                dataStore.saveData("finishpluse", 0L)
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
                    println(
                        "x轴光电状态====0号光电===" + getGpio(0) + "====1号光电===" + getGpio(
                            1
                        )
                    )
                    if (!getGpio(0) && !getGpio(1)) {
                        println("x轴反转")
                        start {
                            timeOut = 1000L * 30
                            with(
                                index = 0,
                                pdv = -64000L,
                                ads = Triple(600 * 100, 600 * 101, 600 * 100),

                                )
                        }
                        println("x轴正转")
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
                            println("复位完成")
                            //复位完成
                        } else {
                            println("复位失败")
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
                            println("复位完成")
                            //复位完成
                        } else {
                            println("复位失败")
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
                            println("复位失败")
                        } else {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = -6500L,
                                    ads = Triple(200 * 100, 200 * 101, 200 * 100),

                                    )
                            }
                            println("复位完成")
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
                        println("复位失败")
                    }
                    //x轴复位===========================================


                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    gpio(2)
                    delay(500L)
                    println("注射泵光电状态====2号光电===" + getGpio(2))
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

    private fun startJob(count: Int) {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(0)
            val selected = dao.getById(_selected.value).firstOrNull()
            if (selected == null) {
                _uiFlags.value = UiFlags.message("未选择程序")
                return@launch
            }

            /**
             * 总步数
             */
            val coagulantpulse = dataStore.readData("coagulantpulse", 67500).toLong()
            println("总步数=========$coagulantpulse")


            /**
             * 完成步数
             */
            var finishpluse = dataStore.readData("finishpluse", 0L)
            println("完成步数=========$finishpluse")
            if (coagulantpulse - finishpluse <= finishpluse) {
                stopJob()
            } else {
                println("===开始制胶===")

                /**
                 * 废液槽位置
                 */
                val wastePosition = dataStore.readData("wastePosition", 0.0)
                println("废液槽位置===$wastePosition")

                /**
                 * 胶板位置
                 */
                val glueBoardPosition = dataStore.readData("glueBoardPosition", 0.0)
                println("胶板位置===$glueBoardPosition")

                /**
                 * 高浓度预排液量
                 */
                val higeRehearsalVolume = dataStore.readData("higeRehearsalVolume", 0.0) * 1000
                println("高浓度预排液量===$higeRehearsalVolume")

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
                 * 促凝剂体积
                 */
                val coagulantVol = selected.coagulant
                println("促凝剂体积===" + coagulantVol)


                /**
                 * 胶液体积
                 */
                val volumeVol = selected.volume * 1000
                println("胶液体积===" + volumeVol)

                /**
                 * 促凝剂预排液量
                 */
                val coagulantRehearsal = coagulantVol / volumeVol * higeRehearsalVolume
                println("促凝剂预排液量===$coagulantRehearsal")


                _job.value?.cancel()
                _job.value = launch {
                    try {
                        cleanLight()
//===================废液槽运动开始=====================
                        println("===废液槽运动开始===")
                        //废液槽位置
                        start {
                            timeOut = 1000L * 60L
                            with(

                                index = 0,
                                pdv = wastePosition,
                                ads = Triple(300 * 100, 400 * 100, 600 * 100),
                            )
                        }
                        println("===废液槽运动结束===")
//===================废液槽运动结束=====================

                        /**
                         * 预排液
                         */
                        //===================预排液开始=====================
                        println("===预排液开始===")
                        start {
                            timeOut = 1000L * 60 * 1

                            val s = speed.toDouble()


                            //柱塞泵-促凝剂  促凝剂体积/胶液体积*higeRehearsalVolume
                            var p1 =
                                pulse(index = 1, dvp = coagulantRehearsal)
                            dataStore.saveData("finishpluse", p1 + finishpluse)
                            p1 = dataStore.readData("finishpluse", 0L)
                            println("预排液p1步数=====" + p1)

                            //高浓度
                            val p2 =
                                pulse(index = 2, dvp = higeRehearsalVolume)
                            println("预排液p2步数=====" + p2)

                            val ad = (2 * rinseSpeed * 100).toLong()


                            val s1 = (p1.toDouble() / p2 * rinseSpeed * 100).toLong()

                            println("s1=====$s1")
                            println("ad=====$ad")

                            with(index = 1, pdv = p1, ads = Triple(s1 * 2, s1 * 2, s1))

                            with(index = 2, pdv = p2, ads = Triple(ad, ad, rinseSpeed * 100))
                        }

                        //===================预排液结束=====================
                        println("===预排液结束===")


//===================制胶位置移动开始=====================
                        println("===制胶位置移动开始===")
                        //制胶位置
                        start {
                            timeOut = 1000L * 60L
//                        with(index = 0, pdv = glueBoardPosition)
                            with(
                                index = 0,
                                pdv = glueBoardPosition,
                                ads = Triple(300 * 100, 400 * 100, 600 * 100),
                            )

                        }
                        println("===制胶位置移动结束===")
                        delay(100)
                        //回复=柱塞泵总步数/160
                        println("===制胶运动开始===")
                        start {
                            timeOut = 1000L * 60 * 10
                            val s = speed.toDouble()
                            var p1 = pulse(index = 1, dvp = coagulantVol)
                            val p2 = pulse(index = 2, dvp = volumeVol)
                            val p3 = pulse(index = 3, dvp = volumeVol)

                            dataStore.saveData("finishpluse", p1 + finishpluse)
                            p1 = dataStore.readData("finishpluse", 0L)

                            println("加液p1步数=====" + p1)
                            println("加液p2步数=====" + p2)
                            println("加液p3步数=====" + p3)

                            val pv1 = (p2 + p3) / 2


                            println("index===1===加速===" + (2.5 * s * 100).toLong() + "===+减速===" + (2.5 * s * 100).toLong() + "===速度===" + ((p1 / (pv1 * 2 / s)) * 100).toLong())
                            println("index===2===加速===" + (s * 100).toLong() + "===+减速===" + (s * s / 2 / pv1 * 100).toLong() + "===速度===" + (s * 100).toLong())
                            println("index===3===加速===" + (s * s / 2 / pv1 * 100).toLong() + "===+减速===" + (s * 100).toLong() + "===速度===" + (s * 100).toLong())


                            with(
                                index = 1,
                                pdv = p1,
                                ads = Triple(
                                    ((p1 / (pv1 * 2 / s)) * 100).toLong(),
                                    ((p1 / (pv1 * 2 / s)) * 100).toLong(),
//                                (2.5 * s * 100).toLong(),
//                                (2.5 * s * 100).toLong(),
                                    ((p1 / (pv1 * 2 / s)) * 100).toLong()
                                )
                            )
                            with(
                                index = 2,
                                pdv = pv1,
                                ads = Triple(
//                                1, 1,
//                                s.toLong()
                                    (s * 100).toLong(),
                                    (s * s / 2 / pv1 * 100).toLong(),
                                    (s * 100).toLong()
                                )
                            )
                            with(
                                index = 3,
                                pdv = pv1,
                                ads = Triple(
//                                1, 1,
//                                s.toLong()
                                    (s * s / 2 / pv1 * 100).toLong(),
                                    (s * 100).toLong(),
                                    (s * 100).toLong()
                                )
                            )

                        }

                        delay(100)
                        println("===制胶运动结束===")
//===================制胶运动结束=====================


                        //制胶完成，清洗运动
                        /**
                         * 冲洗液泵清洗液量
                         */
                        val rinseCleanVolume = dataStore.readData("rinseCleanVolume", 0.0)
                        start {
                            timeOut = 1000L * 60L
                            with(
                                index = 0,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = wastePosition
                            )
                        }

                        start {
                            timeOut = 1000L * 60L
                            with(
                                index = 4,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = rinseCleanVolume * 1000
                            )
                        }

                        delay(100)
                        var complete = dataStore.readData("complete", 0)
                        dataStore.saveData("complete", complete + 1)
                        complete = dataStore.readData("complete", 0)
                        var expectedMakeNum = dataStore.readData("expectedMakenum", 1)
                        println("complete===$complete")
                        println("expectedMakeNum===$expectedMakeNum")
                        if (complete == expectedMakeNum) {
                            _uiFlags.value = UiFlags.objects(6)
                        } else {
                            lightFlashYellow()
                            _uiFlags.value = UiFlags.objects(4)
                        }
                        ApplicationUtils.ctx.playAudio(R.raw.error)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    } finally {
//                        _uiFlags.value = UiFlags.none()

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
                    timeOut = 1000L * 60L
                    with(

                        index = 0,
                        pdv = wastePosition,
                        ads = Triple(300 * 100, 400 * 100, 600 * 100),
                    )
                }

                if (p1Count > 1) {
                    for (i in 0 until Math.ceil(p1Count).toInt()) {
                        start {
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = coagulantpulse
                            )
                        }
                        start {
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = -coagulantpulse
                            )
                        }
                    }

                } else {
                    start {
                        with(
                            index = 1,
                            ads = Triple(600 * 100, 600 * 100, 600 * 100),
                            pdv = coagulantCleanVolume * 1000
                        )
                    }
                }


                start {
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
                println("coagulantFilling===$coagulantFilling")

                val p1 = pulse(index = 1, dvp = coagulantFilling)
                val coagulantpulse = dataStore.readData("coagulantpulse", 67500).toLong()
                println("coagulantpulse===$coagulantpulse")
                val p1Count = p1.toDouble() / coagulantpulse.toDouble()
                println("p1Count===$p1Count")
                println("Math.ceil===p1Count===" + Math.ceil(p1Count).toInt())

                /**
                 * 废液槽位置
                 */
                val wastePosition = dataStore.readData("wastePosition", 0.0)

                start {
                    timeOut = 1000L * 60L
                    with(

                        index = 0,
                        pdv = wastePosition,
                        ads = Triple(300 * 100, 400 * 100, 600 * 100),
                    )
                }

                if (p1Count > 1) {
                    for (i in 0 until Math.ceil(p1Count).toInt()) {
                        start {
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = coagulantpulse
                            )
                        }
                        start {
                            with(
                                index = 1,
                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                pdv = -coagulantpulse
                            )
                        }
                    }

                } else {
                    start {
                        with(
                            index = 1,
                            ads = Triple(600 * 100, 600 * 100, 600 * 100),
                            pdv = coagulantFilling * 1000
                        )
                    }
                }




                start {
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
}