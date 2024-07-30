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
import com.zktony.android.data.entities.Expected
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
 * 预排设置
 */
@Composable
fun rehearsalMode(
    s1: Setting?,
    ex1: Expected?,
    uiEvent: (SettingIntent) -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var setting = s1 ?: Setting()

    val expected = ex1 ?: Expected()

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


    var rinseCleanVolume2_ex by remember(setting) { mutableStateOf(setting.rinseCleanVolume2.toString()) }


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

    /**
     * 促凝剂泵冲洗液量/μL
     */
    var coagulantRinse_ex by remember(setting) { mutableStateOf(setting.coagulantRinse.toString()) }
    //促凝剂泵

    /**
     * 预排恢复默认弹窗
     */
    val expectedResetDialog = remember(setting) { mutableStateOf(false) }


    //================预排设置=============================

    var speChat =
        "[`~!@#$%^&*()+=\\-|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"

    val keyboard = LocalSoftwareKeyboardController.current

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
                text = "预排设置",
                fontSize = 20.sp,
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
                        .height(370.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = Color(rgb(229, 229, 229)),
                        )
                ) {
                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                        text = "管路填充",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                        value = higeFilling_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "高浓度泵/mL", fontSize = 20.sp) },
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
                                if (temp < 0) {
                                    higeFilling_ex = "0"
                                } else if (temp > 20) {
                                    higeFilling_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = lowFilling_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "低浓度泵/mL", fontSize = 20.sp) },
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
                                if (temp < 0) {
                                    lowFilling_ex = "0"
                                } else if (temp > 20) {
                                    lowFilling_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = rinseFilling_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "冲洗液泵/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                rinseFilling_ex = it

                                val temp = rinseFilling_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    rinseFilling_ex = "0"
                                } else if (temp > 20) {
                                    rinseFilling_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = coagulantFilling_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "促凝剂泵/mL", fontSize = 20.sp) },
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
                                if (temp < 0) {
                                    coagulantFilling_ex = "0"
                                } else if (temp > 20) {
                                    coagulantFilling_ex = "20"
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
                        .height(150.dp)
                        .padding(top = 20.3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = Color(rgb(229, 229, 229)),
                        )
                ) {
                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                        text = "预排液量",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                        value = higeRehearsalVolume_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "预排液量/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                higeRehearsalVolume_ex = it
                                val temp = higeRehearsalVolume_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    higeRehearsalVolume_ex = "0"
                                } else if (temp > 20) {
                                    higeRehearsalVolume_ex = "20"
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
                        .height(390.dp)
                        .padding(top = 20.3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = Color(rgb(229, 229, 229)),
                        )
                ) {
                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                        text = "清洗液量",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )


                    OutlinedTextField(
                        modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                        value = higeCleanVolume_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "高浓度泵/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                higeCleanVolume_ex = it
                                val temp = higeCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    higeCleanVolume_ex = "0"
                                } else if (temp > 20) {
                                    higeCleanVolume_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = lowCleanVolume_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "低浓度泵/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                lowCleanVolume_ex = it

                                val temp = lowCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    lowCleanVolume_ex = "0"
                                } else if (temp > 20) {
                                    lowCleanVolume_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = rinseCleanVolume_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "冲洗液泵/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                rinseCleanVolume_ex = it

                                val temp = rinseCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    rinseCleanVolume_ex = "0"
                                } else if (temp > 20) {
                                    rinseCleanVolume_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = coagulantCleanVolume_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "促凝剂泵/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                coagulantCleanVolume_ex = it

                                val temp = coagulantCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    coagulantCleanVolume_ex = "0"
                                } else if (temp > 20) {
                                    coagulantCleanVolume_ex = "20"
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
                    Text(
                        modifier = Modifier.padding(top = 10.dp, start = 24.7.dp),
                        text = "冲洗液量",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        modifier = Modifier.padding(top = 10.dp, start = 47.7.dp),
                        value = rinseCleanVolume_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "待机清洗体积1/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                rinseCleanVolume_ex = it

                                val temp = rinseCleanVolume_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    rinseCleanVolume_ex = "0"
                                } else if (temp > 20) {
                                    rinseCleanVolume_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = rinseCleanVolume2_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "待机清洗体积2/mL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                rinseCleanVolume2_ex = it

                                val temp = rinseCleanVolume2_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    rinseCleanVolume2_ex = "0"
                                } else if (temp > 20) {
                                    rinseCleanVolume2_ex = "20"
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
                        modifier = Modifier.padding(top = 14.8.dp, start = 47.7.dp),
                        value = coagulantRinse_ex,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        label = { Text(text = "促凝剂泵/μL", fontSize = 20.sp) },
                        onValueChange = {
                            if (Pattern.compile(speChat).matcher(it).find()) {
                                Toast.makeText(
                                    context,
                                    "数据不能包含特殊字符！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                coagulantRinse_ex = it

                                val temp = coagulantRinse_ex.toDoubleOrNull() ?: 0.0
                                if (temp < 0) {
                                    coagulantRinse_ex = "0"
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
                        setting.coagulantRinse =
                            coagulantRinse_ex.toDoubleOrNull() ?: 0.0
                        setting.rinseCleanVolume2 =
                            rinseCleanVolume2_ex.toDoubleOrNull() ?: 0.0
                        uiEvent(SettingIntent.UpdateSet(setting))

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
                        expectedResetDialog.value = true
                    }) {
                        Text(text = "恢复默认", fontSize = 18.sp)
                    }

                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }


    }

    //预排恢复默认弹窗
    if (expectedResetDialog.value) {
        AlertDialog(onDismissRequest = { }, title = {
            Text(text = "是否恢复默认设置！")
        }, text = {

        }, confirmButton = {
            Button(
                modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {

                    if (expected.higeCleanDefault == 0.0) {
                        setting.higeCleanVolume = 5.0
                        setting.higeRehearsalVolume = 1.0
                        setting.higeFilling = 3.0
                        setting.lowCleanVolume = 5.0
                        setting.lowFilling = 3.0
                        setting.rinseCleanVolume = 5.0
                        setting.rinseFilling = 3.0
                        setting.coagulantCleanVolume = 5.0
                        setting.coagulantFilling = 3.0
                        uiEvent(SettingIntent.UpdateSet(setting))

                        higeCleanVolume_ex = "5.0"
                        higeRehearsalVolume_ex = "1.0"
                        higeFilling_ex = "3.0"
                        lowCleanVolume_ex = "5.0"
                        lowFilling_ex = "3.0"
                        rinseCleanVolume_ex = "5.0"
                        rinseFilling_ex = "3.0"
                        coagulantCleanVolume_ex = "5.0"
                        coagulantFilling_ex = "3.0"
                    } else {
                        setting.higeCleanVolume = expected.higeCleanDefault
                        setting.higeRehearsalVolume = expected.higeRehearsalDefault
                        setting.higeFilling = expected.higeFillingDefault
                        setting.lowCleanVolume = expected.lowCleanDefault
                        setting.lowFilling = expected.lowFillingDefault
                        setting.rinseCleanVolume = expected.rinseCleanDefault
                        setting.rinseFilling = expected.rinseFillingDefault
                        setting.coagulantCleanVolume = expected.coagulantCleanDefault
                        setting.coagulantFilling = expected.coagulantFillingDefault
                        uiEvent(SettingIntent.UpdateSet(setting))

                        higeCleanVolume_ex = expected.higeCleanDefault.toString()
                        higeRehearsalVolume_ex = expected.higeRehearsalDefault.toString()
                        higeFilling_ex = expected.higeFillingDefault.toString()
                        lowCleanVolume_ex = expected.lowCleanDefault.toString()
                        lowFilling_ex = expected.lowFillingDefault.toString()
                        rinseCleanVolume_ex = expected.rinseCleanDefault.toString()
                        rinseFilling_ex = expected.rinseFillingDefault.toString()
                        coagulantCleanVolume_ex = expected.coagulantCleanDefault.toString()
                        coagulantFilling_ex = expected.coagulantFillingDefault.toString()
                    }


                    expectedResetDialog.value = false
                }) {
                Text(fontSize = 18.sp,text = "确   认")
            }
        }, dismissButton = {
            Button(
                modifier = Modifier.width(100.dp),
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),onClick = { expectedResetDialog.value = false }) {
                Text(fontSize = 18.sp, text = "取   消", color = Color.Black)
            }
        })
    }

}
