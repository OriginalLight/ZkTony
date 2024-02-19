package com.zktony.android.ui

import android.util.Log
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
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.data.entities.Setting
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpd
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.cleanLight
import com.zktony.android.utils.SerialPortUtils.endStartFlashYellow
import com.zktony.android.utils.SerialPortUtils.getGpio
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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
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

    /**
     * 加液次数
     */
    private val _complate = MutableStateFlow(0)

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
     * 废液进度
     */
    private val _wasteprogress = MutableStateFlow(0f)

    /**
     * 使用的冲洗液量
     */
    private val _userinse = MutableStateFlow(0f)

    /**
     * 使用的高浓度液量
     */
    private val _usehigh = MutableStateFlow(0f)

    /**
     * 使用的低浓度液量
     */
    private val _uselow = MutableStateFlow(0f)

    /**
     * 使用的促凝剂液量
     */
    private val _usecoagulant = MutableStateFlow(0f)


    private val _first = MutableStateFlow(false)


    private var syringeJob: Job? = null

    val selected = _selected.asStateFlow()
    val selectedER = _selectedER.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val job = _job.asStateFlow()
    val complate = _complate.asStateFlow()
    val progress = _progress.asStateFlow()
    val calculate = _calculate.asStateFlow()
    val wasteprogress = _wasteprogress.asStateFlow()
    val higemother = _higemother.asStateFlow()
    val lowmother = _lowmother.asStateFlow()
    val first = _first.asStateFlow()

    val soundsThickness = dataStore.readData("soundsThickness", "蜂鸣")

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
        if (soundsThickness == "蜂鸣") {
            //开机
            ApplicationUtils.ctx.playAudio(R.raw.power_buzz)
        } else if (soundsThickness == "语音") {

        }
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


            is HomeIntent.Calculate -> calculate()
            is HomeIntent.HigeLowMotherVol -> higeLowMotherVol()

            is HomeIntent.MoveCom -> moveCom(intent.startNum)

            is HomeIntent.First -> viewModelScope.launch {
                _first.value = true
                if (soundsThickness == "语音") {
                    ApplicationUtils.ctx.playAudio(R.raw.first_voice)
                }
            }

            is HomeIntent.CleanWaste -> viewModelScope.launch {
                if (soundsThickness == "语音") {
                    ApplicationUtils.ctx.playAudio(R.raw.cleanwaste_voice)
                } else if (soundsThickness == "蜂鸣") {
                    ApplicationUtils.ctx.playAudio(R.raw.detection_buzz)
                }
            }

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

                lightFlashYellow()
                delay(100)

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
                    delay(100)
                    lightGreed()
                }
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


    //预计制胶数量=INT（MIN（（高浓度母液量-高浓度泵填充液量）/（制胶体积×制胶高浓度步数/制胶总步数+预排胶液体积×预排高浓度步数/预排总步数），（低浓度母液量-低浓度泵填充液量）/（制胶体积×制胶低浓度泵步数/制胶总步数+预排胶液体积×预排低浓度步数/预排总步数）））
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

                //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）
                //1.1   获取高低浓度的平均校准因子
                val p2jz = (AppStateUtils.hpc[2] ?: { x -> x * 100 }).invoke(1.0)
                Log.d("", "p2jz===$p2jz")
                val p3jz = (AppStateUtils.hpc[3] ?: { x -> x * 100 }).invoke(1.0)
                Log.d("", "p3jz===$p3jz")
                val highLowAvg = (p2jz + p3jz) / 2
                val number3digits: Double = String.format("%.3f", highLowAvg).toDouble()
                Log.d("", "highLowAvg===$highLowAvg")
                Log.d("", "number3digits===$number3digits")
                //1.2   胶液总步数
                val volume = selected.volume
                val volumePulseCount = volume * 1000 * highLowAvg
                Log.d("", "volumePulseCount===$volumePulseCount")
                //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）

                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60
                //制胶速度，根据这个速度转换其他泵的速度
                val speed = dataStore.readData("speed", 180)
                Log.d("", "speed===$speed")
                //制胶所需时间
                val guleTime = volumePulseCount / 51200 / speed * 60
                Log.d("", "guleTime===$guleTime")
                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60

                //04高浓度泵启动速度（rpm）=制胶速度（rpm）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）
                //母液低浓度
                val lowCoagulant = dataStore.readData("lowCoagulant", 4)
                Log.d("", "lowCoagulant===$lowCoagulant")
                //母液高浓度
                val highCoagulant = dataStore.readData("highCoagulant", 20)
                Log.d("", "highCoagulant===$highCoagulant")
                //高浓度泵启动速度
                val highStartSpeed =
                    speed * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant)
                Log.d("", "highStartSpeed===$highStartSpeed")
                //04高浓度泵启动速度（rpm）=制胶速度（rpm）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）

                //05低浓度泵结束速度（rpm）=制胶速度（rpm）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）
                val lowEndSpeed =
                    speed * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant)
                Log.d("", "lowEndSpeed===$lowEndSpeed")
                //05低浓度泵结束速度（rpm）=制胶速度（rpm）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）

                //06高浓度泵结束速度（rpm）=制胶速度-低浓度泵结束速度
                val highEndSpeed = speed - lowEndSpeed
                Log.d("", "highEndSpeed===$highEndSpeed")
                //06高浓度泵结束速度（rpm）=制胶速度-低浓度泵结束速度

                //07低浓度泵启动速度（rpm）=制胶速度-高浓度泵启动速度
                val lowStartSpeed = speed - highStartSpeed
                Log.d("", "lowStartSpeed===$lowStartSpeed")
                //07低浓度泵启动速度（rpm）=制胶速度-高浓度泵启动速度


                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                val guleHighPulse = (highStartSpeed + highEndSpeed) / 2 * guleTime / 60 * 51200
                Log.d("", "guleHighPulse===$guleHighPulse")
                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                val guleLowPulse = (lowStartSpeed + lowEndSpeed) / 2 * guleTime / 60 * 51200
                Log.d("", "guleLowPulse===$guleLowPulse")
                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数


                /**
                 * 高浓度预排液量
                 */
                val higeRehearsalVolume = setting.higeRehearsalVolume
                Log.d("", "higeRehearsalVolume===$higeRehearsalVolume")

                val highExpectedPulseCount = higeRehearsalVolume * highLowAvg
                Log.d("", "highExpectedPulseCount===$highExpectedPulseCount")
                //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）

                //16预排高浓度步数=预排总步数×高浓度泵启动速度（rpm）/制胶速度（rpm）
                val highExpectedPulse = highExpectedPulseCount * highStartSpeed / speed
                Log.d("", "highExpectedPulse===$highExpectedPulse")
                //16预排高浓度步数=预排总步数×高浓度泵启动速度（rpm）/制胶速度（rpm）

                //17预排低浓度步数=预排总步数×低浓度泵启动速度（rpm）/制胶速度（rpm）
                val lowExpectedPulse = highExpectedPulseCount * lowStartSpeed / speed
                Log.d("", "lowExpectedPulse===$lowExpectedPulse")
                //17预排低浓度步数=预排总步数×低浓度泵启动速度（rpm）/制胶速度（rpm）


                /**
                 * 高浓度母液量
                 */
                val highCoagulantVol = dataStore.readData("highCoagulantVol", 0f)
                Log.d("", "highCoagulantVol===$highCoagulantVol")

                /**
                 * 高浓度泵填充液量
                 */
                val higeFilling = setting.higeFilling
                Log.d("", "higeFilling===$higeFilling")


                /**
                 * 低浓度母液量
                 */
                val lowCoagulantVol = dataStore.readData("lowCoagulantVol", 0f)
                Log.d("", "lowCoagulantVol===$lowCoagulantVol")



                Log.d("", "==============================================")
                Log.d("", "高浓度母液量===$highCoagulantVol")
                Log.d("", "高浓度泵填充液量===$higeFilling")
                Log.d("", "高浓度母液量-高浓度泵填充液量===${highCoagulantVol - higeFilling}")
                Log.d("", "制胶体积===$volume")
                Log.d("", "制胶高浓度步数===$guleHighPulse")
                Log.d("", "制胶总步数===$volumePulseCount")
                Log.d("", "预排胶液体积===$higeRehearsalVolume")
                Log.d("", "预排高浓度步数===$highExpectedPulse")
                Log.d("", "预排总步数===$highExpectedPulseCount")
                Log.d(
                    "",
                    "计算1===${(highCoagulantVol - higeFilling) / (volume * guleHighPulse / volumePulseCount + higeRehearsalVolume * highExpectedPulse / highExpectedPulseCount)}"
                )


                Log.d("", "低浓度母液量===$lowCoagulantVol")
                Log.d("", "低浓度泵填充液量===${setting.lowFilling}")
                Log.d("", "制胶低浓度泵步数===$guleLowPulse")
                Log.d("", "预排低浓度步数===$lowExpectedPulse")
                Log.d("", "制胶低浓度泵步数===$guleLowPulse")
                Log.d("", "预排低浓度步数===$lowExpectedPulse")

                Log.d(
                    "",
                    "计算2===${(lowCoagulantVol - setting.lowFilling) / (volume * guleLowPulse / volumePulseCount + higeRehearsalVolume * lowExpectedPulse / highExpectedPulseCount)}"
                )
//预计制胶数量=INT（MIN（（高浓度母液量-高浓度泵填充液量）/（制胶体积×制胶高浓度步数/制胶总步数+预排胶液体积×预排高浓度步数/预排总步数），
// （低浓度母液量-低浓度泵填充液量）/（制胶体积×制胶低浓度泵步数/制胶总步数+预排胶液体积×预排低浓度步数/预排总步数）））

                _calculate.value = Math.min(
                    ((highCoagulantVol) / (volume * guleHighPulse / volumePulseCount + higeRehearsalVolume * highExpectedPulse / highExpectedPulseCount)),
                    ((lowCoagulantVol) / (volume * guleLowPulse / volumePulseCount + higeRehearsalVolume * lowExpectedPulse / highExpectedPulseCount))
                ).toInt()
                delay(500)
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                delay(100)
                _uiFlags.value = UiFlags.message("预计制胶数量计算失败$ex")
            }

        }
    }

    //高浓度预计所需母液体积（mL）=（制胶体积×制胶高浓度泵步数/制胶总步数+预排胶液体积×预排高浓度泵步数/预排总步数）×预计制胶数量+高浓度泵填充液量×2
    //低浓度预计所需母液体积（mL）=（制胶体积×制胶低浓度泵步数/制胶总步数+预排胶液体积×预排低浓度泵步数/预排总步数）×预计制胶数量+低浓度泵填充液量×2
    private fun higeLowMotherVol() {
        viewModelScope.launch {
            try {
                _uiFlags.value = UiFlags.objects(11)

                val selected = dao.getById(_selected.value).firstOrNull()
                Log.d("", "selected===$selected")
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
                val p2jz = (AppStateUtils.hpc[2] ?: { x -> x * 100 }).invoke(1.0)
                Log.d("", "p2jz===$p2jz")
                val p3jz = (AppStateUtils.hpc[3] ?: { x -> x * 100 }).invoke(1.0)
                Log.d("", "p3jz===$p3jz")
                val highLowAvg = (p2jz + p3jz) / 2
                val number3digits: Double = String.format("%.3f", highLowAvg).toDouble()
                Log.d("", "highLowAvg===$highLowAvg")
                Log.d("", "number3digits===$number3digits")
                //1.2   胶液总步数
                val volume = selected.volume
                val volumePulseCount = volume * 1000 * highLowAvg
                Log.d("", "volumePulseCount===$volumePulseCount")
                //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）

                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60
                //制胶速度，根据这个速度转换其他泵的速度
                val speed = dataStore.readData("speed", 180)
                Log.d("", "speed===$speed")
                //制胶所需时间
                val guleTime = volumePulseCount / 51200 / speed * 60
                Log.d("", "guleTime===$guleTime")
                //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60

                //04高浓度泵启动速度（rpm）=制胶速度（rpm）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）
                //母液低浓度
                val lowCoagulant = dataStore.readData("lowCoagulant", 4)
                Log.d("", "lowCoagulant===$lowCoagulant")
                //母液高浓度
                val highCoagulant = dataStore.readData("highCoagulant", 20)
                Log.d("", "highCoagulant===$highCoagulant")
                //高浓度泵启动速度
                val highStartSpeed =
                    speed * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant)
                Log.d("", "highStartSpeed===$highStartSpeed")
                //04高浓度泵启动速度（rpm）=制胶速度（rpm）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）

                //05低浓度泵结束速度（rpm）=制胶速度（rpm）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）
                val lowEndSpeed =
                    speed * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant)
                Log.d("", "lowEndSpeed===$lowEndSpeed")
                //05低浓度泵结束速度（rpm）=制胶速度（rpm）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）

                //06高浓度泵结束速度（rpm）=制胶速度-低浓度泵结束速度
                val highEndSpeed = speed - lowEndSpeed
                Log.d("", "highEndSpeed===$highEndSpeed")
                //06高浓度泵结束速度（rpm）=制胶速度-低浓度泵结束速度

                //07低浓度泵启动速度（rpm）=制胶速度-高浓度泵启动速度
                val lowStartSpeed = speed - highStartSpeed
                Log.d("", "lowStartSpeed===$lowStartSpeed")
                //07低浓度泵启动速度（rpm）=制胶速度-高浓度泵启动速度


                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                val guleHighPulse = (highStartSpeed + highEndSpeed) / 2 * guleTime / 60 * 51200
                Log.d("", "guleHighPulse===$guleHighPulse")
                //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
                val guleLowPulse = (lowStartSpeed + lowEndSpeed) / 2 * guleTime / 60 * 51200
                Log.d("", "guleLowPulse===$guleLowPulse")
                //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数


                /**
                 * 高浓度预排液量
                 */
                val higeRehearsalVolume = setting.higeRehearsalVolume
                Log.d("", "higeRehearsalVolume===$higeRehearsalVolume")

                val highExpectedPulseCount = higeRehearsalVolume * highLowAvg
                Log.d("", "highExpectedPulseCount===$highExpectedPulseCount")
                //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）

                //16预排高浓度步数=预排总步数×高浓度泵启动速度（rpm）/制胶速度（rpm）
                val highExpectedPulse = highExpectedPulseCount * highStartSpeed / speed
                Log.d("", "highExpectedPulse===$highExpectedPulse")
                //16预排高浓度步数=预排总步数×高浓度泵启动速度（rpm）/制胶速度（rpm）

                //17预排低浓度步数=预排总步数×低浓度泵启动速度（rpm）/制胶速度（rpm）
                val lowExpectedPulse = highExpectedPulseCount * lowStartSpeed / speed
                Log.d("", "lowExpectedPulse===$lowExpectedPulse")
                //17预排低浓度步数=预排总步数×低浓度泵启动速度（rpm）/制胶速度（rpm）


                /**
                 * 高浓度母液量
                 */
                val highCoagulantVol = dataStore.readData("highCoagulantVol", 0f)
                Log.d("", "highCoagulantVol===$highCoagulantVol")

                /**
                 * 高浓度泵填充液量
                 */
                val higeFilling = setting.higeFilling
                Log.d("", "higeFilling===$higeFilling")

                /**
                 * 低浓度泵填充液量
                 */
                val lowFilling = setting.lowFilling
                Log.d("", "lowFilling===$lowFilling")

                /**
                 * 低浓度母液量
                 */
                val lowCoagulantVol = dataStore.readData("lowCoagulantVol", 0f)
                Log.d("", "lowCoagulantVol===$lowCoagulantVol")

                val expectedMakenum = dataStore.readData("expectedMakenum", 0)

                val higeMother =
                    ((volume * guleHighPulse / volumePulseCount + higeRehearsalVolume * highExpectedPulse / highExpectedPulseCount) * expectedMakenum + higeFilling * 2).toFloat()
                Log.d("", "higeMother=========$higeMother")
                val higeNumber2digits = String.format("%.2f", higeMother).toFloat()
                Log.d("", "higeNumber2digits=========$higeNumber2digits")
                val higeSolution = String.format("%.1f", higeMother).toFloat()
                Log.d("", "higeSolution=========$higeSolution")

                val lowMother =
                    ((volume * guleLowPulse / volumePulseCount + higeRehearsalVolume * lowExpectedPulse / highExpectedPulseCount) * expectedMakenum + lowFilling * 2).toFloat()
                Log.d("", "lowMother=========$lowMother")
                val lowNumber2digits = String.format("%.2f", lowMother).toFloat()
                Log.d("", "lowNumber2digits=========$lowNumber2digits")
                val lowSolution = String.format("%.1f", lowMother).toFloat()
                Log.d("", "lowSolution=========$lowSolution")



                _higemother.value = higeSolution
                _lowmother.value = lowSolution
                Log.d("", "_higemother=========${_higemother.value}")
                Log.d("", "_lowmother=========${_lowmother.value}")

                delay(200)
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                delay(100)
                _uiFlags.value = UiFlags.message("母液量设置失败$ex")
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
//            gpio(4)
//            if (getGpio(4)) {
//                if (soundsThickness == "语音") {
//                    ApplicationUtils.ctx.playAudio(R.raw.hint_voice)
//                } else if (soundsThickness == "蜂鸣") {
//                    ApplicationUtils.ctx.playAudio(R.raw.hint_buzz)
//                }
//                _uiFlags.value = UiFlags.message("制胶架没有正确放置")
//                return@launch
//            }

            lightYellow()
            delay(100)
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
                speed * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant)
            Log.d(
                "HomeViewModel_startJob",
                "===04高浓度泵启动速度===$highStartSpeed"
            )
            //04高浓度泵启动速度（rpm）=制胶速度（rpm）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）

            //05低浓度泵结束速度（rpm）=制胶速度（rpm）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）
            val lowEndSpeed =
                speed * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant)
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
            val highExpectedSpeed = highStartSpeed * rinseSpeed / speed
            Log.d(
                "HomeViewModel_startJob",
                "===19预排高浓度泵速度===$highExpectedSpeed"
            )
            //19预排高浓度泵速度（rpm）=高浓度泵启动速度（rpm）/制胶速度（rpm）×冲洗液泵速度（rpm）

            //20预排低浓度泵速度（rpm）=低浓度泵启动速度（rpm）/制胶速度（rpm）×冲洗液泵速度（rpm）
            val lowExpectedSpeed = lowStartSpeed * rinseSpeed / speed
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

            var coagulantBool = false
            var coagulantStart = 0L

            Log.d(
                "HomeViewModel_startJob",
                "===运动次数status===$status"
            )
            if (status == 0) {
                _complate.value = 0
            } else {
                //计算柱塞泵是否够下一次运动
                /**
                 * 柱塞泵总行程
                 */
                val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                /**
                 * 已经运动的柱塞泵步数
                 */
                coagulantStart =
                    coagulantExpectedPulse.toLong() * status + coagulantPulseCount.toLong() * status
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

            /**
             * 计算废液槽
             */
            Log.d(
                "HomeViewModel_startJob",
                "===计算前的废液槽加液量===${_wasteprogress.value}"
            )

            _wasteprogress.value +=
                (setting.higeRehearsalVolume / 150).toFloat()

            Log.d(
                "HomeViewModel_startJob",
                "===计算后的废液槽加液量===${_wasteprogress.value}"
            )

            _job.value?.cancel()
            _job.value = launch {
                try {
                    if (soundsThickness == "语音") {
                        ApplicationUtils.ctx.playAudio(R.raw.startjob_voice)
                    } else if (soundsThickness == "蜂鸣") {
                        ApplicationUtils.ctx.playAudio(R.raw.start_buzz)
                    }
                    if (coagulantBool) {
                        Log.d(
                            "HomeViewModel_startJob",
                            "===柱塞泵回到下拉到底==="
                        )
                        start {
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
                    //测算使用时间
                    var startTime = Calendar.getInstance().timeInMillis
                    Log.d(
                        "HomeViewModel",
                        "===预排液开始时间===$startTime"
                    )

                    Log.d(
                        "HomeViewModel",
                        "===柱塞泵的加液步数===${coagulantExpectedPulse.toLong()}====启动速度===$expectedCoagulantSpeed===结束速度===$expectedCoagulantSpeed"
                    )


                    Log.d(
                        "HomeViewModel",
                        "===高浓度泵的加液步数===${highExpectedPulse.toLong()}====启动速度===$highExpectedSpeed===结束速度===$highExpectedSpeed"
                    )


                    Log.d(
                        "HomeViewModel",
                        "===低浓度泵的加液步数===${lowExpectedPulse.toLong()}====启动速度===$lowExpectedSpeed===结束速度===$lowExpectedSpeed"
                    )

                    start {
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
                    var endTime = Calendar.getInstance().timeInMillis
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间===$startTime"
                    )
                    //秒
                    var dTime = (endTime - startTime) / 1000
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间差===$dTime"
                    )
                    var time = (dTime / 3600.0)
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比未精确小数点===$time"
                    )
                    var number3digits = String.format("%.3f", time).toDouble()
                    var number2digits = String.format("%.2f", number3digits).toDouble()
                    var solution = String.format("%.1f", number2digits).toDouble()
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比精确小数点后1位===$solution"
                    )
                    setting.highTime += solution
                    setting.lowLife += solution
                    setting.rinseTime += solution
                    slDao.update(setting)

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

                    launch {
                        var startTime = 0
                        val guleTimeToInt = ceil(guleTime)
                        while (startTime < guleTimeToInt) {
                            delay(1000L)
                            startTime += 1
                            var pro = (startTime / guleTimeToInt).toFloat()
                            Log.d(
                                "HomeViewModel",
                                "===制胶时间取整===$guleTimeToInt===制胶进度===$pro"
                            )
//                            if (pro > 1.0f) {
//                                pro = 1f
//                            }
                            _progress.value = pro
                        }
                    }

                    startTime = Calendar.getInstance().timeInMillis
                    Log.d(
                        "HomeViewModel",
                        "===制胶开始时间===$startTime"
                    )
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


                    endTime = Calendar.getInstance().timeInMillis
                    Log.d(
                        "HomeViewModel",
                        "===制胶结束时间===$startTime"
                    )

                    //秒
                    dTime = (endTime - startTime) / 1000
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间差===$dTime"
                    )
                    time = (dTime / 3600.0)
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比未精确小数点===$time"
                    )
                    number3digits = String.format("%.3f", time).toDouble()
                    number2digits = String.format("%.2f", number3digits).toDouble()
                    solution = String.format("%.1f", number2digits).toDouble()
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比精确小数点后1位===$solution"
                    )
                    setting.highTime += solution
                    setting.lowLife += solution
                    setting.rinseTime += solution
                    slDao.update(setting)





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

//                    start {
//                        timeOut = 1000L * 60L
//                        with(
//                            index = 1,
//                            ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
//                            pdv = -(coagulantPulseCount.toLong() + coagulantExpectedPulse.toLong())
//                        )
//                    }

                    start {
                        timeOut = 1000L * 60L
                        with(
                            index = 4,
                            ads = Triple(rinseSpeed * 20, rinseSpeed * 20, rinseSpeed * 20),
                            pdv = rinseP
                        )
                    }

                    delay(100)
                    _complate.value += 1
                    val expectedMakenum = dataStore.readData("expectedMakenum", 0)
                    if (_complate.value == expectedMakenum) {
                        lightGreed()
                        delay(100)
                        //运动结束
                        if (soundsThickness == "语音") {
                            ApplicationUtils.ctx.playAudio(R.raw.startend_voice)
                        } else if (soundsThickness == "蜂鸣") {
                            ApplicationUtils.ctx.playAudio(R.raw.startend_buzz)
                        }
                        _uiFlags.value = UiFlags.objects(6)
                    } else {
                        //更换制胶架
                        endStartFlashYellow()
                        delay(100)
                        if (soundsThickness == "语音") {
                            ApplicationUtils.ctx.playAudio(R.raw.replace_voice)
                        } else if (soundsThickness == "蜂鸣") {
                            ApplicationUtils.ctx.playAudio(R.raw.hint_buzz)
                        }
                        _uiFlags.value = UiFlags.objects(4)
                    }

                } catch (ex: Exception) {
                    lightRed()
                    delay(100)
                    var experimentRecord = erDao.getById(_selectedER.value).firstOrNull()
                    if (experimentRecord != null) {
                        experimentRecord.status = EPStatus.FAULT
                        experimentRecord.detail = "系统故障"
                        HomeIntent.Update(experimentRecord)
                    }
                    delay(100)
                    errorDao.insert(ErrorRecord(detail = "制胶运动超时"))
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

            val rinseSpeed = dataStore.readData("rinseSpeed", 600L)


            val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
            val coagulantVol = selected.coagulant
            val coagulantPulseCount = coagulantVol * p1jz

            val coagulantExpectedPulse =
                setting.higeRehearsalVolume * coagulantVol / selected.volume * p1jz * 1
            _wasteprogress.value += (setting.rinseCleanVolume / 150).toFloat()


            start {
                timeOut = 1000L * 60L
                with(
                    index = 1,
                    ads = Triple(rinseSpeed * 13, rinseSpeed * 1193, rinseSpeed * 1193),
                    pdv = -(coagulantPulseCount.toLong() * startNum + coagulantExpectedPulse.toLong())
                )
            }

            start {
                timeOut = 1000L * 60L
                with(
                    index = 4,
                    ads = Triple(rinseSpeed * 20, rinseSpeed * 20, rinseSpeed * 20),
                    pdv = setting.rinseCleanVolume * 1000
                )
            }
            _uiFlags.value = UiFlags.none()
            delay(200)


        }

    }

    private fun stopJob() {
        viewModelScope.launch {
            if (soundsThickness == "语音") {
                ApplicationUtils.ctx.playAudio(R.raw.startend_voice)
            }
            _uiFlags.value = UiFlags.objects(1)
            _job.value?.cancel()
            _job.value = null
            delay(200L)
//            stop(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            stop(0, 1, 2, 3, 4)
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
                errorDao.insert(ErrorRecord(detail = "复位超时请重试"))
                _uiFlags.value = UiFlags.message("复位超时请重试")
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
                        ads = Triple(rinseSpeed * 20, rinseSpeed * 20, rinseSpeed * 20),
                        pdv = rinseCleanVolume * 1000
                    )
                }
            }
            delay(100)
            lightGreed()


        }
    }


    private fun clean() {
        viewModelScope.launch {
            if (_uiFlags.value is UiFlags.Objects && (_uiFlags.value as UiFlags.Objects).objects == 2) {
                _uiFlags.value = UiFlags.none()
                stop(1, 2, 3, 4)
            } else {
                _uiFlags.value = UiFlags.objects(2)
                if (soundsThickness == "语音") {
                    ApplicationUtils.ctx.playAudio(R.raw.cleanstart_voice)
                }

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

                    val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂泵校准因子===$p1jz"
                    )
//                    val p1 = pulse(index = 1, dvp = coagulantFilling * 1000)
                    val p1 = (coagulantCleanVolume * 1000 * p1jz).toLong()
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂泵管路填充加液步数===$p1"
                    )
                    /**
                     * 复位后预排步数
                     */
                    val coagulantResetPulse =
                        dataStore.readData("coagulantResetPulse", 1500).toLong()
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂复位后预排步数===$coagulantResetPulse"
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

                    val p1Count = p1.toDouble() / (coagulantpulse - 50000)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count"
                    )

                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count"
                    )
                    //向下取整
                    val count = floor(p1Count).toInt()
                    Log.d(
                        "HomeViewModel_pipeline",
                        "循环向下取整===$count"
                    )
                    val qyu = p1 % (coagulantpulse - 50000)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "取余===$qyu"
                    )

                    _wasteprogress.value +=
                        ((higeCleanVolume + lowCleanVolume + rinseCleanVolume + coagulantCleanVolume) / 150).toFloat()

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
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
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
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = coagulantResetPulse
                            )
                        }
                        delay(100L)

                    }


                    delay(100L)
                    var startTime = Calendar.getInstance().timeInMillis
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
                            ads = Triple(rinseSpeed * 20, rinseSpeed * 20, rinseSpeed * 20),
                            pdv = rinseCleanVolume * 1000
                        )
                    }
                    var endTime = Calendar.getInstance().timeInMillis
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间===$startTime"
                    )
                    //秒
                    val dTime = (endTime - startTime) / 1000
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间差===$dTime"
                    )
                    val time = (dTime / 3600.0)
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比未精确小数点===$time"
                    )
                    val number3digits: Double = String.format("%.3f", time).toDouble()
                    val number2digits: Double = String.format("%.2f", number3digits).toDouble()
                    val solution: Double = String.format("%.1f", number2digits).toDouble()
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比精确小数点后1位===$solution"
                    )
                    slEnetity.highTime += solution
                    slEnetity.lowLife += solution
                    slEnetity.rinseTime += solution
                    slDao.update(slEnetity)
                    delay(200)
                    if (soundsThickness == "语音") {
                        ApplicationUtils.ctx.playAudio(R.raw.cleanend_voice)
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
            println("index===$index")
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
                    val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂泵校准因子===$p1jz"
                    )
//                    val p1 = pulse(index = 1, dvp = coagulantFilling * 1000)
                    val p1 = (coagulantFilling * 1000 * p1jz).toLong()
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
                     * 复位后预排步数
                     */
                    val coagulantResetPulse =
                        dataStore.readData("coagulantResetPulse", 1500).toLong()
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂复位后预排步数===$coagulantResetPulse"
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

                    val p1Count = p1.toDouble() / (coagulantpulse - 50000)
                    Log.d(
                        "HomeViewModel_pipeline",
                        "促凝剂运动次数===$p1Count"
                    )
                    //向下取整
                    val count = floor(p1Count).toInt()
                    Log.d(
                        "HomeViewModel_pipeline",
                        "循环向下取整===$count"
                    )
                    val qyu = p1 % (coagulantpulse - 50000)
                    Log.d(
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
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
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
                                        coagulantSpeed * 13,
                                        coagulantSpeed * 1193,
                                        coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
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
                                    coagulantSpeed * 13,
                                    coagulantSpeed * 1193,
                                    coagulantSpeed * 1193
                                ),
                                pdv = coagulantResetPulse
                            )
                        }
                        delay(100L)

                    }


                    delay(100L)
                    var startTime = Calendar.getInstance().timeInMillis
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
                            ads = Triple(rinseSpeed * 20, rinseSpeed * 20, rinseSpeed * 20),
                            pdv = rinseFilling * 1000
                        )
                    }
                    var endTime = Calendar.getInstance().timeInMillis
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间===$startTime"
                    )
                    //秒
                    val dTime = (endTime - startTime) / 1000
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间差===$dTime"
                    )
                    val time = (dTime / 3600.0)
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比未精确小数点===$time"
                    )
                    val number3digits: Double = String.format("%.3f", time).toDouble()
                    val number2digits: Double = String.format("%.2f", number3digits).toDouble()
                    val solution: Double = String.format("%.1f", number2digits).toDouble()
                    Log.d(
                        "HomeViewModel",
                        "===预排液结束时间1小时的百分比精确小数点后1位===$solution"
                    )
                    slEnetity.highTime += solution
                    slEnetity.lowLife += solution
                    slEnetity.rinseTime += solution
                    slDao.update(slEnetity)
                    delay(100L)
                    if (soundsThickness == "语音") {
                        ApplicationUtils.ctx.playAudio(R.raw.pipeline_voice)
                    }
                    _uiFlags.value = UiFlags.none()
//                    reset()
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
    data class Syringe(val index: Int) : HomeIntent()
    data class Selected(val id: Long) : HomeIntent()
    data object tuopan : HomeIntent()
    data object Clean : HomeIntent()
    data object Reset : HomeIntent()
    data object deng : HomeIntent()
    data class Start(val count: Int) : HomeIntent()
    data object Stop : HomeIntent()

    data class Insert(
        val startRange: Int,
        val endRange: Int,
        val thickness: String,
        val coagulant: Int,
        val volume: Double,
        var number: Int,
        var status: String,
        var detail: String
    ) : HomeIntent()

    data class Update(val entity: ExperimentRecord) : HomeIntent()

    data object Calculate : HomeIntent()
    data object HigeLowMotherVol : HomeIntent()

    data class MoveCom(val startNum: Int) : HomeIntent()

    data object First : HomeIntent()

    data object CleanWaste : HomeIntent()
}