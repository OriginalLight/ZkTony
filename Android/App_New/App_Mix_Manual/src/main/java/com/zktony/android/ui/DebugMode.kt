package com.zktony.android.ui

import android.graphics.Color.rgb
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.Setting
import com.zktony.android.ui.components.DebugModeAppBar
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.line
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.SerialPortUtils.cleanLight
import com.zktony.android.utils.SerialPortUtils.lightFlashYellow
import com.zktony.android.utils.SerialPortUtils.lightGreed
import com.zktony.android.utils.SerialPortUtils.lightRed
import com.zktony.android.utils.SerialPortUtils.lightYellow
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.playAudio
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DebugModeRoute(viewModel: SettingViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val proEntities = viewModel.proEntities.collectAsLazyPagingItems()
    val job by viewModel.job.collectAsStateWithLifecycle()
    val complate by viewModel.complate.collectAsStateWithLifecycle()
    val process by viewModel.progress.collectAsStateWithLifecycle()

    val slEntitiy by viewModel.slEntitiy.collectAsStateWithLifecycle(initialValue = null)


    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
        }
    }

    Column {
//        debugMode(
//            viewModel::dispatch, proEntities.toList(), slEntitiy, job, uiFlags
//        )
//        AnimatedContent(targetState = page) {
//            when (page) {
//                PageType.DEBUGMODE ->
//                else -> {}
//            }
//        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun debugMode(
    uiEvent: (SettingIntent) -> Unit,
    proEntities: List<Program>,
    s1: Setting?,
    job: Job?,
    uiFlags: UiFlags,
) {


    var setting = s1 ?: Setting()

    val scope = rememberCoroutineScope()

    val keyboard = LocalSoftwareKeyboardController.current

    val context = LocalContext.current

    Log.d("debug", "uiFlags====$uiFlags")

    /**
     * 制胶速度，根据这个速度转换其他泵的速度
     */
    val speed = rememberDataSaverState(key = "speed", default = 180)
    var speed_ex by remember { mutableStateOf(speed.value.toString()) }

    /**
     * 胶板位置
     */
    var glueBoardPosition_ex by remember { mutableStateOf(setting.glueBoardPosition.toString()) }

    /**
     * 废液位置
     */
    var wastePosition_ex by remember { mutableStateOf(setting.wastePosition.toString()) }

    //高浓度泵转速
    var higeSpeed by remember { mutableStateOf(0L) }

    //低浓度泵转速
    var lowSpeed by remember { mutableStateOf(0L) }

    //冲洗液泵转速
    var rinseSpeed = rememberDataSaverState(key = "rinseSpeed", default = 600L)
    var rinseSpeed_ex by remember { mutableStateOf(rinseSpeed.value.toString()) }

    //促凝剂泵转速
    var coagulantSpeed = rememberDataSaverState(key = "coagulantSpeed", default = 200L)
    var coagulantSpeed_ex by remember { mutableStateOf(coagulantSpeed.value.toString()) }

    /**
     * 促凝剂步数
     */
    val coagulantpulse = rememberDataSaverState(key = "coagulantpulse", default = 550000)
    var coagulantpulse_ex by remember { mutableStateOf(coagulantpulse.value.toString()) }

    /**
     *促凝剂变速比
     */
    var ratio by remember { mutableStateOf("1") }

    /**
     * 复位等待时间
     */
    val coagulantTime = rememberDataSaverState(key = "coagulantTime", default = 800)
    var coagulantTime_ex by remember { mutableStateOf(coagulantTime.value.toString()) }

    /**
     * 复位后预排步数
     */
    val coagulantResetPulse = rememberDataSaverState(key = "coagulantResetPulse", default = 1500)
    var coagulantResetPulse_ex by remember { mutableStateOf(coagulantResetPulse.value.toString()) }

    /**
     * x轴转速
     */
    val xSpeed = rememberDataSaverState(key = "xSpeed", default = 100L)
    var xSpeed_ex by remember { mutableStateOf(xSpeed.value.toString()) }


    /**
     * 光耦1
     */
    var Optocoupler1 by remember { mutableStateOf(false) }

    /**
     * 光耦2
     */
    var Optocoupler2 by remember { mutableStateOf(false) }

    /**
     * 光耦3
     */
    var Optocoupler3 by remember { mutableStateOf(false) }

    /**
     * 光耦4
     */
    var Optocoupler4 by remember { mutableStateOf(false) }


    /**
     * 状态灯
     */
    var statusLight by remember { mutableStateOf(false) }

    /**
     * 手动控制
     */
    var control by rememberDataSaverState(key = Constants.NAVIGATION, default = false)

    val colors = arrayListOf("红", "黄", "绿")
    var colorsThickness = rememberDataSaverState(key = "colorsThickness", default = colors[0])

    /**
     * 闪烁
     */
    val flashing = arrayListOf("常亮", "闪烁", "关闭")
    var flashingThickness = rememberDataSaverState(key = "flashingThickness", default = flashing[0])


    /**
     * 声音
     */
    var sound by remember { mutableStateOf(false) }


    /**
     * 手动控制
     */
    var soundControl by rememberDataSaverState(key = Constants.NAVIGATION, default = false)


    val sounds = arrayListOf("蜂鸣", "语音", "静音")
    var soundsThickness = rememberDataSaverState(key = "soundsThickness", default = sounds[0])

    //===============制胶弹窗==============================
    /**
     * 制胶的弹窗
     */
    val glueDialog = remember { mutableStateOf(false) }

    /**
     * 制胶数量
     */
    val glueNum = rememberDataSaverState(key = "glueNum", default = 1)
    var glueNum_ex by remember { mutableStateOf(glueNum.value.toString()) }


    val liquid = arrayListOf("是", "否")
    var liquidThickness = remember { mutableStateOf(liquid[0]) }

    var selectIndex by remember { mutableStateOf("") }

    var downMenu by remember { mutableStateOf(false) }
    //===============制胶弹窗==============================


    //===============X轴运行弹窗==============================
    val xDialog = remember { mutableStateOf(false) }

    var xNum by remember { mutableStateOf(1) }
    //===============X轴运行弹窗==============================

    //===============高浓度弹窗==============================
    val highDialog = remember { mutableStateOf(false) }

    var highNum by remember { mutableStateOf(0) }
    //===============高浓度弹窗==============================

    //===============低浓度弹窗==============================
    val lowDialog = remember { mutableStateOf(false) }

    var lowNum by remember { mutableStateOf(0) }
    //===============低浓度弹窗==============================

    //===============低浓度弹窗==============================
    val rinseDialog = remember { mutableStateOf(false) }

    var rinseNum by remember { mutableStateOf(0) }
    //===============低浓度弹窗==============================

    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) {
        uiEvent(SettingIntent.Start(1))
    } else if (uiFlags is UiFlags.Objects && uiFlags.objects == 6) {
        uiEvent(SettingIntent.Stop)
    }

    Column(
        modifier = Modifier
            .padding(start = 26.dp, top = 13.75.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .height(1000.dp)
            .width(540.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(top = 20.dp)
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 25.dp)
                    .size(30.dp)
                    .clickable {
                        uiEvent(SettingIntent.NavTo(PageType.SETTINGS))
                    },
                painter = painterResource(id = R.mipmap.greenarrow),
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 150.dp),
                text = "调试模式",
                fontSize = 20.sp,
                color = Color(rgb(112, 112, 112))
            )
        }

        LazyColumn {
            item {
                Row(
                    modifier = Modifier
                        .padding(start = 5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                    ) {
                        Row {
                            Button(modifier = Modifier
                                .width(100.dp)
                                .height(50.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    if (uiFlags is UiFlags.None) {
                                        uiEvent(SettingIntent.Pipeline)
                                    }
                                }) {
                                Text(text = "填    充", fontSize = 18.sp)
                            }

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    if (uiFlags is UiFlags.None) {
                                        uiEvent(SettingIntent.Clean)
                                    }
                                }) {
                                Text(text = "清    洗", fontSize = 18.sp)
                            }


                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp), colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    if (uiFlags is UiFlags.None) {
                                        uiEvent(SettingIntent.Reset)
                                    }
                                }) {
                                Text(text = "复    位", fontSize = 18.sp)
                            }

                        }

                        Row(
                            modifier = Modifier.padding(top = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                modifier = Modifier.width(100.dp),

                                value = speed_ex,
                                label = { Text(text = "制胶速度") },
                                onValueChange = {
                                    speed_ex = it
                                    speed.value = speed_ex.toIntOrNull() ?: 180
                                    if (speed.value > 600) {
                                        speed.value = 600
                                        speed_ex = "600"
                                    } else if (speed.value < 100) {
                                        speed.value = 100
                                        speed_ex = "100"
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

                            Text(
                                modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                                text = "rpm"
                            )

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    glueDialog.value = true
                                }) {
                                Text(text = "运    行", fontSize = 18.sp)
                            }

                        }

                    }

                    Column(
                        modifier = Modifier.padding(top = 20.dp, start = 50.dp)
                    ) {
                        Button(modifier = Modifier
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "保    存", fontSize = 18.sp)
                        }


                        Button(modifier = Modifier
                            .padding(top = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "重    置", fontSize = 18.sp)
                        }


                        Button(modifier = Modifier
                            .padding(top = 20.dp)
                            .width(120.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "一键清除", fontSize = 18.sp)
                        }


                    }

                }

                Row(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                ) {
                    line(Color(0, 105, 5), 30f, 500f)
                }

            }

            item {
                Column(
                    modifier = Modifier
                        .padding(start = 5.dp, top = 20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                        text = "高浓度泵",
                        fontSize = 20.sp
                    )

                    Row(
                        modifier = Modifier.padding(start = 20.dp)
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),

                            value = higeSpeed.toString(),
                            label = { Text(text = "转速") },
                            onValueChange = {
                                if ((it.toIntOrNull() ?: 0) < 0) {
                                    higeSpeed = 0L
                                } else if ((it.toIntOrNull() ?: 0) > 600) {
                                    higeSpeed = 600L
                                } else {
                                    higeSpeed = it.toLongOrNull() ?: 0L
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
                        Text(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "rpm"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                highDialog.value = true
                            }) {
                            Text(text = "运    行", fontSize = 18.sp)
                        }

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                scope.launch {
                                    SerialPortUtils.start {
                                        timeOut = 1000L * 30
                                        with(
                                            index = 2,
                                            pdv = -32000L,
                                            ads = Triple(600 * 100, 600 * 100, higeSpeed),
                                        )
                                    }
                                }
                            }) {
                            Text(text = "反    向", fontSize = 18.sp)
                        }


                    }

                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                        text = "低浓度泵",
                        fontSize = 20.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),

                            value = lowSpeed.toString(),
                            label = { Text(text = "转速") },
                            onValueChange = {
                                if ((it.toIntOrNull() ?: 0) < 0) {
                                    lowSpeed = 0L
                                } else if ((it.toIntOrNull() ?: 0) > 600) {
                                    lowSpeed = 600L
                                } else {
                                    lowSpeed = it.toLongOrNull() ?: 0L
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
                        Text(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "rpm"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                lowDialog.value = true
                            }) {
                            Text(text = "运    行", fontSize = 18.sp)
                        }

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                scope.launch {
                                    SerialPortUtils.start {
                                        timeOut = 1000L * 30
                                        with(
                                            index = 3,
                                            pdv = -32000L,
                                            ads = Triple(600 * 100, 600 * 100, lowSpeed),
                                        )
                                    }
                                }
                            }) {
                            Text(text = "反    向", fontSize = 18.sp)
                        }


                    }

                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                        text = "冲洗液泵",
                        fontSize = 20.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),

                            value = rinseSpeed_ex,
                            label = { Text(text = "转速") },
                            onValueChange = {
                                rinseSpeed_ex = it
                                rinseSpeed.value = rinseSpeed_ex.toLongOrNull() ?: 600L
                                if (rinseSpeed.value > 600L) {
                                    rinseSpeed.value = 600L
                                    rinseSpeed_ex = "600"
                                } else if (rinseSpeed.value < 0) {
                                    rinseSpeed.value = 0
                                    rinseSpeed_ex = "0"
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
                        Text(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "rpm"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                rinseDialog.value = true
                            }) {
                            Text(text = "运    行", fontSize = 18.sp)
                        }

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                scope.launch {
                                    SerialPortUtils.start {
                                        timeOut = 1000L * 30
                                        with(
                                            index = 4,
                                            pdv = -32000L,
                                            ads = Triple(
                                                600 * 100, 600 * 100, rinseSpeed.value
                                            ),
                                        )
                                    }
                                }
                            }) {
                            Text(text = "反    向", fontSize = 18.sp)
                        }


                    }

                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                        text = "促凝剂泵",
                        fontSize = 20.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),

                            value = coagulantSpeed_ex,
                            label = { Text(text = "转速") },
                            onValueChange = {
                                coagulantSpeed_ex = it
                                coagulantSpeed.value = coagulantSpeed_ex.toLongOrNull() ?: 200L
                                if (coagulantSpeed.value > 200L) {
                                    coagulantSpeed.value = 200L
                                    coagulantSpeed_ex = "200"
                                } else if (coagulantSpeed.value < 0) {
                                    coagulantSpeed.value = 0
                                    coagulantSpeed_ex = "0"
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
                        Text(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "rpm"
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp),

                            value = coagulantpulse_ex,
                            label = { Text(text = "总行程") },
                            onValueChange = {
                                coagulantpulse_ex = it
                                coagulantpulse.value = coagulantpulse_ex.toIntOrNull() ?: 0
                                if (coagulantpulse.value > 550000) {
                                    coagulantpulse.value = 550000
                                    coagulantpulse_ex = "550000"
                                } else if (coagulantpulse.value < 200000) {
                                    coagulantSpeed.value = 200000
                                    coagulantpulse_ex = "200000"
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
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .width(150.dp),
                            enabled = false,
                            value = ratio,
                            label = { Text(text = "促凝剂变速比") },
                            onValueChange = {

                            }
                        )

                    }


                }

            }

            item {
                Column(
                    modifier = Modifier
                        .padding(start = 5.dp, top = 20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 20.dp)
                    ) {
                        OutlinedTextField(

                            modifier = Modifier.width(200.dp),
                            value = coagulantTime_ex,
                            label = { Text(text = "复位等待时间") },
                            onValueChange = {
                                coagulantTime_ex = it
                                coagulantTime.value = coagulantTime_ex.toIntOrNull() ?: 800
                                if (coagulantTime.value < 0) {
                                    coagulantTime.value = 0
                                    coagulantTime_ex = "0"
                                } else if (coagulantTime.value > 2000) {
                                    coagulantTime.value = 2000
                                    coagulantTime_ex = "2000"
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
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .width(200.dp),

                            value = coagulantResetPulse_ex,
                            label = { Text(text = "复位后预排步数") },
                            onValueChange = {
                                coagulantResetPulse_ex = it
                                coagulantResetPulse.value =
                                    coagulantResetPulse_ex.toIntOrNull() ?: 0
                                if (coagulantResetPulse.value < 0) {
                                    coagulantResetPulse.value = 0
                                    coagulantResetPulse_ex = "0"
                                } else if (coagulantResetPulse.value > 3000) {
                                    coagulantResetPulse.value = 3000
                                    coagulantResetPulse_ex = "3000"
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

                    Row(
                        modifier = Modifier
                            .padding(top = 20.dp, start = 20.dp)
                    ) {
                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                scope.launch {
                                    SerialPortUtils.start {
                                        timeOut = 1000L * 30
                                        with(
                                            index = 1,
                                            pdv = coagulantpulse.value.toLong(),
                                            ads = Triple(
                                                coagulantSpeed.value * 13,
                                                coagulantSpeed.value * 1193,
                                                coagulantSpeed.value * 1193
                                            ),
                                        )
                                    }

                                }
                            }) {
                            Text(text = "运    动", fontSize = 18.sp)
                        }

                        Button(modifier = Modifier
                            .padding(start = 100.dp)
                            .width(100.dp)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {
                                if (uiFlags is UiFlags.None) {
                                    uiEvent(SettingIntent.ZSReset)
                                }
                            }) {
                            Text(text = "复    位", fontSize = 18.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .fillMaxWidth()
                    ) {
                        line(Color(0, 105, 5), 30f, 500f)
                    }
                }

            }

            item {
                Row(
                    modifier = Modifier
                        .padding(start = 5.dp, top = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {

                        Text(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                            text = "X轴电机",
                            fontSize = 20.sp
                        )

                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp)
                        ) {

                            OutlinedTextField(
                                modifier = Modifier.width(100.dp),
                                value = xSpeed_ex,

                                label = { Text(text = "转速") },
                                onValueChange = {
                                    xSpeed_ex = it
                                    xSpeed.value = xSpeed_ex.toLongOrNull() ?: 0L
                                    if (xSpeed.value < 0) {
                                        xSpeed.value = 0L
                                        xSpeed_ex = "0"
                                    } else if (xSpeed.value > 200) {
                                        xSpeed.value = 200L
                                        xSpeed_ex = "200"
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
                            Text(
                                modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                                text = "rpm"
                            )

                        }


                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp)
                        ) {

                            OutlinedTextField(
                                modifier = Modifier.width(100.dp),
                                value = wastePosition_ex,

                                label = { Text(text = "废液槽坐标") },
                                onValueChange = {
                                    wastePosition_ex = it
                                    setting.wastePosition = wastePosition_ex.toDoubleOrNull() ?: 0.0
                                    if (setting.wastePosition < 0) {
                                        setting.wastePosition = 0.0
                                        wastePosition_ex = "0"
                                    } else if (setting.wastePosition > 32) {
                                        setting.wastePosition = 32.0
                                        wastePosition_ex = "32"
                                    }
                                    uiEvent(SettingIntent.UpdateSet(setting))
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
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        SerialPortUtils.start {
                                            timeOut = 1000L * 60L
                                            with(
                                                index = 0,
                                                ads = Triple(
                                                    xSpeed.value,
                                                    xSpeed.value,
                                                    xSpeed.value
                                                ),
                                                pdv = setting.wastePosition
                                            )
                                        }
                                    }
                                }) {
                                Text(text = "移    动", fontSize = 18.sp)
                            }

                        }


                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp)
                        ) {

                            OutlinedTextField(
                                modifier = Modifier.width(100.dp),
                                value = glueBoardPosition_ex,

                                label = { Text(text = "制胶架坐标") },
                                onValueChange = {
                                    glueBoardPosition_ex = it
                                    setting.glueBoardPosition =
                                        glueBoardPosition_ex.toDoubleOrNull() ?: 0.0
                                    if (setting.glueBoardPosition < 0) {
                                        setting.glueBoardPosition = 0.0
                                        glueBoardPosition_ex = "0"
                                    } else if (setting.glueBoardPosition > 32) {
                                        setting.glueBoardPosition = 32.0
                                        glueBoardPosition_ex = "32"
                                    }
                                    uiEvent(SettingIntent.UpdateSet(setting))
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
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    scope.launch {
                                        SerialPortUtils.start {
                                            timeOut = 1000L * 60L
                                            with(
                                                index = 0,
                                                ads = Triple(
                                                    xSpeed.value,
                                                    xSpeed.value,
                                                    xSpeed.value
                                                ),
                                                pdv = setting.glueBoardPosition
                                            )
                                        }
                                    }
                                }) {
                                Text(text = "移    动", fontSize = 18.sp)
                            }

                        }

                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp)
                        ) {

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    xDialog.value = true
                                }) {
                                Text(text = "运    行", fontSize = 18.sp)
                            }

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {
                                    if (uiFlags is UiFlags.None) {
                                        uiEvent(SettingIntent.XReset)
                                    }
                                }) {
                                Text(text = "复    位", fontSize = 18.sp)
                            }


                        }


                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .fillMaxWidth(1f)
                    ) {
                        Text(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                            text = "光耦",
                            fontSize = 20.sp
                        )

                        Row(
                            modifier = Modifier.padding(start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "光耦1"
                            )

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {

                                }) {
                                Text(text = "检    测", fontSize = 18.sp)
                            }

                            Checkbox(checked = Optocoupler1, enabled = false, onCheckedChange = {
                                Optocoupler1 = it
                            })
                        }

                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "光耦2"
                            )

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {

                                }) {
                                Text(text = "检    测", fontSize = 18.sp)
                            }

                            Checkbox(checked = Optocoupler2, enabled = false, onCheckedChange = {
                                Optocoupler2 = it
                            })
                        }

                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "光耦3"
                            )

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {

                                }) {
                                Text(text = "检    测", fontSize = 18.sp)
                            }

                            Checkbox(checked = Optocoupler3, enabled = false, onCheckedChange = {
                                Optocoupler3 = it
                            })
                        }

                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "光耦4"
                            )

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {

                                }) {
                                Text(text = "检    测", fontSize = 18.sp)
                            }

                            Checkbox(checked = Optocoupler4, enabled = false, onCheckedChange = {
                                Optocoupler4 = it
                            })
                        }

                    }
                }
                Row(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                ) {
                    line(Color(0, 105, 5), 30f, 500f)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(start = 5.dp, top = 20.dp)
                        .height(220.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(220.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "状态灯"
                            )

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {

                                }) {
                                Text(text = "检    测", fontSize = 18.sp)
                            }

                            Checkbox(checked = statusLight, enabled = false, onCheckedChange = {
                                statusLight = it
                            })
                        }


                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "手动控制"
                            )
                            Switch(colors = SwitchDefaults.colors(
                                checkedTrackColor = Color(rgb(0, 105, 52)),
                            ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .padding(start = 10.dp),
                                checked = control,
                                onCheckedChange = {
                                    control = it
                                })
                        }

                        Row(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colors.forEach {
                                Row {
                                    RadioButton(
                                        colors = RadioButtonDefaults.colors(
                                            Color(
                                                rgb(
                                                    0,
                                                    105,
                                                    52
                                                )
                                            )
                                        ),
                                        enabled = control,
                                        selected = it == colorsThickness.value,
                                        onClick = {
                                            colorsThickness.value = it
                                            if (flashingThickness.value == "常亮") {
                                                if (colorsThickness.value == "红") {
                                                    scope.launch {
                                                        lightRed()
                                                    }
                                                } else if (colorsThickness.value == "黄") {
                                                    scope.launch {
                                                        lightYellow()

                                                    }
                                                } else if (colorsThickness.value == "绿") {
                                                    scope.launch {
                                                        lightGreed()
                                                    }
                                                }
                                            } else if (flashingThickness.value == "闪烁") {
                                                if (colorsThickness.value == "黄") {
                                                    flashingThickness.value = "闪烁"
                                                    scope.launch {
                                                        lightFlashYellow()
                                                    }
                                                } else if (colorsThickness.value == "红" || colorsThickness.value == "绿") {
                                                    flashingThickness.value = "常亮"
                                                    if (colorsThickness.value == "红") {
                                                        scope.launch {
                                                            lightRed()
                                                        }
                                                    } else {
                                                        scope.launch {
                                                            lightGreed()
                                                        }
                                                    }
                                                }
                                            }
                                        })
                                    Text(text = it)
                                }

                                Spacer(modifier = Modifier.width(20.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            flashing.forEach {
                                Row {
                                    RadioButton(
                                        colors = RadioButtonDefaults.colors(
                                            Color(
                                                rgb(
                                                    0,
                                                    105,
                                                    52
                                                )
                                            )
                                        ),
                                        enabled = control,
                                        selected = it == flashingThickness.value,
                                        onClick = {
                                            flashingThickness.value = it
                                            if (flashingThickness.value == "闪烁") {
                                                colorsThickness.value = "黄"
                                                scope.launch {
                                                    lightFlashYellow()
                                                }
                                            } else if (flashingThickness.value == "常亮") {
                                                if (colorsThickness.value == "红") {
                                                    scope.launch {
                                                        lightRed()

                                                    }
                                                } else if (colorsThickness.value == "黄") {
                                                    scope.launch {
                                                        lightYellow()

                                                    }
                                                } else if (colorsThickness.value == "绿") {
                                                    scope.launch {
                                                        lightGreed()
                                                    }
                                                }

                                            } else {
                                                scope.launch {
                                                    cleanLight()
                                                }
                                            }
                                        })
                                    Text(text = it)
                                }

                            }
                        }


                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .fillMaxWidth(1f)
                            .height(220.dp)
                    ) {

                        Row(
                            modifier = Modifier.padding(start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "声音"
                            )

                            Button(modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(rgb(0, 105, 52))
                                ),
                                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                                onClick = {

                                }) {
                                Text(text = "检    测", fontSize = 18.sp)
                            }

                            Checkbox(checked = sound, enabled = false, onCheckedChange = {
                                sound = it
                            })
                        }

                        Row(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                                text = "手动控制"
                            )
                            Switch(
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(rgb(0, 105, 52)),
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .padding(start = 10.dp),
                                checked = soundControl,
                                onCheckedChange = {
                                    soundControl = it
                                })
                        }

                        Row(
                            modifier = Modifier.padding(top = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            sounds.forEach {
                                Row {
                                    RadioButton(
                                        colors = RadioButtonDefaults.colors(
                                            Color(
                                                rgb(
                                                    0,
                                                    105,
                                                    52
                                                )
                                            )
                                        ),
                                        enabled = soundControl,
                                        selected = it == soundsThickness.value,
                                        onClick = {
                                            soundsThickness.value = it
                                        })
                                    Text(text = it)
                                }

                            }
                        }

                    }


                    Spacer(modifier = Modifier.height(20.dp))


                }

            }


        }


    }


    //制胶弹窗
    if (glueDialog.value) {
        AlertDialog(onDismissRequest = { }, title = {}, text = {

            Column {

                Row {
                    Text(text = "请输入制胶数量:")

                    OutlinedTextField(
                        modifier = Modifier.width(100.dp),
                        value = glueNum_ex,
                        label = { Text(text = "不输入默认持续运行") },
                        onValueChange = {
                            glueNum_ex = it
                            glueNum.value = glueNum_ex.toIntOrNull() ?: 1
                            if (glueNum.value < 0) {
                                glueNum.value = 0
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

                Row(
                    modifier = Modifier.padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "是否加液:"
                    )

                    liquid.forEach {
                        Row {
                            RadioButton(
                                colors = RadioButtonDefaults.colors(
                                    Color(
                                        rgb(
                                            0,
                                            105,
                                            52
                                        )
                                    )
                                ),
                                selected = it == liquidThickness.value, onClick = {
                                    liquidThickness.value = it
                                })
                            Text(text = it)
                        }

                        Spacer(modifier = Modifier.width(20.dp))
                    }
                }

                Row(
                    modifier = Modifier.padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "程序名称:"
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 10.dp, start = 20.dp)
                            .clickable {
                                if (proEntities.isNotEmpty()) {
                                    downMenu = true
                                }
                            }, text = if (selectIndex == "") "请选择程序" else selectIndex
                    )


                    DropdownMenu(
                        expanded = downMenu,
                        onDismissRequest = {},
                        modifier = Modifier.width(100.dp)
                    ) {
                        proEntities.forEach {
                            DropdownMenuItem(text = {
                                Text(text = it.displayText)
                            }, onClick = {
                                uiEvent(SettingIntent.Selected(it.id))
                                selectIndex = it.displayText
                                downMenu = false

                            })
                        }
                    }
                }


            }


        }, confirmButton = {
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                if (selectIndex == "" || speed.value == 0) {
                    Toast.makeText(
                        context,
                        "请选择制胶程序或制胶速度不能等于0！",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    uiEvent(SettingIntent.Start(0))
                }
            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                uiEvent(SettingIntent.Stop)
                glueDialog.value = false
            }) {
                Text(text = "停止")
            }
        })
    }

    //X轴运行弹窗
    if (xDialog.value) {
        AlertDialog(onDismissRequest = { xDialog.value = false }, title = {}, text = {
            Row {
                Text(text = "请输入制胶数量:")

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = xNum.toString(),
                    label = { Text(text = "不输入默认运行1次") },
                    onValueChange = {
                        xNum = it.toIntOrNull() ?: 1
                        if (xNum < 0) {
                            xNum = 0
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
            TextButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ),
                enabled = if (job == null) true else false,
                onClick = {
                    for (i in 1..xNum) {
                        scope.launch {
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = setting.wastePosition,
                                    ads = Triple(xSpeed.value, xSpeed.value, xSpeed.value),
                                )
                            }
                            delay(100)
                            SerialPortUtils.start {
                                timeOut = 1000L * 30
                                with(
                                    index = 0,
                                    pdv = setting.glueBoardPosition,
                                    ads = Triple(xSpeed.value, xSpeed.value, xSpeed.value),
                                )
                            }

                            delay(100)
                        }
                    }


                }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                scope.launch {
                    SerialPortUtils.stop(0, 1, 2, 3, 4)
                    xDialog.value = false
                }
            }) {
                Text(text = "停止")
            }
        })
    }

    //高浓度弹窗
    if (highDialog.value) {
        AlertDialog(onDismissRequest = {
            scope.launch {
                SerialPortUtils.stop(0, 1, 2, 3, 4)
                highDialog.value = false
            }
        }, title = {}, text = {
            Row {
                Text(text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = highNum.toString(),


                    label = { Text(text = "不输入默认持续运行") },
                    onValueChange = {
                        highNum = it.toIntOrNull() ?: 0
                        if (highNum < 0) {
                            highNum = 0
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
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                if (highNum == 0) {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 2,
                                pdv = 3200L * 1000,
                                ads = Triple(600 * 100, 600 * 100, higeSpeed * 100),
                            )
                        }
                    }
                } else {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 2,
                                pdv = highNum,
                                ads = Triple(600 * 100, 600 * 100, higeSpeed * 100),
                            )
                        }
                    }
                }

            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                scope.launch {
                    SerialPortUtils.stop(0, 1, 2, 3, 4)
                    highDialog.value = false
                }
            }) {
                Text(text = "停止")
            }
        })
    }

    //低浓度弹窗
    if (lowDialog.value) {
        AlertDialog(onDismissRequest = {
            scope.launch {
                SerialPortUtils.stop(0, 1, 2, 3, 4)
                lowDialog.value = false
            }
        }, title = {}, text = {
            Row {
                Text(text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = lowNum.toString(),
                    label = { Text(text = "不输入默认持续运行") },
                    onValueChange = {
                        lowNum = it.toIntOrNull() ?: 0
                        if (lowNum < 0) {
                            lowNum = 0
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
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                if (lowNum == 0) {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 3,
                                pdv = 3200L * 1000,
                                ads = Triple(600 * 100, 600 * 100, lowSpeed * 100),
                            )
                        }
                    }
                } else {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 3,
                                pdv = lowNum,
                                ads = Triple(600 * 100, 600 * 100, lowSpeed * 100),
                            )
                        }
                    }
                }

            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                scope.launch {
                    SerialPortUtils.stop(0, 1, 2, 3, 4)
                    lowDialog.value = false
                }
            }) {
                Text(text = "停止")
            }
        })
    }

    //冲洗泵弹窗
    if (rinseDialog.value) {
        AlertDialog(onDismissRequest = {
            scope.launch {
                SerialPortUtils.stop(0, 1, 2, 3, 4)
                rinseDialog.value = false
            }
        }, title = {}, text = {
            Row {
                Text(text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = rinseNum.toString(),
                    label = { Text(text = "不输入默认持续运行") },
                    onValueChange = {
                        rinseNum = it.toIntOrNull() ?: 0
                        if (rinseNum < 0) {
                            rinseNum = 0
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
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                if (rinseNum == 0) {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 4,
                                pdv = 3200L * 1000,
                                ads = Triple(600 * 100, 600 * 100, rinseSpeed.value * 100),
                            )
                        }
                    }
                } else {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 4,
                                pdv = rinseNum,
                                ads = Triple(600 * 100, 600 * 100, rinseSpeed.value * 100),
                            )
                        }
                    }
                }

            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(rgb(0, 105, 52))
            ), onClick = {
                scope.launch {
                    SerialPortUtils.stop(0, 1, 2, 3, 4)
                    rinseDialog.value = false
                }
            }) {
                Text(text = "停止")
            }
        })
    }


}