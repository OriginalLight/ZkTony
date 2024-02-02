package com.zktony.android.ui

import android.graphics.Color.rgb
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.TableTextBody
import com.zktony.android.ui.components.TableTextHead
import com.zktony.android.ui.mothersettingprogressbar.CoagulantProgressBarVertical
import com.zktony.android.ui.mothersettingprogressbar.HighCoagulantProgressBarVertical
import com.zktony.android.ui.mothersettingprogressbar.LowCoagulantProgressBarVertical
import com.zktony.android.ui.mothersettingprogressbar.VerticalProgressBar
import com.zktony.android.ui.mothersettingprogressbar.WasteVerticalProgressBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.mothersettingprogressbar.WaterVerticalProgressBar
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.line
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.AppStateUtils.hpc
import com.zktony.android.utils.AppStateUtils.hpd
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.playAudio
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction1

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val selectedER by viewModel.selectedER.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()
    val job by viewModel.job.collectAsStateWithLifecycle()
    val complate by viewModel.complate.collectAsStateWithLifecycle()
    val process by viewModel.progress.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()

    val erEntities = viewModel.erEntities.collectAsLazyPagingItems()


    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.PROGRAM_LIST -> viewModel.dispatch(HomeIntent.NavTo(PageType.HOME))
                else -> {}
            }
        }
    }

    BackHandler { navigation() }


    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(HomeIntent.Flags(UiFlags.none()))
        }
    }
    Box {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.mipmap.bkimg),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeAppBar(page) { navigation() }
            operate(
                entities,
                entities.toList(),
                erEntities.toList(),
                selected,
                selectedER,
                uiFlags,
                job,
                viewModel::dispatch,
                complate,
                process
            )
        }
    }

}

/**
 * 制胶操作
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun operate(
    entitiesLazy: LazyPagingItems<Program>,
    entities: List<Program>,
    erEntities: List<ExperimentRecord>,
    selected: Long,
    selectedER: Long,
    uiFlags: UiFlags,
    job: Job?,
    uiEvent: (HomeIntent) -> Unit,
    complate: Int,
    process: Float
) {

    val scope = rememberCoroutineScope()

    val keyboard = LocalSoftwareKeyboardController.current

    val context = LocalContext.current

    /**
     * 选中的程序
     */
    var selectedIndex by remember { mutableStateOf(0) }

    var programId = rememberDataSaverState(key = "programid", default = 1L)

    var erSelectedIndex by remember { mutableStateOf(0L) }

    /**
     * 选中的实体
     */
    val program = entities.find {
        it.id == programId.value
    } ?: Program()

    val experimentRecord = erEntities.find {
        it.id == selectedER
    } ?: ExperimentRecord()

    uiEvent(HomeIntent.Selected(program.id))


    /**
     * 纯水弹窗
     */
    val waterDialog = remember { mutableStateOf(false) }

    /**
     * 促凝剂弹窗
     */
    val coagulantDialog = remember { mutableStateOf(false) }

    /**
     * 低浓度弹窗
     */
    val lowDialog = remember { mutableStateOf(false) }

    /**
     * 高浓度弹窗
     */
    val highDialog = remember { mutableStateOf(false) }

    /**
     * 纯水液量/ml
     */
    val water = rememberDataSaverState(key = "water", default = 0f)
    var water_ex by remember { mutableStateOf(water.value.format(1)) }

    /**
     * 促凝剂液量/ml
     */
    val coagulant = rememberDataSaverState(key = "coagulant", default = 0f)
    var coagulant_ex by remember { mutableStateOf(coagulant.value.format(1)) }

    /**
     * 促凝剂浓度
     */
    val concentration = rememberDataSaverState(key = "concentration", default = 0)
    var concentration_ex by remember { mutableStateOf(concentration.value.toString()) }


    /**
     * 低浓度液量/ml
     */
    val lowCoagulantVol = rememberDataSaverState(key = "lowCoagulantVol", default = 0f)
    var lowCoagulantVol_ex by remember { mutableStateOf(lowCoagulantVol.value.format(1)) }

    /**
     * 低浓度
     */
    val lowCoagulant = rememberDataSaverState(key = "lowCoagulant", default = 0)
    var lowCoagulant_ex by remember { mutableStateOf(lowCoagulant.value.toString()) }


    /**
     * 高浓度液量/ml
     */
    val highCoagulantVol = rememberDataSaverState(key = "highCoagulantVol", default = 0f)
    var highCoagulantVol_ex by remember { mutableStateOf(highCoagulantVol.value.format(1)) }

    /**
     * 高浓度
     */
    val highCoagulant = rememberDataSaverState(key = "highCoagulant", default = 0)
    var highCoagulant_ex by remember { mutableStateOf(highCoagulant.value.toString()) }


    /**
     *  废液
     */
    val waste = rememberDataSaverState(key = "waste", default = 0f)
    waste.value = 0f
    var waste_ex by remember { mutableStateOf(waste.value.format(1)) }

    /**
     * 制胶预排-高浓度预排液
     */
    var higeRehearsalVolume = rememberDataSaverState(key = "higeRehearsalVolume", default = 0.0)


    /**
     * 制胶清洗-冲洗液泵清洗液量
     */
    var rinseCleanVolume = rememberDataSaverState(key = "rinseCleanVolume", default = 0.0)


    /**
     * 程序列表弹窗
     */
    val programListDialog = remember { mutableStateOf(false) }


    /**
     * 预计制胶数量
     */
    val expectedMakeNum = rememberDataSaverState(key = "expectedMakenum", default = 1)
    var expectedMakeNum_ex by remember { mutableStateOf(expectedMakeNum.value.toString()) }


    /**
     * 开始制胶/停止制胶
     */
    var startMake by remember { mutableStateOf("开始制胶") }

    /**
     * 继续制胶弹窗
     */
    val continueGlueDialog = remember { mutableStateOf(false) }

    /**
     * 清空废液槽弹窗
     */
    val wasteDialog = remember { mutableStateOf(false) }


    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) {
        continueGlueDialog.value = true
    } else if (uiFlags is UiFlags.Objects && uiFlags.objects == 6) {
        experimentRecord.status = EPStatus.COMPLETED
        experimentRecord.number = complate
        uiEvent(HomeIntent.Update(experimentRecord))
        continueGlueDialog.value = false
        uiEvent(HomeIntent.Stop)
    }

    Column(
        modifier = Modifier
            .padding(start = 13.75.dp)
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
            .background(Color.White)
            .height(904.9.dp)
            .width((572.5).dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 48.92.dp, top = 21.4.dp),
            text = "母液设置",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(
                0,
                105,
                52
            )
        )
        /**
         * 母液设置
         */
        Row(
            modifier = Modifier
                .padding(start = 48.92.dp, top = 18.22.dp)
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .clickable(onClick = {
                        waterDialog.value = true
                    })
            ) {
                //纯水进度条
                WaterVerticalProgressBar(0f, water_ex)
            }

            Row(
                modifier = Modifier
                    .padding(start = 22.82.dp)
                    .clickable(onClick = {
                        coagulantDialog.value = true
                    })
            ) {
                //促凝剂进度条
                CoagulantProgressBarVertical(
                    0f,
                    coagulant_ex,
                    concentration_ex
                )
            }

            Row(
                modifier = Modifier
                    .padding(start = 22.82.dp)
                    .clickable(onClick = {
                        lowDialog.value = true
                    })
            ) {
                //低浓度进度条
                LowCoagulantProgressBarVertical(
                    0f,
                    lowCoagulantVol_ex,
                    lowCoagulant_ex
                )
            }

            Row(
                modifier = Modifier
                    .padding(start = 22.82.dp)
                    .clickable(onClick = {
                        highDialog.value = true
                    })
            ) {
                //高浓度进度条
                HighCoagulantProgressBarVertical(
                    0f,
                    highCoagulantVol_ex,
                    highCoagulant_ex
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 22.53.dp)
                .fillMaxWidth()
        ) {
            line(Color(0, 105, 5), 48.25f, 528.5f)
        }

        Row(
            modifier = Modifier
                .padding(start = 48.92.dp, top = 21.4.dp)
                .fillMaxWidth()
        ) {
            /**
             * 程序设置
             */
            Column(
                modifier = Modifier
                    .clickable(onClick = {
                        programListDialog.value = true
                    })
            ) {
                Text(
                    text = program.displayText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(
                        0,
                        105,
                        52
                    )
                )
                Text(
                    modifier = Modifier.padding(top = 14.dp),
                    text = "浓 度:" + program.startRange + "%~" + program.endRange + "%",
                    fontSize = 18.sp
                )
                Text(
                    modifier = Modifier.padding(top = 14.dp),
                    text = "厚 度:" + program.thickness + "mm",
                    fontSize = 18.sp
                )

                Text(
                    modifier = Modifier.padding(top = 14.dp),
                    text = "胶液体积:" + program.volume + "mL",
                    fontSize = 18.sp
                )
                Text(
                    modifier = Modifier.padding(top = 14.dp),
                    text = "促凝剂体积:" + program.coagulant + "μL",
                    fontSize = 18.sp
                )

            }

            Column(modifier = Modifier.padding(start = 80.dp)) {
                Text(
                    text = "制胶数量",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(
                        0,
                        105,
                        52
                    )
                )

                Text(
                    modifier = Modifier.padding(top = 21.2.dp),
                    fontSize = 18.sp,
                    text = "预计制胶数量"
                )
                Row(
                    modifier = Modifier.padding(top = 13.7.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(id = R.mipmap.reduce), contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                if (expectedMakeNum.value > 1) {
                                    expectedMakeNum.value -= 1
                                    expectedMakeNum_ex = expectedMakeNum.value.toString()
                                }

                            }
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.2.dp),
                        textStyle = TextStyle.Default.copy(
                            fontSize = 29.sp,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        value = expectedMakeNum_ex,
                        onValueChange = {
                            expectedMakeNum_ex = it
                            expectedMakeNum.value = it.toIntOrNull() ?: 0

                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        )
                    )
                    Image(
                        painter = painterResource(id = R.mipmap.add), contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                if (expectedMakeNum_ex.toIntOrNull() ?: 1 < expectedMakeNum.value) {
                                    expectedMakeNum.value += 1
                                    expectedMakeNum_ex = expectedMakeNum.value.toString()
                                }

                            }
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 14.0.dp),
                    fontSize = 18.sp,
                    text = "已制胶数量:$complate"
                )
            }

        }

        Row(
            modifier = Modifier
                .padding(top = 22.53.dp)
                .fillMaxWidth()
        ) {
            line(Color(0, 105, 5), 48.25f, 528.5f)
        }

        Text(
            modifier = Modifier.padding(start = 48.92.dp, top = 21.4.dp),
            text = "制胶进度",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(
                0,
                105,
                52
            )
        )
        Row(modifier = Modifier.padding(start = 48.92.dp, top = 21.4.dp)) {

            Row(
                modifier = Modifier
                    .clickable(onClick = {

                    })
            ) {
                //废液进度条
                WasteVerticalProgressBar(waste.value / 150f, waste_ex)
            }

            Row(
                modifier = Modifier
                    .padding(start = 17.8.dp)
                    .clickable(onClick = {

                    })
            ) {
                //制胶进度的进度条
                VerticalProgressBar(
                    if (process > 0.01f) process else 0f,
                    if (process < 0.01f) "0.0" else (process * 100).toString()
                )
            }

        }

        /**
         * 操作按钮
         */
        Row(
            modifier = Modifier
                .padding(top = 21.4.dp)
                .fillMaxWidth()
                .height(143.98.dp)
                .background(Color(239, 239, 239))
        ) {

            Column(
                modifier = Modifier
                    .padding(start = 47.55.dp)
            ) {
                Row {
                    Image(
                        painter = painterResource(id = if (job == null) R.mipmap.start else R.mipmap.stop),
                        contentDescription = null,
                        modifier = Modifier
                            .size(63.dp, 63.dp)
                            .clickable {
                                if (job == null) {
                                    if (uiFlags is UiFlags.None) {

                                        val coagulantRehearsal =
                                            (program.coagulant / 1000) / program.volume * higeRehearsalVolume.value

                                        waste.value +=
                                            coagulantRehearsal.toFloat() + higeRehearsalVolume.value.toFloat()

                                        waste_ex = waste.value.toString()
                                        if (waste.value / 150f > 1f) {
                                            wasteDialog.value = true
                                        } else {
                                            startMake = "停止制胶"
                                            uiEvent(HomeIntent.Start(0))
                                            uiEvent(
                                                HomeIntent.Insert(
                                                    program.startRange,
                                                    program.endRange,
                                                    program.thickness,
                                                    program.coagulant,
                                                    program.volume,
                                                    complate,
                                                    EPStatus.RUNNING,
                                                    ""
                                                )
                                            )
                                            Log.d(
                                                "Home",
                                                "selectedER===$selectedER"
                                            )

                                        }

                                    }
                                } else {
                                    experimentRecord.status = EPStatus.ABORT
                                    experimentRecord.detail = "手动停止制胶"
                                    uiEvent(HomeIntent.Update(experimentRecord))
                                    startMake = "开始制胶"
                                    uiEvent(HomeIntent.Stop)

                                }
                            }
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.filling),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 82.0.dp)
                            .size(63.dp, 63.dp)
                            .clickable {
                                scope.launch {
                                    Log.d(
                                        "Home",
                                        "uiFlags" + uiFlags
                                    )
                                    if (uiFlags is UiFlags.None) {
                                        uiEvent(HomeIntent.Pipeline(1))
                                    }
//                                        else {
//                                            uiEvent(HomeIntent.Pipeline(0))
//                                        }
                                }

                            }
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.clean),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 80.4.dp)
                            .size(63.dp, 63.dp)
                            .clickable {
                                scope.launch {
                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 2)) {
                                        uiEvent(HomeIntent.Clean)
                                    }
                                }

                            }
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.reset),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 71.5.dp)
                            .size(63.dp, 63.dp)
                            .clickable {
                                if (uiFlags is UiFlags.None) {
                                    waste.value = 0f
                                    uiEvent(HomeIntent.Reset)
                                }
                            }
                    )
                }

                Row {
                    Text(
                        text = if (job == null) "开始制胶" else "停止制胶",
                        color = if (job == null) Color.Black else Color.Red,
                        fontSize = 18.sp
                    )
                    Text(
                        modifier = Modifier.padding(start = 70.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) "填充中" else "管路填充",
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) Color.Red else Color.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        modifier = Modifier.padding(start = 68.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) "清洗中" else "管路清洗",
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) Color.Red else Color.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        modifier = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) Modifier.padding(
                            start = 72.dp
                        ) else Modifier.padding(start = 78.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) "复位中" else "复 位",
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) Color.Red else Color.Black,
                        fontSize = 18.sp
                    )
                }

            }


        }


    }



    if (wasteDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    fontSize = 16.sp,
                    text = "废液槽已满，请清空废液槽！"
                )
            },
            text = {
            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    wasteDialog.value = false
                    waste.value = 0f
                    waste_ex = "0"
                }) {
                    Text(text = "确定")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    wasteDialog.value = false
                }) {
                    Text(text = "取消")
                }
            })
    }

    /**
     * 继续制胶弹窗
     */
    if (continueGlueDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = {
            },
            text = {
                Text(
                    fontSize = 16.sp,
                    text = "请更换制胶架，继续制胶!"
                )

            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    experimentRecord.number = complate
                    uiEvent(HomeIntent.Update(experimentRecord))
                    continueGlueDialog.value = false
                    uiEvent(HomeIntent.Start(1))
                }) {
                    Text(text = "继续")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    experimentRecord.number = complate
                    experimentRecord.status = EPStatus.ABORT
                    experimentRecord.detail = "手动停止制胶"
                    uiEvent(HomeIntent.Update(experimentRecord))
                    continueGlueDialog.value = false
                    uiEvent(HomeIntent.Stop)
                }) {
                    Text(text = "停止")
                }
            })
    }

    /**
     * 纯水弹窗
     */
    if (waterDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = {
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        fontSize = 16.sp,
                        text = "冲洗液量/ml："
                    )
                    OutlinedTextField(
                        modifier = Modifier.width(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(rgb(0, 105, 52)),
                            focusedLabelColor = Color(rgb(0, 105, 52)),
                            cursorColor = Color(rgb(0, 105, 52))
                        ),
                        value = water_ex,
                        label = { Text(text = "ml") },
                        onValueChange = {
                            water_ex = it
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        )
                    )

                }
            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    if (water_ex.toFloatOrNull() ?: 0f <= 50) {
                        water.value = water_ex.toFloatOrNull() ?: 0f
                        waterDialog.value = false
                    } else {
                        Toast.makeText(
                            context,
                            "容量不能大于50ML！",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    water_ex = water.value.toString()
                    waterDialog.value = false
                }) {
                    Text(text = "取消")
                }
            })
    }


    /**
     * 促凝剂弹窗
     */
    if (coagulantDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = {

            },
            text = {

                Column {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 16.sp,
                            text = "促凝剂浓度/%："
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            value = concentration_ex,
                            label = { Text(text = "%") },
                            onValueChange = {
                                concentration_ex = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            )
                        )
                        Text(
                            fontSize = 16.sp,
                            text = "%"
                        )

                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 16.sp,
                            text = "促凝剂液量/ml："
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            value = coagulant_ex,
                            label = { Text(text = "ml") },
                            onValueChange = {
                                coagulant_ex = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            )
                        )

                    }
                }

            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    if (coagulant_ex.toFloatOrNull() ?: 0f <= 50) {
                        concentration.value = concentration_ex.toIntOrNull() ?: 0
                        coagulant.value = coagulant_ex.toFloatOrNull() ?: 0f
                        coagulantDialog.value = false
                    } else {
                        Toast.makeText(
                            context,
                            "容量不能大于50ML！",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    concentration_ex = concentration.value.toString()
                    coagulant_ex = coagulant.value.toString()
                    coagulantDialog.value = false
                }) {
                    Text(text = "取消")
                }
            })
    }


    /**
     * 低浓度弹窗
     */
    if (lowDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = {

            },
            text = {


                Column {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 16.sp,
                            text = "低浓度浓度/%："
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            value = lowCoagulant_ex,
                            label = { Text(text = "%") },
                            onValueChange = {
                                lowCoagulant_ex = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            )
                        )
                        Text(
                            fontSize = 16.sp,
                            text = "%"
                        )

                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 16.sp,
                            text = "低浓度液量/ml："
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            value = lowCoagulantVol_ex,
                            label = { Text(text = "ml") },
                            onValueChange = {
                                lowCoagulantVol_ex = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            )
                        )

                    }
                }

            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    if (lowCoagulantVol_ex.toFloatOrNull() ?: 0f <= 50) {
                        lowCoagulant.value = lowCoagulant_ex.toIntOrNull() ?: 0
                        lowCoagulantVol.value = lowCoagulantVol_ex.toFloatOrNull() ?: 0f
                        lowDialog.value = false
                    } else {
                        Toast.makeText(
                            context,
                            "容量不能大于50ML！",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    lowCoagulant_ex = lowCoagulant.value.toString()
                    lowCoagulantVol_ex = lowCoagulantVol.value.toString()
                    lowDialog.value = false
                }) {
                    Text(text = "取消")
                }
            })
    }


    /**
     * 高浓度弹窗
     */
    if (highDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
            },
            text = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 16.sp,
                            text = "高浓度浓度/%："
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            value = highCoagulant_ex,
                            label = { Text(text = "%") },
                            onValueChange = {
                                highCoagulant_ex = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            )
                        )
                        Text(
                            fontSize = 16.sp,
                            text = "%"
                        )

                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 16.sp,
                            text = "高浓度液量/ml："
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(rgb(0, 105, 52)),
                                focusedLabelColor = Color(rgb(0, 105, 52)),
                                cursorColor = Color(rgb(0, 105, 52))
                            ),
                            value = highCoagulantVol_ex,
                            label = { Text(text = "ml") },
                            onValueChange = {
                                highCoagulantVol_ex = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            )
                        )

                    }
                }

            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    if (highCoagulantVol_ex.toFloatOrNull() ?: 0f <= 50) {
                        highCoagulant.value = highCoagulant_ex.toIntOrNull() ?: 0
                        highCoagulantVol.value = highCoagulantVol_ex.toFloatOrNull() ?: 0f
                        highDialog.value = false
                    } else {
                        Toast.makeText(
                            context,
                            "容量不能大于50ML！",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {
                    highCoagulant_ex = highCoagulant.value.toString()
                    highCoagulantVol_ex = highCoagulantVol.value.toString()
                    highDialog.value = false
                }) {
                    Text(text = "取消")
                }
            })
    }

    /**
     * 程序列表弹窗
     */
    if (programListDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            text = {
                //	定义列宽
                val cellWidthList = arrayListOf(70, 100, 130, 90, 100, 120)
                //	使用lazyColumn来解决大数据量时渲染的性能问题
                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                ) {
                    //	粘性标题
                    stickyHeader {
                        Row(Modifier.background(Color(android.graphics.Color.rgb(0, 105, 52)))) {
                            TableTextHead(text = "序号", width = cellWidthList[0])
                            TableTextHead(text = "名称", width = cellWidthList[1])
                            TableTextHead(text = "浓度", width = cellWidthList[2])
                            TableTextHead(text = "厚度", width = cellWidthList[3])
                        }
                    }
                    itemsIndexed(entitiesLazy) { index, item ->
                        val selectedEntity = item == entities[selectedIndex]
                        Row(
                            modifier = Modifier
                                .background(
                                    if (index % 2 == 0) Color(
                                        android.graphics.Color.rgb(
                                            239,
                                            239,
                                            239
                                        )
                                    ) else Color.White
                                )
                                .clickable(onClick = {
                                    selectedIndex = index
                                    val entity = entities[selectedIndex]
                                    if (entity != null) {
                                        programId.value = entity.id
                                        Log.d(
                                            "Test",
                                            "home中选中的entityId===" + entity.id
                                        )
                                        uiEvent(HomeIntent.Selected(entity.id))
                                    }

                                })
                        ) {
                            TableTextBody(
                                text = "" + item.id,
                                width = cellWidthList[0],
                                selectedEntity
                            )
                            TableTextBody(
                                text = item.displayText,
                                width = cellWidthList[1],
                                selectedEntity
                            )
                            TableTextBody(
                                text = "" + item.startRange + "%~" + item.endRange + "%",
                                width = cellWidthList[2], selectedEntity
                            )
                            TableTextBody(
                                text = item.thickness + "mm",
                                width = cellWidthList[3],
                                selectedEntity
                            )
                        }
                    }

                }
            }, confirmButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = {

                    programListDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ), onClick = { programListDialog.value = false }) {
                    Text(text = "取消")
                }
            })
    }
}

