package com.zktony.android.ui

import android.util.Log
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
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.Setting
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
    val slEntities = viewModel.slEntities.collectAsLazyPagingItems()
    val ncEntities = viewModel.ncEntities.collectAsLazyPagingItems()

    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.SETTINGS -> navigationActions.navigateUp()
            }
        }
    }

    BackHandler { navigation() }


    Column {
        DebugModeAppBar(page) {
            navigation()
        }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.SETTINGS -> SettingLits(
                    slEntities.toList(),
                    ncEntities.toList(),
                    application,
                    progress,
                    viewModel::dispatch
                )

                PageType.DEBUGMODE -> debug(
                    viewModel::dispatch, proEntities.toList(), slEntities.toList()
                )

                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SettingLits(
    slEntities: List<Setting>,
    ncEntities: List<NewCalibration>,
    application: Application?,
    progress: Int,
    uiEvent: (SettingIntent) -> Unit
) {

    var setting = slEntities.find {
        it.id == 1L
    } ?: Setting()
    Log.d(
        "Setting",
        "setting=========$setting"
    )
    if (setting.id == 0L) {
        uiEvent(
            SettingIntent.InsertSet(
                0.0, 0.0, 0.0, 500.0, 500.0, 500.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0
            )
        )
    }

    var newCalibration = ncEntities.find {
        it.id == 1L
    } ?: NewCalibration()

    if (newCalibration.id == 0L) {
        uiEvent(
            SettingIntent.InsertNC(
                0.0, 0.0, 0.0, 500.0,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0
            )
        )
    }


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
         *  高浓度泵预计使用时间
         */
        var highTimeExpected_ex by remember { mutableStateOf(setting.highTimeExpected.toString()) }

        /**
         *  低浓度泵预计使用时间
         */
        var lowTimeExpected_ex by remember { mutableStateOf(setting.lowTimeExpected.toString()) }

        /**
         *  冲洗液泵预计使用时间
         */
        var rinseTimeExpected_ex by remember { mutableStateOf(setting.rinseTimeExpected.toString()) }


        /**
         * 配件的弹窗
         */
        val accessoriesDialog = remember { mutableStateOf(false) }

        //===============配件寿命==============================


        //===============位置设置==============================
        /**
         * 胶板位置
         */
        var glueBoardPosition_ex by remember { mutableStateOf(setting.glueBoardPosition.toString()) }

        /**
         * 废液位置
         */
        var wastePosition_ex by remember { mutableStateOf(setting.wastePosition.toString()) }

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
        var higeCleanVolume_ex by remember { mutableStateOf(setting.higeCleanVolume.toString()) }


        /**
         * 高浓度预排液量
         */
        var higeRehearsalVolume_ex by remember { mutableStateOf(setting.higeRehearsalVolume.toString()) }

        /**
         * 高浓度管路填充
         */
        var higeFilling_ex by remember { mutableStateOf(setting.higeFilling.toString()) }

        //高浓度

        //低浓度
        /**
         * 低浓度清洗液量
         */
        var lowCleanVolume_ex by remember { mutableStateOf(setting.lowCleanVolume.toString()) }


        /**
         * 低浓度管路填充
         */
        var lowFilling_ex by remember { mutableStateOf(setting.lowFilling.toString()) }
        //低浓度

        //冲洗液泵
        /**
         * 冲洗液泵清洗液量
         */
        var rinseCleanVolume_ex by remember { mutableStateOf(setting.rinseCleanVolume.toString()) }


        /**
         * 冲洗液泵管路填充
         */
        var rinseFilling_ex by remember { mutableStateOf(setting.rinseFilling.toString()) }
        //冲洗液泵

        //促凝剂泵
        /**
         * 促凝剂泵清洗液量
         */
        var coagulantCleanVolume_ex by remember { mutableStateOf(setting.coagulantCleanVolume.toString()) }

        /**
         * 促凝剂泵管路填充
         */
        var coagulantFilling_ex by remember { mutableStateOf(setting.coagulantFilling.toString()) }
        //促凝剂泵

        /**
         * 预排恢复默认弹窗
         */
        val expectedResetDialog = remember { mutableStateOf(false) }


        //================预排设置=============================


        //================校准设置=============================
        //高浓度
        /**
         * 加液量1
         */
        var higeLiquidVolume1_ex by remember { mutableStateOf(newCalibration.higeLiquidVolume1.toString()) }

        /**
         * 加液量2
         */
        var higeLiquidVolume2_ex by remember { mutableStateOf(newCalibration.higeLiquidVolume2.toString()) }


        /**
         * 加液量3
         */
        var higeLiquidVolume3_ex by remember { mutableStateOf(newCalibration.higeLiquidVolume3.toString()) }
        //高浓度


        //低浓度

        /**
         * 加液量1
         */
        var lowLiquidVolume1_ex by remember { mutableStateOf(newCalibration.lowLiquidVolume1.toString()) }

        /**
         * 加液量2
         */
        var lowLiquidVolume2_ex by remember { mutableStateOf(newCalibration.lowLiquidVolume2.toString()) }


        /**
         * 加液量3
         */
        var lowLiquidVolume3_ex by remember { mutableStateOf(newCalibration.lowLiquidVolume3.toString()) }
        //低浓度

        //冲洗液泵

        /**
         * 加液量1
         */
        var rinseLiquidVolume1_ex by remember { mutableStateOf(newCalibration.rinseLiquidVolume1.toString()) }

        /**
         * 加液量2
         */
        var rinseLiquidVolume2_ex by remember { mutableStateOf(newCalibration.rinseLiquidVolume2.toString()) }


        /**
         * 加液量3
         */
        var rinseLiquidVolume3_ex by remember { mutableStateOf(newCalibration.rinseLiquidVolume3.toString()) }
        //冲洗液泵

        //促凝剂泵

        /**
         * 加液量1
         */
        var coagulantLiquidVolume1_ex by remember { mutableStateOf(newCalibration.coagulantLiquidVolume1.toString()) }

        /**
         * 加液量2
         */

        var coagulantLiquidVolume2_ex by remember { mutableStateOf(newCalibration.coagulantLiquidVolume2.toString()) }


        /**
         * 加液量3
         */

        var coagulantLiquidVolume3_ex by remember { mutableStateOf(newCalibration.coagulantLiquidVolume3.toString()) }
        //促凝剂泵


        /**
         * 校准恢复默认弹窗
         */
        val calibrationResetDialog = remember { mutableStateOf(false) }
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
                                setting.higeCleanVolume = higeCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                setting.higeRehearsalVolume =
                                    higeRehearsalVolume_ex.toDoubleOrNull() ?: 0.0
                                setting.higeFilling = higeFilling_ex.toDoubleOrNull() ?: 0.0
                                setting.lowCleanVolume = lowCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                setting.lowFilling = lowFilling_ex.toDoubleOrNull() ?: 0.0
                                setting.rinseCleanVolume =
                                    rinseCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                setting.rinseFilling = rinseFilling_ex.toDoubleOrNull() ?: 0.0
                                setting.coagulantCleanVolume =
                                    coagulantCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                setting.coagulantFilling =
                                    coagulantFilling_ex.toDoubleOrNull() ?: 0.0
                                Log.d(
                                    "Setting",
                                    "保存的===setting=========$setting"
                                )
                                uiEvent(SettingIntent.UpdateSet(setting))

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
                                expectedResetDialog.value = true
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
                                        coagulantpulse.value,
                                        newCalibration.coagulantAvg * 1000
                                    )

                                    AppStateUtils.hpc[2] =
                                        calculateCalibrationFactorNew(
                                            64000,
                                            newCalibration.higeAvg * 1000
                                        )

                                    AppStateUtils.hpc[3] =
                                        calculateCalibrationFactorNew(
                                            64000,
                                            newCalibration.lowAvg * 1000
                                        )

                                    AppStateUtils.hpc[4] =
                                        calculateCalibrationFactorNew(
                                            64000,
                                            newCalibration.rinseAvg * 1000
                                        )
                                    uiEvent(SettingIntent.UpdateNC(newCalibration))

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
                                    calibrationResetDialog.value = true

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

        //校准恢复默认弹窗
        if (calibrationResetDialog.value) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(text = "是否恢复默认设置！")
                },
                text = {

                }, confirmButton = {
                    TextButton(onClick = {
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
                            0,
                            0.0
                        )

                        AppStateUtils.hpc[2] =
                            calculateCalibrationFactorNew(0, 0.0)

                        AppStateUtils.hpc[3] =
                            calculateCalibrationFactorNew(0, 0.0)

                        AppStateUtils.hpc[4] =
                            calculateCalibrationFactorNew(0, 0.0)
                        calibrationResetDialog.value = false
                    }) {
                        Text(text = "确认")
                    }
                }, dismissButton = {
                    TextButton(onClick = { calibrationResetDialog.value = false }) {
                        Text(text = "取消")
                    }
                })
        }

        //预排恢复默认弹窗
        if (expectedResetDialog.value) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(text = "是否恢复默认设置！")
                },
                text = {

                }, confirmButton = {
                    TextButton(onClick = {
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
                    TextButton(onClick = { expectedResetDialog.value = false }) {
                        Text(text = "取消")
                    }
                })
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
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            setting.highTimeExpected =
                                                highTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                            uiEvent(SettingIntent.UpdateSet(setting))
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
                                            setting.highTime = 0.0
                                            uiEvent(SettingIntent.UpdateSet(setting))
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
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            setting.lowTimeExpected =
                                                lowTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                            uiEvent(SettingIntent.UpdateSet(setting))
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
                                            setting.lowLife = 0.0
                                            uiEvent(SettingIntent.UpdateSet(setting))
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
                                    Button(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                        onClick = {
                                            setting.rinseTimeExpected =
                                                rinseTimeExpected_ex.toDoubleOrNull() ?: 0.0
                                            uiEvent(SettingIntent.UpdateSet(setting))
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
                                            setting.rinseTime = 0.0
                                            uiEvent(SettingIntent.UpdateSet(setting))
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
                                        text = setting.rinseTimeExpected.toString()
                                    )
                                    Text(modifier = Modifier.padding(start = 10.dp), text = "小时")
                                }

                            }
                        }


                    }


                }, confirmButton = {
                    if (factoryAdminPwd.value == currentPwd.value) {
                        TextButton(onClick = {
                            setting.lowTimeExpected = lowTimeExpected_ex.toDoubleOrNull() ?: 0.0
                            setting.highTimeExpected = highTimeExpected_ex.toDoubleOrNull() ?: 0.0
                            setting.rinseTimeExpected = rinseTimeExpected_ex.toDoubleOrNull() ?: 0.0
                            uiEvent(SettingIntent.UpdateSet(setting))
                            accessoriesDialog.value = false
                        }) {
                            Text(text = "保存")
                        }
                    } else {
                        TextButton(onClick = {
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
                            verticalAlignment = CenterVertically
                        ) {
                            OutlinedTextField(value = glueBoardPosition_ex,
                                label = { Text(text = "胶板位置") },
                                onValueChange = {
                                    glueBoardPosition_ex = it
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
                                                pdv = glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
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
                            verticalAlignment = CenterVertically
                        ) {
                            OutlinedTextField(value = wastePosition_ex,
                                label = { Text(text = "废液槽位置") },
                                onValueChange = {
                                    wastePosition_ex = it
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
                                                pdv = wastePosition_ex.toDoubleOrNull() ?: 0.0
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
                            verticalAlignment = CenterVertically
                        ) {
                            Button(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    setting.glueBoardPosition = 0.0
                                    setting.wastePosition = 0.0
                                    uiEvent(SettingIntent.UpdateSet(setting))
                                    positionDialog.value = false
                                }
                            ) {
                                Text(text = "恢复默认", fontSize = 18.sp)
                            }
                        }

                    }


                }, confirmButton = {
                    TextButton(onClick = {
                        setting.wastePosition = wastePosition_ex.toDoubleOrNull() ?: 0.0
                        setting.glueBoardPosition = glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                        uiEvent(SettingIntent.UpdateSet(setting))
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
fun debug(
    uiEvent: (SettingIntent) -> Unit,
    proEntities: List<Program>,
    slEntities: List<Setting>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        debugMode(uiEvent, proEntities, slEntities)
    }
}

