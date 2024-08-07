package com.zktony.android.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.blankj.utilcode.util.ShellUtils
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.dao.ErrorRecordDao
import com.zktony.android.data.dao.ExpectedDao
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.dao.NewCalibrationDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.dao.SportsLogDao
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.Setting
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.SerialPortUtils.stop
import com.zktony.android.utils.extra.Application
import com.zktony.android.utils.extra.DownloadState
import com.zktony.android.utils.extra.download
import com.zktony.android.utils.extra.httpCall
import com.zktony.android.utils.extra.playAudio
import com.zktony.android.utils.internal.ExceptionPolicy
import com.zktony.serialport.utils.logInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

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
    private val erDao: ExperimentRecordDao,
    private val expectedDao: ExpectedDao,
) : ViewModel() {

    private val _application = MutableStateFlow<Application?>(null)
    private val _selected = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.SETTINGS)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    private val _job = MutableStateFlow<Job?>(null)
    private val _job2 = MutableStateFlow<Job?>(null)

    private val _currentpwd = MutableStateFlow("")


    private val _updateMsg = MutableStateFlow("")

    /**
     * 加液次数
     */
    private val _complate = MutableStateFlow(0)


    //导入数据更新
    private val _speed = MutableStateFlow(0)
    private val _rinseSpeed = MutableStateFlow(0L)
    private val _xSpeed = MutableStateFlow(0L)
    private val _coagulantSpeed = MutableStateFlow(0L)
    private val _coagulantpulse = MutableStateFlow(0)
    private val _coagulantTime = MutableStateFlow(0)
    private val _coagulantResetPulse = MutableStateFlow(0)

    /**
     * 柱塞泵加液计数
     */
    private val _stautsNum = MutableStateFlow(1)

    private val coagulantStart = MutableStateFlow(0L)

    val speedFlow = _speed.asStateFlow()
    val rinseSpeedFlow = _rinseSpeed.asStateFlow()
    val xSpeedFlow = _xSpeed.asStateFlow()
    val coagulantSpeedFlow = _coagulantSpeed.asStateFlow()
    val coagulantpulseFlow = _coagulantpulse.asStateFlow()
    val coagulantTimeFlow = _coagulantTime.asStateFlow()
    val coagulantResetPulseFlow = _coagulantResetPulse.asStateFlow()

    val application = _application.asStateFlow()
    val selected = _selected.asStateFlow()
    val progress = _progress.asStateFlow()
    val page = _page.asStateFlow()
    val currentpwd = _currentpwd.asStateFlow()
    val updateMsg = _updateMsg.asStateFlow()

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


    val expected = expectedDao.getById(1L)

    val slEntitiy = slDao.getById(1L)

    val ncEntitiy = ncDao.getById(1L)
    val job = _job.asStateFlow()


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
                    ApplicationUtils.ctx.playAudio(R.raw.setting_voice)
                }


            }

            is SettingIntent.ClearAll -> viewModelScope.launch {
                proDao.deleteByAll()
                errorDao.deleteByAll()
                erDao.deleteByAll()

                var txtList =
                    File("sdcard/Download").listFiles { _, name -> name.endsWith(".txt") }?.toList()
                        ?: emptyList()

                txtList.forEach {
                    it.delete()
                }


            }

            is SettingIntent.Login -> viewModelScope.launch {
                _currentpwd.value = intent.pwd
            }

            is SettingIntent.ImportData -> importData(intent.filePath)

            is SettingIntent.UpdateApkU -> updateApkU(intent.context, intent.apkPath)

            is SettingIntent.XStart -> viewModelScope.launch {

                val setting = slEntitiy.firstOrNull()
                if (setting != null) {
                    val xSpeed = dataStore.readData("xSpeed", 100L)

                    _job2.value?.cancel()
                    _job2.value = launch {
                        try {
                            for (i in 1..intent.xNum) {
                                start {
                                    timeOut = 1000L * 30
                                    with(
                                        index = 0,
                                        pdv = setting.wastePosition,
                                        ads = Triple(
                                            xSpeed * 20,
                                            xSpeed * 20,
                                            xSpeed * 20
                                        ),
                                    )
                                }
                                delay(100)
                                start {
                                    timeOut = 1000L * 30
                                    with(
                                        index = 0,
                                        pdv = setting.glueBoardPosition,
                                        ads = Triple(
                                            xSpeed * 20,
                                            xSpeed * 20,
                                            xSpeed * 20
                                        ),
                                    )
                                }
                                delay(100)
                            }
                        } catch (e: Exception) {
                        } finally {
                            _job2.value?.cancel()
                            _job2.value = null
                        }
                    }

                }
            }

            is SettingIntent.XStop -> viewModelScope.launch {
                _job2.value?.cancel()
                _job2.value = null
                delay(200L)
                stop(0, 1, 2, 3, 4)
            }

            is SettingIntent.exit -> viewModelScope.launch {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    // Set the extra preferences to show the button bar and custom text
                    putExtra("extra_prefs_show_button_bar", true)
                    putExtra(
                        "extra_prefs_set_next_text",
                        ApplicationUtils.ctx.getString(R.string.finish)
                    )
                    putExtra(
                        "extra_prefs_set_back_text",
                        ApplicationUtils.ctx.getString(R.string.cancel)
                    )
                }
                // Launch the Wi-Fi settings screen
                ApplicationUtils.ctx.startActivity(intent)
            }

            is SettingIntent.CopyFileToUSB -> copyFileToUSB(intent.usbPath)

            is SettingIntent.FillCoagulant -> viewModelScope.launch {
                val coagulantFill = intent.coagulantVol

                /**
                 * 促凝剂总行程
                 */
                val coagulantpulse = dataStore.readData("coagulantpulse", 550000).toLong()

                /**
                 * 复位后预排步数
                 */
                val coagulantResetPulse =
                    dataStore.readData("coagulantResetPulse", 1500).toLong()


                /**
                 * 促凝剂转速
                 */
                val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)


                val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                var p1 = (coagulantFill * 1000 * p1jz).toLong()


                val p1Count = p1.toDouble() / (coagulantpulse - 50000)

                //向下取整
                val count = floor(p1Count).toInt()

                val qyu = p1 % (coagulantpulse - 50000)


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

                val rinseSpeed = dataStore.readData("rinseSpeed", 200L)

                val setting = slEntitiy.firstOrNull()
                if (setting != null) {
                    start {
                        timeOut = 1000L * 30
                        with(
                            index = 4,
                            ads = Triple(
                                rinseSpeed * 30,
                                rinseSpeed * 30,
                                rinseSpeed * 30
                            ),
                            pdv = setting.rinseCleanVolume * 1000
                        )
                    }
                }


            }

        }
    }

    private fun copyFileToUSB(usbPath: String) {

        val targetDir = File(usbPath)

        var files =
            File("sdcard/Download").listFiles { _, name -> name.endsWith(".txt") }?.toList()
                ?: emptyList()

        files?.forEach { file ->
            val targetFile = File(targetDir, file.name)
            try {
                file.copyTo(targetFile, overwrite = true)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


    }


    private fun updateApkU(context: Context, apkPath: String) {
        viewModelScope.launch {
            try {
                //获取未安装apk的版本
                val versionCode = getApkCode(context, apkPath)
                var currentCode = BuildConfig.VERSION_CODE
                if (versionCode > currentCode) {
                    _updateMsg.value = ""
                    val apkFile = File(apkPath)
                    ApplicationUtils.installApp(apkFile)
                } else {
                    _updateMsg.value = "更新文件版本过低！"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _updateMsg.value = "更新数据错误！"
                return@launch
            } finally {
                _uiFlags.value = UiFlags.none()
            }
        }

    }


    fun getApkCode(context: Context, apkFilePath: String): Int {
        val info =
            context.packageManager.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES)
        if (info != null) {
            //当前使用的android7系统，android9之后使用longVersionCode
            return info.versionCode
        }
        return -1
    }

    fun getApkVersionInfo(apkFilePath: String): Pair<Int, String> {
        var versionCode = 0
        var versionName = ""

        try {
            // 打开APK文件作为ZipFile
            val zipFile = ZipFile(apkFilePath)
            // 获取APK中的AndroidManifest.xml文件
            val manifestEntry = zipFile.getEntry("AndroidManifest.xml")
            if (manifestEntry != null) {
                // 读取AndroidManifest.xml文件内容
                val inputStream = zipFile.getInputStream(manifestEntry)
                val xmlPullParserFactory = XmlPullParserFactory.newInstance()
                val xmlPullParser = xmlPullParserFactory.newPullParser()
                xmlPullParser.setInput(inputStream, null)

                // 解析XML文件，查找versionCode和versionName
                var eventType = xmlPullParser.getEventType()
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xmlPullParser.getName() == "manifest") {
                            val versionCodeAttr =
                                xmlPullParser.getAttributeValue(null, "versionCode")
                            val versionNameAttr =
                                xmlPullParser.getAttributeValue(null, "versionName")
                            if (versionCodeAttr != null) {
                                versionCode = versionCodeAttr.toInt()
                            }
                            if (versionNameAttr != null) {
                                versionName = versionNameAttr
                            }
                        }
                    }
                    eventType = xmlPullParser.next()
                }
                inputStream.close()
            }
            zipFile.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(versionCode, versionName)
    }

    private fun importData(filePath: String) {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(11)
            try {
                //新建
                var speChat =
                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"

                val pro1 = proDao.getById(1L).firstOrNull()
                var proBool = false
                if (pro1 == null) {
                    proBool = true
                }

                var num = 0

                File(filePath).bufferedReader().useLines { lines ->
                    for (line in lines) {
                        if (line.isNotEmpty()) {
                            var textList = ArrayList<String>()
                            var contents = line.split(",")
                            contents.forEach {
                                val byte = it.split(":").get(1)
                                textList.add(byte)
                            }
                            if (num < 3) {
                                if (proBool) {
                                    programInsert(
                                        (num + 1).toLong(),
                                        textList[0],
                                        textList[1].toDouble(),
                                        textList[2].toDouble(),
                                        textList[3],
                                        textList[4].toInt(),
                                        textList[5].toDouble(),
                                        textList[6]
                                    )
                                } else {
                                    var progrm = proDao.getById((num + 1).toLong()).firstOrNull()
                                    if (progrm != null) {
                                        progrm.displayText = textList[0]
                                        progrm.startRange = textList[1].toDouble()
                                        progrm.endRange = textList[2].toDouble()
                                        progrm.thickness = textList[3]
                                        progrm.coagulant = textList[4].toInt()
                                        progrm.volume = textList[5].toDouble()
                                        progrm.founder = textList[6]
                                        programUpdate(progrm)
                                    }
                                }
                            } else if (num == 3) {
                                val setting = slEntitiy.firstOrNull()
                                if (setting != null) {
                                    setting.higeCleanVolume =
                                        textList[0].toDouble()
                                    setting.higeRehearsalVolume =
                                        textList[1].toDouble()
                                    setting.higeFilling = textList[2].toDouble()
                                    setting.lowCleanVolume =
                                        textList[3].toDouble()
                                    setting.lowFilling = textList[4].toDouble()
                                    setting.rinseCleanVolume =
                                        textList[5].toDouble()
                                    setting.rinseFilling = textList[6].toDouble()
                                    setting.coagulantCleanVolume =
                                        textList[7].toDouble()
                                    setting.coagulantFilling =
                                        textList[8].toDouble()
                                    setting.wastePosition =
                                        textList[9].toDouble()
                                    setting.glueBoardPosition =
                                        textList[10].toDouble()
                                    setting.coagulantRinse =
                                        textList[11].toDouble()

                                    slDao.update(setting)

                                    val expectedEntity = expected.firstOrNull()

                                    if (expectedEntity != null) {
                                        expectedEntity.higeCleanDefault = setting.higeCleanVolume
                                        expectedEntity.higeRehearsalDefault =
                                            setting.higeRehearsalVolume
                                        expectedEntity.higeFillingDefault = setting.higeFilling
                                        expectedEntity.lowCleanDefault = setting.lowCleanVolume
                                        expectedEntity.lowFillingDefault = setting.lowFilling
                                        expectedEntity.rinseCleanDefault = setting.rinseCleanVolume
                                        expectedEntity.rinseFillingDefault = setting.rinseFilling
                                        expectedEntity.coagulantCleanDefault =
                                            setting.coagulantCleanVolume
                                        expectedEntity.coagulantFillingDefault =
                                            setting.coagulantFilling
                                        expectedEntity.coagulantRinseDefault =
                                            setting.coagulantRinse
                                        expectedDao.update(expectedEntity)
                                    }

                                }

                            } else if (num == 4) {
                                val newCalibration = ncEntitiy.firstOrNull()
                                if (newCalibration != null) {
                                    newCalibration.higeLiquidVolume1 = textList[0].toDouble()
                                    newCalibration.higeLiquidVolume2 = textList[1].toDouble()
                                    newCalibration.higeLiquidVolume3 = textList[2].toDouble()
                                    newCalibration.higeAvg =
                                        (textList[0].toDouble() + textList[1].toDouble() + textList[2].toDouble()) / 3

                                    newCalibration.lowLiquidVolume1 = textList[3].toDouble()
                                    newCalibration.lowLiquidVolume2 = textList[4].toDouble()
                                    newCalibration.lowLiquidVolume3 = textList[5].toDouble()
                                    newCalibration.lowAvg =
                                        (textList[4].toDouble() + textList[5].toDouble() + textList[6].toDouble()) / 3

                                    newCalibration.rinseLiquidVolume1 = textList[6].toDouble()
                                    newCalibration.rinseLiquidVolume2 = textList[7].toDouble()
                                    newCalibration.rinseLiquidVolume3 = textList[8].toDouble()
                                    newCalibration.rinseAvg =
                                        (textList[7].toDouble() + textList[8].toDouble() + textList[9].toDouble()) / 3

                                    newCalibration.coagulantLiquidVolume1 = textList[9].toDouble()
                                    newCalibration.coagulantLiquidVolume2 = textList[10].toDouble()
                                    newCalibration.coagulantLiquidVolume3 = textList[11].toDouble()
                                    newCalibration.coagulantAvg =
                                        (textList[9].toDouble() + textList[10].toDouble() + textList[11].toDouble()) / 3

                                    ncDao.update(newCalibration)
                                }
                            } else {
                                _speed.value = textList[0].toInt()
                                _rinseSpeed.value = textList[1].toLong()
                                _xSpeed.value = textList[2].toLong()
                                _coagulantSpeed.value = textList[3].toLong()
                                _coagulantpulse.value = textList[4].toInt()
                                _coagulantTime.value = textList[5].toInt()
                                _coagulantResetPulse.value = textList[6].toInt()
                            }
                        }
                        num++
                    }
                }
                delay(500)
                _uiFlags.value = UiFlags.message("导入完成！")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiFlags.value = UiFlags.message("数据错误！")
                _uiFlags.value = UiFlags.none()
                return@launch
            } finally {
                _uiFlags.value = UiFlags.none()
            }
        }


    }

    private fun programUpdate(entity: Program) {
        viewModelScope.launch {
            proDao.update(entity)
        }
    }


    private fun programInsert(
        id: Long,
        displayText: String,
        startRange: Double,
        endRange: Double,
        thickness: String,
        coagulant: Int,
        volume: Double,
        founder: String
    ) {
        viewModelScope.launch {
            proDao.insert(
                Program(
                    id = id,
                    displayText = displayText,
                    startRange = startRange,
                    endRange = endRange,
                    thickness = thickness,
                    coagulant = coagulant,
                    volume = volume,
                    founder = founder
                )
            )
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
                 * 复位后预排步数
                 */
                val coagulantResetPulse =
                    dataStore.readData("coagulantResetPulse", 1500).toLong()

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
                //向下取整
                val count = floor(p1Count).toInt()

                val qyu = p1 % (coagulantpulse - 50000)

                /**
                 * 废液槽位置
                 */
                val wastePosition = dataStore.readData("wastePosition", 0.0)

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
                     * 复位后预排步数
                     */
                    val coagulantResetPulse =
                        dataStore.readData("coagulantResetPulse", 1500).toLong()

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

                    //向下取整
                    val count = floor(p1Count).toInt()
                    val qyu = p1 % (coagulantpulse - 50000)

                    /**
                     * 废液槽位置
                     */
                    val wastePosition = slEnetity.wastePosition

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
                    logInfo(
                        "setting_xreset",
                        "x轴光电状态====0号光电===" + SerialPortUtils.getGpio(0) + "====1号光电===" + SerialPortUtils.getGpio(
                            1
                        )
                    )
                    if (!SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        logInfo(
                            "setting_xreset",
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
                            "setting_xreset",
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
                            "setting_xreset",
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
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {

                            logInfo(
                                "setting_xreset",
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
                                "setting_xreset",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_xreset",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (!SerialPortUtils.getGpio(0) && SerialPortUtils.getGpio(1)) {
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
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "setting_xreset",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_xreset",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        start {
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
                            logInfo(
                                "setting_xreset",
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
                            logInfo(
                                "setting_xreset",
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
                            "setting_xreset",
                            "复位失败"
                        )
                    }
                    //x轴复位===========================================


                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    SerialPortUtils.gpio(2)
                    delay(500L)
                    logInfo(
                        "setting_xreset",
                        "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                    )
                    if (!SerialPortUtils.getGpio(2)) {
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
                        SerialPortUtils.gpio(2)
                        delay(1500L)
                        logInfo(
                            "setting_xreset",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        delay(300L)
                        if (!SerialPortUtils.getGpio(2)) {

                            delay(300L)
                            logInfo(
                                "setting_xreset",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_xreset",
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
                        logInfo(
                            "setting_xreset",
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
                        SerialPortUtils.gpio(2)
                        delay(500L)
                        logInfo(
                            "setting_xreset",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        if (SerialPortUtils.getGpio(2)) {
                            logInfo(
                                "setting_xreset",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        } else {
                            logInfo(
                                "setting_xreset",
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
                    logInfo(
                        "setting_xreset",
                        "x轴光电状态====0号光电===" + SerialPortUtils.getGpio(0) + "====1号光电===" + SerialPortUtils.getGpio(
                            1
                        )
                    )
                    if (!SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        logInfo(
                            "setting_xreset",
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
                            "setting_xreset",
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
                            "setting_xreset",
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
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {

                            logInfo(
                                "setting_xreset",
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
                                "setting_xreset",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_xreset",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (!SerialPortUtils.getGpio(0) && SerialPortUtils.getGpio(1)) {
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
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "setting_xreset",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_xreset",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        start {
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
                            logInfo(
                                "setting_xreset",
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
                            logInfo(
                                "setting_xreset",
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
                            "setting_xreset",
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
                    logInfo(
                        "setting_zsreset",
                        "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                    )
                    if (!SerialPortUtils.getGpio(2)) {
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
                        SerialPortUtils.gpio(2)
                        delay(1500L)
                        logInfo(
                            "setting_zsreset",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        delay(300L)
                        if (!SerialPortUtils.getGpio(2)) {

                            delay(300L)
                            logInfo(
                                "setting_zsreset",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_zsreset",
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
                        logInfo(
                            "setting_zsreset",
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
                        SerialPortUtils.gpio(2)
                        delay(500L)
                        logInfo(
                            "setting_zsreset",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        if (SerialPortUtils.getGpio(2)) {
                            logInfo(
                                "setting_zsreset",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        } else {
                            logInfo(
                                "setting_zsreset",
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
            _uiFlags.value = UiFlags.objects(1)
            _job.value?.cancel()
            _job.value = null
            delay(200L)
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
                    logInfo(
                        "setting_stopJob",
                        "x轴光电状态====0号光电===" + SerialPortUtils.getGpio(0) + "====1号光电===" + SerialPortUtils.getGpio(
                            1
                        )
                    )
                    if (!SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        logInfo(
                            "setting_stopJob",
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
                            "setting_stopJob",
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
                            "setting_stopJob",
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
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {

                            logInfo(
                                "setting_stopJob",
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
                                "setting_stopJob",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_stopJob",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (!SerialPortUtils.getGpio(0) && SerialPortUtils.getGpio(1)) {
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
                        SerialPortUtils.gpio(0)
                        delay(500L)
                        if (SerialPortUtils.getGpio(0)) {
                            start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = 1600L,
                                    ads = Triple(1600, 1600, 1600),

                                    )
                            }
                            logInfo(
                                "setting_stopJob",
                                "复位完成"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_stopJob",
                                "复位失败"
                            )
                            //复位失败
                        }

                    } else if (SerialPortUtils.getGpio(0) && !SerialPortUtils.getGpio(1)) {
                        start {
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
                            logInfo(
                                "setting_stopJob",
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
                            logInfo(
                                "setting_stopJob",
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
                            "setting_stopJob",
                            "复位失败"
                        )
                    }
                    //x轴复位===========================================


                    // 查询GPIO状态
                    //柱塞泵复位===========================================
                    SerialPortUtils.gpio(2)
                    delay(500L)
                    logInfo(
                        "setting_stopJob",
                        "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                    )
                    if (!SerialPortUtils.getGpio(2)) {
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
                        SerialPortUtils.gpio(2)
                        delay(1500L)
                        logInfo(
                            "setting_stopJob",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        delay(300L)
                        if (!SerialPortUtils.getGpio(2)) {

                            delay(300L)
                            logInfo(
                                "setting_stopJob",
                                "柱塞泵复位成功"
                            )
                            //复位完成
                        } else {
                            logInfo(
                                "setting_stopJob",
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
                        logInfo(
                            "setting_stopJob",
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
                        SerialPortUtils.gpio(2)
                        delay(500L)
                        logInfo(
                            "setting_stopJob",
                            "注射泵光电状态====2号光电===" + SerialPortUtils.getGpio(2)
                        )
                        if (SerialPortUtils.getGpio(2)) {
                            logInfo(
                                "setting_stopJob",
                                "柱塞泵复位失败"
                            )
                            //复位失败
                        } else {
                            logInfo(
                                "setting_stopJob",
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
                start {
                    timeOut = 1000L * 60L * 10
                    with(
                        index = 4,
                        ads = Triple(rinseSpeed * 30, rinseSpeed * 30, rinseSpeed * 30),
                        pdv = rinseCleanVolume * 1000
                    )
                }
            }
        }
    }

    private fun startJob(status: Int) {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.objects(0)
            val selected = proDao.getById(_selected.value).firstOrNull()
            if (selected == null) {
                _uiFlags.value = UiFlags.message("未选择程序")
                return@launch
            }


            val setting = slDao.getById(1L).firstOrNull()
            if (setting == null) {
                _uiFlags.value = UiFlags.message("系统参数无数据")
                return@launch
            }


            val xSpeed = dataStore.readData("xSpeed", 600L)



            logInfo(
                "setting_startJob",
                "===制胶前期准备数据开始==="
            )
            //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）
            //1.1   获取高低浓度的平均校准因子
            val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
            val p2jz = (AppStateUtils.hpc[2] ?: { x -> x * 100 }).invoke(1.0)
            val p3jz = (AppStateUtils.hpc[3] ?: { x -> x * 100 }).invoke(1.0)
            val highLowAvg = (p2jz + p3jz) / 2
            logInfo(
                "setting_startJob",
                "===获取高低浓度的平均校准因子===$highLowAvg"
            )
            //1.2   胶液总步数
            val volumePulseCount = selected.volume * 1000 * highLowAvg
            logInfo(
                "setting_startJob",
                "===01胶液总步数===$volumePulseCount"
            )
            //01胶液总步数=制胶体积（mL）×1000×高低浓度平均校准因子（步/μL）

            //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）
            val coagulantVol = selected.coagulant
            logInfo(
                "setting_startJob",
                "===促凝剂加液量===$coagulantVol"
            )
            //促凝剂总步数
            val coagulantPulseCount = coagulantVol * p1jz
            logInfo(
                "setting_startJob",
                "===02促凝剂总步数===$coagulantPulseCount"
            )
            //02促凝剂总步数=促凝剂体积（μL）×校准数据（步/μL）

            //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60
            //制胶速度，根据这个速度转换其他泵的速度
            val speed = dataStore.readData("speed", 180)
            logInfo(
                "setting_startJob",
                "===制胶速度===$speed"
            )
            //制胶所需时间
            val guleTime = volumePulseCount / 51200 / speed * 60
            logInfo(
                "setting_startJob",
                "===03制胶所需时间===$guleTime"
            )
            //03制胶所需时间（s）=制胶总步数/每圈脉冲数/制胶速度（rpm）×60

            //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）
            val guleFlow = selected.volume * 1000 / guleTime
            logInfo(
                "setting_startJob",
                "===03A制胶总流速===$guleFlow"
            )
            //03A制胶总流速（μL/s）=制胶体积（mL）×1000/制胶所需时间（s）

            //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            //母液低浓度
            val lowCoagulant = dataStore.readData("lowCoagulant", 4.0)
            logInfo(
                "setting_startJob",
                "===母液低浓度===$lowCoagulant"
            )
            //母液高浓度
            val highCoagulant = dataStore.readData("highCoagulant", 20.0)
            logInfo(
                "setting_startJob",
                "===母液高浓度===$highCoagulant"
            )
            //高浓度泵启动速度
            val highStartSpeed =
                guleFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
            logInfo(
                "setting_startJob",
                "===04高浓度泵启动速度===$highStartSpeed"
            )
            //04高浓度泵启动速度（rpm）=制胶总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowEndSpeed =
                guleFlow * (selected.startRange - highCoagulant) / (lowCoagulant - highCoagulant) * p3jz * 60 / 51200
            logInfo(
                "setting_startJob",
                "===05低浓度泵结束速度===$lowEndSpeed"
            )
            //05低浓度泵结束速度（rpm）=制胶总流速（μL/s）×（制胶低浓度-母液高浓度）/（母液低浓度-母液高浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            val highEndSpeed =
                guleFlow * (lowCoagulant - selected.startRange) / (lowCoagulant - highCoagulant) * p2jz * 60 / 51200
            logInfo(
                "setting_startJob",
                "===06高浓度泵结束速度===$highEndSpeed"
            )
            //06高浓度泵结束速度（rpm）=制胶总流速（μL/s）×（母液低浓度-制胶低浓度）/（母液低浓度-母液高浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowStartSpeed =
                guleFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
            logInfo(
                "setting_startJob",
                "===07低浓度泵启动速度===$lowStartSpeed"
            )
            //07低浓度泵启动速度（rpm）=制胶总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比
            //促凝剂变速比-默认1
            val ratio = 1
            logInfo(
                "setting_startJob",
                "===促凝剂变速比===$ratio"
            )
            //促凝剂泵启动速度
            val coagulantStartSpeed = coagulantPulseCount / 51200 / guleTime * 60 * ratio
            logInfo(
                "setting_startJob",
                "===08促凝剂泵启动速度===$coagulantStartSpeed"
            )
            //08促凝剂泵启动速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×促凝剂变速比

            //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）
            val coagulantEndSpeed = coagulantPulseCount / 51200 / guleTime * 60 * (2 - ratio)
            logInfo(
                "setting_startJob",
                "===09促凝剂泵结束速度===$coagulantEndSpeed"
            )
            //09促凝剂泵结束速度（rpm）=促凝剂泵总步数/每圈脉冲数/制胶所需时间（s）×60×（2-促凝剂变速比）

            //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
            logInfo(
                "setting_startJob",
                "===高浓度泵启动速度===$highStartSpeed====高浓度泵结束速度===$highEndSpeed===制胶所需时间===$guleTime==="
            )
            val guleHighPulse = (highStartSpeed + highEndSpeed) / 2 * guleTime / 60 * 51200
            logInfo(
                "setting_startJob",
                "===10制胶高浓度泵步数===$guleHighPulse"
            )
            //10制胶高浓度泵步数=（高浓度泵启动速度（rpm）+高浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

            //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数
            val guleLowPulse = (lowStartSpeed + lowEndSpeed) / 2 * guleTime / 60 * 51200
            logInfo(
                "setting_startJob",
                "===11制胶低浓度泵步数===$guleLowPulse"
            )
            //11制胶低浓度泵步数=（低浓度泵启动速度（rpm）+低浓度泵结束速度（rpm））/2×制胶所需时间（s）/60×每圈脉冲数

            //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））
            val highAcc = abs(highStartSpeed - highEndSpeed) / guleTime
            logInfo(
                "setting_startJob",
                "===12高浓度泵加速度===$highAcc"
            )
            //12高浓度泵加速度（rpm/s）=ABS（（高浓度泵启动速度（rpm）-高浓度泵结束速度（rpm））/制胶所需时间（s））

            //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)
            val lowAcc = abs(lowStartSpeed - lowEndSpeed) / guleTime
            logInfo(
                "setting_startJob",
                "===13低浓度泵加速度===$lowAcc"
            )
            //13低浓度泵加速度（rpm/s）=ABS（（低浓度泵启动速度（rpm）-低浓度泵结束速度（rpm））/制胶所需时间（s）)

            //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）
            val coagulantAcc = abs(coagulantStartSpeed - coagulantEndSpeed) / guleTime
            logInfo(
                "setting_startJob",
                "===14促凝剂泵加速度===$coagulantAcc"
            )
            //14促凝剂泵加速度（rpm/s）=ABS（促凝剂泵启动速度（rpm）-促凝剂泵结束速度（rpm））/制胶所需时间（s）

            logInfo(
                "setting_startJob",
                "===制胶前期准备数据结束==="
            )

            logInfo(
                "setting_startJob",
                "===预排前期准备数据开始==="
            )

            //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）
            /**
             * 高浓度预排液量
             */
            val higeRehearsalVolume = setting.higeRehearsalVolume * 1000
            logInfo(
                "setting_startJob",
                "===高浓度预排液量===$higeRehearsalVolume"
            )

            val highExpectedPulseCount = higeRehearsalVolume * highLowAvg
            logInfo(
                "setting_startJob",
                "===15预排总步数===$highExpectedPulseCount"
            )
            //15预排总步数=预排胶液体积（mL）×1000×平均校准数据（步/μL）

            /**
             *冲洗液泵转速
             */
            val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
            logInfo(
                "setting_startJob",
                "===冲洗液泵转速===$rinseSpeed"
            )

            //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60
            val expectedTime = highExpectedPulseCount / 51200 / rinseSpeed * 60
            logInfo(
                "setting_startJob",
                "===16预排时间===$expectedTime"
            )
            //16预排时间=预排总步数/每圈步数/冲洗液泵速度（rpm）×60

            //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）
            val expectedFlow = higeRehearsalVolume / expectedTime
            logInfo(
                "setting_startJob",
                "===17预排总流速===$expectedFlow"
            )
            //17预排总流速（μL/s）=高浓度泵预排液量（mL）×1000/预排时间（s）

            //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数
            val highExpectedSpeed =
                expectedFlow * (selected.endRange - lowCoagulant) / (highCoagulant - lowCoagulant) * p2jz * 60 / 51200
            logInfo(
                "setting_startJob",
                "===18预排高浓度泵速度===$highExpectedSpeed"
            )
            //18预排高浓度泵速度（rpm）=预排总流速（μL/s）×（制胶高浓度-母液低浓度）/（母液高浓度-母液低浓度）×高浓度泵校准数据（步/μL）*60/每圈脉冲数

            //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数
            val lowExpectedSpeed =
                expectedFlow * (highCoagulant - selected.endRange) / (highCoagulant - lowCoagulant) * p3jz * 60 / 51200
            logInfo(
                "setting_startJob",
                "===19预排低浓度泵速度===$lowExpectedSpeed"
            )
            //19预排低浓度泵速度（rpm）=预排总流速（μL/s）×（母液高浓度-制胶高浓度）/（母液高浓度-母液低浓度）×低浓度泵校准数据（步/μL）*60/每圈脉冲数

            //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比
            val coagulantExpectedPulse =
                setting.higeRehearsalVolume * selected.coagulant / selected.volume * p1jz * ratio
            logInfo(
                "setting_startJob",
                "===20预排促凝剂步数===$coagulantExpectedPulse"
            )
            //20预排促凝剂步数=预排胶液体积（mL）×促凝剂体积（μL）/制胶体积（mL）×校准数据（步/μL）×促凝剂变速比


            //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
            val highExpectedPulse = highExpectedSpeed * expectedTime / 60 * 51200
            logInfo(
                "setting_startJob",
                "===21预排高浓度泵步数===$highExpectedPulse"
            )
            //21预排高浓度泵步数=预排高浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

            //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数
            val lowExpectedPulse = lowExpectedSpeed * expectedTime / 60 * 51200
            logInfo(
                "setting_startJob",
                "===22预排低浓度泵步数===$lowExpectedPulse"
            )
            //22预排低浓度泵步数=预排低浓度泵速度（rpm）*预排时间（s）/60×每圈脉冲数

            //23预排促凝剂泵速度=预排促凝剂泵步数/每圈步数/预排时间（s）×60×促凝剂转速比
            val coagulantExpectedSpeed = coagulantExpectedPulse / 51200 / expectedTime * 60 * ratio
            logInfo(
                "setting_startJob",
                "===23预排促凝剂泵速度===$coagulantExpectedSpeed"
            )
            //23预排促凝剂泵速度=预排促凝剂泵总步数/每圈步数/预排时间（s）×60×促凝剂转速比


            logInfo(
                "setting_startJob",
                "===预排前期准备数据结束==="
            )


            _job.value?.cancel()
            _job.value = launch {
                try {
                    for (i in 1..100) {
                        logInfo(
                            "setting_startJob",
                            "第$i" + "老化运动开始"
                        )
                        var coagulantBool = false

                        if (i == 1) {
                            _stautsNum.value = 1
                            _complate.value = 0
                            coagulantStart.value =
                                (coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong()) * _stautsNum.value
                        } else {
                            //计算柱塞泵是否够下一次运动
                            /**
                             * 柱塞泵总行程
                             */
                            val coagulantpulse =
                                dataStore.readData("coagulantpulse", 550000).toLong()

                            /**
                             * 柱塞泵剩余步数
                             */
                            val coagulantSy = coagulantpulse - coagulantStart.value
                            logInfo(
                                "setting_startJob",
                                "===柱塞泵剩余步数===$coagulantSy"
                            )

                            if (coagulantSy < coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong()) {
                                /**
                                 * 柱塞泵剩余步数不够加液
                                 */
                                _stautsNum.value = 1
                                coagulantBool = true
                                logInfo(
                                    "setting_startJob",
                                    "===柱塞泵剩余步数的_stautsNum.value===${_stautsNum.value}"
                                )
                            } else {
                                _stautsNum.value += 1
                                /**
                                 * 已经运动的柱塞泵步数
                                 */
                                coagulantStart.value =
                                    (coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong()) * _stautsNum.value

                            }

                        }

                        //第一次运行的管路填充
                        if (i == 1) {
                            /**
                             * 高浓度管路填充
                             */

                            logInfo(
                                "setting_startJob",
                                "第一次运行的管路填充"
                            )
                            val slEnetity = slDao.getById(1L).firstOrNull()
                            if (slEnetity != null) {
                                val higeFilling = slEnetity.higeFilling
                                logInfo(
                                    "setting_pipeline",
                                    "高浓度管路填充液量===$higeFilling"
                                )

                                /**
                                 * 低浓度管路填充
                                 */
                                val lowFilling = slEnetity.lowFilling
                                logInfo(
                                    "setting_pipeline",
                                    "低浓度管路填充液量===$lowFilling"
                                )

                                /**
                                 * 冲洗液泵管路填充
                                 */
                                val rinseFilling = slEnetity.rinseFilling

                                logInfo(
                                    "setting_pipeline",
                                    "冲洗液泵管路填充液量===$rinseFilling"
                                )

                                /**
                                 * 促凝剂泵管路填充
                                 */
                                val coagulantFilling = slEnetity.coagulantFilling
                                logInfo(
                                    "setting_pipeline",
                                    "促凝剂泵管路填充液量===$coagulantFilling"
                                )
                                val p1jz = (AppStateUtils.hpc[1] ?: { x -> x * 100 }).invoke(1.0)
                                logInfo(
                                    "setting_pipeline",
                                    "促凝剂泵校准因子===$p1jz"
                                )

                                val coagulantpipeline = dataStore.readData("coagulantpipeline", 50)

                                logInfo(
                                    "setting_pipeline",
                                    "coagulantpipeline===$coagulantpipeline"
                                )

                                var p1 = (coagulantFilling * 1000 * p1jz).toLong()
                                val p2 = (coagulantpipeline * p1jz).toLong()
                                logInfo(
                                    "setting_pipeline",
                                    "p2===$p2"
                                )
                                p1 -= p2
                                logInfo(
                                    "setting_pipeline",
                                    "促凝剂泵管路填充加液步数===$p1"
                                )
                                /**
                                 * 促凝剂总行程
                                 */
                                val coagulantpulse =
                                    dataStore.readData("coagulantpulse", 550000).toLong()
                                logInfo(
                                    "setting_pipeline",
                                    "促凝剂总行程===$coagulantpulse"
                                )
                                /**
                                 * 复位后预排步数
                                 */
                                val coagulantResetPulse =
                                    dataStore.readData("coagulantResetPulse", 1500).toLong()
                                logInfo(
                                    "setting_pipeline",
                                    "促凝剂复位后预排步数===$coagulantResetPulse"
                                )

                                /**
                                 * 促凝剂转速
                                 */
                                val coagulantSpeed = dataStore.readData("coagulantSpeed", 200L)
                                logInfo(
                                    "setting_pipeline",
                                    "促凝剂转速===$coagulantSpeed"
                                )

                                /**
                                 * 冲洗转速
                                 */
                                val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                                logInfo(
                                    "setting_pipeline",
                                    "冲洗转速===$rinseSpeed"
                                )

                                /**
                                 * x轴转速
                                 */
                                val xSpeed = dataStore.readData("xSpeed", 100L)
                                logInfo(
                                    "setting_pipeline",
                                    "x轴转速===$xSpeed"
                                )

                                /**
                                 * 废液槽位置
                                 */
                                val wastePosition = slEnetity.wastePosition
                                logInfo(
                                    "setting_pipeline",
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
                                logInfo(
                                    "setting_pipeline",
                                    "促凝剂运动次数===$p1Count"
                                )
                                //向下取整
                                val count = floor(p1Count).toInt()
                                logInfo(
                                    "setting_pipeline",
                                    "循环向下取整===$count"
                                )
                                val qyu = p1 % (coagulantpulse - 50000)
                                logInfo(
                                    "setting_pipeline",
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
                                start {
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
                                            rinseSpeed * 30,
                                            rinseSpeed * 30,
                                            rinseSpeed * 30
                                        ),
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

                            }
                            delay(100L)
                        }

                        //促凝剂不够增加促凝剂量
                        if (coagulantBool) {
                            logInfo(
                                "setting_startJob",
                                "===柱塞泵回到下拉到底==="
                            )
                            start {
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
                                "setting_startJob",
                                "===柱塞泵回到下拉到底==="
                            )

                            coagulantStart.value =
                                (coagulantExpectedPulse.toLong() + coagulantPulseCount.toLong()) * _stautsNum.value

                        }


//===================废液槽运动开始=====================
                        logInfo(
                            "setting_startJob",
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
                            "setting_startJob",
                            "===废液槽运动结束==="
                        )
//===================废液槽运动结束=====================

                        /**
                         * 预排液
                         */
                        //===================预排液开始=====================
                        logInfo(
                            "setting_startJob",
                            "===预排液开始==="
                        )

                        start {
                            exceptionPolicy = ExceptionPolicy.SKIP
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
                        logInfo(
                            "setting_startJob",
                            "===预排液结束==="
                        )


                        //===================制胶位置移动开始=====================
                        logInfo(
                            "setting_startJob",
                            "===制胶位置移动开始==="
                        )
                        //制胶位置
                        start {
                            exceptionPolicy = ExceptionPolicy.SKIP
                            timeOut = 1000L * 60L
//                        with(index = 0, pdv = glueBoardPosition)
                            with(
                                index = 0,
                                pdv = setting.glueBoardPosition,
                                ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                            )

                        }
                        logInfo(
                            "setting_startJob",
                            "===制胶位置移动结束==="
                        )
                        delay(100)

                        logInfo(
                            "setting_startJob",
                            "===制胶运动开始==="
                        )

                        logInfo(
                            "setting_startJob",
                            "===柱塞泵参数===步数===$coagulantPulseCount===加速度===${(coagulantAcc * 13).toLong()}===开始速度===${(coagulantStartSpeed * 1193).toLong()}===结束速度==${(coagulantEndSpeed * 1193).toLong()}"
                        )
                        logInfo(
                            "setting_startJob",
                            "===高浓度泵参数===步数===${guleHighPulse.toLong()}===加速度===${(highAcc * 13).toLong()}===开始速度===${(highEndSpeed * 1193).toLong()}===结束速度==${(highStartSpeed * 1193).toLong()}"
                        )
                        logInfo(
                            "setting_startJob",
                            "===低浓度泵参数===步数===${guleLowPulse.toLong()}===加速度===${(lowAcc * 13).toLong()}===开始速度===${(lowStartSpeed * 1193).toLong()}===结束速度==${(lowEndSpeed * 1193).toLong()}"
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


                        logInfo(
                            "setting_startJob",
                            "===制胶运动结束==="
                        )


                        //===================制胶运动结束=====================
                        val rinseSpeed = dataStore.readData("rinseSpeed", 600L)
                        logInfo(
                            "setting_startJob",
                            "冲洗转速===$rinseSpeed"
                        )
                        delay(2000)
                        //制胶完成，清洗运动
                        /**
                         * 冲洗液泵清洗液量
                         */
                        val rinseP =
                            SerialPortUtils.pulse(index = 4, dvp = setting.rinseCleanVolume * 1000)

                        start {
                            exceptionPolicy = ExceptionPolicy.SKIP
                            timeOut = 1000L * 60L
                            with(
                                index = 0,
                                ads = Triple(xSpeed * 20, xSpeed * 20, xSpeed * 20),
                                pdv = setting.wastePosition
                            )
                        }
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

                        delay(100)
                        logInfo(
                            "setting_startJob",
                            "第$i" + "老化运动结束"
                        )
                    }
                    _complate.value += 1
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

//            // 获取系统版本号（API 级别）
//            val sdkInt = Build.VERSION.SDK_INT
//            logInfo("SystemInfo", "获取系统版本号（API 级别）: $sdkInt")
//
//            // 获取设备名
//            val device = Build.DEVICE
//            logInfo("SystemInfo", "获取设备名: $device")
//
//            // 获取设备型号
//            val model = Build.MODEL
//            logInfo("SystemInfo", "获取设备型号: $model")
//
//            // 获取设备制造商
//            val manufacturer = Build.MANUFACTURER
//            logInfo("SystemInfo", "获取设备制造商: $manufacturer")

            // 获取系统版本名称
            val release = Build.VERSION.RELEASE
            logInfo("setting_SystemInfo", "获取系统版本名称: $release")

            if (release == "6.0.1") {
                //Android6.0.1系统是迈冲
                val intent = Intent("com.tchip.changeBarHideStatus")
                ShellUtils.execCmd(
                    "settings put system systembar_hide " + if (nav) "0" else "1",
                    true
                )
                ApplicationUtils.ctx.sendBroadcast(intent)
            } else {
                //Android7.1系统是软方
                val intent = Intent().apply {
                    action = "ACTION_SHOW_NAVBAR"
                    putExtra("cmd", if (nav) "show" else "hide")
                }
                ApplicationUtils.ctx.sendBroadcast(intent)
            }


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

    data class ImportData(val filePath: String) : SettingIntent()

    data class UpdateApkU(val context: Context, val apkPath: String) : SettingIntent()

    data class XStart(val xNum: Int) : SettingIntent()

    data object XStop : SettingIntent()
    data object exit : SettingIntent()

    data class CopyFileToUSB(val context: Context, val usbPath: String) : SettingIntent()


    data class FillCoagulant(val coagulantVol: Double) : SettingIntent()
}