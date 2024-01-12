package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.DebugModeAppBar
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.SerialPortUtils.cleanLight
import com.zktony.android.utils.SerialPortUtils.lightFlashYellow
import com.zktony.android.utils.SerialPortUtils.lightGreed
import com.zktony.android.utils.SerialPortUtils.lightRed
import com.zktony.android.utils.SerialPortUtils.lightYellow
import com.zktony.android.utils.extra.format
import kotlinx.coroutines.launch

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
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.DEBUGMODE -> navigationActions.navigateUp()
                else -> viewModel.dispatch(SettingIntent.NavTo(PageType.SETTINGS))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
        }
    }

    Column {
        DebugModeAppBar(PageType.DEBUGMODE) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.DEBUGMODE -> debugMode(
                    viewModel::dispatch, entities.toList(), proEntities.toList(), selected
                )

                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun debugMode(
    dispatch: (SettingIntent) -> Unit,
    entities: List<Motor>,
    proEntities: List<Program>,
    selected: Long
) {

    val scope = rememberCoroutineScope()

    val keyboard = LocalSoftwareKeyboardController.current

    /**
     * 制胶速度，根据这个速度转换其他泵的速度
     */
    val speed = rememberDataSaverState(key = "speed", default = 0)
    var speed_ex by remember { mutableStateOf(speed.value.toString()) }

    /**
     * 废液槽坐标
     */
    val wasteCoordinates = rememberDataSaverState(key = "wasteCoordinates", default = 0.0)
    var wasteCoordinates_ex by remember { mutableStateOf(wasteCoordinates.value.format(1)) }

    /**
     * 制胶架坐标
     */
    val makeCoordinates = rememberDataSaverState(key = "makeCoordinates", default = 0.0)
    var makeCoordinates_ex by remember { mutableStateOf(makeCoordinates.value.format(1)) }

    //高浓度泵转速
    var higeSpeed by remember { mutableStateOf(0L) }

    //低浓度泵转速
    var lowSpeed by remember { mutableStateOf(0L) }

    //冲洗液泵转速
    var rinseSpeed = rememberDataSaverState(key = "rinseSpeed", default = 0L)
    var rinseSpeed_ex by remember { mutableStateOf(rinseSpeed.value.toString()) }

    //促凝剂泵转速
    var coagulantSpeed by remember { mutableStateOf(0L) }

    /**
     * 促凝剂步数
     */
    val coagulantpulse = rememberDataSaverState(key = "coagulantpulse", default = 67500)
    var coagulantpulse_ex by remember { mutableStateOf(coagulantpulse.value.toString()) }

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

    var xSpeed by remember { mutableStateOf(0L) }


    if (entities.isNotEmpty()) {
        higeSpeed = entities[2].speed
        lowSpeed = entities[3].speed
        coagulantSpeed = entities[1].speed
        xSpeed = entities[0].speed
    }

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
     * 光耦5
     */
    var Optocoupler5 by remember { mutableStateOf(false) }


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

    var calibrationMap = hashMapOf<String, Float>()


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
    var glueNum by remember { mutableStateOf(0) }


    val liquid = arrayListOf("是", "否")
    var liquidThickness = remember { mutableStateOf(liquid[0]) }

    var selectIndex by remember { mutableStateOf("") }

    var downMenu by remember { mutableStateOf(false) }
    //===============制胶弹窗==============================


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


    LazyColumn {
        item {
            Row(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .height(220.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                ) {
                    Row {
                        Button(modifier = Modifier
                            .width(100.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "填    充", fontSize = 18.sp)
                        }

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "清    洗", fontSize = 18.sp)
                        }


                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

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
                                speed.value = speed_ex.toIntOrNull() ?: 0
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
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "步/s"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
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
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {

                        }) {
                        Text(text = "保    存", fontSize = 18.sp)
                    }


                    Button(modifier = Modifier
                        .padding(top = 20.dp)
                        .width(100.dp)
                        .height(50.dp),
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {

                        }) {
                        Text(text = "重    置", fontSize = 18.sp)
                    }


                    Button(modifier = Modifier
                        .padding(top = 20.dp)
                        .width(120.dp)
                        .height(50.dp),
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {

                        }) {
                        Text(text = "一键清除", fontSize = 18.sp)
                    }


                }

            }
        }

        item {
            Column(
                modifier = Modifier
                    .padding(start = 5.dp, top = 20.dp)
                    .fillMaxWidth()
                    .height(700.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
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
                        onValueChange = { higeSpeed = it.toLong() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboard?.hide()
                        })
                    )
                    Text(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "步/s"
                    )

                    Button(modifier = Modifier
                        .padding(start = 20.dp)
                        .width(100.dp)
                        .height(50.dp),
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
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {
                            scope.launch {
                                SerialPortUtils.start {
                                    timeOut = 1000L * 30
                                    with(
                                        index = 2,
                                        pdv = -32000L,
                                        ads = Triple(300, 400, speed.value.toLong()),
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
                        onValueChange = { lowSpeed = it.toLong() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboard?.hide()
                        })
                    )
                    Text(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "步/s"
                    )

                    Button(modifier = Modifier
                        .padding(start = 20.dp)
                        .width(100.dp)
                        .height(50.dp),
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
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {
                            scope.launch {
                                SerialPortUtils.start {
                                    timeOut = 1000L * 30
                                    with(
                                        index = 3,
                                        pdv = -32000L,
                                        ads = Triple(300, 400, speed.value.toLong()),
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
                            rinseSpeed.value = rinseSpeed_ex.toLongOrNull() ?: 0L
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
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "步/s"
                    )

                    Button(modifier = Modifier
                        .padding(start = 20.dp)
                        .width(100.dp)
                        .height(50.dp),
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
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {
                            scope.launch {
                                SerialPortUtils.start {
                                    timeOut = 1000L * 30
                                    with(
                                        index = 4,
                                        pdv = -32000L,
                                        ads = Triple(300, 400, speed.value.toLong()),
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
                        value = coagulantSpeed.toString(),
                        label = { Text(text = "转速") },
                        onValueChange = { coagulantSpeed = it.toLong() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboard?.hide()
                        })
                    )
                    Text(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "步/s"
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
                    modifier = Modifier.padding(top = 20.dp, start = 50.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.width(200.dp),
                        value = coagulantTime_ex,
                        label = { Text(text = "复位等待时间") },
                        onValueChange = {
                            coagulantTime_ex = it
                            coagulantTime.value = coagulantTime_ex.toIntOrNull() ?: 0
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
                            coagulantResetPulse.value = coagulantResetPulse_ex.toIntOrNull() ?: 0
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
                    modifier = Modifier.padding(top = 20.dp, start = 100.dp)
                ) {
                    Button(modifier = Modifier
                        .padding(start = 20.dp)
                        .width(100.dp)
                        .height(50.dp),
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {
                            scope.launch {

                                SerialPortUtils.start {
                                    timeOut = 1000L * 30
                                    with(
                                        index = 1,
                                        pdv = 260000L,
                                        ads = Triple(300, 400, speed.value.toLong()),
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
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                        onClick = {

                        }) {
                        Text(text = "复    位", fontSize = 18.sp)
                    }
                }

            }
        }

        item {
            Row(
                modifier = Modifier
                    .padding(start = 5.dp, top = 20.dp)
                    .height(350.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(350.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
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
                            value = xSpeed.toString(),
                            label = { Text(text = "转速") },
                            onValueChange = { xSpeed = it.toLong() },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboard?.hide()
                            })
                        )
                        Text(
                            modifier = Modifier.padding(top = 20.dp, start = 20.dp), text = "步/s"
                        )

                    }


                    Row(
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp)
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            value = wasteCoordinates_ex,
                            label = { Text(text = "废液槽坐标") },
                            onValueChange = { wasteCoordinates_ex = it },
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
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "移    动", fontSize = 18.sp)
                        }

                    }


                    Row(
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp)
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            value = makeCoordinates_ex,
                            label = { Text(text = "制胶架坐标") },
                            onValueChange = { makeCoordinates_ex = it },
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
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

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
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "运    行", fontSize = 18.sp)
                        }

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "复    位", fontSize = 18.sp)
                        }


                    }


                }

                Column(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .fillMaxWidth(1f)
                        .height(350.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
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
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "光耦1"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
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
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "光耦1"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
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
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "光耦1"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
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
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "光耦1"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
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
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "光耦1"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
                            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                            onClick = {

                            }) {
                            Text(text = "检    测", fontSize = 18.sp)
                        }

                        Checkbox(checked = Optocoupler1, enabled = false, onCheckedChange = {
                            Optocoupler1 = it
                        })
                    }
                }
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
                        .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "状态灯"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
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
                        Switch(modifier = Modifier
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
                                RadioButton(enabled = control,
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
                                            }else if (colorsThickness.value == "红" ||colorsThickness.value == "绿"){
                                                flashingThickness.value = "常亮"
                                                if(colorsThickness.value == "红"){
                                                    scope.launch {
                                                        lightRed()
                                                    }
                                                }else{
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
                                RadioButton(enabled = control,
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

                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }


                }

                Column(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .fillMaxWidth(1f)
                        .height(220.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                ) {

                    Row(
                        modifier = Modifier.padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp), text = "声音"
                        )

                        Button(modifier = Modifier
                            .padding(start = 20.dp)
                            .width(100.dp)
                            .height(50.dp),
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
                        Switch(modifier = Modifier
                            .height(32.dp)
                            .padding(start = 10.dp),
                            checked = soundControl,
                            onCheckedChange = {
                                soundControl = it
                            })
                    }

                    Row(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        sounds.forEach {
                            Row {
                                RadioButton(enabled = soundControl,
                                    selected = it == soundsThickness.value,
                                    onClick = {
                                        soundsThickness.value = it
                                    })
                                Text(text = it)
                            }

                            Spacer(modifier = Modifier.width(20.dp))
                        }
                    }

                }


            }

        }


    }


    //制胶弹窗
    if (highDialog.value) {
        AlertDialog(onDismissRequest = { }, title = {}, text = {

            Column {

                Row {
                    Text(text = "请输入制胶数量:")

                    OutlinedTextField(
                        modifier = Modifier.width(100.dp),
                        value = glueNum.toString(),
                        label = { Text(text = "不输入默认持续运行") },
                        onValueChange = {
                            glueNum = it.toIntOrNull() ?: 0
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
                        text = "是否加液:"
                    )

                    liquid.forEach {
                        Row {
                            RadioButton(selected = it == liquidThickness.value, onClick = {
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
                        modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                        text = "程序名称:"
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 10.dp, start = 20.dp)
                            .clickable {
                                if (proEntities.size > 0) {
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
                                selectIndex = it.displayText
                                println("selectIndex===" + selectIndex)
                                downMenu = false

                            })
                        }
                    }
                }


            }


        }, confirmButton = {
            TextButton(onClick = {


            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(onClick = {
                highDialog.value = false
            }) {
                Text(text = "关闭")
            }
        })
    }

    //高浓度弹窗
    if (highDialog.value) {
        AlertDialog(onDismissRequest = { highDialog.value = false }, title = {}, text = {
            Row {
                Text(text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = highNum.toString(),
                    label = { Text(text = "不输入默认持续运行") },
                    onValueChange = {
                        highNum = it.toIntOrNull() ?: 0
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
            TextButton(onClick = {
                if (highNum == 0) {

                } else {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 2,
                                pdv = highNum,
                                ads = Triple(300, 400, speed.value.toLong()),
                            )
                        }
                    }
                }

            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(onClick = {
                highDialog.value = false
            }) {
                Text(text = "停止")
            }
        })
    }

    //低浓度弹窗
    if (lowDialog.value) {
        AlertDialog(onDismissRequest = { lowDialog.value = false }, title = {}, text = {
            Row {
                Text(text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = lowNum.toString(),
                    label = { Text(text = "不输入默认持续运行") },
                    onValueChange = {
                        lowNum = it.toIntOrNull() ?: 0
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
            TextButton(onClick = {
                if (lowNum == 0) {

                } else {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 3,
                                pdv = lowNum,
                                ads = Triple(300, 400, speed.value.toLong()),
                            )
                        }
                    }
                }

            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(onClick = {
                lowDialog.value = false
            }) {
                Text(text = "停止")
            }
        })
    }

    //冲洗泵弹窗
    if (rinseDialog.value) {
        AlertDialog(onDismissRequest = { rinseDialog.value = false }, title = {}, text = {
            Row {
                Text(text = "请输入运行步数:")

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = rinseNum.toString(),
                    label = { Text(text = "不输入默认持续运行") },
                    onValueChange = {
                        rinseNum = it.toIntOrNull() ?: 0
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
            TextButton(onClick = {
                if (rinseNum == 0) {

                } else {
                    scope.launch {
                        SerialPortUtils.start {
                            timeOut = 1000L * 30
                            with(
                                index = 4,
                                pdv = rinseNum,
                                ads = Triple(300, 400, speed.value.toLong()),
                            )
                        }
                    }
                }

            }) {
                Text(text = "开始")
            }
        }, dismissButton = {
            TextButton(onClick = {
                rinseDialog.value = false
            }) {
                Text(text = "停止")
            }
        })
    }


}