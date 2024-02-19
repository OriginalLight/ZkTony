package com.zktony.android.ui

import android.graphics.Color.rgb
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
//import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.dao.ErrorRecordDao
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.Setting
import com.zktony.android.ui.components.DebugModeAppBar
import com.zktony.android.ui.components.TableTextBody
import com.zktony.android.ui.components.TableTextHead
import com.zktony.android.ui.components.VerificationCodeField
import com.zktony.android.ui.components.VerificationCodeItem
import com.zktony.android.ui.navigation.NavigationActions
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
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.extra.Application
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.Job
//import com.zktony.serialport.BuildConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingRoute(viewModel: SettingViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current

    val application by viewModel.application.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val page by viewModel.page.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()

    val job by viewModel.job.collectAsStateWithLifecycle()
    val complate by viewModel.complate.collectAsStateWithLifecycle()
    val process by viewModel.progress.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val proEntities = viewModel.proEntities.collectAsLazyPagingItems()
    val erroeEntities = viewModel.errorEntities.collectAsLazyPagingItems()

    val slEntitiy by viewModel.slEntitiy.collectAsStateWithLifecycle(initialValue = null)
    val ncEntitiy by viewModel.ncEntitiy.collectAsStateWithLifecycle(initialValue = null)
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.SETTINGS -> navigationActions.navigateUp()
                else -> viewModel.dispatch(SettingIntent.NavTo(PageType.SETTINGS))
            }
        }
    }

    BackHandler { navigation() }

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
                        application,
                        progress,
                        viewModel::dispatch,
                        erroeEntities
                    )

                    PageType.DEBUGMODE -> debug(
                        viewModel::dispatch,
                        proEntities.toList(),
                        slEntitiy,
                        job,
                        uiFlags,
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
    application: Application?,
    progress: Int,
    uiEvent: (SettingIntent) -> Unit,
    erroeEntities: LazyPagingItems<ErrorRecord>
) {
    var setting = s1 ?: Setting()
    Log.d("","setting====$setting")

    var newCalibration = c1 ?: NewCalibration()

    val context = LocalContext.current

    val keyboard = LocalSoftwareKeyboardController.current

    var switchColum by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    var navigation by rememberDataSaverState(key = Constants.NAVIGATION, default = false)

    var selectRudio = rememberDataSaverState(key = "selectRudio", default = 1)

    //================密码相关=============================
    var pwdShow by remember { mutableStateOf(false) }

    /**
     * 设备管理员
     */
    val deviceAdminPwd = rememberDataSaverState(key = "deviceAdminPwd", default = "123456")

    /**
     * 超级管理员
     */
    val superAdminPwd = rememberDataSaverState(key = "superAdminPwd", default = "234567")

    /**
     * 厂家管理员
     */
    val factoryAdminPwd = rememberDataSaverState(key = "superAdminPwd", default = "345678")

    /**
     * 当前使用密码
     */
    val currentPwd = rememberDataSaverState(key = "currentPwd", default = "")

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


    /**
     * 配件的弹窗
     */
    val accessoriesDialog = remember(setting) { mutableStateOf(false) }

    //===============配件寿命==============================


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
    //===============位置设置==============================


    //================预排设置=============================
    //高浓度
    /**
     * 高浓度清洗液量
     */

    var higeCleanVolume_ex by remember(setting) { mutableStateOf(setting.higeCleanVolume.toString()) }

    /**
     * 高浓度预排液量
     */
    var higeRehearsalVolume_ex by remember(setting) { mutableStateOf(setting.higeRehearsalVolume.toString()) }

    /**
     * 高浓度管路填充
     */
    var higeFilling_ex by remember(setting) { mutableStateOf(setting.higeFilling.toString()) }

    //高浓度

    //低浓度
    /**
     * 低浓度清洗液量
     */
    var lowCleanVolume_ex by remember(setting) { mutableStateOf(setting.lowCleanVolume.toString()) }


    /**
     * 低浓度管路填充
     */
    var lowFilling_ex by remember(setting) { mutableStateOf(setting.lowFilling.toString()) }
    //低浓度

    //冲洗液泵
    /**
     * 冲洗液泵清洗液量
     */
    var rinseCleanVolume_ex by remember(setting) { mutableStateOf(setting.rinseCleanVolume.toString()) }


    /**
     * 冲洗液泵管路填充
     */
    var rinseFilling_ex by remember(setting) { mutableStateOf(setting.rinseFilling.toString()) }
    //冲洗液泵

    //促凝剂泵
    /**
     * 促凝剂泵清洗液量
     */
    var coagulantCleanVolume_ex by remember(setting) { mutableStateOf(setting.coagulantCleanVolume.toString()) }

    /**
     * 促凝剂泵管路填充
     */
    var coagulantFilling_ex by remember(setting) { mutableStateOf(setting.coagulantFilling.toString()) }
    //促凝剂泵

    /**
     * 预排恢复默认弹窗
     */
    val expectedResetDialog = remember(setting) { mutableStateOf(false) }


    //================预排设置=============================


    //================校准设置=============================
    //高浓度
    /**
     * 加液量1
     */
    var higeLiquidVolume1_ex by remember(newCalibration) { mutableStateOf(newCalibration.higeLiquidVolume1.toString()) }

    /**
     * 加液量2
     */
    var higeLiquidVolume2_ex by remember(newCalibration) { mutableStateOf(newCalibration.higeLiquidVolume2.toString()) }


    /**
     * 加液量3
     */
    var higeLiquidVolume3_ex by remember(newCalibration) { mutableStateOf(newCalibration.higeLiquidVolume3.toString()) }
    //高浓度


    //低浓度

    /**
     * 加液量1
     */
    var lowLiquidVolume1_ex by remember(newCalibration) { mutableStateOf(newCalibration.lowLiquidVolume1.toString()) }

    /**
     * 加液量2
     */
    var lowLiquidVolume2_ex by remember(newCalibration) { mutableStateOf(newCalibration.lowLiquidVolume2.toString()) }


    /**
     * 加液量3
     */
    var lowLiquidVolume3_ex by remember(newCalibration) { mutableStateOf(newCalibration.lowLiquidVolume3.toString()) }
    //低浓度

    //冲洗液泵

    /**
     * 加液量1
     */
    var rinseLiquidVolume1_ex by remember(newCalibration) { mutableStateOf(newCalibration.rinseLiquidVolume1.toString()) }

    /**
     * 加液量2
     */
    var rinseLiquidVolume2_ex by remember(newCalibration) { mutableStateOf(newCalibration.rinseLiquidVolume2.toString()) }


    /**
     * 加液量3
     */
    var rinseLiquidVolume3_ex by remember(newCalibration) { mutableStateOf(newCalibration.rinseLiquidVolume3.toString()) }
    //冲洗液泵

    //促凝剂泵

    /**
     * 加液量1
     */
    var coagulantLiquidVolume1_ex by remember(newCalibration) { mutableStateOf(newCalibration.coagulantLiquidVolume1.toString()) }

    /**
     * 加液量2
     */

    var coagulantLiquidVolume2_ex by remember(newCalibration) { mutableStateOf(newCalibration.coagulantLiquidVolume2.toString()) }


    /**
     * 加液量3
     */

    var coagulantLiquidVolume3_ex by remember(newCalibration) { mutableStateOf(newCalibration.coagulantLiquidVolume3.toString()) }
    //促凝剂泵


    /**
     * 校准恢复默认弹窗
     */
    val calibrationResetDialog = remember(newCalibration) { mutableStateOf(false) }
    //================校准设置=============================


    //================校准数据=============================

    /**
     * 促凝剂步数
     */
    val coagulantpulse = rememberDataSaverState(key = "coagulantpulse", default = 550000)


    //================校准数据=============================


    var selectedIndex by remember { mutableStateOf(0) }
    //	定义列宽
    val cellWidthList = arrayListOf(70, 115, 241)


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
                            if (switchColum == 0) Color(
                                0, 105, 52
                            ) else Color.White
                        )
                        .clickable {
                            switchColum = 0
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp),
                        color = if (switchColum == 0) Color.White else Color(rgb(112, 112, 112)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        text = "预"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 45.dp),
                        color = if (switchColum == 0) Color.White else Color(rgb(112, 112, 112)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        text = "排"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 68.dp),
                        color = if (switchColum == 0) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "设"
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 90.dp),
                        color = if (switchColum == 0) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "置"
                    )
                }

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
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "校"
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 45.dp),
                        color = if (switchColum == 1) Color.White else Color(rgb(112, 112, 112)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        text = "准"
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
                        .padding(top = 36.2.dp)
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
                        .padding(top = 36.2.dp)
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

            if (switchColum == 0) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, start = 50.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(280.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                text = "高浓度泵",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = higeCleanVolume_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "清洗液量/mL", fontSize = 14.sp) },
                                onValueChange = {
                                    higeCleanVolume_ex = it

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
                                modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                                value = higeRehearsalVolume_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "预排液量/mL", fontSize = 14.sp) },
                                onValueChange = {
                                    higeRehearsalVolume_ex = it
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
                                modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                                value = higeFilling_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "管路填充/mL", fontSize = 14.sp) },
                                onValueChange = {
                                    higeFilling_ex = it
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
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(220.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                text = "低浓度泵",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )


                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = lowCleanVolume_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "清洗液量/mL", fontSize = 14.sp) },
                                onValueChange = { lowCleanVolume_ex = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )


                            OutlinedTextField(
                                modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                                value = lowFilling_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "管路填充/mL", fontSize = 14.sp) },
                                onValueChange = { lowFilling_ex = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )
                        }

                    }

                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(220.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                text = "冲洗液泵",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = rinseCleanVolume_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "冲洗液量/mL", fontSize = 14.sp) },
                                onValueChange = { rinseCleanVolume_ex = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )


                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = rinseFilling_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "管路填充/mL", fontSize = 14.sp) },
                                onValueChange = { rinseFilling_ex = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )

                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(220.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                text = "促凝剂泵",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = coagulantCleanVolume_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "清洗液量/mL", fontSize = 14.sp) },
                                onValueChange = { coagulantCleanVolume_ex = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )


                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = coagulantFilling_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "管路填充/mL", fontSize = 14.sp) },
                                onValueChange = { coagulantFilling_ex = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboard?.hide()
                                })
                            )

                        }


                    }


                    item {
                        Row(
                            modifier = Modifier.padding(top = 20.dp)
                        ) {

                            Button(modifier = Modifier
                                .padding(start = 60.dp)
                                .width(112.0.dp)
                                .height(41.5.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ), shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp), onClick = {
                                if ((higeCleanVolume_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (higeRehearsalVolume_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (higeFilling_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (lowCleanVolume_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (lowFilling_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (rinseCleanVolume_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (coagulantCleanVolume_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (coagulantFilling_ex.toDoubleOrNull() ?: 0.0) > 0
                                ) {
                                    setting.higeCleanVolume =
                                        higeCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                    setting.higeRehearsalVolume =
                                        higeRehearsalVolume_ex.toDoubleOrNull() ?: 0.0
                                    setting.higeFilling = higeFilling_ex.toDoubleOrNull() ?: 0.0
                                    setting.lowCleanVolume =
                                        lowCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                    setting.lowFilling = lowFilling_ex.toDoubleOrNull() ?: 0.0
                                    setting.rinseCleanVolume =
                                        rinseCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                    setting.rinseFilling = rinseFilling_ex.toDoubleOrNull() ?: 0.0
                                    setting.coagulantCleanVolume =
                                        coagulantCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                    setting.coagulantFilling =
                                        coagulantFilling_ex.toDoubleOrNull() ?: 0.0
                                    uiEvent(SettingIntent.UpdateSet(setting))

                                    Toast.makeText(
                                        context, "保存成功！", Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context, "数据不能小于0！", Toast.LENGTH_SHORT
                                    ).show()
                                }


                            }) {
                                Text(text = "保    存", fontSize = 18.sp)
                            }


                            Button(modifier = Modifier
                                .padding(start = 40.dp)
                                .width(120.0.dp)
                                .height(41.5.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ), shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp), onClick = {
                                expectedResetDialog.value = true
                            }) {
                                Text(text = "恢复默认", fontSize = 18.sp)
                            }

                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }


            } else if (switchColum == 1) {
                var rinseSpeed = rememberDataSaverState(key = "rinseSpeed", default = 600L)
                var coagulantSpeed = rememberDataSaverState(key = "coagulantSpeed", default = 200L)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, start = 50.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(290.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {
                            Row {
                                Text(
                                    modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                    text = "高浓度泵",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(modifier = Modifier
                                    .padding(top = 10.dp, start = 150.dp)
                                    .width(100.dp)
                                    .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        scope.launch {
                                            start {
                                                timeOut = 1000L * 30
                                                with(
                                                    index = 2,
                                                    pdv = 51200L * 50,
                                                    ads = Triple(
                                                        rinseSpeed.value * 13,
                                                        rinseSpeed.value * 1193,
                                                        rinseSpeed.value * 1193
                                                    ),

                                                    )
                                            }
                                        }
                                    }) {
                                    Text(text = "加    液", fontSize = 16.sp)
                                }
                            }

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = higeLiquidVolume1_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量1/g", fontSize = 14.sp) },
                                onValueChange = {
                                    higeLiquidVolume1_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = higeLiquidVolume2_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量2/g", fontSize = 14.sp) },
                                onValueChange = {
                                    higeLiquidVolume2_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = higeLiquidVolume3_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量3/g", fontSize = 14.sp) },
                                onValueChange = {
                                    higeLiquidVolume3_ex = it

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
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(310.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {

                            Row {
                                Text(
                                    modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                    text = "低浓度泵",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(modifier = Modifier
                                    .padding(top = 10.dp, start = 150.dp)
                                    .width(100.dp)
                                    .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        scope.launch {
                                            start {
                                                timeOut = 1000L * 30
                                                with(
                                                    index = 3,
                                                    pdv = 51200L * 50,
                                                    ads = Triple(
                                                        rinseSpeed.value * 13,
                                                        rinseSpeed.value * 1193,
                                                        rinseSpeed.value * 1193
                                                    ),

                                                    )
                                            }
                                        }
                                    }) {
                                    Text(text = "加    液", fontSize = 16.sp)
                                }
                            }

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = lowLiquidVolume1_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量1/g", fontSize = 14.sp) },
                                onValueChange = {
                                    lowLiquidVolume1_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = lowLiquidVolume2_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量2/g", fontSize = 14.sp) },
                                onValueChange = {
                                    lowLiquidVolume2_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = lowLiquidVolume3_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量3/g", fontSize = 14.sp) },
                                onValueChange = {
                                    lowLiquidVolume3_ex = it

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
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(310.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {

                            Row {
                                Text(
                                    modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                    text = "冲洗液泵",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(modifier = Modifier
                                    .padding(top = 10.dp, start = 150.dp)
                                    .width(100.dp)
                                    .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        scope.launch {
                                            start {
                                                timeOut = 1000L * 30
                                                with(
                                                    index = 4,
                                                    pdv = 3200L * 50,
                                                    ads = Triple(
                                                        rinseSpeed.value * 13,
                                                        rinseSpeed.value * 1193,
                                                        rinseSpeed.value * 1193
                                                    ),

                                                    )
                                            }
                                        }
                                    }) {
                                    Text(text = "加    液", fontSize = 16.sp)
                                }
                            }

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = rinseLiquidVolume1_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量1/g", fontSize = 14.sp) },
                                onValueChange = {
                                    rinseLiquidVolume1_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = rinseLiquidVolume2_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量2/g", fontSize = 14.sp) },
                                onValueChange = {
                                    rinseLiquidVolume2_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = rinseLiquidVolume3_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量3/g", fontSize = 14.sp) },
                                onValueChange = {
                                    rinseLiquidVolume3_ex = it

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
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .width(400.dp)
                                .height(310.dp)
                                .padding(top = 20.3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    color = Color(rgb(239, 239, 239)),
                                )
                        ) {

                            Row {
                                Text(
                                    modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                                    text = "促凝剂泵",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(modifier = Modifier
                                    .padding(top = 10.dp, start = 150.dp)
                                    .width(100.dp)
                                    .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                    onClick = {
                                        scope.launch {
                                            start {
                                                timeOut = 1000L * 30
                                                with(
                                                    index = 1,
                                                    pdv = coagulantpulse.value.toLong(),
                                                    ads = Triple(
                                                        rinseSpeed.value * 13,
                                                        rinseSpeed.value * 1193,
                                                        rinseSpeed.value * 1193
                                                    ),

                                                    )
                                            }
                                        }
                                    }) {
                                    Text(text = "加    液", fontSize = 16.sp)
                                }
                            }

                            OutlinedTextField(
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = coagulantLiquidVolume1_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量1/g", fontSize = 14.sp) },
                                onValueChange = {
                                    coagulantLiquidVolume1_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = coagulantLiquidVolume2_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量2/g", fontSize = 14.sp) },
                                onValueChange = {
                                    coagulantLiquidVolume2_ex = it

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
                                modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                                value = coagulantLiquidVolume3_ex,
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 18.sp,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "加液量3/g", fontSize = 14.sp) },
                                onValueChange = {
                                    coagulantLiquidVolume3_ex = it

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
                    }

                    item {
                        Row(
                            modifier = Modifier.padding(top = 20.dp)
                        ) {

                            Button(modifier = Modifier
                                .padding(start = 60.dp)
                                .width(112.0.dp)
                                .height(41.5.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ), shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp), onClick = {

                                if ((higeLiquidVolume1_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (higeLiquidVolume2_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (higeLiquidVolume3_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (lowLiquidVolume1_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (lowLiquidVolume2_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (lowLiquidVolume3_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (rinseLiquidVolume1_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (rinseLiquidVolume2_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (rinseLiquidVolume3_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (coagulantLiquidVolume1_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (coagulantLiquidVolume2_ex.toDoubleOrNull() ?: 0.0) > 0 ||
                                    (coagulantLiquidVolume3_ex.toDoubleOrNull() ?: 0.0) > 0
                                ) {
                                    newCalibration.higeLiquidVolume1 =
                                        higeLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.higeLiquidVolume2 =
                                        higeLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.higeLiquidVolume3 =
                                        higeLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    newCalibration.lowLiquidVolume1 =
                                        lowLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.lowLiquidVolume2 =
                                        lowLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.lowLiquidVolume3 =
                                        lowLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    newCalibration.rinseLiquidVolume1 =
                                        rinseLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.rinseLiquidVolume2 =
                                        rinseLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.rinseLiquidVolume3 =
                                        rinseLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    newCalibration.coagulantLiquidVolume1 =
                                        coagulantLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.coagulantLiquidVolume2 =
                                        coagulantLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    newCalibration.coagulantLiquidVolume3 =
                                        coagulantLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    newCalibration.higeAvg =
                                        (newCalibration.higeLiquidVolume1 + newCalibration.higeLiquidVolume2 + newCalibration.higeLiquidVolume3) / 3
                                    newCalibration.lowAvg =
                                        (newCalibration.lowLiquidVolume1 + newCalibration.lowLiquidVolume2 + newCalibration.lowLiquidVolume3) / 3
                                    newCalibration.rinseAvg =
                                        (newCalibration.rinseLiquidVolume1 + newCalibration.rinseLiquidVolume2 + newCalibration.rinseLiquidVolume3) / 3
                                    newCalibration.coagulantAvg =
                                        (newCalibration.coagulantLiquidVolume1 + newCalibration.coagulantLiquidVolume1 + newCalibration.coagulantLiquidVolume1) / 3


                                    AppStateUtils.hpc[0] =
                                        calculateCalibrationFactorNew(64000, 120.0)

                                    AppStateUtils.hpc[1] = calculateCalibrationFactorNew(
                                        coagulantpulse.value, newCalibration.coagulantAvg * 1000
                                    )

                                    AppStateUtils.hpc[2] = calculateCalibrationFactorNew(
                                        51200 * 50, newCalibration.higeAvg * 1000
                                    )

                                    AppStateUtils.hpc[3] = calculateCalibrationFactorNew(
                                        51200 * 50, newCalibration.lowAvg * 1000
                                    )

                                    AppStateUtils.hpc[4] = calculateCalibrationFactorNew(
                                        3200 * 50, newCalibration.rinseAvg * 1000
                                    )



                                    uiEvent(SettingIntent.UpdateNC(newCalibration))

                                    Toast.makeText(
                                        context, "保存成功！", Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context, "数据不能小于0！", Toast.LENGTH_SHORT
                                    ).show()
                                }


                            }) {
                                Text(text = "保    存", fontSize = 18.sp)
                            }


                            Button(modifier = Modifier
                                .padding(start = 40.dp)
                                .width(120.0.dp)
                                .height(41.5.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ), shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp), onClick = {
                                calibrationResetDialog.value = true

                            }) {
                                Text(text = "恢复默认", fontSize = 18.sp)
                            }

                        }
                        Spacer(modifier = Modifier.height(30.dp))
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
                                            239, 239, 239
                                        )
                                    ) else Color.White
                                )
                                .clickable(onClick = {
                                    selectedIndex = index
                                })
                        ) {
                            TableTextBody(text = "" + item.id, width = cellWidthList[0], selected)
                            TableTextBody(
                                text = "" + item.createTime.dateFormat("yyyy-MM-dd"),
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, start = 50.dp),
                ) {

                    /**
                     * 未输入密码或登出
                     */
                    if (currentPwd.value == "") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            VerificationCodeField(digits = 6, inputCallback = {
                                pwdShow = true
                                currentPwd.value = it
                            }) { text, focused ->
                                VerificationCodeItem(text, focused)
                            }
                        }
                    } else {
                        if (factoryAdminPwd.value == currentPwd.value) {
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

                            Row(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .fillMaxWidth()
                            ) {
                                line(Color(rgb(240, 240, 240)), 0f, 400f)
                            }
                        }

                        Row(modifier = Modifier.clickable {

                        }) {
                            Text(
                                modifier = Modifier.padding(top = 20.dp),
                                text = "运行日志",
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

                        Row(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .fillMaxWidth()
                        ) {
                            line(Color(rgb(240, 240, 240)), 0f, 400f)
                        }

                        Row(modifier = Modifier.clickable {
                            uiEvent(SettingIntent.Network)
                        }) {
                            Text(
                                modifier = Modifier.padding(top = 20.dp),
                                text = "网络设置",
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

                        Row(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .fillMaxWidth()
                        ) {
                            line(Color(rgb(240, 240, 240)), 0f, 400f)
                        }

                        Row(modifier = Modifier.clickable {

                        }) {
                            Text(
                                modifier = Modifier.padding(top = 20.dp),
                                text = "系统更新",
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

                        Row(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .fillMaxWidth()
                        ) {
                            line(Color(rgb(240, 240, 240)), 0f, 400f)
                        }

                        if (superAdminPwd.value == currentPwd.value || deviceAdminPwd.value == currentPwd.value) {
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

                            Row(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .fillMaxWidth()
                            ) {
                                line(Color(rgb(240, 240, 240)), 0f, 400f)
                            }
                        }

                        Row(modifier = Modifier.clickable {
                            accessoriesDialog.value = true
                        }) {
                            Text(
                                modifier = Modifier.padding(top = 20.dp),
                                text = "配件寿命",
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

                        Row(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .fillMaxWidth()
                        ) {
                            line(Color(rgb(240, 240, 240)), 0f, 400f)
                        }

                        Row(modifier = Modifier.clickable {
                            positionDialog.value = true
                        }) {
                            Text(
                                modifier = Modifier.padding(top = 20.dp),
                                text = "位置设置",
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
                                        color = Color(rgb(238, 238, 238)), shape = CircleShape
                                    )
                                    .padding(horizontal = 4.dp),
                            ) {
                                AUDIO_DESTINATION.forEach { destination ->
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

                        Row(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .fillMaxWidth()
                        ) {
                            line(Color(rgb(240, 240, 240)), 0f, 400f)
                        }

                        Button(modifier = Modifier
                            .padding(start = 141.dp, top = 60.dp)
                            .width(131.8.dp)
                            .height(41.5.dp), colors = ButtonDefaults.buttonColors(
                            containerColor = Color(rgb(0, 105, 52))
                        ), shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp), onClick = {
                            currentPwd.value = ""
                            switchColum = 0

                        }) {
                            Text(text = "登出", fontSize = 18.sp)
                        }

                    }

                }

            }


        }

        //校准恢复默认弹窗
        if (calibrationResetDialog.value) {
            AlertDialog(onDismissRequest = { }, title = {
                Text(text = "是否恢复默认设置！")
            }, text = {

            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    newCalibration.higeLiquidVolume1 = 0.0
                    newCalibration.higeLiquidVolume2 = 0.0
                    newCalibration.higeLiquidVolume3 = 0.0
                    newCalibration.higeAvg = 0.0
                    newCalibration.lowLiquidVolume1 = 0.0
                    newCalibration.lowLiquidVolume2 = 0.0
                    newCalibration.lowLiquidVolume3 = 0.0
                    newCalibration.lowAvg = 0.0

                    newCalibration.rinseLiquidVolume1 = 0.0
                    newCalibration.rinseLiquidVolume2 = 0.0
                    newCalibration.rinseLiquidVolume3 = 0.0
                    newCalibration.rinseAvg = 0.0

                    newCalibration.coagulantLiquidVolume1 = 0.0
                    newCalibration.coagulantLiquidVolume2 = 0.0
                    newCalibration.coagulantLiquidVolume3 = 0.0
                    newCalibration.coagulantAvg = 0.0


                    higeLiquidVolume1_ex = "0.0"
                    higeLiquidVolume2_ex = "0.0"
                    higeLiquidVolume3_ex = "0.0"

                    lowLiquidVolume1_ex = "0.0"
                    lowLiquidVolume2_ex = "0.0"
                    lowLiquidVolume3_ex = "0.0"

                    rinseLiquidVolume1_ex = "0.0"
                    rinseLiquidVolume2_ex = "0.0"
                    rinseLiquidVolume3_ex = "0.0"

                    coagulantLiquidVolume1_ex = "0.0"
                    coagulantLiquidVolume2_ex = "0.0"
                    coagulantLiquidVolume3_ex = "0.0"

                    AppStateUtils.hpc[1] = calculateCalibrationFactorNew(
                        0, 0.0
                    )

                    AppStateUtils.hpc[2] = calculateCalibrationFactorNew(0, 0.0)

                    AppStateUtils.hpc[3] = calculateCalibrationFactorNew(0, 0.0)

                    AppStateUtils.hpc[4] = calculateCalibrationFactorNew(0, 0.0)
                    calibrationResetDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = { calibrationResetDialog.value = false }) {
                    Text(text = "取消")
                }
            })
        }

        //预排恢复默认弹窗
        if (expectedResetDialog.value) {
            AlertDialog(onDismissRequest = { }, title = {
                Text(text = "是否恢复默认设置！")
            }, text = {

            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    setting.higeCleanVolume = 0.0
                    setting.higeRehearsalVolume = 0.0
                    setting.higeFilling = 0.0
                    setting.lowCleanVolume = 0.0
                    setting.lowFilling = 0.0
                    setting.rinseCleanVolume = 0.0
                    setting.rinseFilling = 0.0
                    setting.coagulantCleanVolume = 0.0
                    setting.coagulantFilling = 0.0
                    uiEvent(SettingIntent.UpdateSet(setting))

                    higeCleanVolume_ex = "0.0"
                    higeRehearsalVolume_ex = "0.0"
                    higeFilling_ex = "0.0"
                    lowCleanVolume_ex = "0.0"
                    lowFilling_ex = "0.0"
                    rinseCleanVolume_ex = "0.0"
                    rinseFilling_ex = "0.0"
                    coagulantCleanVolume_ex = "0.0"
                    coagulantFilling_ex = "0.0"
                    expectedResetDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = { expectedResetDialog.value = false }) {
                    Text(text = "取消")
                }
            })
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
                        onValueChange = { oldPwd.value = it },
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
                        onValueChange = { newPwd.value = it },
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
                        onValueChange = { newPwd2.value = it },
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
                TextButton(colors = ButtonDefaults.buttonColors(
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
                                currentPwd.value = ""
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
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = { updatePwdDialog.value = false }) {
                    Text(text = "取消")
                }
            })
        }

        //配件寿命弹窗
        if (accessoriesDialog.value) {
            AlertDialog(onDismissRequest = { accessoriesDialog.value = false }, title = {
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
                                modifier = Modifier.padding(start = 10.dp),
                                text = setting.highTime.toString()
                            )
                            Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            if (factoryAdminPwd.value == currentPwd.value) {
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
                                    onValueChange = { highTimeExpected_ex = it },
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
                                modifier = Modifier.padding(start = 10.dp),
                                text = setting.lowLife.toString()
                            )
                            Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {

                            if (factoryAdminPwd.value == currentPwd.value) {
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
                                    onValueChange = { lowTimeExpected_ex = it },
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
                                modifier = Modifier.padding(start = 10.dp),
                                text = setting.rinseTime.toString()
                            )
                            Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            if (factoryAdminPwd.value == currentPwd.value) {
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
                                    onValueChange = { rinseTimeExpected_ex = it },
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
                if (factoryAdminPwd.value == currentPwd.value) {
                    TextButton(colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        setting.lowTimeExpected = lowTimeExpected_ex.toDoubleOrNull() ?: 0.0
                        setting.highTimeExpected = highTimeExpected_ex.toDoubleOrNull() ?: 0.0
                        setting.rinseTimeExpected = rinseTimeExpected_ex.toDoubleOrNull() ?: 0.0
                        uiEvent(SettingIntent.UpdateSet(setting))
                        accessoriesDialog.value = false
                    }) {
                        Text(text = "保存")
                    }
                } else {
                    TextButton(colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        setting.highTime = 0.0
                        setting.lowLife = 0.0
                        setting.rinseTime = 0.0
                        uiEvent(SettingIntent.UpdateSet(setting))
                        accessoriesDialog.value = false
                    }) {
                        Text(text = "全部重置")
                    }
                }

            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = { accessoriesDialog.value = false }) {
                    Text(text = "返回")
                }
            })
        }

        //位置设置弹窗
        if (positionDialog.value) {
            AlertDialog(onDismissRequest = { positionDialog.value = false }, title = {
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
                                glueBoardPosition_ex = it
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
                                wastePosition_ex = it
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
                                uiEvent(SettingIntent.UpdateSet(setting))
                                positionDialog.value = false
                            }) {
                            Text(text = "恢复默认", fontSize = 18.sp)
                        }
                    }

                }


            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    setting.wastePosition = wastePosition_ex.toDoubleOrNull() ?: 0.0
                    setting.glueBoardPosition = glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                    uiEvent(SettingIntent.UpdateSet(setting))
                    positionDialog.value = false
                }) {
                    Text(text = "保存")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = { positionDialog.value = false }) {
                    Text(text = "取消")
                }
            })
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
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        debugMode(uiEvent, proEntities, slEntities, job, uiFlags)
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

