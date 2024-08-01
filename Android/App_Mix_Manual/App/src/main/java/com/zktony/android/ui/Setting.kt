package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.BuildConfig
//import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.Expected
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.Setting
import com.zktony.android.data.entities.SportsLog
import com.zktony.android.ui.components.DebugModeAppBar
import com.zktony.android.ui.components.TableTextBody
import com.zktony.android.ui.components.TableTextHead
import com.zktony.android.ui.components.VerificationCodeField
import com.zktony.android.ui.components.VerificationCodeItem
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.line
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactorNew
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.SerialPortUtils.lightGreed
import com.zktony.android.utils.SerialPortUtils.lightYellow
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.SerialPortUtils.version
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.embeddedVersion
import com.zktony.serialport.ext.readInt16LE
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
//import com.zktony.serialport.BuildConfig
import kotlinx.coroutines.launch
import java.io.File
import java.lang.reflect.Method
import java.util.regex.Pattern


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingRoute(viewModel: SettingViewModel, homeViewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current

    val application by viewModel.application.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val page by viewModel.page.collectAsStateWithLifecycle()
    val currentpwd by viewModel.currentpwd.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()

    val uiFlagsHome by homeViewModel.uiFlags.collectAsStateWithLifecycle()

    val job by viewModel.job.collectAsStateWithLifecycle()
    val proEntities = viewModel.proEntities.collectAsLazyPagingItems()
    val erroeEntities = viewModel.errorEntities.collectAsLazyPagingItems()

    val slEntitiy by viewModel.slEntitiy.collectAsStateWithLifecycle(initialValue = null)
    val ncEntitiy by viewModel.ncEntitiy.collectAsStateWithLifecycle(initialValue = null)

    val expected by viewModel.expected.collectAsStateWithLifecycle(initialValue = null)


    //导入数据更新
    val speedFlow by viewModel.speedFlow.collectAsStateWithLifecycle()
    val rinseSpeedFlow by viewModel.rinseSpeedFlow.collectAsStateWithLifecycle()
    val xSpeedFlow by viewModel.xSpeedFlow.collectAsStateWithLifecycle()
    val coagulantSpeedFlow by viewModel.coagulantSpeedFlow.collectAsStateWithLifecycle()
    val coagulantpulseFlow by viewModel.coagulantpulseFlow.collectAsStateWithLifecycle()
    val coagulantTimeFlow by viewModel.coagulantTimeFlow.collectAsStateWithLifecycle()
    val coagulantResetPulseFlow by viewModel.coagulantResetPulseFlow.collectAsStateWithLifecycle()
    val updateMsg by viewModel.updateMsg.collectAsStateWithLifecycle()


    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.SETTINGS -> navigationActions.navigateUp()
                else -> viewModel.dispatch(SettingIntent.NavTo(PageType.SETTINGS))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(SettingIntent.Flags(UiFlags.none()))
        }
    }


    Box {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.mipmap.bkimg),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )
        Column {
            if (page == PageType.SETTINGS) {
                DebugModeAppBar(page) {
                    navigation()
                }
            }
            AnimatedContent(targetState = page) {
                when (page) {
                    PageType.SETTINGS -> SettingLits(
                        slEntitiy,
                        ncEntitiy,
                        viewModel::dispatch,
                        erroeEntities,
                        currentpwd,
                        expected,
                    )

                    PageType.DEBUGMODE -> debug(
                        viewModel::dispatch,
                        proEntities.toList(),
                        slEntitiy,
                        job,
                        uiFlags,
                        speedFlow,
                        rinseSpeedFlow,
                        xSpeedFlow,
                        coagulantSpeedFlow,
                        coagulantpulseFlow,
                        coagulantTimeFlow,
                        coagulantResetPulseFlow,
                    )

                    PageType.SPORTSLOG -> sportsLog(
                        viewModel::dispatch,
                    )

                    PageType.UPGRADE -> upgrade(
                        viewModel::dispatch,
                        homeViewModel::dispatch,
                        updateMsg,
                    )

                    PageType.REHEARSAL -> rehearsal(
                        slEntitiy,
                        expected,
                        viewModel::dispatch,
                    )

                    PageType.CALIBRATION -> calibration(
                        ncEntitiy,
                        viewModel::dispatch,
                    )

                    else -> {}
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SettingLits(
    s1: Setting?,
    c1: NewCalibration?,
    uiEvent: (SettingIntent) -> Unit,
    erroeEntities: LazyPagingItems<ErrorRecord>,
    currentpwd: String,
    ex1: Expected?,
) {
    var setting = s1 ?: Setting()


    val context = LocalContext.current

    val keyboard = LocalSoftwareKeyboardController.current

    var switchColum by remember { mutableStateOf(1) }

    val scope = rememberCoroutineScope()

    var navigation by rememberDataSaverState(key = Constants.NAVIGATION, default = false)

    var selectRudio = rememberDataSaverState(key = "selectRudio", default = 1)


    var speChat = "[`~!@#$%^&*()+=\\-|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"


    //================密码相关=============================
    var pwdShow by remember { mutableStateOf(false) }

    /**
     * 设备管理员
     */
    val deviceAdminPwd = rememberDataSaverState(key = "deviceAdminPwd", default = "123456")

    /**
     * 超级管理员
     */
    val superAdminPwd = rememberDataSaverState(key = "superAdminPwd", default = "922042")

    /**
     * 厂家管理员
     */
    val factoryAdminPwd = rememberDataSaverState(key = "superAdminPwd", default = "240229")


    /**
     * 修改密码的弹窗
     */
    val updatePwdDialog = remember { mutableStateOf(false) }

    var oldPwd = remember { mutableStateOf("") }
    var newPwd = remember { mutableStateOf("") }
    var newPwd2 = remember { mutableStateOf("") }

    //================密码相关=============================

    //===============配件寿命==============================


    /**
     *  高浓度泵预计使用时间
     */
    var highTimeExpected_ex by remember(setting) { mutableStateOf(setting.highTimeExpected.toString()) }

    /**
     *  低浓度泵预计使用时间
     */
    var lowTimeExpected_ex by remember(setting) { mutableStateOf(setting.lowTimeExpected.toString()) }

    /**
     *  冲洗液泵预计使用时间
     */
    var rinseTimeExpected_ex by remember(setting) { mutableStateOf(setting.rinseTimeExpected.toString()) }

    var useHighTime by remember(setting) { mutableStateOf(setting.highTime.toString()) }

    var useLowTime by remember(setting) { mutableStateOf(setting.lowLife.toString()) }

    var useRinseTime by remember(setting) { mutableStateOf(setting.rinseTime.toString()) }


    /**
     * 配件的弹窗
     */
    val accessoriesDialog = remember(setting) { mutableStateOf(false) }

    //===============配件寿命==============================

    //===============填充设置==============================

    var higeFilling = rememberDataSaverState(key = "hige_Filling", default = 0.0)
    var higeFilling_ex by remember { mutableStateOf(higeFilling.value.toString()) }

    var lowFilling = rememberDataSaverState(key = "low_Filling", default = 0.0)
    var lowFilling_ex by remember { mutableStateOf(lowFilling.value.toString()) }

    var coagulantFilling = rememberDataSaverState(key = "coagulant_Filling", default = 0.0)
    var coagulantFilling_ex by remember { mutableStateOf(coagulantFilling.value.toString()) }

    //===============填充设置==============================


    //===============位置设置==============================
    /**
     * 胶板位置
     */
    var glueBoardPosition_ex by remember(setting) { mutableStateOf(setting.glueBoardPosition.toString()) }

    /**
     * 废液位置
     */
    var wastePosition_ex by remember(setting) { mutableStateOf(setting.wastePosition.toString()) }

    /**
     * 位置的弹窗
     */
    val positionDialog = remember { mutableStateOf(false) }


    /**
     * 关于软件的弹窗
     */
    val softwareDialog = remember { mutableStateOf(false) }
    //===============位置设置==============================


    var selectedIndex by remember { mutableStateOf(0) }
    //	定义列宽
    val cellWidthList = arrayListOf(70, 115, 241)

    //下位机版本号
    var lowVersion by remember { mutableStateOf("Unknown") }


    var rinseSpeed = rememberDataSaverState(key = "rinseSpeed", default = 600L)


    Column(
        modifier = Modifier
            .padding(start = 13.75.dp)
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
            .background(Color.White)
            .height(904.9.dp)
            .width((572.5).dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 36.2.dp)
                        .width(39.2.dp)
                        .height(142.1.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (switchColum == 1) Color(
                                0, 105, 52
                            ) else Color.White
                        )
                        .clickable {
                            switchColum = 1
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp),
                        color = if (switchColum == 1) Color.White else Color(rgb(112, 112, 112)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        text = "填"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 45.dp),
                        color = if (switchColum == 1) Color.White else Color(rgb(112, 112, 112)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        text = "充"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 68.dp),
                        color = if (switchColum == 1) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "设"
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 90.dp),
                        color = if (switchColum == 1) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "置"
                    )
                }


                Box(
                    modifier = Modifier
                        .padding(top = 126.2.dp)
                        .width(39.2.dp)
                        .height(142.1.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (switchColum == 2) Color(
                                0, 105, 52
                            ) else Color.White
                        )
                        .clickable {
                            switchColum = 2
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp),
                        color = if (switchColum == 2) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "故"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 45.dp),
                        color = if (switchColum == 2) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "障"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 68.dp),
                        color = if (switchColum == 2) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "记"
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 90.dp),
                        color = if (switchColum == 2) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "录"
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 126.2.dp)
                        .width(39.2.dp)
                        .height(142.1.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (switchColum == 3) Color(
                                0, 105, 52
                            ) else Color.White
                        )
                        .clickable {
                            switchColum = 3
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp),
                        color = if (switchColum == 3) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "高"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 45.dp),
                        color = if (switchColum == 3) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "级"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 68.dp),
                        color = if (switchColum == 3) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "设"
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 90.dp),
                        color = if (switchColum == 3) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "置"
                    )
                }


            }

            if (switchColum == 1) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, start = 50.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(200.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(229, 229, 229)),
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                text = "高浓度泵",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                                value = higeFilling_ex,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "填充液量/mL", fontSize = 20.sp) },
                                textStyle = TextStyle(fontSize = 20.sp),
                                onValueChange = {
                                    if (Pattern.compile(speChat).matcher(it).find()) {
                                        Toast.makeText(
                                            context,
                                            "数据不能包含特殊字符！",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        higeFilling_ex = it
                                        val temp = higeFilling_ex.toDoubleOrNull() ?: 0.0
                                        higeFilling.value = temp
                                        if (temp < 0) {
                                            higeFilling_ex = "0"
                                            higeFilling.value = 0.0
                                        }
                                    }

                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )
                            Row(modifier = Modifier.padding(top = 10.dp, start = 230.dp)) {
                                Button(
                                    modifier = Modifier
                                        .width(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    onClick = {
                                        scope.launch {
                                            if (higeFilling.value > 0) {
                                                start {
                                                    timeOut = 1000L * 30
                                                    with(
                                                        index = 2,
                                                        pdv = higeFilling.value * 1000,
                                                        ads = Triple(
                                                            rinseSpeed.value * 13,
                                                            rinseSpeed.value * 1193,
                                                            rinseSpeed.value * 1193
                                                        ),

                                                        )
                                                }

                                                start {
                                                    timeOut = 1000L * 30
                                                    with(
                                                        index = 4,
                                                        ads = Triple(
                                                            rinseSpeed.value * 30,
                                                            rinseSpeed.value * 30,
                                                            rinseSpeed.value * 30
                                                        ),
                                                        pdv = setting.rinseCleanVolume * 1000
                                                    )
                                                }
                                            }

                                        }
                                    }) {
                                    Text(fontSize = 18.sp, text = "填   充")
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(200.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(229, 229, 229)),
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                text = "低浓度泵",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                                value = lowFilling_ex,
                                textStyle = TextStyle(fontSize = 20.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "填充液量/mL", fontSize = 20.sp) },
                                onValueChange = {
                                    if (Pattern.compile(speChat).matcher(it).find()) {
                                        Toast.makeText(
                                            context,
                                            "数据不能包含特殊字符！",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        lowFilling_ex = it
                                        val temp = lowFilling_ex.toDoubleOrNull() ?: 0.0
                                        lowFilling.value = temp
                                        if (temp < 0) {
                                            lowFilling_ex = "0"
                                            lowFilling.value = 0.0
                                        }
                                    }

                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )

                            Row(modifier = Modifier.padding(top = 10.dp, start = 230.dp)) {
                                Button(
                                    modifier = Modifier
                                        .width(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    onClick = {
                                        scope.launch {
                                            if (lowFilling.value > 0) {
                                                start {
                                                    timeOut = 1000L * 30
                                                    with(
                                                        index = 3,
                                                        pdv = lowFilling.value * 1000,
                                                        ads = Triple(
                                                            rinseSpeed.value * 13,
                                                            rinseSpeed.value * 1193,
                                                            rinseSpeed.value * 1193
                                                        ),

                                                        )
                                                }

                                                start {
                                                    timeOut = 1000L * 30
                                                    with(
                                                        index = 4,
                                                        ads = Triple(
                                                            rinseSpeed.value * 30,
                                                            rinseSpeed.value * 30,
                                                            rinseSpeed.value * 30
                                                        ),
                                                        pdv = setting.rinseCleanVolume * 1000
                                                    )
                                                }
                                            }
                                        }
                                    }) {
                                    Text(fontSize = 18.sp, text = "填   充")
                                }
                            }


                        }

                    }

                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(200.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(229, 229, 229)),
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                text = "促凝剂泵",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                                value = coagulantFilling_ex,
                                textStyle = TextStyle(fontSize = 20.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "填充液量/mL", fontSize = 20.sp) },
                                onValueChange = {
                                    if (Pattern.compile(speChat).matcher(it).find()) {
                                        Toast.makeText(
                                            context,
                                            "数据不能包含特殊字符！",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        coagulantFilling_ex = it
                                        val temp = coagulantFilling_ex.toDoubleOrNull() ?: 0.0
                                        coagulantFilling.value = temp
                                        if (temp < 0) {
                                            coagulantFilling_ex = "0"
                                            coagulantFilling.value = 0.0
                                        }
                                    }

                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )

                            Row(modifier = Modifier.padding(top = 10.dp, start = 230.dp)) {
                                Button(
                                    modifier = Modifier
                                        .width(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    onClick = {
                                        if (coagulantFilling.value > 0) {
                                            uiEvent(SettingIntent.FillCoagulant(coagulantFilling.value))
                                        }
                                    }) {
                                    Text(fontSize = 18.sp, text = "填   充")
                                }
                            }


                        }
                    }

                }

            } else if (switchColum == 2) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, start = 50.dp),
                ) {
                    stickyHeader {
                        Row(
                            Modifier.background(Color(rgb(0, 105, 52)))
                        ) {
                            TableTextHead(text = "序号", width = cellWidthList[0])
                            TableTextHead(text = "时        间", width = cellWidthList[1])
                            TableTextHead(text = "故障情况", width = cellWidthList[2])
                        }
                    }

                    itemsIndexed(erroeEntities) { index, item ->
                        val selected = item == erroeEntities[selectedIndex]
                        Row(
                            modifier = Modifier
                                .background(
                                    if (index % 2 == 0) Color(
                                        rgb(
                                            229, 229, 229
                                        )
                                    ) else Color.White
                                )
                                .clickable(onClick = {
                                    selectedIndex = index
                                })
                        ) {
                            TableTextBody(
                                text = (index + 1).toString(), width = cellWidthList[0], selected
                            )
                            TableTextBody(
                                text = "" + item.createTime.dateFormat("yyyy/MM/dd"),
                                width = cellWidthList[1],
                                selected
                            )
                            TableTextBody(
                                text = item.detail, width = cellWidthList[2], selected
                            )

                        }
                    }


                }


            } else if (switchColum == 3) {

                var modelsThickness =
                    rememberDataSaverState(key = "modelsThickness", default = "G1520")


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, start = 50.dp),
                ) {

                    /**
                     * 未输入密码或登出
                     */
                    if (currentpwd == "") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            VerificationCodeField(digits = 6, inputCallback = {
                                pwdShow = true
                                uiEvent(SettingIntent.Login(it))
                            }) { text, focused ->
                                VerificationCodeItem(text, focused)
                            }
                        }
                    } else {
                        val items = listOf(
                            "预排设置",
                            "校准设置",
                            "运行日志",
                            "网络设置",
                            "操作系统设置",
                            "系统更新",
                            "配件寿命",
                            "位置设置",
                            "关于软件",
                        )
                        LazyColumn {

                            item {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 20.dp)
                                        .fillMaxWidth()
                                ) {
                                    line(Color(rgb(240, 240, 240)), 0f, 400f)
                                }
                                Row {
                                    Text(
                                        modifier = Modifier.padding(top = 20.dp),
                                        text = "导航栏",
                                        fontSize = 30.sp
                                    )
                                    Switch(colors = SwitchDefaults.colors(
                                        checkedTrackColor = Color(rgb(0, 105, 52)),
                                    ),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .padding(top = 40.dp, start = 220.dp),
                                        checked = navigation,
                                        onCheckedChange = {
                                            scope.launch {
                                                navigation = it
                                                uiEvent(SettingIntent.Navigation(it))
                                            }
                                        })
                                }



                                if (factoryAdminPwd.value == currentpwd) {
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 20.dp)
                                            .fillMaxWidth()
                                    ) {
                                        line(Color(rgb(240, 240, 240)), 0f, 400f)
                                    }

                                    Row(modifier = Modifier.clickable {
                                        uiEvent(SettingIntent.NavTo(PageType.DEBUGMODE))
                                    }) {
                                        Text(
                                            modifier = Modifier.padding(top = 20.dp),
                                            text = "调试模式",
                                            fontSize = 30.sp
                                        )

                                        Image(
                                            modifier = Modifier
                                                .padding(top = 20.dp, start = 200.dp)
                                                .size(40.dp),
                                            painter = painterResource(id = R.mipmap.rightarrow),
                                            contentDescription = null
                                        )
                                    }

                                }

                                if (superAdminPwd.value == currentpwd || deviceAdminPwd.value == currentpwd) {
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 20.dp)
                                            .fillMaxWidth()
                                    ) {
                                        line(Color(rgb(240, 240, 240)), 0f, 400f)
                                    }

                                    Row(modifier = Modifier.clickable {
                                        updatePwdDialog.value = true
                                    }) {
                                        Text(
                                            modifier = Modifier.padding(top = 20.dp),
                                            text = "修改密码",
                                            fontSize = 30.sp
                                        )

                                        Image(
                                            modifier = Modifier
                                                .padding(top = 20.dp, start = 200.dp)
                                                .size(40.dp),
                                            painter = painterResource(id = R.mipmap.rightarrow),
                                            contentDescription = null
                                        )
                                    }


                                }

                            }

                            items(items) { item ->

                                Row(
                                    modifier = Modifier
                                        .padding(top = 20.dp)
                                        .fillMaxWidth()
                                ) {
                                    line(Color(rgb(240, 240, 240)), 0f, 400f)
                                }

                                Row(modifier = Modifier.clickable {

                                    when (item) {
                                        "预排设置" -> {
                                            uiEvent(SettingIntent.NavTo(PageType.REHEARSAL))
                                        }

                                        "校准设置" -> {
                                            uiEvent(SettingIntent.NavTo(PageType.CALIBRATION))
                                        }

                                        "运行日志" -> {
                                            uiEvent(SettingIntent.NavTo(PageType.SPORTSLOG))
                                        }

                                        "网络设置" -> {
                                            uiEvent(SettingIntent.Network)
                                        }

                                        "操作系统设置" -> {
                                            uiEvent(SettingIntent.exit)
                                        }

                                        "系统更新" -> {
                                            uiEvent(SettingIntent.NavTo(PageType.UPGRADE))
                                        }

                                        "配件寿命" -> {
                                            accessoriesDialog.value = true
                                        }

                                        "位置设置" -> {
                                            positionDialog.value = true
                                        }

                                        "关于软件" -> {
                                            scope.launch {
                                                lowVersion = embeddedVersion()
                                                delay(100)
                                                softwareDialog.value = true
                                            }
                                        }
                                    }

                                }) {
                                    Text(
                                        modifier = Modifier.padding(top = 20.dp),
                                        text = item,
                                        fontSize = 30.sp
                                    )

                                    Image(
                                        modifier = Modifier
                                            .padding(
                                                top = 20.dp,
                                                start = if (item == "操作系统设置") 140.dp else 200.dp
                                            )
                                            .size(40.dp),
                                        painter = painterResource(id = R.mipmap.rightarrow),
                                        contentDescription = null
                                    )
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 20.dp)
                                        .fillMaxWidth()
                                ) {
                                    line(Color(rgb(240, 240, 240)), 0f, 400f)
                                }
                                Row {
                                    Text(
                                        modifier = Modifier.padding(top = 20.dp),
                                        text = "声音设置",
                                        fontSize = 30.sp
                                    )

                                    Row(
                                        modifier = Modifier
                                            .padding(top = 20.dp)
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .background(
                                                color = Color(rgb(238, 238, 238)),
                                                shape = CircleShape
                                            )
                                            .padding(horizontal = 4.dp),
                                    ) {
                                        AUDIO_DESTINATION.forEach { destination ->
                                            if (modelsThickness.value == "G1520") {
                                                ElevatedButton(
                                                    modifier = Modifier.height(32.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (selectRudio.value == destination.id) Color(
                                                            rgb(
                                                                0, 105, 52
                                                            )
                                                        ) else Color(rgb(238, 238, 238)),
                                                    ),
                                                    onClick = {
                                                        selectRudio.value = destination.id
                                                        uiEvent(SettingIntent.Sound(selectRudio.value))
                                                    },
                                                ) {
                                                    Text(
                                                        text = destination.name,
                                                        fontSize = 13.sp,
                                                        color = if (selectRudio.value == destination.id) Color.White else Color.Black
                                                    )
                                                }
                                            } else {
                                                if (destination.id != 2) {
                                                    ElevatedButton(
                                                        modifier = Modifier.height(32.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (selectRudio.value == destination.id) Color(
                                                                rgb(
                                                                    0, 105, 52
                                                                )
                                                            ) else Color(rgb(238, 238, 238)),
                                                        ),
                                                        onClick = {
                                                            selectRudio.value = destination.id
                                                            uiEvent(SettingIntent.Sound(selectRudio.value))
                                                        },
                                                    ) {
                                                        Text(
                                                            text = destination.name,
                                                            fontSize = 13.sp,
                                                            color = if (selectRudio.value == destination.id) Color.White else Color.Black
                                                        )
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 20.dp)
                                        .fillMaxWidth()
                                ) {
                                    line(Color(rgb(240, 240, 240)), 0f, 400f)
                                }

                                Button(modifier = Modifier
                                    .padding(start = 141.dp, top = 40.dp)
                                    .width(131.8.dp)
                                    .height(41.5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp),
                                    onClick = {
                                        uiEvent(SettingIntent.Login(""))
                                        switchColum = 0

                                    }) {
                                    Text(text = "登   出", fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }


                        }
                    }

                }

            }


        }

        //修改密码弹窗
        if (updatePwdDialog.value) {
            AlertDialog(onDismissRequest = { updatePwdDialog.value = false }, title = {
                Text(text = "修改密码")
            }, text = {
                Column {

                    OutlinedTextField(
                        value = oldPwd.value,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "请输入原密码") },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (it.length > 6) {
                                    Toast.makeText(
                                        context, "长度不能大于6！", Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    oldPwd.value = it
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboard?.hide()
                        })
                    )

                    OutlinedTextField(
                        value = newPwd.value,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "请输入新密码") },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                newPwd.value = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboard?.hide()
                        })
                    )

                    OutlinedTextField(
                        value = newPwd2.value,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "请确认新密码") },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                newPwd2.value = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboard?.hide()
                        })
                    )

                }


            }, confirmButton = {
                Button(modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {

                    if (oldPwd.value == deviceAdminPwd.value) {
                        if (newPwd.value == newPwd2.value) {
                            if (newPwd.value.length != 6) {
                                Toast.makeText(
                                    context, "新密码要6位数字！", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                deviceAdminPwd.value = newPwd.value
                                updatePwdDialog.value = false
                                uiEvent(SettingIntent.Login(""))
                                switchColum = 0
                            }

                        } else {
                            Toast.makeText(
                                context, "新密码不一致！", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context, "原密码错误！", Toast.LENGTH_SHORT
                        ).show()
                    }


                }) {
                    Text(fontSize = 18.sp, text = "确   认")
                }
            }, dismissButton = {
                Button(modifier = Modifier.width(100.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ), onClick = { updatePwdDialog.value = false }) {
                    Text(fontSize = 18.sp, text = "取   消", color = Color.Black)
                }
            })
        }

        //配件寿命弹窗
        if (accessoriesDialog.value) {
            AlertDialog(onDismissRequest = { }, title = {
                Text(text = "配件寿命")
            }, text = {
                Column {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            Text(text = "高浓度泵", fontSize = 25.sp)
                            Text(modifier = Modifier.padding(start = 10.dp), text = "已使用:")
                            Text(
                                modifier = Modifier.padding(start = 10.dp), text = useHighTime
                            )
                            Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            if (factoryAdminPwd.value == currentpwd) {
                                Button(modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        setting.highTimeExpected =
                                            highTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                        uiEvent(SettingIntent.UpdateSet(setting))
                                    }) {
                                    Text(text = "保    存", fontSize = 18.sp)
                                }

                                Text(
                                    modifier = Modifier.padding(start = 10.dp), text = "预期寿命:"
                                )

                                OutlinedTextField(
                                    modifier = Modifier.width(100.dp),
                                    value = highTimeExpected_ex,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(rgb(0, 105, 52)),
                                        focusedLabelColor = Color(rgb(0, 105, 52)),
                                        cursorColor = Color(rgb(0, 105, 52))
                                    ),
                                    label = { },
                                    onValueChange = {
                                        if (Pattern.compile(speChat).matcher(it).find()) {
                                            Toast.makeText(
                                                context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            highTimeExpected_ex = it
                                            val temp = highTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                            if (temp < 0) {
                                                highTimeExpected_ex = "0"
                                            } else if (temp > 2000) {
                                                highTimeExpected_ex = "2000"
                                            }
                                        }

                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboard?.hide()
                                    })
                                )

                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            } else {
                                Button(modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        useHighTime = "0.0"
                                        setting.highTime = 0.0
                                        uiEvent(SettingIntent.UpdateSet(setting))
                                    }) {
                                    Text(text = "重    置", fontSize = 18.sp)
                                }

                                Text(
                                    modifier = Modifier.padding(start = 10.dp), text = "预期寿命:"
                                )
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = setting.highTimeExpected.toString()
                                )
                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            }

                        }
                    }

                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            Text(text = "低浓度泵", fontSize = 25.sp)
                            Text(modifier = Modifier.padding(start = 10.dp), text = "已使用:")
                            Text(
                                modifier = Modifier.padding(start = 10.dp), text = useLowTime
                            )
                            Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {

                            if (factoryAdminPwd.value == currentpwd) {
                                Button(modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        setting.lowTimeExpected =
                                            lowTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                        uiEvent(SettingIntent.UpdateSet(setting))
                                    }) {
                                    Text(text = "保    存", fontSize = 18.sp)
                                }

                                Text(
                                    modifier = Modifier.padding(start = 10.dp), text = "预期寿命:"
                                )

                                OutlinedTextField(
                                    modifier = Modifier.width(100.dp),
                                    value = lowTimeExpected_ex,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(rgb(0, 105, 52)),
                                        focusedLabelColor = Color(rgb(0, 105, 52)),
                                        cursorColor = Color(rgb(0, 105, 52))
                                    ),
                                    label = { },
                                    onValueChange = {
                                        if (Pattern.compile(speChat).matcher(it).find()) {
                                            Toast.makeText(
                                                context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            lowTimeExpected_ex = it

                                            val temp = lowTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                            if (temp < 0) {
                                                lowTimeExpected_ex = "0"
                                            } else if (temp > 2000) {
                                                lowTimeExpected_ex = "2000"
                                            }
                                        }

                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboard?.hide()
                                    })
                                )

                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            } else {
                                Button(modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        useLowTime = "0.0"
                                        setting.lowLife = 0.0
                                        uiEvent(SettingIntent.UpdateSet(setting))
                                    }) {
                                    Text(text = "重    置", fontSize = 18.sp)
                                }

                                Text(
                                    modifier = Modifier.padding(start = 10.dp), text = "预期寿命:"
                                )
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = setting.lowTimeExpected.toString()
                                )
                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            }


                        }
                    }

                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            Text(text = "冲洗液泵", fontSize = 25.sp)
                            Text(modifier = Modifier.padding(start = 10.dp), text = "已使用:")

                            Text(
                                modifier = Modifier.padding(start = 10.dp), text = useRinseTime
                            )
                            Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            if (factoryAdminPwd.value == currentpwd) {
                                Button(modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        setting.rinseTimeExpected =
                                            rinseTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                        uiEvent(SettingIntent.UpdateSet(setting))
                                    }) {
                                    Text(text = "保    存", fontSize = 18.sp)
                                }

                                Text(
                                    modifier = Modifier.padding(start = 10.dp), text = "预期寿命:"
                                )

                                OutlinedTextField(
                                    modifier = Modifier.width(100.dp),
                                    value = rinseTimeExpected_ex,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(rgb(0, 105, 52)),
                                        focusedLabelColor = Color(rgb(0, 105, 52)),
                                        cursorColor = Color(rgb(0, 105, 52))
                                    ),
                                    label = { },
                                    onValueChange = {
                                        if (Pattern.compile(speChat).matcher(it).find()) {
                                            Toast.makeText(
                                                context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            rinseTimeExpected_ex = it

                                            val temp = rinseTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                            if (temp < 0) {
                                                rinseTimeExpected_ex = "0"
                                            } else if (temp > 2000) {
                                                rinseTimeExpected_ex = "2000"
                                            }
                                        }

                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboard?.hide()
                                    })
                                )

                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            } else {
                                Button(modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        useRinseTime = "0.0"
                                        setting.rinseTime = 0.0
                                        uiEvent(SettingIntent.UpdateSet(setting))
                                    }) {
                                    Text(text = "重    置", fontSize = 18.sp)
                                }

                                Text(
                                    modifier = Modifier.padding(start = 10.dp), text = "预期寿命:"
                                )
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = setting.rinseTimeExpected.toString()
                                )
                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            }

                        }
                    }


                }


            }, confirmButton = {
                if (factoryAdminPwd.value == currentpwd) {
                    Button(modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        setting.lowTimeExpected = lowTimeExpected_ex.toDoubleOrNull() ?: 0.0
                        setting.highTimeExpected = highTimeExpected_ex.toDoubleOrNull() ?: 0.0
                        setting.rinseTimeExpected = rinseTimeExpected_ex.toDoubleOrNull() ?: 0.0
                        uiEvent(SettingIntent.UpdateSet(setting))
                        accessoriesDialog.value = false
                    }) {
                        Text(fontSize = 18.sp, text = "保   存")
                    }
                } else {
                    Button(modifier = Modifier.width(120.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        useHighTime = "0.0"
                        useLowTime = "0.0"
                        useRinseTime = "0.0"
                        setting.highTime = 0.0
                        setting.lowLife = 0.0
                        setting.rinseTime = 0.0
                        uiEvent(SettingIntent.UpdateSet(setting))
                    }) {
                        Text(fontSize = 18.sp, text = "全部重置")
                    }
                }

            }, dismissButton = {
                Button(modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = { accessoriesDialog.value = false }) {
                    Text(fontSize = 18.sp, text = "返   回")
                }
            })
        }

        //位置设置弹窗
        if (positionDialog.value) {
            AlertDialog(onDismissRequest = { }, title = {
                Text(text = "位置设置")
            }, text = {
                Column {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = CenterVertically
                    ) {
                        OutlinedTextField(
                            value = glueBoardPosition_ex,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            label = { Text(text = "胶板位置") },
                            onValueChange = {
                                if (Pattern.compile(speChat).matcher(it).find()) {
                                    Toast.makeText(
                                        context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    glueBoardPosition_ex = it
                                    val temp = glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                                    if (temp < 0) {
                                        glueBoardPosition_ex = "0"
                                    } else if (temp > 50) {
                                        glueBoardPosition_ex = "50"
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboard?.hide()
                            })
                        )

                        Button(modifier = Modifier
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                scope.launch {
                                    start {
                                        timeOut = 1000L * 60L
                                        with(
                                            index = 0,
                                            ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                            pdv = glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                                        )
                                    }
                                }

                            }) {
                            Text(text = "移    动", fontSize = 18.sp)
                        }

                    }

                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = CenterVertically
                    ) {
                        OutlinedTextField(
                            value = wastePosition_ex,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            label = { Text(text = "废液槽位置") },
                            onValueChange = {
                                if (Pattern.compile(speChat).matcher(it).find()) {
                                    Toast.makeText(
                                        context, "数据不能包含特殊字符！", Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    wastePosition_ex = it
                                    val temp = wastePosition_ex.toDoubleOrNull() ?: 0.0
                                    if (temp < 0) {
                                        wastePosition_ex = "0"
                                    } else if (temp > 30) {
                                        wastePosition_ex = "30"
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboard?.hide()
                            })
                        )

                        Button(modifier = Modifier
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                scope.launch {
                                    start {
                                        timeOut = 1000L * 60L
                                        with(
                                            index = 0,
                                            ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                            pdv = wastePosition_ex.toDoubleOrNull() ?: 0.0
                                        )
                                    }
                                }

                            }) {
                            Text(text = "移    动", fontSize = 18.sp)
                        }

                    }
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = CenterVertically
                    ) {
                        Button(modifier = Modifier
                            .width(130.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                setting.glueBoardPosition = 0.0
                                setting.wastePosition = 0.0
                                glueBoardPosition_ex = "0.0"
                                wastePosition_ex = "0.0"
                                uiEvent(SettingIntent.UpdateSet(setting))
                            }) {
                            Text(text = "恢复默认", fontSize = 18.sp)
                        }
                    }

                }


            }, confirmButton = {
                Button(modifier = Modifier.width(100.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ), onClick = { positionDialog.value = false }) {
                    Text(fontSize = 18.sp, text = "取   消", color = Color.Black)
                }
            }, dismissButton = {
                Button(modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    setting.wastePosition = wastePosition_ex.toDoubleOrNull() ?: 0.0
                    setting.glueBoardPosition = glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                    uiEvent(SettingIntent.UpdateSet(setting))
                    positionDialog.value = false
                }) {
                    Text(text = "保   存")
                }
            })
        }

        //关于软件弹窗
        if (softwareDialog.value) {
            val snNumber = rememberDataSaverState(key = "snNumber", default = "")
            AlertDialog(onDismissRequest = {
                softwareDialog.value = false
            }, title = {
                Text(text = "软件信息")
            }, text = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = CenterVertically
                    ) {

                        Text(
                            text = "应用软件版本号:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            text = "主控板固件版本号:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "$lowVersion",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            text = "状态指示板固件版本号:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            text = "SN码:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = snNumber.value,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                }


            }, confirmButton = {}, dismissButton = {})
        }

    }


}


@Composable
fun debug(
    uiEvent: (SettingIntent) -> Unit,
    proEntities: List<Program>,
    slEntities: Setting?,
    job: Job?,
    uiFlags: UiFlags,
    speedFlow: Int,
    rinseSpeedFlow: Long,
    xSpeedFlow: Long,
    coagulantSpeedFlow: Long,
    coagulantpulseFlow: Int,
    coagulantTimeFlow: Int,
    coagulantResetPulseFlow: Int,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        debugMode(
            uiEvent,
            proEntities,
            slEntities,
            job,
            uiFlags,
            speedFlow,
            rinseSpeedFlow,
            xSpeedFlow,
            coagulantSpeedFlow,
            coagulantpulseFlow,
            coagulantTimeFlow,
            coagulantResetPulseFlow
        )
    }
}


@Composable
fun sportsLog(
    uiEvent: (SettingIntent) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        sportsLogMode(uiEvent)
    }
}


@Composable
fun upgrade(
    uiEvent: (SettingIntent) -> Unit,
    uiEventHome: (HomeIntent) -> Unit,
    updateMsg: String,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        upgradeMode(uiEvent, uiEventHome, updateMsg)
//        EmbeddedTest(uiEventHome)
    }
}

@Composable
fun rehearsal(
    s1: Setting?,
    ex1: Expected?,
    uiEvent: (SettingIntent) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        rehearsalMode(s1, ex1, uiEvent)
    }
}

@Composable
fun calibration(
    c1: NewCalibration?,
    uiEvent: (SettingIntent) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        calibrationMode(c1, uiEvent)
    }
}


val AUDIO_DESTINATION = listOf(
    AudioDestination(
        id = 1, name = "蜂鸣"
    ), AudioDestination(
        id = 2, name = "语音"
    ), AudioDestination(
        id = 3, name = "静音"
    )
)

data class AudioDestination(
    val id: Int, val name: String
)
