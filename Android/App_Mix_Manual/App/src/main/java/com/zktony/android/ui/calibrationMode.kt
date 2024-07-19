package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.LazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Expected
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Setting
import com.zktony.android.data.entities.SportsLog
import com.zktony.android.ui.brogressbar.HorizontalProgressBar
import com.zktony.android.ui.components.TableTextBody
import com.zktony.android.ui.components.TableTextHead
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.getStoragePath
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.line
import com.zktony.android.utils.AlgorithmUtils.calculateCalibrationFactorNew
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.SerialPortUtils.lightGreed
import com.zktony.android.utils.SerialPortUtils.lightYellow
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.extra.UpgradeState
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.embeddedUpgrade
import com.zktony.android.utils.extra.embeddedVersion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.ceil

/**
 * 校准设置
 */
@Composable
fun calibrationMode(
    c1: NewCalibration?,
    uiEvent: (SettingIntent) -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()


    var newCalibration = c1 ?: NewCalibration()

    var speChat =
        "[`~!@#$%^&*()+=\\-|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"

    val keyboard = LocalSoftwareKeyboardController.current


    var rinseSpeed = rememberDataSaverState(key = "rinseSpeed", default = 600L)
    var coagulantSpeed = rememberDataSaverState(key = "coagulantSpeed", default = 200L)


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
                text = "校准设置",
                fontSize = 22.sp,
                color = Color(rgb(112, 112, 112))
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, start = 70.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .width(400.dp)
                        .height(290.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = Color(rgb(229, 229, 229)),
                        )
                ) {
                    Row {
                        Text(
                            modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                            text = "高浓度泵",
                            fontSize = 22.sp,
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
                                    lightYellow()
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
                                    lightGreed()
                                }

                            }) {
                            Text(text = "加    液", fontSize = 18.sp)
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
                        label = { Text(text = "加液量1/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                higeLiquidVolume1_ex = it

                                val temp = higeLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    higeLiquidVolume1_ex = "0"
                                } else if (temp > 50) {
                                    higeLiquidVolume1_ex = "50"
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
                        label = { Text(text = "加液量2/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                higeLiquidVolume2_ex = it

                                val temp = higeLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    higeLiquidVolume2_ex = "0"
                                } else if (temp > 50) {
                                    higeLiquidVolume2_ex = "50"
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
                        label = { Text(text = "加液量3/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                higeLiquidVolume3_ex = it

                                val temp = higeLiquidVolume3_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    higeLiquidVolume3_ex = "0"
                                } else if (temp > 50) {
                                    higeLiquidVolume3_ex = "50"
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
                            color = Color(rgb(229, 229, 229)),
                        )
                ) {

                    Row {
                        Text(
                            modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                            text = "低浓度泵",
                            fontSize = 22.sp,
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
                                    lightYellow()
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
                                    lightGreed()
                                }

                            }) {
                            Text(text = "加    液", fontSize = 18.sp)
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
                        label = { Text(text = "加液量1/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                lowLiquidVolume1_ex = it

                                val temp = lowLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    lowLiquidVolume1_ex = "0"
                                } else if (temp > 50) {
                                    lowLiquidVolume1_ex = "50"
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
                        label = { Text(text = "加液量2/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                lowLiquidVolume2_ex = it

                                val temp = lowLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    lowLiquidVolume2_ex = "0"
                                } else if (temp > 50) {
                                    lowLiquidVolume2_ex = "50"
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
                        label = { Text(text = "加液量3/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                lowLiquidVolume3_ex = it

                                val temp = lowLiquidVolume3_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    lowLiquidVolume3_ex = "0"
                                } else if (temp > 50) {
                                    lowLiquidVolume3_ex = "50"
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
                            color = Color(rgb(229, 229, 229)),
                        )
                ) {

                    Row {
                        Text(
                            modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                            text = "冲洗液泵",
                            fontSize = 22.sp,
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
                                    lightYellow()
                                    start {
                                        timeOut = 1000L * 30
                                        with(
                                            index = 4,
                                            pdv = 3200L * 50,
                                            ads = Triple(
                                                rinseSpeed.value * 20,
                                                rinseSpeed.value * 20,
                                                rinseSpeed.value * 20
                                            ),

                                            )
                                    }
                                    lightGreed()
                                }

                            }) {
                            Text(text = "加    液", fontSize = 18.sp)
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
                        label = { Text(text = "加液量1/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                rinseLiquidVolume1_ex = it

                                val temp = rinseLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    rinseLiquidVolume1_ex = "0"
                                } else if (temp > 50) {
                                    rinseLiquidVolume1_ex = "50"
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
                        label = { Text(text = "加液量2/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                rinseLiquidVolume2_ex = it

                                val temp = rinseLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    rinseLiquidVolume2_ex = "0"
                                } else if (temp > 50) {
                                    rinseLiquidVolume2_ex = "50"
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
                        label = { Text(text = "加液量3/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                rinseLiquidVolume3_ex = it

                                val temp = rinseLiquidVolume3_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    rinseLiquidVolume3_ex = "0"
                                } else if (temp > 50) {
                                    rinseLiquidVolume3_ex = "50"
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
                            color = Color(rgb(229, 229, 229)),
                        )
                ) {

                    Row {
                        Text(
                            modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                            text = "促凝剂泵",
                            fontSize = 22.sp,
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
                                    lightYellow()
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
                                    delay(1000)
                                    start {
                                        timeOut = 1000L * 30
                                        with(
                                            index = 1,
                                            pdv = -coagulantpulse.value.toLong(),
                                            ads = Triple(
                                                rinseSpeed.value * 13,
                                                rinseSpeed.value * 1193,
                                                rinseSpeed.value * 1193
                                            ),

                                            )
                                    }
                                    lightGreed()
                                }

                            }) {
                            Text(text = "加    液", fontSize = 18.sp)
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
                        label = { Text(text = "加液量1/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                coagulantLiquidVolume1_ex = it

                                val temp = coagulantLiquidVolume1_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    coagulantLiquidVolume1_ex = "0"
                                } else if (temp > 50) {
                                    coagulantLiquidVolume1_ex = "50"
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
                        label = { Text(text = "加液量2/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                coagulantLiquidVolume2_ex = it

                                val temp = coagulantLiquidVolume2_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    coagulantLiquidVolume2_ex = "0"
                                } else if (temp > 50) {
                                    coagulantLiquidVolume2_ex = "50"
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
                        label = { Text(text = "加液量3/g", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                coagulantLiquidVolume3_ex = it

                                val temp = coagulantLiquidVolume3_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    coagulantLiquidVolume3_ex = "0"
                                } else if (temp > 50) {
                                    coagulantLiquidVolume3_ex = "50"
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

    }

    //校准恢复默认弹窗
    if (calibrationResetDialog.value) {
        AlertDialog(onDismissRequest = { }, title = {
            Text(text = "是否恢复默认设置！")
        }, text = {

        }, confirmButton = {
            Button(
                modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    newCalibration.higeLiquidVolume1 = 1.6
                    newCalibration.higeLiquidVolume2 = 1.6
                    newCalibration.higeLiquidVolume3 = 1.6
                    newCalibration.higeAvg = 1.6
                    newCalibration.lowLiquidVolume1 = 1.6
                    newCalibration.lowLiquidVolume2 = 1.6
                    newCalibration.lowLiquidVolume3 = 1.6
                    newCalibration.lowAvg = 1.6

                    newCalibration.rinseLiquidVolume1 = 1.6
                    newCalibration.rinseLiquidVolume2 = 1.6
                    newCalibration.rinseLiquidVolume3 = 1.6
                    newCalibration.rinseAvg = 1.6

                    newCalibration.coagulantLiquidVolume1 = 1.0
                    newCalibration.coagulantLiquidVolume2 = 1.0
                    newCalibration.coagulantLiquidVolume3 = 1.0
                    newCalibration.coagulantAvg = 1.0

                    uiEvent(SettingIntent.UpdateNC(newCalibration))
                    higeLiquidVolume1_ex = "1.6"
                    higeLiquidVolume2_ex = "1.6"
                    higeLiquidVolume3_ex = "1.6"

                    lowLiquidVolume1_ex = "1.6"
                    lowLiquidVolume2_ex = "1.6"
                    lowLiquidVolume3_ex = "1.6"

                    rinseLiquidVolume1_ex = "1.6"
                    rinseLiquidVolume2_ex = "1.6"
                    rinseLiquidVolume3_ex = "1.6"

                    coagulantLiquidVolume1_ex = "1.0"
                    coagulantLiquidVolume2_ex = "1.0"
                    coagulantLiquidVolume3_ex = "1.0"

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

                    calibrationResetDialog.value = false
                }) {
                Text(fontSize = 18.sp, text = "确   认")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp),
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),onClick = { calibrationResetDialog.value = false }) {
                Text(fontSize = 18.sp, text = "取   消", color = Color.Black)
            }
        })
    }
}
