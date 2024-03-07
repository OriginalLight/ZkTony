package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.storage.StorageManager
import android.util.Log
import android.view.View.OnLongClickListener
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.TriStateCheckbox
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
import com.zktony.android.utils.AppStateUtils.hpd
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
import java.io.File
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.ceil

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DebugModeRoute(viewModel: SettingViewModel) {

    val snackbarHostState = LocalSnackbarHostState.current

    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()


    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
        }
    }

}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun debugMode(
    uiEvent: (SettingIntent) -> Unit,
    proEntities: List<Program>,
    s1: Setting?,
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


    var setting = s1 ?: Setting()

    val scope = rememberCoroutineScope()

    val keyboard = LocalSoftwareKeyboardController.current

    val context = LocalContext.current


    /**
     * 制胶速度，根据这个速度转换其他泵的速度
     */
    val speed = rememberDataSaverState(key = "speed", default = 180)
    var speed_ex by remember { mutableStateOf(speed.value.toString()) }

    /**
     * 胶板位置
     */
    var glueBoardPosition_ex by remember(setting) { mutableStateOf(setting.glueBoardPosition.toString()) }

    /**
     * 废液位置
     */
    var wastePosition_ex by remember(setting) { mutableStateOf(setting.wastePosition.toString()) }

    //高浓度泵转速
    var higeSpeed by remember { mutableStateOf(0L) }

    //低浓度泵转速
    var lowSpeed by remember { mutableStateOf(0L) }

    //冲洗液泵转速
    var rinseSpeed = rememberDataSaverState(key = "rinseSpeed", default = 600L)
    var rinseSpeed_ex by remember { mutableStateOf(rinseSpeed.value.toString()) }

    //促凝剂泵转速
    var coagulantSpeed = rememberDataSaverState(key = "coagulantSpeed", default = 300L)
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
     * 柱塞泵填充微升
     */
    val coagulantPipeline = rememberDataSaverState(key = "coagulantpipeline", default = 50)
    var coagulantPipeline_ex by remember { mutableStateOf(coagulantPipeline.value.toString()) }

    /**
     * x轴转速
     */
    val xSpeed = rememberDataSaverState(key = "xSpeed", default = 250L)
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
    var highNum_ex by remember { mutableStateOf(highNum.toString()) }
    //===============高浓度弹窗==============================

    //===============低浓度弹窗==============================
    val lowDialog = remember { mutableStateOf(false) }

    var lowNum by remember { mutableStateOf(0) }
    var lowNum_ex by remember { mutableStateOf(lowNum.toString()) }
    //===============低浓度弹窗==============================

    //===============低浓度弹窗==============================
    val rinseDialog = remember { mutableStateOf(false) }

    var rinseNum by remember { mutableStateOf(0) }
    var rinseNum_ex by remember { mutableStateOf(rinseNum.toString()) }
    //===============低浓度弹窗==============================

    /**
     * 一键清除的弹窗
     */
    val clearAllDialog = remember { mutableStateOf(false) }


    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) {
        uiEvent(SettingIntent.Start(1))
    } else if (uiFlags is UiFlags.Objects && uiFlags.objects == 6) {
        uiEvent(SettingIntent.Stop)
    } else if (uiFlags is UiFlags.Objects && uiFlags.objects == 11) {
        speed.value = speedFlow
        speed_ex = speedFlow.toString()
        rinseSpeed.value = rinseSpeedFlow
        rinseSpeed_ex = rinseSpeedFlow.toString()
        xSpeed.value = xSpeedFlow
        xSpeed_ex = xSpeedFlow.toString()
        coagulantSpeed.value = coagulantSpeedFlow
        coagulantSpeed_ex = coagulantSpeedFlow.toString()
        coagulantpulse.value = coagulantpulseFlow
        coagulantpulse_ex = coagulantpulseFlow.toString()
        coagulantTime.value = coagulantTimeFlow
        coagulantTime_ex = coagulantTimeFlow.toString()
        coagulantResetPulse.value = coagulantResetPulseFlow
        coagulantResetPulse_ex = coagulantResetPulseFlow.toString()
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
                                    val temp = it.toIntOrNull() ?: 0
                                    if (temp > 300) {
                                        speed.value = 300
                                        speed_ex = "300"
                                    } else if (temp <= 0) {
                                        speed.value = 0
                                        speed_ex = "0"
                                    } else {
                                        speed_ex = it
                                        speed.value = speed_ex.toIntOrNull() ?: 0
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
                                //获取usb地址
                                val path = getStoragePath(context, true)
                                if (!"".equals(path)) {
                                    val filePath = "$path/zktony/config.txt"
                                    uiEvent(SettingIntent.ImportData(filePath = filePath))
                                } else {
                                    Toast.makeText(
                                        context,
                                        "U盘不存在！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                            Text(text = "导入数据", fontSize = 18.sp)
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
                                clearAllDialog.value = true
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
                                if (coagulantSpeed.value > 500L) {
                                    coagulantSpeed.value = 500L
                                    coagulantSpeed_ex = "500L"
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
                                } else if (coagulantpulse.value < 0) {
                                    coagulantpulse.value = 0
                                    coagulantpulse_ex = "0"
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

                            modifier = Modifier.width(150.dp),
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
                                .width(150.dp),

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

                        OutlinedTextField(
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .width(100.dp),

                            value = coagulantPipeline_ex,
                            label = { Text(text = "填充微升") },
                            onValueChange = {
                                coagulantPipeline_ex = it
                                coagulantPipeline.value =
                                    coagulantPipeline_ex.toIntOrNull() ?: 0
                                if (coagulantPipeline.value < 0) {
                                    coagulantPipeline.value = 0
                                    coagulantPipeline_ex = "0"
                                } else if (coagulantPipeline.value > 500) {
                                    coagulantPipeline.value = 500
                                    coagulantPipeline_ex = "500"
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
                            Text(text = "运    行", fontSize = 18.sp)
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
                                    } else if (xSpeed.value > 400) {
                                        xSpeed.value = 400L
                                        xSpeed_ex = "400"
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
                                    } else if (setting.wastePosition > 30) {
                                        setting.wastePosition = 30.0
                                        wastePosition_ex = "30"
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
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20
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
                                    } else if (setting.glueBoardPosition > 30) {
                                        setting.glueBoardPosition = 30.0
                                        glueBoardPosition_ex = "30"
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
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20
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
                                text = "X轴左"
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
                                    Optocoupler1 = false

                                    scope.launch {
                                        SerialPortUtils.start {
                                            timeOut = 1000L * 30
                                            with(
                                                index = 0,
                                                pdv = 0L,
                                                ads = Triple(
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20
                                                ),
                                            )
                                        }

                                        delay(100)

                                        SerialPortUtils.gpio(0)
                                        delay(500L)
                                        if (SerialPortUtils.getGpio(0)) {
                                            Optocoupler1 = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "未检测到X轴左！",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

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
                                text = "X轴右"
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
                                    Optocoupler2 = false
                                    scope.launch {
                                        SerialPortUtils.start {
                                            timeOut = 1000L * 30
                                            with(
                                                index = 0,
                                                pdv = 30.0,
                                                ads = Triple(
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20,
                                                    xSpeed.value * 20
                                                ),
                                            )
                                        }

                                        delay(100)

                                        SerialPortUtils.gpio(1)
                                        delay(500L)
                                        if (SerialPortUtils.getGpio(1)) {
                                            Optocoupler2 = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "未检测到X轴右！",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
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
                                text = "促凝剂"
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
                                    Optocoupler3 = false
                                    scope.launch {
                                        SerialPortUtils.start {
                                            timeOut = 1000L * 30
                                            with(
                                                index = 1,
                                                pdv = -64000L,
                                                ads = Triple(200 * 13, 200 * 1193, 200 * 1193),
                                            )
                                        }

                                        SerialPortUtils.gpio(2)
                                        delay(500L)
                                        if (SerialPortUtils.getGpio(2)) {
                                            Optocoupler3 = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "未检测到促凝剂！",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
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
                                text = "制胶架"
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
                                    Optocoupler4 = false
                                    scope.launch {
                                        SerialPortUtils.gpio(3)
                                        delay(500L)
                                        if (SerialPortUtils.getGpio(3)) {
                                            Optocoupler4 = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "未检测到制胶架！",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }
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
                                    var state = 0
                                    statusLight = false
                                    hpd[11] = false
                                    hpd[12] = false
                                    hpd[13] = false
                                    scope.launch {
                                        delay(100)
                                        lightRed()
                                        delay(100)
                                        if (hpd[11] == true) {
                                            state = 1
                                        }
                                        delay(100)
                                        lightYellow()
                                        delay(100)
                                        if (hpd[12] == true) {
                                            state = 2
                                        }
                                        delay(100)
                                        lightGreed()
                                        delay(100)
                                        if (hpd[13] == true) {
                                            state = 3
                                        }

                                        if (state != 3) {
                                            Toast.makeText(
                                                context,
                                                "灯光检测异常",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            statusLight = true
                                        }

                                    }

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
                                        selected = it == soundsThickness.value,
                                        onClick = {
                                            soundsThickness.value = it
                                            if (soundsThickness.value == "蜂鸣") {
                                                uiEvent(SettingIntent.Sound(1))
                                            } else if (soundsThickness.value == "语音") {
                                                uiEvent(SettingIntent.Sound(2))
                                            }
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

    //一键清除弹窗
    if (clearAllDialog.value) {
        AlertDialog(onDismissRequest = { }, title = {}, text = {
            Text(fontSize = 18.sp, text = "是否清除全部数据?")
        }, confirmButton = {
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    uiEvent(SettingIntent.ClearAll)
                    clearAllDialog.value = false
                }) {
                Text(text = "确     定")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    clearAllDialog.value = false
                }) {
                Text(text = "取     消")
            }
        })
    }

    //制胶弹窗
    if (glueDialog.value) {
        AlertDialog(onDismissRequest = { }, title = {}, text = {

            Column {

                Row(
                    modifier = Modifier.padding(top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        fontSize = 18.sp,
                        text = "请输入制胶数量:"
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .width(200.dp)
                            .padding(start = 10.dp),
                        value = glueNum_ex,
                        label = { Text(fontSize = 13.sp, text = "不输入默认持续运行") },
                        onValueChange = {
                            glueNum_ex = it
                            glueNum.value = glueNum_ex.toIntOrNull() ?: 1
                            if (glueNum.value < 0) {
                                glueNum.value = 0
                                glueNum_ex = "0"
                            } else if (glueNum.value > 100) {
                                glueNum.value = 100
                                glueNum_ex = "100"
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
                        modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                        fontSize = 18.sp,
                        text = "程序名称:"
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 10.dp, start = 20.dp)
                            .clickable {
                                if (proEntities.isNotEmpty()) {
                                    downMenu = true
                                }
                            },
                        fontSize = 13.sp,
                        text = if (selectIndex == "") "请选择程序" else selectIndex
                    )


                    DropdownMenu(
                        expanded = downMenu,
                        onDismissRequest = {},
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
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
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
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
                Text(text = "开     始")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    uiEvent(SettingIntent.Stop)
                    glueDialog.value = false
                }) {
                Text(text = "停     止")
            }
        })
    }

    //X轴运行弹窗
    if (xDialog.value) {
        AlertDialog(onDismissRequest = { xDialog.value = false }, title = {}, text = {
            Row {
                Text(
                    fontSize = 18.sp, text = "请输入制胶数量:"
                )

                OutlinedTextField(
                    modifier = Modifier.width(200.dp),
                    value = xNum.toString(),
                    label = {
                        Text(
                            fontSize = 13.sp, text = "不输入默认运行1次"
                        )
                    },
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
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ),
                enabled = job == null,
                onClick = {
                    uiEvent(SettingIntent.XStart(xNum))
                }) {
                Text(text = "开     始")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    uiEvent(SettingIntent.XStop)
                    xDialog.value = false
                }) {
                Text(text = "停     止")
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(fontSize = 18.sp, text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(200.dp),
                    value = highNum_ex,
                    label = { },
                    onValueChange = {
                        highNum_ex = it
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
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    highNum = highNum_ex.toIntOrNull() ?: 0
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 2,
                                pdv = highNum,
                                ads = Triple(
                                    higeSpeed * 13,
                                    higeSpeed * 1193,
                                    higeSpeed * 1193
                                ),
                            )
                        }
                    }

                }) {
                Text(text = "开     始")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    scope.launch {
                        SerialPortUtils.stop(0, 1, 2, 3, 4)
                        highDialog.value = false
                    }
                }) {
                Text(text = "停     止")
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
                Text(fontSize = 18.sp, text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(200.dp),
                    value = lowNum_ex,
                    label = { },
                    onValueChange = {
                        lowNum_ex = it
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
            Button(
                modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    scope.launch {
                        lowNum = lowNum_ex.toIntOrNull() ?: 0
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 3,
                                pdv = lowNum,
                                ads = Triple(lowSpeed * 13, lowSpeed * 1193, lowSpeed * 1193),
                            )
                        }
                    }

                }) {
                Text(text = "开     始")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    scope.launch {
                        SerialPortUtils.stop(0, 1, 2, 3, 4)
                        lowDialog.value = false
                    }
                }) {
                Text(text = "停     止")
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
                Text(fontSize = 18.sp, text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(200.dp),
                    value = rinseNum_ex,
                    label = { },
                    onValueChange = {
                        rinseNum_ex = it
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
            Button(
                modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    rinseNum = rinseNum_ex.toIntOrNull() ?: 0
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 4,
                                pdv = rinseNum.toLong(),
                                ads = Triple(
                                    rinseSpeed.value * 40,
                                    rinseSpeed.value * 40,
                                    rinseSpeed.value * 40
                                ),
                            )
                        }
                    }

                }) {
                Text(text = "开     始")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    scope.launch {
                        SerialPortUtils.stop(0, 1, 2, 3, 4)
                        rinseDialog.value = false
                    }
                }) {
                Text(text = "停     止")
            }
        })
    }


}


private fun getStoragePath(context: Context, isUsb: Boolean): String? {
    var path = ""
    val mStorageManager: StorageManager =
        context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val volumeInfoClazz: Class<*>
    val diskInfoClaszz: Class<*>
    try {
        volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo")
        diskInfoClaszz = Class.forName("android.os.storage.DiskInfo")
        val StorageManager_getVolumes: Method =
            Class.forName("android.os.storage.StorageManager").getMethod("getVolumes")
        val VolumeInfo_GetDisk: Method = volumeInfoClazz.getMethod("getDisk")
        val VolumeInfo_GetPath: Method = volumeInfoClazz.getMethod("getPath")
        val DiskInfo_IsUsb: Method = diskInfoClaszz.getMethod("isUsb")
        val DiskInfo_IsSd: Method = diskInfoClaszz.getMethod("isSd")
        val List_VolumeInfo = (StorageManager_getVolumes.invoke(mStorageManager) as List<Any>)
        for (i in List_VolumeInfo.indices) {
            val volumeInfo = List_VolumeInfo[i]
            val diskInfo: Any = VolumeInfo_GetDisk.invoke(volumeInfo) ?: continue
            val sd = DiskInfo_IsSd.invoke(diskInfo) as Boolean
            val usb = DiskInfo_IsUsb.invoke(diskInfo) as Boolean
            val file: File = VolumeInfo_GetPath.invoke(volumeInfo) as File
            if (isUsb == usb) { //usb
                assert(file != null)
                path = file.getAbsolutePath()
                Log.d(
                    "Progarm",
                    "usb的path=====$path"
                )
            } else if (!isUsb == sd) { //sd
                assert(file != null)
                path = file.getAbsolutePath()
            }
        }
    } catch (e: Exception) {
        Log.d(
            "Progarm",
            "获取usb地址异常=====" + e.printStackTrace()
        )
        e.printStackTrace()
    }
    Log.d(
        "Progarm",
        "usb的path===未获取到==$path"
    )
    return path
}