package com.zktony.android.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.R
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpd
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.SerialPortUtils.cleanLight
import com.zktony.android.utils.SerialPortUtils.endStartFlashYellow
import com.zktony.android.utils.SerialPortUtils.getGpio
import com.zktony.android.utils.SerialPortUtils.gpio
import com.zktony.android.utils.SerialPortUtils.lightFlashYellow
import com.zktony.android.utils.SerialPortUtils.lightRed
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
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class DebugModeViewModel @Inject constructor(
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

    fun dispatch(intent: DebugModeIntent) {
        when (intent) {
            is DebugModeIntent.Clean -> clean()
            is DebugModeIntent.NavTo -> _page.value = intent.page
            is DebugModeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is DebugModeIntent.Pipeline -> pipeline(intent.index)
            is DebugModeIntent.Reset -> reset()
            is DebugModeIntent.deng -> dengguang()
            is DebugModeIntent.tuopan -> guangdian3()
            is DebugModeIntent.Start -> startJob(intent.count)
            is DebugModeIntent.Stop -> stopJob()
            is DebugModeIntent.Syringe -> syringe(intent.index)
            is DebugModeIntent.Selected -> _selected.value = intent.id
            is DebugModeIntent.Insert -> viewModelScope.launch {
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

            is DebugModeIntent.Update -> viewModelScope.launch { erDao.update(intent.entity) }
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
                    Log.d(
                        "HomeViewModel",
                        "x轴光电状态====0号光电===" + getGpio(0) + "====1号光电===" + getGpio(1)
                    )
                    if (!getGpio(0) && !getGpio(1)) {
                        Log.d(
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
                        Log.d(
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
                        Log.d(
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

                            Log.d(
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
                                pdv = 3200L,
                                ads = Triple(1600, 1600, 1600),
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
                                    pdv = -3300L,
                                    ads = Triple(1600, 1600, 1600),

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
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + getGpio(2)
                        )
                        delay(300L)
                        if (!getGpio(2)) {

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
                        Log.d(
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
                        Log.d(
                            "HomeViewModel",
                            "注射泵光电状态====2号光电===" + getGpio(2)
                        )
                        if (getGpio(2)) {
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

            if (status == 0) {
                _complate.value = 0
            }
            /**
             * 要完成的制胶数量
             */
            val expectedMakeNum = dataStore.readData("expectedMakenum", 1)

            val xSpeed = dataStore.readData("xSpeed", 600L)

            val selectRudio = dataStore.readData("selectRudio", 1)

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

            //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）
            val guleFlow = selected.volume * 1000 / guleTime
            Log.d(
                "HomeViewModel_startJob",
                "===03A制胶总流速===$guleFlow"
            )
            //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）

            //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            //母液低浓度
            val lowCoagulant = dataStore.readData("lowCoagulant", 4)
            Log.d(
                "HomeViewModel_startJob",
                "===母液低浓度===$lowCoagulant"
            )
            //母液高浓度
            val highCoagulant = dataStore.readData("highCoagulant", 20)
            Log.d(
                "HomeViewModel_startJob",
                "===母液高浓度===$highCoagulant"
            )
            //高浓度泵启动速度
            val highStartSpeed =
                guleFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
            Log.d(
                "HomeViewModel_startJob",
                "===04高浓度泵启动速度===$highStartSpeed"
            )
            //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowEndSpeed =
                guleFlow * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant) * p3jz * 60 / 51200
            Log.d(
                "HomeViewModel_startJob",
                "===05低浓度泵结束速度===$lowEndSpeed"
            )
            //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            val highEndSpeed =
                guleFlow * (lowCoagulant - selected.startRange) / (lowCoagulant - highCoagulant) * p2jz * 60 / 51200
            Log.d(
                "HomeViewModel_startJob",
                "===06高浓度泵结束速度===$highEndSpeed"
            )
            //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowStartSpeed =
                guleFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
            Log.d(
                "HomeViewModel_startJob",
                "===07低浓度泵启动速度===$lowStartSpeed"
            )
            //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

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

            /**
             *冲洗液泵转速
             */
            val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
            Log.d(
                "HomeViewModel_startJob",
                "===冲洗液泵转速===$rinseSpeed"
            )

            //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60
            val expectedTime = highExpectedPulseCount / 51200 / rinseSpeed * 60
            Log.d(
                "HomeViewModel_startJob",
                "===16预排时间===$expectedTime"
            )
            //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60

            //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）
            val expectedFlow = higeRehearsalVolume / expectedTime
            Log.d(
                "HomeViewModel_startJob",
                "===17预排总流速===$expectedFlow"
            )
            //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）

            //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            val highExpectedSpeed =
                expectedFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
            Log.d(
                "HomeViewModel_startJob",
                "===18预排高浓度泵速度===$highExpectedSpeed"
            )
            //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowExpectedSpeed =
                expectedFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
            Log.d(
                "HomeViewModel_startJob",
                "===19预排低浓度泵速度===$lowExpectedSpeed"
            )
            //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比
            val coagulantExpectedPulse =
                setting.higeRehearsalVolume * selected.coagulant / selected.volume * p1jz * ratio
            Log.d(
                "HomeViewModel_startJob",
                "===20预排促凝剂步数===$coagulantExpectedPulse"
            )
            //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比


            //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
            val highExpectedPulse = highExpectedSpeed * expectedTime / 60 * 51200
            Log.d(
                "HomeViewModel_startJob",
                "===21预排高浓度泵步数===$highExpectedPulse"
            )
            //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

            //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
            val lowExpectedPulse = lowExpectedSpeed * expectedTime / 60 * 51200
            Log.d(
                "HomeViewModel_startJob",
                "===22预排低浓度泵步数===$lowExpectedPulse"
            )
            //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

            //23预排促凝剂泵速度=预排促凝剂泵步数/每圈步数/预排时间（s）×60×促凝剂转速比
            val coagulantExpectedSpeed = coagulantExpectedPulse / 51200 / expectedTime * 60 * ratio
            Log.d(
                "HomeViewModel_startJob",
                "===23预排促凝剂泵速度===$coagulantExpectedSpeed"
            )
            //23预排促凝剂泵速度=预排促凝剂泵总步数/每圈步数/预排时间（s）×60×促凝剂转速比




            _job.value?.cancel()
            _job.value = launch {
                try {
                    cleanLight()


//===================废液槽运动开始=====================
                    Log.d(
                        "HomeViewModel_startJob",
                        "===废液槽运动开始==="
                    )
                    //废液槽位置
                    start {
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
                    start {
                        timeOut = 1000L * 60 * 1

                        with(
                            index = 1,
                            pdv = coagulantExpectedPulse.toLong(),
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

//                    launch {
//                        var startTime = 0
//                        val guleTimeToInt = ceil(guleTime)
//                        while (startTime < guleTimeToInt) {
//                            delay(1000L)
//                            startTime += 1
//                            var pro = (startTime / guleTimeToInt).toFloat()
//                            Log.d(
//                                "HomeViewModel",
//                                "===制胶时间取整===$guleTimeToInt===制胶进度===$pro"
//                            )
//                            _progress.value = pro
//                        }
//                    }

                    start {
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
                    val rinseP = pulse(index = 4, dvp = setting.rinseCleanVolume * 1000)

                    start {
                        timeOut = 1000L * 60L
                        with(
                            index = 0,
                            ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                            pdv = setting.wastePosition
                        )
                    }

                    start {
                        timeOut = 1000L * 60L
                        with(
                            index = 1,
                            ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
                            pdv = -(coagulantPulseCount.toLong() + coagulantExpectedPulse.toLong())
                        )
                    }

                    start {
                        timeOut = 1000L * 60L
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                            pdv = rinseP
                        )
                    }

                    delay(100)
                    _complate.value += 1

                    if (_complate.value == expectedMakeNum) {
                        SerialPortUtils.lightGreed()
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
                            ApplicationUtils.ctx.playAudio(R.raw.hint_buzz)
                        }
                        _uiFlags.value = UiFlags.objects(4)
                    }


                } catch (ex: Exception) {
                    var experimentRecord = erDao.getById(_selectedER.value).firstOrNull()
                    if (experimentRecord != null) {
                        experimentRecord.status = EPStatus.FAULT
                        experimentRecord.detail = "系统故障"
                        DebugModeIntent.Update(experimentRecord)
                    }
                    ex.printStackTrace()
                } finally {

                    _job.value?.cancel()
                    _job.value = null
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


                val slEnetity = slDao.getById(1L).firstOrNull()
                if (slEnetity != null) {

                    /**
                     * 高浓度清洗液量
                     */
                    val higeCleanVolume = slEnetity.higeCleanVolume
                    Log.d(
                        "HomeViewModel_clean",
                        "高浓度清洗液量===$higeCleanVolume"
                    )
                    /**
                     * 低浓度清洗液量
                     */
                    val lowCleanVolume = slEnetity.lowCleanVolume
                    Log.d(
                        "HomeViewModel_clean",
                        "低浓度清洗液量===$lowCleanVolume"
                    )

                    /**
                     * 冲洗液泵清洗液量
                     */
                    val rinseCleanVolume = slEnetity.rinseCleanVolume
                    Log.d(
                        "HomeViewModel_clean",
                        "冲洗液泵清洗液量===$rinseCleanVolume"
                    )

                    /**
                     * 促凝剂泵清洗液量
                     */
                    val coagulantCleanVolume = slEnetity.coagulantCleanVolume
                    Log.d(
                        "HomeViewModel_clean",
                        "促凝剂泵清洗液量===$coagulantCleanVolume"
                    )

                    val p1 = pulse(index = 1, dvp = coagulantCleanVolume * 1000)
                    Log.d(
                        "HomeViewModel_clean",
                        "促凝剂泵加液步数===$p1"
                    )

                    /**
                     * 促凝剂总长度
                     */
                    val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()
                    Log.d(
                        "HomeViewModel_clean",
                        "促凝剂总长度===$coagulantpulse"
                    )

                    /**
                     * 促凝剂转速
                     */
                    val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)
                    Log.d(
                        "HomeViewModel_clean",
                        "促凝剂转速===$coagulantSpeed"
                    )

                    /**
                     * 冲洗转速
                     */
                    val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                    Log.d(
                        "HomeViewModel_clean",
                        "冲洗转速===$rinseSpeed"
                    )

                    /**
                     * x轴转速
                     */
                    val xSpeed = dataStore.readData("xSpeed", 100L)
                    Log.d(
                        "HomeViewModel_clean",
                        "x轴转速===$xSpeed"
                    )

                    val p1Count = p1.toDouble() / coagulantpulse.toDouble()
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count===促凝剂实际运行次数===${
                            Math.ceil(p1Count).toInt()
                        }"
                    )

                    /**
                     * 废液槽位置
                     */
                    val wastePosition = slEnetity.wastePosition
                    Log.d(
                        "HomeViewModel_pipeline",
                        "废液槽位置===$wastePosition"
                    )

                    start {
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
                            start {
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
                            start {
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
                        start {
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
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
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
                delay(100L)
                reset()
            } else {
                _uiFlags.value = UiFlags.objects(4 + index)

                /**
                 * 高浓度管路填充
                 */
                val slEnetity = slDao.getById(1L).firstOrNull()
                if (slEnetity != null) {
                    val higeFilling = slEnetity.higeFilling
                    Log.d(
                        "HomeViewModel_pipeline",
                        "高浓度管路填充液量===$higeFilling"
                    )

                    /**
                     * 低浓度管路填充
                     */
                    val lowFilling = slEnetity.lowFilling
                    Log.d(
                        "HomeViewModel_pipeline",
                        "低浓度管路填充液量===$lowFilling"
                    )

                    /**
                     * 冲洗液泵管路填充
                     */
                    val rinseFilling = slEnetity.rinseFilling

                    Log.d(
                        "HomeViewModel_pipeline",
                        "冲洗液泵管路填充液量===$rinseFilling"
                    )

                    /**
                     * 促凝剂泵管路填充
                     */
                    val coagulantFilling = slEnetity.coagulantFilling
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂泵管路填充液量===$coagulantFilling"
                    )

                    val p1 = pulse(index = 1, dvp = coagulantFilling * 1000)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂泵管路填充加液步数===$p1"
                    )
                    /**
                     * 促凝剂总行程
                     */
                    val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂总行程===$coagulantpulse"
                    )
                    /**
                     * 促凝剂转速
                     */
                    val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂转速===$coagulantSpeed"
                    )

                    /**
                     * 冲洗转速
                     */
                    val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "冲洗转速===$rinseSpeed"
                    )

                    /**
                     * x轴转速
                     */
                    val xSpeed = dataStore.readData("xSpeed", 100L)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "x轴转速===$xSpeed"
                    )

                    val p1Count = p1.toDouble() / (coagulantpulse - 51200).toDouble()
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count===促凝剂实际运行次数===${
                            Math.ceil(p1Count).toInt()
                        }"
                    )

                    /**
                     * 废液槽位置
                     */
                    val wastePosition = dataStore.readData("wastePosition", 0.0)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "废液槽位置===$wastePosition"
                    )

                    start {
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
                            start {
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
                            start {
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
                        start {
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

                    reset()
                }


            }
        }
    }
}

/**
 * 状态；0-运行中；1-已完成；2-中止；3-故障
 */


sealed class DebugModeIntent {
    data class NavTo(val page: Int) : DebugModeIntent()
    data class Flags(val uiFlags: UiFlags) : DebugModeIntent()
    data class Pipeline(val index: Int) : DebugModeIntent()
    data class Syringe(val index: Int) : DebugModeIntent()
    data class Selected(val id: Long) : DebugModeIntent()
    data object tuopan : DebugModeIntent()
    data object Clean : DebugModeIntent()
    data object Reset : DebugModeIntent()
    data object deng : DebugModeIntent()
    data class Start(val count: Int) : DebugModeIntent()
    data object Stop : DebugModeIntent()

    data class Insert(
        val startRange: Double,
        val endRange: Double,
        val thickness: String,
        val coagulant: Int,
        val volume: Double,
        var number: Int,
        var status: String,
        var detail: String
    ) : DebugModeIntent()

    data class Update(val entity: ExperimentRecord) : DebugModeIntent()
}