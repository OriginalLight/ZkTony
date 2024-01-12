package com.zktony.android.ui

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.TableText
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
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ApplicationUtils
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
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()
    val job by viewModel.job.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()

//    val xpulse = rememberDataSaverState(key = "xpulse", default = 0L)
//    val coagulantpulse = rememberDataSaverState(key = "coagulantpulse", default = 0L)

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

    Column {
        HomeAppBar(page) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.HOME -> operate(
                    entities,
                    entities.toList(),
                    selected,
                    uiFlags,
                    job,
                    viewModel::dispatch
                )
            }
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
    selected: Long,
    uiFlags: UiFlags,
    job: Job?,
    uiEvent: (HomeIntent) -> Unit
) {

    val scope = rememberCoroutineScope()

    val keyboard = LocalSoftwareKeyboardController.current


    /**
     * 选中的程序
     */
    var selectedIndex by remember { mutableStateOf(0) }

    var programId = rememberDataSaverState(key = "programid", default = 1L)

    /**
     * 选中的实体
     */
    val program = entities.find {
        it.id == programId.value
    } ?: Program()

    /**
     * 纯水进度
     */
    val waterSweepState = remember {
        mutableStateOf(0f)
    }

    /**
     * 促凝剂进度
     */
    val coagulantSweepState = remember {
        mutableStateOf(0f)
    }

    /**
     * 低浓度进度
     */
    val lowCoagulantSweepState = remember {
        mutableStateOf(0f)
    }

    /**
     * 高浓度进度
     */
    val highCoagulantSweepState = remember {
        mutableStateOf(0f)
    }

    /**
     * 废液
     */
    val wasteSweepState = remember {
        mutableStateOf(0f)
    }

    /**
     * 制胶进度
     */
    val progressSweepState = remember {
        mutableStateOf(0f)
    }

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
    val concentration = rememberDataSaverState(key = "concentration", default = 0f)
    var concentration_ex by remember { mutableStateOf(concentration.value.format(1)) }


    /**
     * 低浓度液量/ml
     */
    val lowCoagulantVol = rememberDataSaverState(key = "lowCoagulantVol", default = 0f)
    var lowCoagulantVol_ex by remember { mutableStateOf(lowCoagulantVol.value.format(1)) }

    /**
     * 低浓度
     */
    val lowCoagulant = rememberDataSaverState(key = "lowCoagulant", default = 0f)
    var lowCoagulant_ex by remember { mutableStateOf(lowCoagulant.value.format(1)) }


    /**
     * 高浓度液量/ml
     */
    val highCoagulantVol = rememberDataSaverState(key = "highCoagulantVol", default = 0f)
    var highCoagulantVol_ex by remember { mutableStateOf(highCoagulantVol.value.format(1)) }

    /**
     * 高浓度
     */
    val highCoagulant = rememberDataSaverState(key = "highCoagulant", default = 0f)
    var highCoagulant_ex by remember { mutableStateOf(highCoagulant.value.format(1)) }


    /**
     *  废液
     */
    val waste = rememberDataSaverState(key = "waste", default = 0f)
    var waste_ex by remember { mutableStateOf(waste.value.format(1)) }


    /**
     *  制胶进度
     */
    val progressVol = rememberDataSaverState(key = "progressVol", default = 0f)
    var progressVol_ex by remember { mutableStateOf(progressVol.value.format(1)) }

    /**
     * 程序列表弹窗
     */
    val programListDialog = remember { mutableStateOf(false) }

    /**
     * 已完成的制胶数量
     */
    var complete = rememberDataSaverState(key = "complete", default = 0)
    var complete_ex by remember { mutableStateOf(complete.value.toString()) }


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

    println("uiFlags is UiFlags.Objects$uiFlags")
    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) {
        complete = rememberDataSaverState(key = "complete", default = 0)
        continueGlueDialog.value = true
    } else if (uiFlags is UiFlags.Objects && uiFlags.objects == 6) {
        complete = rememberDataSaverState(key = "complete", default = 0)
        continueGlueDialog.value = false
        uiEvent(HomeIntent.Stop)
    }



    Column(modifier = Modifier.padding(start = 20.dp, top = 20.dp)) {


        Text(
            modifier = Modifier.padding(start = 40.dp),
            text = "母液设置",
            fontSize = 20.sp
        )
        /**
         * 母液设置
         */
        Row(modifier = Modifier.padding(start = 50.dp, top = 20.dp)) {

            Row(
                modifier = Modifier
                    .clickable(onClick = {
                        waterDialog.value = true
                    })
            ) {
                //纯水进度条
                WaterVerticalProgressBar(waterSweepState, water_ex)
            }

            Row(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .clickable(onClick = {
                        coagulantDialog.value = true
                    })
            ) {
                //促凝剂进度条
                CoagulantProgressBarVertical(coagulantSweepState, coagulant_ex, concentration_ex)
            }

            Row(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .clickable(onClick = {
                        lowDialog.value = true
                    })
            ) {
                //低浓度进度条
                LowCoagulantProgressBarVertical(
                    lowCoagulantSweepState,
                    lowCoagulantVol_ex,
                    lowCoagulant_ex
                )
            }

            Row(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .clickable(onClick = {
                        highDialog.value = true
                    })
            ) {
                //高浓度进度条
                HighCoagulantProgressBarVertical(
                    highCoagulantSweepState,
                    highCoagulantVol_ex,
                    highCoagulant_ex
                )
            }
        }

        /**
         * 程序设置
         */
        Column(
            modifier = Modifier
                .padding(start = 20.dp, top = 20.dp)
                .clickable(onClick = {
                    programListDialog.value = true
                })
        ) {
            Text(
                modifier = Modifier.padding(start = 40.dp),
                text = "程序名称:" + program.displayText,
                fontSize = 20.sp
            )
            Row {
                Text(
                    modifier = Modifier.padding(start = 40.dp),
                    text = "浓度:" + program.startRange + "%~" + program.endRange + "%",
                    fontSize = 20.sp
                )
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "厚度:" + program.thickness + "mm",
                    fontSize = 20.sp
                )
            }

            Row {
                Text(
                    modifier = Modifier.padding(start = 40.dp),
                    text = "胶液体积:" + program.volume + "mL",
                    fontSize = 20.sp
                )
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "促凝剂体积:" + program.coagulant + "μL",
                    fontSize = 20.sp
                )
            }

        }


        /**
         * 制胶数量
         */
        Row(
            modifier = Modifier.padding(start = 50.dp, top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fontSize = 16.sp,
                text = "预计制胶数量:"
            )

            OutlinedTextField(
                modifier = Modifier.width(100.dp),
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
            Text(text = "已制胶数量:" + complete.value)
        }


        Text(
            modifier = Modifier.padding(start = 40.dp, top = 20.dp),
            text = "制胶进度",
            fontSize = 20.sp
        )
        Row(modifier = Modifier.padding(start = 50.dp, top = 20.dp)) {

            Row(
                modifier = Modifier
                    .clickable(onClick = {

                    })
            ) {
                //废液进度条
                WasteVerticalProgressBar(wasteSweepState, waste_ex)
            }

            Row(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .clickable(onClick = {

                    })
            ) {
                //制胶进度的进度条
                VerticalProgressBar(progressSweepState, progressVol_ex)
            }

        }

        /**
         * 操作按钮
         */
        Row(modifier = Modifier.padding(start = 50.dp, top = 40.dp)) {

            Column {
                Row {
                    Image(
                        painter = painterResource(id = if (job == null) R.mipmap.start else R.mipmap.stop),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                if (job == null) {
                                    if (uiFlags is UiFlags.None) {
                                        complete.value = 0
                                        complete_ex = "0"
                                        startMake = "停止制胶"
                                        uiEvent(HomeIntent.Start(complete.value))
                                    }
                                } else {
                                    startMake = "开始制胶"
                                    uiEvent(HomeIntent.Stop)

                                }
                            }
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.filling), contentDescription = null,
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(100.dp)
                            .clickable {
                                scope.launch {
                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 3)) {
                                        if (uiFlags is UiFlags.None) {
                                            uiEvent(HomeIntent.Pipeline(1))
                                        } else {
                                            uiEvent(HomeIntent.Pipeline(0))
                                        }
                                    }
                                }

                            }
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.clean), contentDescription = null,
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(100.dp)
                            .clickable {
                                scope.launch {
                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 2)) {
                                        uiEvent(HomeIntent.Clean)
                                    }
                                }

                            }
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.reset), contentDescription = null,
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(100.dp)
                            .clickable {
                                if (job == null) {
                                    uiEvent(HomeIntent.Reset)
                                }
                            }
                    )
                }

                Row {
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = if (job == null) "开始制胶" else "停止制胶",
                        color = if (job == null) Color.Black else Color.Red,
                        fontSize = 18.sp
                    )
                    Text(
                        modifier = Modifier.padding(start = 50.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) "填充中" else "管路填充",
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) Color.Red else Color.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        modifier = Modifier.padding(start = 50.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) "清洗中" else "管路清洗",
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) Color.Red else Color.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        modifier = Modifier.padding(start = 60.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) "复位中" else "复位",
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) Color.Red else Color.Black,
                        fontSize = 18.sp
                    )
                }

            }


        }


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
                TextButton(onClick = {
                    continueGlueDialog.value = false
                    uiEvent(HomeIntent.Start(complete.value))
                }) {
                    Text(text = "继续")
                }
            }, dismissButton = {
                TextButton(onClick = {
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
            onDismissRequest = { waterDialog.value = false },
            title = {
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        fontSize = 16.sp,
                        text = "纯水液量/ml："
                    )
                    OutlinedTextField(
                        modifier = Modifier.width(100.dp),
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
                TextButton(onClick = {
                    water.value = water_ex.toFloatOrNull() ?: 0f
                    waterDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { waterDialog.value = false }) {
                    Text(text = "取消")
                }
            })
    }


    /**
     * 促凝剂弹窗
     */
    if (coagulantDialog.value) {
        AlertDialog(
            onDismissRequest = { coagulantDialog.value = false },
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
                TextButton(onClick = {
                    concentration.value = concentration_ex.toFloatOrNull() ?: 0f
                    coagulant.value = coagulant_ex.toFloatOrNull() ?: 0f
                    coagulantDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { coagulantDialog.value = false }) {
                    Text(text = "取消")
                }
            })
    }


    /**
     * 低浓度弹窗
     */
    if (lowDialog.value) {
        AlertDialog(
            onDismissRequest = { lowDialog.value = false },
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
                TextButton(onClick = {
                    lowCoagulant.value = lowCoagulant_ex.toFloatOrNull() ?: 0f
                    lowCoagulantVol.value = lowCoagulantVol_ex.toFloatOrNull() ?: 0f
                    lowDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { lowDialog.value = false }) {
                    Text(text = "取消")
                }
            })
    }


    /**
     * 高浓度弹窗
     */
    if (highDialog.value) {
        AlertDialog(
            onDismissRequest = { highDialog.value = false },
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
                TextButton(onClick = {
                    highCoagulant.value = highCoagulant_ex.toFloatOrNull() ?: 0f
                    highCoagulantVol.value = highCoagulantVol_ex.toFloatOrNull() ?: 0f
                    highDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { highDialog.value = false }) {
                    Text(text = "取消")
                }
            })
    }

    /**
     * 程序列表弹窗
     */
    if (programListDialog.value) {
        AlertDialog(
            onDismissRequest = { programListDialog.value = false },
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
                        Row(Modifier.background(Color.Gray)) {
                            TableText(text = "序号", width = cellWidthList[0])
                            TableText(text = "名称", width = cellWidthList[1])
                            TableText(text = "浓度", width = cellWidthList[2])
                            TableText(text = "厚度", width = cellWidthList[3])
                        }
                    }
                    itemsIndexed(entitiesLazy) { index, item ->
                        val selectedEntity = item == entities[selectedIndex]
                        Row(
                            modifier = Modifier
                                .background(if (selectedEntity) Color.Gray else Color.White)
                                .clickable(onClick = {
                                    selectedIndex = index
                                    val entity = entities[selectedIndex]
                                    if (entity != null) {
                                        programId.value = entity.id
                                        HomeIntent.Selected(entity.id)
                                    }

                                })
                        ) {
                            TableText(text = "" + item.id, width = cellWidthList[0])
                            TableText(text = item.displayText, width = cellWidthList[1])
                            TableText(
                                text = "" + item.startRange + "%~" + item.endRange + "%",
                                width = cellWidthList[2]
                            )
                            TableText(text = item.thickness + "mm", width = cellWidthList[3])
                        }
                    }

                }
            }, confirmButton = {
                TextButton(onClick = {

                    programListDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { programListDialog.value = false }) {
                    Text(text = "取消")
                }
            })
    }
}


//@Composable
//fun ProgramList(
//    entities: LazyPagingItems<Program>,
//    dispatch: KFunction1<HomeIntent, Unit>,
//) {
//    val scope = rememberCoroutineScope()
//
//    LazyVerticalGrid(
//        modifier = Modifier.padding(16.dp),
//        contentPadding = PaddingValues(16.dp),
//        columns = GridCells.Fixed(2),
//        horizontalArrangement = Arrangement.spacedBy(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        itemsIndexed(entities) { index, item ->
//            ListItem(
//                modifier = Modifier
//                    .clip(MaterialTheme.shapes.small)
//                    .clickable {
//                        scope.launch {
//                            dispatch(HomeIntent.Selected(item.id))
//                            dispatch(HomeIntent.NavTo(PageType.HOME))
//                        }
//                    },
//                headlineContent = {
//                    Text(
//                        text = item.displayText,
//                        style = MaterialTheme.typography.titleMedium,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                },
//                supportingContent = {
//                    Text(
//                        text = item.createTime.dateFormat("yyyy/MM/dd"),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray
//                    )
//                },
//                leadingContent = {
//                    Text(
//                        text = "${index + 1}、",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontStyle = FontStyle.Italic
//                    )
//                },
//                colors = ListItemDefaults.colors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeContent(
//    entities: List<Program>,
//    selected: Long,
//    uiFlags: UiFlags,
//    job: Job?,
//    uiEvent: (HomeIntent) -> Unit
//) {
//    val scope = rememberCoroutineScope()
//    val navigationActions = LocalNavigationActions.current
//    val snackbarHostState = LocalSnackbarHostState.current
//    var time by remember { mutableLongStateOf(0L) }
//    val program = entities.find { it.id == selected } ?: Program()
//
//    LaunchedEffect(key1 = job) {
//        while (true) {
//            if (job != null) {
//                time += 1
//            } else {
//                time = 0
//            }
//            delay(1000L)
//        }
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Column(
//            modifier = Modifier.weight(1f),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//        ) {
//            Card(
//                onClick = {
//                    scope.launch {
//                        if (job == null) {
//                            uiEvent(HomeIntent.NavTo(PageType.PROGRAM_LIST))
//                        }
//                    }
//                },
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Column {
//                        Text(
//                            text = program.displayText,
//                            style = TextStyle(
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 20.sp,
//                                fontStyle = FontStyle.Italic,
//                            )
//                        )
//                        Text(
//                            text = program.createTime.dateFormat("yyyy/MM/dd"),
//                            style = TextStyle(
//                                fontFamily = FontFamily.Monospace,
//                                fontSize = 12.sp,
//                            ),
//                            color = Color.Gray,
//                        )
//                    }
//                    Icon(
//                        modifier = Modifier.size(24.dp),
//                        imageVector = Icons.Default.ArrowRight,
//                        contentDescription = null
//                    )
//                }
//            }
//
//            Card {
//                Box(
//                    modifier = Modifier.padding(8.dp),
//                ) {
//                    if (job != null) {
//                        CircularProgressIndicator(
//                            modifier = Modifier
//                                .size(24.dp)
//                                .align(Alignment.TopStart),
//                            strokeWidth = 4.dp,
//                        )
//                    }
//                    Text(
//                        modifier = Modifier.fillMaxWidth(),
//                        text = time.timeFormat(),
//                        textAlign = TextAlign.Center,
//                        style = TextStyle(
//                            fontSize = 64.sp,
//                            fontWeight = FontWeight.Bold,
//                            fontFamily = FontFamily.Monospace,
//                        )
//                    )
//                }
//            }
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                Card {
//                    Text(
//                        modifier = Modifier
//                            .padding(16.dp),
//                        text = stringResource(id = R.string.glue_making),
//                        style = MaterialTheme.typography.titleLarge,
//                    )
//                }
//
//                Card(
//                    modifier = Modifier.weight(1f),
//                ) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
////                        text = program.dosage.coagulant.format(1),
//                        text = "",
//                        style = MaterialTheme.typography.titleLarge,
//                        textAlign = TextAlign.Center,
//                        fontFamily = FontFamily.Monospace,
//                    )
//                }
//
//
//                Card(
//                    modifier = Modifier.weight(1f),
//                ) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        text = "",
////                        text = program.dosage.colloid.format(1),
//                        style = MaterialTheme.typography.titleLarge,
//                        textAlign = TextAlign.Center,
//                        fontFamily = FontFamily.Monospace,
//                    )
//                }
//            }
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                Card {
//                    Text(
//                        modifier = Modifier
//                            .padding(16.dp),
//                        text = stringResource(id = R.string.pre_drain),
//                        style = MaterialTheme.typography.titleLarge,
//                    )
//                }
//
//                Card(
//                    modifier = Modifier.weight(1f),
//                ) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        text = "",
////                        text = program.dosage.preCoagulant.format(1),
//                        style = MaterialTheme.typography.titleLarge,
//                        textAlign = TextAlign.Center,
//                        fontFamily = FontFamily.Monospace,
//                    )
//                }
//
//                Card(
//                    modifier = Modifier.weight(1f),
//                ) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        text = "",
////                        text = program.dosage.preColloid.format(1),
//                        style = MaterialTheme.typography.titleLarge,
//                        textAlign = TextAlign.Center,
//                        fontFamily = FontFamily.Monospace,
//                    )
//                }
//            }
//        }
//
//        Box(
//            modifier = Modifier.weight(1f),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                modifier = Modifier
//                    .size(196.dp)
//                    .clip(CircleShape)
//                    .clickable {
//                        scope.launch {
//                            if (uiFlags is UiFlags.None) {
//                                if (job == null) {
//                                    if (selected == 0L) {
//                                        navigationActions.navigate(Route.PROGRAM)
//                                    } else {
//                                        uiEvent(HomeIntent.Start)
//                                    }
//                                } else {
//                                    uiEvent(HomeIntent.Stop)
//                                }
//                            } else {
//                                snackbarHostState.showSnackbar("请先完成当前操作")
//                            }
//                        }
//                    },
//                imageVector = if (job == null) Icons.Default.PlayArrow else Icons.Default.Close,
//                contentDescription = null,
//                tint = if (job == null) MaterialTheme.colorScheme.primary else Color.Red
//            )
//
//            if (job == null) {
//                Column(
//                    modifier = Modifier.align(Alignment.CenterEnd),
//                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                    horizontalAlignment = Alignment.End
//                ) {
//                    Text(
//                        modifier = Modifier
//                            .background(
//                                color = MaterialTheme.colorScheme.surfaceVariant,
//                                shape = MaterialTheme.shapes.small
//                            )
//                            .clip(MaterialTheme.shapes.small)
//                            .clickable {
//                                scope.launch {
//                                    if (uiFlags !is UiFlags.None) {
//                                        uiEvent(HomeIntent.Reset)
//                                    }
//                                }
//                            }
//                            .padding(vertical = 8.dp, horizontal = 16.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) "复位中" else "复位",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) Color.Red else Color.Unspecified
//                    )
//                    Text(
//                        modifier = Modifier
//                            .background(
//                                color = MaterialTheme.colorScheme.surfaceVariant,
//                                shape = MaterialTheme.shapes.small
//                            )
//                            .clip(MaterialTheme.shapes.small)
//                            .clickable {
//                                scope.launch {
//                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 2)) {
//                                        uiEvent(HomeIntent.Clean)
//                                    }
//                                }
//                            }
//                            .padding(vertical = 8.dp, horizontal = 16.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) "清洗中" else "清洗",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) Color.Red else Color.Unspecified
//                    )
//                    Text(
//                        modifier = Modifier
//                            .background(
//                                color = MaterialTheme.colorScheme.surfaceVariant,
//                                shape = MaterialTheme.shapes.small
//                            )
//                            .clip(MaterialTheme.shapes.small)
//                            .clickable {
//                                scope.launch {
//                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 3)) {
//                                        if (uiFlags is UiFlags.None) {
//                                            uiEvent(HomeIntent.Pipeline(1))
//                                        } else {
//                                            uiEvent(HomeIntent.Pipeline(0))
//                                        }
//                                    }
//                                }
//                            }
//                            .padding(vertical = 8.dp, horizontal = 16.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 3) "填充胶体中" else "填充胶体",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 3) Color.Red else Color.Unspecified
//                    )
//                    Text(
//                        modifier = Modifier
//                            .background(
//                                color = MaterialTheme.colorScheme.surfaceVariant,
//                                shape = MaterialTheme.shapes.small
//                            )
//                            .clip(MaterialTheme.shapes.small)
//                            .clickable {
//                                scope.launch {
//                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 4)) {
//                                        if (uiFlags is UiFlags.None) {
//                                            uiEvent(HomeIntent.Pipeline(2))
//                                        } else {
//                                            uiEvent(HomeIntent.Pipeline(0))
//                                        }
//                                    }
//                                }
//                            }
//                            .padding(vertical = 8.dp, horizontal = 16.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) "回吸胶体中" else "回吸胶体",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) Color.Red else Color.Unspecified
//                    )
//                    Text(
//                        modifier = Modifier
//                            .background(
//                                color = MaterialTheme.colorScheme.surfaceVariant,
//                                shape = MaterialTheme.shapes.small
//                            )
//                            .clip(MaterialTheme.shapes.small)
//                            .clickable {
//                                scope.launch {
//                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 5)) {
//                                        if (uiFlags is UiFlags.None) {
//                                            uiEvent(HomeIntent.Syringe(1))
//                                        } else {
//                                            uiEvent(HomeIntent.Syringe(0))
//                                        }
//                                    }
//                                }
//                            }
//                            .padding(vertical = 8.dp, horizontal = 16.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) "填充促凝剂中" else "填充促凝剂",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) Color.Red else Color.Unspecified
//                    )
//                    Text(
//                        modifier = Modifier
//                            .background(
//                                color = MaterialTheme.colorScheme.surfaceVariant,
//                                shape = MaterialTheme.shapes.small
//                            )
//                            .clip(MaterialTheme.shapes.small)
//                            .clickable {
//                                scope.launch {
//                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 6)) {
//                                        if (uiFlags is UiFlags.None) {
//                                            uiEvent(HomeIntent.Pipeline(2))
//                                        } else {
//                                            uiEvent(HomeIntent.Pipeline(0))
//                                        }
//                                    }
//                                }
//                            }
//                            .padding(vertical = 8.dp, horizontal = 16.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 6) "回吸促凝剂中" else "回吸促凝剂",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 6) Color.Red else Color.Unspecified
//                    )
//                }
//            }
//        }
//    }
//}