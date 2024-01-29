package com.zktony.android.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
//import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.dao.NewCalibrationDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Setting
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.extra.Application
import com.zktony.android.utils.extra.httpCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dao: MotorDao,
    private val proDao: ProgramDao,
    private val slDao: SettingDao,
    private val ncDao: NewCalibrationDao
) : ViewModel() {

    private val _application = MutableStateFlow<Application?>(null)
    private val _selected = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.SETTINGS)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())


    val application = _application.asStateFlow()
    val selected = _selected.asStateFlow()
    val progress = _progress.asStateFlow()
    val page = _page.asStateFlow()

    val uiFlags = _uiFlags.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    val proEntities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { proDao.getByPage() }.flow.cachedIn(viewModelScope)

//    val slEntities = Pager(
//        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
//    ) { slDao.getByPage() }.flow.cachedIn(viewModelScope)

    val slEntitiy = slDao.getById(1L)

    val ncEntitiy = ncDao.getById(1L)

//    val ncEntities = Pager(
//        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
//    ) { ncDao.getByPage() }.flow.cachedIn(viewModelScope)


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
//                if (application.versionCode > BuildConfig.VERSION_CODE
//                    && application.downloadUrl.isNotEmpty()
//                    && _progress.value == 0
//                ) {
//                    _progress.value = 1
//                    application.downloadUrl.download(
//                        File(
//                            ApplicationUtils.ctx.getExternalFilesDir(null),
//                            "update.apk"
//                        )
//                    ).collect {
//                        when (it) {
//                            is DownloadState.Success -> {
//                                _progress.value = 0
//                                ApplicationUtils.installApp(it.file)
//                            }
//
//                            is DownloadState.Err -> {
//                                _progress.value = 0
//                                _uiFlags.value = UiFlags.message("下载失败: ${it.t.message}")
//                            }
//
//                            is DownloadState.Progress -> {
//                                _progress.value = maxOf(it.progress, 1)
//                            }
//                        }
//                    }
//                }
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

}