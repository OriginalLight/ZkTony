package com.zktony.android.ui

import android.widget.TableRow
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
//import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.ui.components.CoordinateInput
import com.zktony.android.ui.components.DebugModeAppBar
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.MotorItem
import com.zktony.android.ui.components.SettingsAppBar
import com.zktony.android.ui.components.TableText
import com.zktony.android.ui.components.VerificationCodeField
import com.zktony.android.ui.components.VerificationCodeItem
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.items
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.AlgorithmUtils
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactorNew
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.extra.Application
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
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

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val proEntities = viewModel.proEntities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.SETTINGS -> navigationActions.navigateUp()
                PageType.MOTOR_DETAIL -> viewModel.dispatch(SettingIntent.NavTo(PageType.MOTOR_LIST))
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

    Column {
//        HomeAppBar(page) { navigation() }
        DebugModeAppBar(page) {
            navigation()
        }
//        SettingsAppBar(page, viewModel::dispatch) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.SETTINGS -> SettingLits(application, progress, viewModel::dispatch)
                PageType.DEBUGMODE -> debug(
                    viewModel::dispatch, entities.toList(), proEntities.toList(),
                    selected,
                )
//                PageType.SETTINGS -> SettingContent(application, progress, viewModel::dispatch)
                PageType.MOTOR_LIST -> MotorList(entities, viewModel::dispatch)
                PageType.MOTOR_DETAIL -> MotorDetail(
                    entities.toList(),
                    selected,
                    viewModel::dispatch
                )

                PageType.CONFIG -> ConfigList()
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SettingLits(
    application: Application?, progress: Int, uiEvent: (SettingIntent) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val context = LocalContext.current

        val keyboard = LocalSoftwareKeyboardController.current

        var switchColum by remember { mutableStateOf(0) }

        val scope = rememberCoroutineScope()

        var navigation by rememberDataSaverState(key = Constants.NAVIGATION, default = false)

        val audio = arrayListOf("蜂鸣", "语音", "静音")
        var audioThickness = rememberDataSaverState(key = "audioThickness", default = audio[0])

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
         *  高浓度泵使用的时间
         */
        val highTime = rememberDataSaverState(key = "highTime", default = 1.0)

        /**
         *  低浓度泵使用的时间
         */
        val lowLife = rememberDataSaverState(key = "lowTime", default = 1.0)

        /**
         *  冲洗液泵使用的时间
         */
        val rinseTime = rememberDataSaverState(key = "rinseTime", default = 1.0)


        /**
         *  高浓度泵预计使用时间
         */
        val highTimeExpected = rememberDataSaverState(key = "highTimeExpected", default = 500)
        var highTimeExpected_ex by remember { mutableStateOf(highTimeExpected.value.toString()) }

        /**
         *  低浓度泵预计使用时间
         */
        val lowTimeExpected = rememberDataSaverState(key = "lowTimeExpected", default = 500)
        var lowTimeExpected_ex by remember { mutableStateOf(lowTimeExpected.value.toString()) }

        /**
         *  冲洗液泵预计使用时间
         */
        val rinseTimeExpected = rememberDataSaverState(key = "rinseTimeExpected", default = 500)
        var rinseTimeExpected_ex by remember { mutableStateOf(rinseTimeExpected.value.toString()) }

        /**
         * 配件的弹窗
         */
        val accessoriesDialog = remember { mutableStateOf(false) }

        //===============配件寿命==============================


        //===============位置设置==============================
        /**
         * 胶板位置
         */
        val glueBoardPosition = rememberDataSaverState(key = "glueBoardPosition", default = 0.0)
        var glueBoardPosition_ex by remember { mutableStateOf(glueBoardPosition.value.format(1)) }

        /**
         * 废液位置
         */
        val wastePosition = rememberDataSaverState(key = "wastePosition", default = 0.0)
        var wastePosition_ex by remember { mutableStateOf(wastePosition.value.format(1)) }

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
        var higeCleanVolume = rememberDataSaverState(key = "higeCleanVolume", default = 0.0)
        var higeCleanVolume_ex by remember { mutableStateOf(higeCleanVolume.value.format(1)) }


        /**
         * 高浓度预排液量
         */
        var higeRehearsalVolume = rememberDataSaverState(key = "higeRehearsalVolume", default = 0.0)
        var higeRehearsalVolume_ex by remember { mutableStateOf(higeRehearsalVolume.value.format(1)) }

        /**
         * 高浓度管路填充
         */
        var higeFilling = rememberDataSaverState(key = "higeFilling", default = 0.0)
        var higeFilling_ex by remember { mutableStateOf(higeFilling.value.format(1)) }

        //高浓度

        //低浓度
        /**
         * 低浓度清洗液量
         */
        var lowCleanVolume = rememberDataSaverState(key = "lowCleanVolume", default = 0.0)
        var lowCleanVolume_ex by remember { mutableStateOf(lowCleanVolume.value.format(1)) }


        /**
         * 低浓度管路填充
         */
        var lowFilling = rememberDataSaverState(key = "lowFilling", default = 0.0)
        var lowFilling_ex by remember { mutableStateOf(lowFilling.value.format(1)) }
        //低浓度

        //冲洗液泵
        /**
         * 冲洗液泵清洗液量
         */
        var rinseCleanVolume = rememberDataSaverState(key = "rinseCleanVolume", default = 0.0)
        var rinseCleanVolume_ex by remember { mutableStateOf(rinseCleanVolume.value.format(1)) }


        /**
         * 冲洗液泵管路填充
         */
        var rinseFilling = rememberDataSaverState(key = "rinseFilling", default = 0.0)
        var rinseFilling_ex by remember { mutableStateOf(rinseFilling.value.format(1)) }
        //冲洗液泵

        //促凝剂泵
        /**
         * 促凝剂泵清洗液量
         */
        var coagulantCleanVolume =
            rememberDataSaverState(key = "coagulantCleanVolume", default = 0.0)
        var coagulantCleanVolume_ex by remember { mutableStateOf(coagulantCleanVolume.value.format(1)) }

        /**
         * 促凝剂泵管路填充
         */
        var coagulantFilling = rememberDataSaverState(key = "coagulantFilling", default = 0.0)
        var coagulantFilling_ex by remember { mutableStateOf(coagulantFilling.value.format(1)) }
        //促凝剂泵

        //================预排设置=============================


        //================校准设置=============================
        //高浓度
        /**
         * 加液量1
         */
        var higeLiquidVolume1 = rememberDataSaverState(key = "higeLiquidVolume1", default = 0.0)
        var higeLiquidVolume1_ex by remember { mutableStateOf(higeLiquidVolume1.value.format(1)) }

        /**
         * 加液量2
         */
        var higeLiquidVolume2 = rememberDataSaverState(key = "higeLiquidVolume2", default = 0.0)
        var higeLiquidVolume2_ex by remember { mutableStateOf(higeLiquidVolume2.value.format(1)) }


        /**
         * 加液量3
         */
        var higeLiquidVolume3 = rememberDataSaverState(key = "higeLiquidVolume3", default = 0.0)
        var higeLiquidVolume3_ex by remember { mutableStateOf(higeLiquidVolume3.value.format(1)) }
        //高浓度


        //低浓度

        /**
         * 加液量1
         */
        var lowLiquidVolume1 = rememberDataSaverState(key = "lowLiquidVolume1", default = 0.0)
        var lowLiquidVolume1_ex by remember { mutableStateOf(lowLiquidVolume1.value.format(1)) }

        /**
         * 加液量2
         */
        var lowLiquidVolume2 = rememberDataSaverState(key = "lowLiquidVolume2", default = 0.0)
        var lowLiquidVolume2_ex by remember { mutableStateOf(lowLiquidVolume2.value.format(1)) }


        /**
         * 加液量3
         */
        var lowLiquidVolume3 = rememberDataSaverState(key = "lowLiquidVolume3", default = 0.0)
        var lowLiquidVolume3_ex by remember { mutableStateOf(lowLiquidVolume3.value.format(1)) }
        //低浓度

        //冲洗液泵

        /**
         * 加液量1
         */
        var rinseLiquidVolume1 = rememberDataSaverState(key = "rinseLiquidVolume1", default = 0.0)
        var rinseLiquidVolume1_ex by remember { mutableStateOf(rinseLiquidVolume1.value.format(1)) }

        /**
         * 加液量2
         */
        var rinseLiquidVolume2 = rememberDataSaverState(key = "rinseLiquidVolume2", default = 0.0)
        var rinseLiquidVolume2_ex by remember { mutableStateOf(rinseLiquidVolume2.value.format(1)) }


        /**
         * 加液量3
         */
        var rinseLiquidVolume3 = rememberDataSaverState(key = "rinseLiquidVolume3", default = 0.0)
        var rinseLiquidVolume3_ex by remember { mutableStateOf(rinseLiquidVolume3.value.format(1)) }
        //冲洗液泵

        //促凝剂泵

        /**
         * 加液量1
         */
        var coagulantLiquidVolume1 =
            rememberDataSaverState(key = "coagulantLiquidVolume1", default = 0.0)
        var coagulantLiquidVolume1_ex by remember {
            mutableStateOf(
                coagulantLiquidVolume1.value.format(
                    1
                )
            )
        }

        /**
         * 加液量2
         */
        var coagulantLiquidVolume2 =
            rememberDataSaverState(key = "coagulantLiquidVolume2", default = 0.0)
        var coagulantLiquidVolume2_ex by remember {
            mutableStateOf(
                coagulantLiquidVolume2.value.format(
                    1
                )
            )
        }


        /**
         * 加液量3
         */
        var coagulantLiquidVolume3 =
            rememberDataSaverState(key = "coagulantLiquidVolume3", default = 0.0)
        var coagulantLiquidVolume3_ex by remember {
            mutableStateOf(
                coagulantLiquidVolume3.value.format(
                    1
                )
            )
        }
        //促凝剂泵

        //================校准设置=============================


        //================校准数据=============================

        /**
         * 促凝剂步数
         */
        val coagulantpulse = rememberDataSaverState(key = "coagulantpulse", default = 270000)


        //================校准数据=============================


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 20.dp)
            ) {
                Column(modifier = Modifier
                    .padding(top = 100.dp)
                    .clickable {
                        switchColum = 0
                    }) {
                    Text(
                        color = if (switchColum == 0) Color.Red else Color.Black, text = "预"
                    )

                    Text(
                        color = if (switchColum == 0) Color.Red else Color.Black, text = "排"
                    )

                    Text(
                        color = if (switchColum == 0) Color.Red else Color.Black, text = "设"
                    )
                    Text(
                        color = if (switchColum == 0) Color.Red else Color.Black, text = "置"
                    )

                }


                Column(modifier = Modifier
                    .padding(top = 100.dp)
                    .clickable {
                        switchColum = 1
                    }) {
                    Text(
                        color = if (switchColum == 1) Color.Red else Color.Black, text = "校"
                    )

                    Text(
                        color = if (switchColum == 1) Color.Red else Color.Black, text = "准"
                    )

                    Text(
                        color = if (switchColum == 1) Color.Red else Color.Black, text = "设"
                    )
                    Text(
                        color = if (switchColum == 1) Color.Red else Color.Black, text = "置"
                    )

                }

                Column(modifier = Modifier
                    .padding(top = 100.dp)
                    .clickable {
                        switchColum = 2
                    }) {
                    Text(
                        color = if (switchColum == 2) Color.Red else Color.Black, text = "故"
                    )

                    Text(
                        color = if (switchColum == 2) Color.Red else Color.Black, text = "障"
                    )

                    Text(
                        color = if (switchColum == 2) Color.Red else Color.Black, text = "记"
                    )
                    Text(
                        color = if (switchColum == 2) Color.Red else Color.Black, text = "录"
                    )

                }
                Column(modifier = Modifier
                    .padding(top = 100.dp)
                    .clickable {
                        switchColum = 3
                    }) {
                    Text(
                        color = if (switchColum == 3) Color.Red else Color.Black, text = "高"
                    )

                    Text(
                        color = if (switchColum == 3) Color.Red else Color.Black, text = "级"
                    )

                    Text(
                        color = if (switchColum == 3) Color.Red else Color.Black, text = "设"
                    )
                    Text(
                        color = if (switchColum == 3) Color.Red else Color.Black, text = "置"
                    )

                }

            }

            if (switchColum == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "高浓度泵", fontSize = 30.sp
                    )
                    OutlinedTextField(value = higeCleanVolume_ex,
                        label = { Text(text = "清洗液量/mL") },
                        onValueChange = {
                            higeCleanVolume_ex = it

                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))

                    OutlinedTextField(value = higeRehearsalVolume_ex,
                        label = { Text(text = "预排液量/mL") },
                        onValueChange = {
                            higeRehearsalVolume_ex = it
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))


                    OutlinedTextField(value = higeFilling_ex,
                        label = { Text(text = "管路填充/mL") },
                        onValueChange = {
                            higeFilling_ex = it
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))


                    Text(
                        text = "低浓度泵", fontSize = 30.sp
                    )


                    OutlinedTextField(value = lowCleanVolume_ex,
                        label = { Text(text = "清洗液量/mL") },
                        onValueChange = { lowCleanVolume_ex = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))


                    OutlinedTextField(value = lowFilling_ex,
                        label = { Text(text = "管路填充/mL") },
                        onValueChange = { lowFilling_ex = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))


                    Text(
                        text = "冲洗液泵", fontSize = 30.sp
                    )


                    OutlinedTextField(value = rinseCleanVolume_ex,
                        label = { Text(text = "冲洗液量/mL") },
                        onValueChange = { rinseCleanVolume_ex = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))


                    OutlinedTextField(value = rinseFilling_ex,
                        label = { Text(text = "管路填充/mL") },
                        onValueChange = { rinseFilling_ex = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))

                    Text(
                        text = "促凝剂泵", fontSize = 30.sp
                    )


                    OutlinedTextField(value = coagulantCleanVolume_ex,
                        label = { Text(text = "清洗液量/mL") },
                        onValueChange = { coagulantCleanVolume_ex = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))


                    OutlinedTextField(value = coagulantFilling_ex,
                        label = { Text(text = "管路填充/mL") },
                        onValueChange = { coagulantFilling_ex = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ))

                    Row(
                        modifier = Modifier.padding(top = 20.dp)
                    ) {

                        Button(modifier = Modifier
                            .padding(start = 10.dp)
                            .width(150.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                higeCleanVolume.value = higeCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                higeRehearsalVolume.value =
                                    higeRehearsalVolume_ex.toDoubleOrNull() ?: 0.0
                                higeFilling.value = higeFilling_ex.toDoubleOrNull() ?: 0.0
                                lowCleanVolume.value = lowCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                lowFilling.value = lowFilling_ex.toDoubleOrNull() ?: 0.0
                                rinseCleanVolume.value = rinseCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                rinseFilling.value = rinseFilling_ex.toDoubleOrNull() ?: 0.0
                                coagulantCleanVolume.value =
                                    coagulantCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                coagulantFilling.value = coagulantFilling_ex.toDoubleOrNull() ?: 0.0

                                Toast.makeText(
                                    context, "保存成功！", Toast.LENGTH_SHORT
                                ).show()

                            }) {
                            Text(text = "保    存", fontSize = 18.sp)
                        }


                        Button(modifier = Modifier
                            .padding(start = 40.dp)
                            .width(150.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                higeCleanVolume.value = 0.0
                                higeRehearsalVolume.value = 0.0
                                higeFilling.value = 0.0
                                lowCleanVolume.value = 0.0
                                lowFilling.value = 0.0
                                rinseCleanVolume.value = 0.0
                                rinseFilling.value = 0.0
                                coagulantCleanVolume.value = 0.0
                                coagulantFilling.value = 0.0


                                higeCleanVolume_ex = "0.0"
                                higeRehearsalVolume_ex = "0.0"
                                higeFilling_ex = "0.0"
                                lowCleanVolume_ex = "0.0"
                                lowFilling_ex = "0.0"
                                rinseCleanVolume_ex = "0.0"
                                rinseFilling_ex = "0.0"
                                coagulantCleanVolume_ex = "0.0"
                                coagulantFilling_ex = "0.0"

                            }) {
                            Text(text = "恢复默认", fontSize = 18.sp)
                        }

                    }


                }


            } else if (switchColum == 1) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Row {
                            Text(
                                text = "高浓度泵", fontSize = 30.sp
                            )

                            Button(modifier = Modifier
                                .padding(start = 30.dp)
                                .width(100.dp)
                                .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        start {
                                            timeOut = 1000L * 30
                                            with(
                                                index = 2,
                                                pdv = 64000L,
                                                ads = Triple(600 * 100, 600 * 100, 600 * 100),

                                                )
                                        }
                                    }
                                }) {
                                Text(text = "加    液", fontSize = 18.sp)
                            }
                        }


                        OutlinedTextField(value = higeLiquidVolume1_ex,
                            label = { Text(text = "加液量1/g") },
                            onValueChange = {
                                higeLiquidVolume1_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = higeLiquidVolume2_ex,
                            label = { Text(text = "加液量2/g") },
                            onValueChange = {
                                higeLiquidVolume2_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = higeLiquidVolume3_ex,
                            label = { Text(text = "加液量3/g") },
                            onValueChange = {
                                higeLiquidVolume3_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))




                        Row {
                            Text(
                                text = "低浓度泵", fontSize = 30.sp
                            )

                            Button(modifier = Modifier
                                .padding(start = 30.dp)
                                .width(100.dp)
                                .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        start {
                                            timeOut = 1000L * 30
                                            with(
                                                index = 3,
                                                pdv = 64000L,
                                                ads = Triple(600 * 100, 600 * 100, 600 * 100),

                                                )
                                        }
                                    }
                                }) {
                                Text(text = "加    液", fontSize = 18.sp)
                            }
                        }

                        OutlinedTextField(value = lowLiquidVolume1_ex,
                            label = { Text(text = "加液量1/g") },
                            onValueChange = {
                                lowLiquidVolume1_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = lowLiquidVolume2_ex,
                            label = { Text(text = "加液量2/g") },
                            onValueChange = {
                                lowLiquidVolume2_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = lowLiquidVolume3_ex,
                            label = { Text(text = "加液量3/g") },
                            onValueChange = {
                                lowLiquidVolume3_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))



                        Row {
                            Text(
                                text = "冲洗液泵", fontSize = 30.sp
                            )

                            Button(modifier = Modifier
                                .padding(start = 30.dp)
                                .width(100.dp)
                                .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        start {
                                            timeOut = 1000L * 30
                                            with(
                                                index = 4,
                                                pdv = 64000L,
                                                ads = Triple(600 * 100, 600 * 100, 600 * 100),

                                                )
                                        }
                                    }
                                }) {
                                Text(text = "加    液", fontSize = 18.sp)
                            }
                        }

                        OutlinedTextField(value = rinseLiquidVolume1_ex,
                            label = { Text(text = "加液量1/g") },
                            onValueChange = {
                                rinseLiquidVolume1_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = rinseLiquidVolume2_ex,
                            label = { Text(text = "加液量2/g") },
                            onValueChange = {
                                rinseLiquidVolume2_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = rinseLiquidVolume3_ex,
                            label = { Text(text = "加液量3/g") },
                            onValueChange = {
                                rinseLiquidVolume3_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))



                        Row {
                            Text(
                                text = "促凝剂泵", fontSize = 30.sp
                            )

                            Button(modifier = Modifier
                                .padding(start = 30.dp)
                                .width(100.dp)
                                .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        start {
                                            timeOut = 1000L * 30
                                            with(
                                                index = 1,
                                                pdv = coagulantpulse.value.toLong(),
                                                ads = Triple(600 * 100, 600 * 100, 600 * 100),

                                                )
                                        }
                                    }
                                }) {
                                Text(text = "加    液", fontSize = 18.sp)
                            }
                        }

                        OutlinedTextField(value = coagulantLiquidVolume1_ex,
                            label = { Text(text = "加液量1/g") },
                            onValueChange = {
                                coagulantLiquidVolume1_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = coagulantLiquidVolume2_ex,
                            label = { Text(text = "加液量2/g") },
                            onValueChange = {
                                coagulantLiquidVolume2_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = coagulantLiquidVolume3_ex,
                            label = { Text(text = "加液量3/g") },
                            onValueChange = {
                                coagulantLiquidVolume3_ex = it

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))




                        Row(
                            modifier = Modifier.padding(top = 20.dp)
                        ) {

                            Button(modifier = Modifier
                                .padding(start = 10.dp)
                                .width(150.dp)
                                .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    higeLiquidVolume1.value =
                                        higeLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    higeLiquidVolume2.value =
                                        higeLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    higeLiquidVolume3.value =
                                        higeLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    lowLiquidVolume1.value =
                                        lowLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    lowLiquidVolume2.value =
                                        lowLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    lowLiquidVolume3.value =
                                        lowLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    rinseLiquidVolume1.value =
                                        rinseLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    rinseLiquidVolume2.value =
                                        rinseLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    rinseLiquidVolume3.value =
                                        rinseLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    coagulantLiquidVolume1.value =
                                        coagulantLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                    coagulantLiquidVolume2.value =
                                        coagulantLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                    coagulantLiquidVolume3.value =
                                        coagulantLiquidVolume3_ex.toDoubleOrNull() ?: 0.0

                                    val higeAvg =
                                        (higeLiquidVolume1.value + higeLiquidVolume2.value + higeLiquidVolume3.value) / 3
                                    val lowAvg =
                                        (lowLiquidVolume1.value + lowLiquidVolume2.value + lowLiquidVolume3.value) / 3
                                    val rinseAvg =
                                        (rinseLiquidVolume1.value + rinseLiquidVolume2.value + rinseLiquidVolume3.value) / 3
                                    val coagulantAvg =
                                        (coagulantLiquidVolume1.value + coagulantLiquidVolume1.value + coagulantLiquidVolume1.value) / 3




                                    AppStateUtils.hpc[0] =
                                        calculateCalibrationFactorNew(64000, 120.0)

                                    AppStateUtils.hpc[1] = calculateCalibrationFactorNew(
                                        coagulantpulse.value,
                                        coagulantAvg * 1000
                                    )

                                    AppStateUtils.hpc[2] =
                                        calculateCalibrationFactorNew(64000, higeAvg * 1000)

                                    AppStateUtils.hpc[3] =
                                        calculateCalibrationFactorNew(64000, lowAvg * 1000)

                                    AppStateUtils.hpc[4] =
                                        calculateCalibrationFactorNew(64000, rinseAvg * 1000)

                                    println("AppStateUtils  hpc=====0====" + AppStateUtils.hpc[0])
                                    println("AppStateUtils  hpc=====1====" + AppStateUtils.hpc[1])
                                    println("AppStateUtils  hpc=====2====" + AppStateUtils.hpc[2])
                                    println("AppStateUtils  hpc=====3====" + AppStateUtils.hpc[3])
                                    println("AppStateUtils  hpc=====4====" + AppStateUtils.hpc[4])


                                    Toast.makeText(
                                        context, "保存成功！", Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                Text(text = "保    存", fontSize = 18.sp)
                            }


                            Button(modifier = Modifier
                                .padding(start = 40.dp)
                                .width(150.dp)
                                .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    higeLiquidVolume1.value = 0.0
                                    higeLiquidVolume2.value = 0.0
                                    higeLiquidVolume3.value = 0.0

                                    lowLiquidVolume1.value = 0.0
                                    lowLiquidVolume2.value = 0.0
                                    lowLiquidVolume3.value = 0.0

                                    rinseLiquidVolume1.value = 0.0
                                    rinseLiquidVolume2.value = 0.0
                                    rinseLiquidVolume3.value = 0.0

                                    coagulantLiquidVolume1.value = 0.0
                                    coagulantLiquidVolume2.value = 0.0
                                    coagulantLiquidVolume3.value = 0.0


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


                                }) {
                                Text(text = "恢复默认", fontSize = 18.sp)
                            }

                        }


                    }
                }
            } else if (switchColum == 2) {


            } else if (switchColum == 3) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    /**
                     * 未输入密码或登出
                     */
                    if (currentPwd.value == "") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding(),
                            contentAlignment = Alignment.Center
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
                                    modifier = Modifier
                                        .padding(top = 50.dp),
                                    text = "导航栏", fontSize = 30.sp
                                )
                                Switch(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .padding(start = 10.dp, top = 70.dp),
                                    checked = navigation,
                                    onCheckedChange = {
                                        scope.launch {
                                            navigation = it
                                            uiEvent(SettingIntent.Navigation(it))
                                        }
                                    })
                            }

                            Text(
                                modifier = Modifier
                                    .padding(top = 50.dp)
                                    .clickable {
                                        uiEvent(SettingIntent.NavTo(PageType.DEBUGMODE))
                                    },
                                text = "调试模式", fontSize = 30.sp
                            )
                        }
                        /**
                         * 设备管理员
                         */
                        Text(
                            modifier = Modifier
                                .padding(top = 50.dp),
                            text = "运行日志", fontSize = 30.sp
                        )
                        Text(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .clickable {
                                    uiEvent(SettingIntent.Network)
                                },
                            text = "网络设置", fontSize = 30.sp
                        )
                        Text(
                            modifier = Modifier
                                .padding(top = 20.dp),
                            text = "系统更新", fontSize = 30.sp
                        )
                        if (superAdminPwd.value == currentPwd.value || deviceAdminPwd.value == currentPwd.value) {
                            Text(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .clickable {
                                        updatePwdDialog.value = true
                                    },
                                text = "修改密码", fontSize = 30.sp
                            )
                        }

                        Text(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .clickable {
                                    accessoriesDialog.value = true
                                },
                            text = "配件寿命", fontSize = 30.sp
                        )
                        Text(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .clickable {
                                    positionDialog.value = true
                                },
                            text = "位置设置", fontSize = 30.sp
                        )
                        Text(
                            modifier = Modifier
                                .padding(top = 20.dp),
                            text = "声音设置", fontSize = 30.sp
                        )

                        Row {
                            audio.forEach {
                                Row {
                                    RadioButton(
                                        selected = it == audioThickness.value,
                                        onClick = {
                                            audioThickness.value = it
                                        }
                                    )
                                    Text(text = it)
                                }

                                Spacer(modifier = Modifier.width(20.dp))
                            }
                        }


                        Button(modifier = Modifier
                            .padding(start = 40.dp, top = 60.dp)
                            .width(100.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                currentPwd.value = ""
                                switchColum = 0

                            }) {
                            Text(text = "登出", fontSize = 18.sp)
                        }


                    }

                }

            }


        }


        //修改密码弹窗
        if (updatePwdDialog.value) {
            AlertDialog(
                onDismissRequest = { updatePwdDialog.value = false },
                title = {
                    Text(text = "修改密码")
                },
                text = {
                    Column {

                        OutlinedTextField(value = oldPwd.value,
                            label = { Text(text = "请输入原密码") },
                            onValueChange = { oldPwd.value = it },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = newPwd.value,
                            label = { Text(text = "请输入新密码") },
                            onValueChange = { newPwd.value = it },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                        OutlinedTextField(value = newPwd2.value,
                            label = { Text(text = "请确认新密码") },
                            onValueChange = { newPwd2.value = it },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ))

                    }


                }, confirmButton = {
                    TextButton(onClick = {

                        if (oldPwd.value == deviceAdminPwd.value) {
                            if (newPwd.value == newPwd2.value) {
                                if (newPwd.value.length != 6) {
                                    Toast.makeText(
                                        context,
                                        "新密码要6位数字！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    deviceAdminPwd.value = newPwd.value
                                    updatePwdDialog.value = false
                                    currentPwd.value = ""
                                    switchColum = 0
                                }

                            } else {
                                Toast.makeText(
                                    context,
                                    "新密码不一致！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "原密码错误！",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    }) {
                        Text(text = "确认")
                    }
                }, dismissButton = {
                    TextButton(onClick = { updatePwdDialog.value = false }) {
                        Text(text = "取消")
                    }
                })
        }

        //配件寿命弹窗
        if (accessoriesDialog.value) {
            AlertDialog(
                onDismissRequest = { accessoriesDialog.value = false },
                title = {
                    Text(text = "配件寿命")
                },
                text = {
                    Column {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "高浓度泵", fontSize = 25.sp)
                                Text(modifier = Modifier.padding(start = 10.dp), text = "已使用:")
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = highTime.value.toString()
                                )
                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (factoryAdminPwd.value == currentPwd.value) {
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            highTimeExpected.value = highTimeExpected_ex.toInt()
                                        }
                                    ) {
                                        Text(text = "保    存", fontSize = 18.sp)
                                    }

                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = "预期寿命:"
                                    )

                                    OutlinedTextField(
                                        modifier = Modifier.width(100.dp),
                                        value = highTimeExpected_ex,
                                        label = { },
                                        onValueChange = { highTimeExpected_ex = it },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboard?.hide()
                                            }
                                        ))

                                    Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                                } else {
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            highTime.value = 0.0
                                        }
                                    ) {
                                        Text(text = "重    置", fontSize = 18.sp)
                                    }

                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = "预期寿命:"
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = highTimeExpected.value.toString()
                                    )
                                }

                            }
                        }

                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "低浓度泵", fontSize = 25.sp)
                                Text(modifier = Modifier.padding(start = 10.dp), text = "已使用:")
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = lowLife.value.toString()
                                )
                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                if (factoryAdminPwd.value == currentPwd.value) {
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            lowTimeExpected.value = lowTimeExpected_ex.toInt()
                                        }
                                    ) {
                                        Text(text = "保    存", fontSize = 18.sp)
                                    }

                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = "预期寿命:"
                                    )

                                    OutlinedTextField(
                                        modifier = Modifier.width(100.dp),
                                        value = lowTimeExpected_ex,
                                        label = { },
                                        onValueChange = { lowTimeExpected_ex = it },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboard?.hide()
                                            }
                                        ))

                                    Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                                } else {
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            lowLife.value = 0.0
                                        }
                                    ) {
                                        Text(text = "重    置", fontSize = 18.sp)
                                    }

                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = "预期寿命:"
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = lowTimeExpected.value.toString()
                                    )
                                    Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                                }


                            }
                        }

                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "冲洗液泵", fontSize = 25.sp)
                                Text(modifier = Modifier.padding(start = 10.dp), text = "已使用:")

                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = rinseTime.value.toString()
                                )
                                Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (factoryAdminPwd.value == currentPwd.value) {
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            rinseTimeExpected.value = rinseTimeExpected_ex.toInt()
                                        }
                                    ) {
                                        Text(text = "保    存", fontSize = 18.sp)
                                    }

                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = "预期寿命:"
                                    )

                                    OutlinedTextField(
                                        modifier = Modifier.width(100.dp),
                                        value = rinseTimeExpected_ex,
                                        label = { },
                                        onValueChange = { rinseTimeExpected_ex = it },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboard?.hide()
                                            }
                                        ))

                                    Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                                } else {
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            rinseTime.value = 0.0
                                        }
                                    ) {
                                        Text(text = "重    置", fontSize = 18.sp)
                                    }

                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = "预期寿命:"
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 10.dp),
                                        text = rinseTimeExpected.value.toString()
                                    )
                                    Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                                }

                            }
                        }


                    }


                }, confirmButton = {
                    if (factoryAdminPwd.value == currentPwd.value) {
                        TextButton(onClick = {
                            lowTimeExpected.value = lowTimeExpected_ex.toInt()
                            highTimeExpected.value = highTimeExpected_ex.toInt()
                            rinseTimeExpected.value = rinseTimeExpected_ex.toInt()
                            accessoriesDialog.value = false
                        }) {
                            Text(text = "保存")
                        }
                    } else {
                        TextButton(onClick = {
                            highTime.value = 0.0
                            lowLife.value = 0.0
                            rinseTime.value = 0.0
                            accessoriesDialog.value = false
                        }) {
                            Text(text = "全部重置")
                        }
                    }

                }, dismissButton = {
                    TextButton(onClick = { accessoriesDialog.value = false }) {
                        Text(text = "返回")
                    }
                })
        }

        //位置设置弹窗
        if (positionDialog.value) {
            AlertDialog(
                onDismissRequest = { positionDialog.value = false },
                title = {
                    Text(text = "位置设置")
                },
                text = {
                    Column {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(value = glueBoardPosition_ex,
                                label = { Text(text = "胶板位置") },
                                onValueChange = {
                                    glueBoardPosition_ex = it
                                    glueBoardPosition.value =
                                        glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ))

                            Button(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        start {
                                            timeOut = 1000L * 60L
                                            with(
                                                index = 0,
                                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                                pdv = glueBoardPosition.value
                                            )
                                        }
                                    }
                                }
                            ) {
                                Text(text = "移    动", fontSize = 18.sp)
                            }

                        }

                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(value = wastePosition_ex,
                                label = { Text(text = "废液槽位置") },
                                onValueChange = {
                                    wastePosition_ex = it
                                    wastePosition.value =
                                        wastePosition_ex.toDoubleOrNull() ?: 0.0
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ))

                            Button(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        start {
                                            timeOut = 1000L * 60L
                                            with(
                                                index = 0,
                                                ads = Triple(600 * 100, 600 * 100, 600 * 100),
                                                pdv = wastePosition.value
                                            )
                                        }
                                    }

                                }
                            ) {
                                Text(text = "移    动", fontSize = 18.sp)
                            }

                        }
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {

                                    glueBoardPosition.value = 0.0
                                    wastePosition.value = 0.0
                                }
                            ) {
                                Text(text = "恢复默认", fontSize = 18.sp)
                            }
                        }

                    }


                }, confirmButton = {
                    TextButton(onClick = {
                        wastePosition.value = wastePosition_ex.toDoubleOrNull() ?: 0.0
                        glueBoardPosition.value = glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                        positionDialog.value = false

                    }) {
                        Text(text = "保存")
                    }
                }, dismissButton = {
                    TextButton(onClick = { positionDialog.value = false }) {
                        Text(text = "取消")
                    }
                })
        }


    }


}


@Composable
fun SettingContent(
    application: Application?, progress: Int, uiEvent: (SettingIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    var navigation by rememberDataSaverState(key = Constants.NAVIGATION, default = false)
    var helpInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp, color = Color.LightGray, shape = MaterialTheme.shapes.small
                )
                .animateContentSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    icon = Icons.Outlined.Navigation,
                    text = stringResource(id = R.string.navigation)
                ) {
                    Switch(
                        modifier = Modifier.height(32.dp),
                        checked = navigation,
                        onCheckedChange = {
                            scope.launch {
                                navigation = it
                                uiEvent(SettingIntent.Navigation(it))
                            }
                        })
                }
            }

            item {
                SettingsCard(icon = Icons.Outlined.Wifi,
                    text = stringResource(id = R.string.network),
                    onClick = { uiEvent(SettingIntent.Network) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight, contentDescription = null
                    )
                }
            }

            item {
                SettingsCard(icon = Icons.Outlined.Security,
                    text = stringResource(id = R.string.parameters),
                    onClick = { uiEvent(SettingIntent.NavTo(PageType.AUTH)) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight, contentDescription = null
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp, color = Color.LightGray, shape = MaterialTheme.shapes.small
                )
                .animateContentSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    icon = Icons.Outlined.Info, text = stringResource(id = R.string.version)
                ) {
//                    Text(
//                        text = BuildConfig.VERSION_NAME,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic
//                    )
                }
            }

            item {
                SettingsCard(icon = Icons.Outlined.HelpOutline,
                    text = if (helpInfo) stringResource(id = R.string.qrcode) else stringResource(id = R.string.help),
                    onClick = { helpInfo = !helpInfo }) {
                    if (helpInfo) {
                        Text(
                            text = "025-68790636",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowRight, contentDescription = null
                        )
                    }
                }
            }

//            item {
//                val image = if (application == null) {
//                    Icons.Outlined.Sync
//                } else {
//                    if (application.versionCode > BuildConfig.VERSION_CODE) {
//                        Icons.Outlined.Grade
//                    } else {
//                        Icons.Outlined.Verified
//                    }
//                }
//
//                val text = if (application == null) {
//                    stringResource(id = R.string.update)
//                } else {
//                    if (progress == 3) {
//                        if (application.versionCode > BuildConfig.VERSION_CODE) {
//                            stringResource(id = R.string.update_available)
//                        } else {
//                            stringResource(id = R.string.already_latest)
//                        }
//                    } else {
//                        stringResource(id = R.string.downloading)
//                    }
//                }
//
//                SettingsCard(icon = image, text = text, onClick = {
//                    scope.launch {
//                        if (ApplicationUtils.isNetworkAvailable()) {
//                            uiEvent(SettingIntent.CheckUpdate)
//                        } else {
//                            snackbarHostState.showSnackbar(message = "网络不可用")
//                        }
//                    }
//                }) {
//
//                    if (application == null) {
//                        Icon(
//                            imageVector = Icons.Default.Check, contentDescription = null
//                        )
//                    } else {
//                        if (progress == 0) {
//                            if (application.versionCode > BuildConfig.VERSION_CODE) {
//                                Icon(
//                                    imageVector = Icons.Default.ArrowCircleUp,
//                                    contentDescription = null
//                                )
//                            } else {
//                                Icon(
//                                    imageVector = Icons.Default.Done, contentDescription = null
//                                )
//                            }
//                        } else {
//                            Text(
//                                text = "${progress}%",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold,
//                                fontStyle = FontStyle.Italic
//                            )
//                        }
//                    }
//                }
//            }

            if (helpInfo) {
                // Display the help info
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.Center),
                            painter = painterResource(id = R.mipmap.qrcode),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    onClick: () -> Unit = { },
    icon: ImageVector,
    text: String? = null,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        )
        .clip(MaterialTheme.shapes.medium)
        .clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary
        )
        text?.let {
            Text(
                text = text, style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        content.invoke()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun debug(
    dispatch: (SettingIntent) -> Unit,
    entities: List<Motor>,
    proEntities: List<Program>,
    selected: Long
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        debugMode(dispatch, entities, proEntities, selected)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Authentication(dispatch: (SettingIntent) -> Unit) {

    var show by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    AnimatedContent(targetState = show) {
        if (it) {
            LazyVerticalGrid(
                contentPadding = PaddingValues(16.dp),
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ListItem(modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            scope.launch {
                                dispatch(SettingIntent.NavTo(PageType.MOTOR_LIST))
                            }
                        }, headlineContent = {
                        Text(
                            text = stringResource(id = R.string.motor_config),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }, trailingContent = {
                        Icon(
                            imageVector = Icons.Default.Cyclone, contentDescription = null
                        )
                    }, colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    )
                }
                item {
                    ListItem(modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            scope.launch {
                                dispatch(SettingIntent.NavTo(PageType.CONFIG))
                            }
                        }, headlineContent = {
                        Text(
                            text = stringResource(id = R.string.system_config),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }, trailingContent = {
                        Icon(
                            imageVector = Icons.Default.Tune, contentDescription = null
                        )
                    }, colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(), contentAlignment = Alignment.Center
            ) {
                VerificationCodeField(digits = 6, inputCallback = {
                    show = true
                }) { text, focused ->
                    VerificationCodeItem(text, focused)
                }
            }
        }
    }
}

@Composable
fun MotorList(
    entities: LazyPagingItems<Motor>, dispatch: (SettingIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    LazyVerticalGrid(
        modifier = Modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = GridCells.Fixed(3)
    ) {
        items(items = entities) { item ->
            MotorItem(item = item, onClick = {
                scope.launch {
                    dispatch(SettingIntent.Selected(item.id))
                    dispatch(SettingIntent.NavTo(PageType.MOTOR_DETAIL))
                }
            }, onDelete = {
                scope.launch {
                    dispatch(SettingIntent.Delete(item.id))
                    snackbarHostState.showSnackbar(message = "删除成功")
                }
            })
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MotorDetail(
    entities: List<Motor>, selected: Long, dispatch: (SettingIntent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val motor = entities.find { it.id == selected } ?: Motor(displayText = "None")
    var displayText by remember { mutableStateOf(motor.displayText) }
    var acceleration by remember { mutableStateOf(motor.acceleration.toString()) }
    var deceleration by remember { mutableStateOf(motor.deceleration.toString()) }
    var speed by remember { mutableStateOf(motor.speed.toString()) }
    var index by remember { mutableStateOf(motor.index.toString()) }

    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
    )

    val keyboardActions = KeyboardActions(onDone = {
        softKeyboard?.hide()
        focusManager.clearFocus()
    })

    val colors = TextFieldDefaults.colors(
        unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent
    )

    val textStyle = TextStyle(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        fontFamily = FontFamily.Monospace
    )

    LazyColumn(
        modifier = Modifier.imePadding(),
        contentPadding = PaddingValues(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = index,
                onValueChange = {
                    scope.launch {
                        index = it
                        dispatch(
                            SettingIntent.Update(
                                motor.copy(index = it.toIntOrNull() ?: 0)
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Numbers, contentDescription = null
                        )
                    }
                },
                suffix = {
                    Text(text = "编号", style = textStyle)
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = acceleration,
                onValueChange = {
                    scope.launch {
                        acceleration = it
                        dispatch(
                            SettingIntent.Update(
                                motor.copy(
                                    acceleration = it.toLongOrNull() ?: 0L
                                )
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = stringResource(id = R.string.acceleration)
                        )
                    }
                },
                suffix = {
                    Text(text = stringResource(id = R.string.acceleration), style = textStyle)
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = deceleration,
                onValueChange = {
                    scope.launch {
                        deceleration = it
                        dispatch(
                            SettingIntent.Update(
                                motor.copy(
                                    deceleration = it.toLongOrNull() ?: 0L
                                )
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = stringResource(id = R.string.deceleration)
                        )
                    }
                },
                suffix = {
                    Text(text = stringResource(id = R.string.deceleration), style = textStyle)
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = speed,
                onValueChange = {
                    scope.launch {
                        speed = it
                        dispatch(
                            SettingIntent.Update(
                                motor.copy(
                                    speed = it.toLongOrNull() ?: 0L
                                )
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = stringResource(id = R.string.speed)
                        )
                    }
                },
                suffix = {
                    Text(text = stringResource(id = R.string.speed), style = textStyle)
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = displayText,
                onValueChange = {
                    scope.launch {
                        displayText = it
                        dispatch(SettingIntent.Update(motor.copy(displayText = it)))
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = stringResource(id = R.string.remark)
                        )
                    }
                },
                suffix = {
                    Text(text = stringResource(id = R.string.remark), style = textStyle)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
    }
}


@Composable
fun ConfigList(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.imePadding(),
        contentPadding = PaddingValues(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {

            var abscissa by rememberDataSaverState(key = Constants.ZT_0001, default = 0.0)
            var ordinate by rememberDataSaverState(key = Constants.ZT_0002, default = 0.0)
            var tankAbscissa by rememberDataSaverState(
                key = Constants.ZT_0003, default = 0.0
            )
            var tankOrdinate by rememberDataSaverState(
                key = Constants.ZT_0004, default = 0.0
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CoordinateInput(modifier = Modifier.weight(1f),
                    title = "行程",
                    point = Point(x = abscissa, y = ordinate),
                    onCoordinateChange = {
                        scope.launch {
                            abscissa = it.x
                            ordinate = it.y
                        }
                    }) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = abscissa)
                            with(index = 1, pdv = ordinate)
                        }
                    }
                }
                CoordinateInput(modifier = Modifier.weight(1f),
                    title = "废液槽",
                    point = Point(x = tankAbscissa, y = tankOrdinate),
                    onCoordinateChange = {
                        scope.launch {
                            tankAbscissa = it.x
                            tankOrdinate = it.y
                        }
                    }) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = tankAbscissa)
                            with(index = 1, pdv = tankOrdinate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingsPreview() {
    SettingContent(null, 0) {}
}
