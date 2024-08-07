package com.zktony.android.ui

import android.graphics.Color.rgb
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.zktony.android.ui.mothersettingprogressbar.WaterVerticalProgressBar
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.line
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.extra.format
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * 四通道
 */
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
    val calculate by viewModel.calculate.collectAsStateWithLifecycle()
    val wasteprogress by viewModel.wasteprogress.collectAsStateWithLifecycle()

    val higemother by viewModel.higemother.collectAsStateWithLifecycle()
    val lowmother by viewModel.lowmother.collectAsStateWithLifecycle()
    val watermother by viewModel.watermother.collectAsStateWithLifecycle()
    val coagulantmother by viewModel.coagulantmother.collectAsStateWithLifecycle()
    val first by viewModel.first.collectAsStateWithLifecycle()

    val pipelineDialogOpen by viewModel.pipelineDialogOpen.collectAsStateWithLifecycle()
    val cleanDialogOpen by viewModel.cleanDialogOpen.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val erEntities = viewModel.erEntities.collectAsLazyPagingItems()

    val hint by viewModel.hint.collectAsStateWithLifecycle()


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
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeAppBar(page, uiFlags is UiFlags.Objects) {
                navigation()
            }
            operate(
                entities,
                entities.toList(),
                erEntities.toList(),
                selectedER,
                uiFlags,
                job,
                viewModel::dispatch,
                complate,
                process,
                calculate,
                wasteprogress,
                higemother,
                lowmother,
                first,
                hint,
                pipelineDialogOpen,
                cleanDialogOpen,
                watermother,
                coagulantmother
            )
        }
    }

}

@Composable
fun operate(
    entitiesLazy: LazyPagingItems<Program>,
    entities: List<Program>,
    erEntities: List<ExperimentRecord>,
    selectedER: Long,
    uiFlags: UiFlags,
    job: Job?,
    uiEvent: (HomeIntent) -> Unit,
    complate: Int,
    process: Float,
    calculate: Int,
    wasteprogress: Float,
    higemother: Float,
    lowmother: Float,
    first: Boolean,
    hint: Boolean,
    pipelineDialogOpen: Boolean,
    cleanDialogOpen: Boolean,
    watermother: Float,
    coagulantmother: Float,
) {
    Row(
        modifier = Modifier
//            .background(Color.White)
            .fillMaxWidth()
            .fillMaxHeight()
//            .padding(start = 13.75.dp)
//            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
//            .height(904.9.dp)
//            .width((572.5).dp)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 40.dp, top = 60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "A"
                , fontSize = 25.sp
                , fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(size = 10.dp))
                    .background(Color.White)
                    .width(250.dp)
                    .height(650.dp)
            ) {


            }
        }



        Column(
            modifier = Modifier
                .padding(start = 40.dp, top = 60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "B"
                , fontSize = 25.sp
                , fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(size = 10.dp))
                    .background(Color.White)
                    .width(250.dp)
                    .height(650.dp)
            ) {


            }
        }

        Column(
            modifier = Modifier
                .padding(start = 40.dp, top = 60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "C"
                , fontSize = 25.sp
                , fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(size = 10.dp))
                    .background(Color.White)
                    .width(250.dp)
                    .height(650.dp)
            ) {


            }
        }

        Column(
            modifier = Modifier
                .padding(start = 40.dp, top = 60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "D"
                , fontSize = 25.sp
                , fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(size = 10.dp))
                    .background(Color.White)
                    .width(250.dp)
                    .height(650.dp)
            ) {


            }
        }











    }
}

//@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
//@Composable
//fun operate(
//    entitiesLazy: LazyPagingItems<Program>,
//    entities: List<Program>,
//    erEntities: List<ExperimentRecord>,
//    selectedER: Long,
//    uiFlags: UiFlags,
//    job: Job?,
//    uiEvent: (HomeIntent) -> Unit,
//    complate: Int,
//    process: Float,
//    calculate: Int,
//    wasteprogress: Float,
//    higemother: Float,
//    lowmother: Float,
//    first: Boolean,
//    hint: Boolean,
//    pipelineDialogOpen: Boolean,
//    cleanDialogOpen: Boolean,
//    watermother: Float,
//    coagulantmother: Float,
//) {
//
//    val scope = rememberCoroutineScope()
//
//    val keyboard = LocalSoftwareKeyboardController.current
//
//    val context = LocalContext.current
//
//
//    var speChat =
//        "[`~!@#$%^&*()+=\\-|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"
//
//
//    if (!first) {
//        uiEvent(HomeIntent.First)
//    }
//
//    /**
//     * 选中的程序
//     */
//    var selectedIndex by remember { mutableStateOf(0) }
//
//    var programId = rememberDataSaverState(key = "programid", default = 1L)
//
//
//    /**
//     * 选中的实体
//     */
//    val program = entities.find {
//        it.id == programId.value
//    } ?: Program()
//
//    val experimentRecord = erEntities.find {
//        it.id == selectedER
//    } ?: ExperimentRecord()
//
//
//    uiEvent(HomeIntent.Selected(program.id))
//
//
//    /**
//     * 纯水弹窗
//     */
//    val waterDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 促凝剂弹窗
//     */
//    val coagulantDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 低浓度弹窗
//     */
//    val lowDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 高浓度弹窗
//     */
//    val highDialog = remember { mutableStateOf(false) }
//
//
//    /**
//     * 促凝剂浓度
//     */
//    val concentration = rememberDataSaverState(key = "concentration", default = 10)
//    var concentration_ex by remember { mutableStateOf(concentration.value.toString()) }
//
//
//    /**
//     * 低浓度
//     */
//    val lowCoagulant = rememberDataSaverState(key = "lowCoagulant", default = 4)
//    var lowCoagulant_ex by remember { mutableStateOf(lowCoagulant.value.toString()) }
//
//
//    /**
//     * 高浓度
//     */
//    val highCoagulant = rememberDataSaverState(key = "highCoagulant", default = 20)
//    var highCoagulant_ex by remember { mutableStateOf(highCoagulant.value.toString()) }
//
//
//    /**
//     *  废液
//     */
//    /**
//     * 程序列表弹窗
//     */
//    val programListDialog = remember { mutableStateOf(false) }
//
//
//    /**
//     * 保存的预计制胶数量
//     */
//    val expectedMakeNum = rememberDataSaverState(key = "expectedMakenum", default = 0)
//    var expectedMakeNum_ex by remember { mutableStateOf(expectedMakeNum.value.toString()) }
//
//    /**
//     * 开始制胶/停止制胶
//     */
//    var startMake by remember { mutableStateOf("开始制胶") }
//
//    /**
//     * 继续制胶弹窗
//     */
//    val continueGlueDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 停止制胶弹窗
//     */
//    val stopGlueDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 清洗弹窗
//     */
//    val cleanDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 填充弹窗
//     */
//    val pipelineDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 清空废液槽弹窗
//     */
//    val wasteDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 开始制胶弹窗
//     */
//    val guleDialog = remember { mutableStateOf(false) }
//
//    /**
//     * 纯水进度
//     */
//    var waterSweepState by remember {
//        mutableStateOf(0f)
//    }
//
//    /**
//     * 促凝剂进度
//     */
//    var coagulantSweepState by remember {
//        mutableStateOf(0f)
//    }
//
//    /**
//     * 低浓度进度
//     */
//    var lowCoagulantSweepState by remember {
//        mutableStateOf(0f)
//    }
//
//    /**
//     * 高浓度进度
//     */
//    var highCoagulantSweepState by remember {
//        mutableStateOf(0f)
//    }
//
//
//    /**
//     * 同步LazyRow滑动
//     */
//    val stateRowX = rememberLazyListState() // State for the first Row, X
//    val stateRowY = rememberLazyListState() // State for the second Row, Y
//
//    LaunchedEffect(stateRowX.firstVisibleItemScrollOffset) {
//        stateRowY.scrollToItem(
//            stateRowX.firstVisibleItemIndex,
//            stateRowX.firstVisibleItemScrollOffset
//        )
//    }
//
//    LaunchedEffect(stateRowY.firstVisibleItemScrollOffset) {
//        stateRowX.scrollToItem(
//            stateRowY.firstVisibleItemIndex,
//            stateRowY.firstVisibleItemScrollOffset
//        )
//    }
//
//
//    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) {
//        continueGlueDialog.value = true
//    } else if (uiFlags is UiFlags.Objects && uiFlags.objects == 6) {
//        expectedMakeNum.value = 0
//        expectedMakeNum_ex = "0"
//        uiEvent(HomeIntent.MotherVolZero)
//        experimentRecord.status = EPStatus.COMPLETED
//        experimentRecord.number = complate
//        uiEvent(HomeIntent.Update(experimentRecord))
//        continueGlueDialog.value = false
//        uiEvent(HomeIntent.MoveCom(complate))
//    }
//    Column(
//        modifier = Modifier
//            .padding(start = 13.75.dp)
//            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
//            .background(Color.White)
//            .height(904.9.dp)
//            .width((572.5).dp)
//    ) {
//        Text(
//            modifier = Modifier.padding(start = 48.92.dp, top = 21.4.dp),
//            text = "母液设置",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(
//                0,
//                105,
//                52
//            )
//        )
//        /**
//         * 母液设置
//         */
//        Row(
//            modifier = Modifier
//                .padding(start = 48.92.dp, top = 18.22.dp)
//                .fillMaxWidth()
//        ) {
//
//            Row(
//                modifier = Modifier
//                    .clickable(onClick = {
//                        if (uiFlags is UiFlags.None) {
//                            waterDialog.value = true
//                        }
//                    })
//            ) {
//                //纯水进度条
////                WaterVerticalProgressBar(
////                    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4 || uiFlags is UiFlags.Objects && uiFlags.objects == 0) watermother / waterSweepState else 0f,
////                    watermother.toString()
////                )
//                WaterVerticalProgressBar(
//                    0f,
//                    watermother.toString()
//                )
//            }
//
//            Row(
//                modifier = Modifier
//                    .padding(start = 22.82.dp)
//                    .clickable(onClick = {
//                        if (uiFlags is UiFlags.None) {
//                            coagulantDialog.value = true
//                        }
//                    })
//            ) {
//                //促凝剂进度条
////                CoagulantProgressBarVertical(
////                    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4 || uiFlags is UiFlags.Objects && uiFlags.objects == 0) coagulantmother / coagulantSweepState else 0f,
////                    coagulantmother.toString(),
////                    concentration_ex
////                )
//                CoagulantProgressBarVertical(
//                     0f,
//                    coagulantmother.toString(),
//                    concentration_ex
//                )
//            }
//
//            Row(
//                modifier = Modifier
//                    .padding(start = 22.82.dp)
//                    .clickable(onClick = {
//                        if (uiFlags is UiFlags.None) {
//                            lowDialog.value = true
//                        }
//                    })
//            ) {
//                //低浓度进度条
////                LowCoagulantProgressBarVertical(
////                    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4 || uiFlags is UiFlags.Objects && uiFlags.objects == 0) lowmother / lowCoagulantSweepState else 0f,
////                    lowmother.toString(),
////                    lowCoagulant_ex
////                )
//
//                LowCoagulantProgressBarVertical(
//                    0f,
//                    lowmother.toString(),
//                    lowCoagulant_ex
//                )
//            }
//
//            Row(
//                modifier = Modifier
//                    .padding(start = 22.82.dp)
//                    .clickable(onClick = {
//                        if (uiFlags is UiFlags.None) {
//                            highDialog.value = true
//                        }
//                    })
//            ) {
//                //高浓度进度条
////                HighCoagulantProgressBarVertical(
////                    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4 || uiFlags is UiFlags.Objects && uiFlags.objects == 0) higemother / highCoagulantSweepState else 0f,
////                    higemother.toString(),
////                    highCoagulant_ex
////                )
//
//                HighCoagulantProgressBarVertical(
//                     0f,
//                    higemother.toString(),
//                    highCoagulant_ex
//                )
//            }
//        }
//
//        Row(
//            modifier = Modifier
//                .padding(top = 22.53.dp)
//                .fillMaxWidth()
//        ) {
//            line(Color(0, 105, 5), 48.25f, 528.5f)
//        }
//
//        Row(
//            modifier = Modifier
//                .padding(start = 48.92.dp, top = 21.4.dp)
//                .fillMaxWidth()
//        ) {
//            /**
//             * 程序设置
//             */
//            Column(
//                modifier = Modifier
//                    .clickable(onClick = {
//                        if (uiFlags is UiFlags.None) {
//                            programListDialog.value = true
//                        }
//                    })
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text = program.displayText,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(
//                            0,
//                            105,
//                            52
//                        )
//                    )
//
//                    Icon(
//                        modifier = Modifier
//                            .height(50.dp)
//                            .width(50.dp),
//                        imageVector = Icons.Default.ArrowDropDown,
//                        contentDescription = null
//                    )
//
//                }
//
//                Text(
//                    modifier = Modifier.padding(top = 14.dp),
//                    text = "浓 度:" + program.startRange + "%~" + program.endRange + "%",
//                    fontSize = 18.sp
//                )
//                Text(
//                    modifier = Modifier.padding(top = 14.dp),
//                    text = "厚 度:" + program.thickness + "mm",
//                    fontSize = 18.sp
//                )
//
//                Text(
//                    modifier = Modifier.padding(top = 14.dp),
//                    text = "胶液体积:" + program.volume + "mL",
//                    fontSize = 18.sp
//                )
//                Text(
//                    modifier = Modifier.padding(top = 14.dp),
//                    text = "促凝剂体积:" + program.coagulant + "μL",
//                    fontSize = 18.sp
//                )
//
//            }
//
//            Column(modifier = Modifier.padding(start = 120.dp)) {
//                Text(
//                    text = "制胶数量",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(
//                        0,
//                        105,
//                        52
//                    )
//                )
//
//                Text(
//                    modifier = Modifier.padding(top = 21.2.dp),
//                    fontSize = 18.sp,
//                    text = "预计制胶数量"
//                )
//                Row(
//                    modifier = Modifier.padding(top = 13.7.dp),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    Image(
//                        painter = painterResource(id = R.mipmap.reduce), contentDescription = null,
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clickable {
//                                if (job == null) {
//                                    if (uiFlags is UiFlags.None) {
//                                        if (program.id != 0L) {
//                                            if (expectedMakeNum.value > 1) {
//                                                expectedMakeNum.value -= 1
//                                                expectedMakeNum_ex =
//                                                    expectedMakeNum.value.toString()
//
//                                                uiEvent(HomeIntent.HigeLowMotherVol)
//                                            }
//                                        } else {
//                                            Toast
//                                                .makeText(
//                                                    context,
//                                                    "没有制胶程序！",
//                                                    Toast.LENGTH_SHORT
//                                                )
//                                                .show()
//                                        }
//                                    }
//                                }
//
//
//                            }
//                    )
//
//                    OutlinedTextField(
//                        modifier = Modifier
//                            .width(60.dp)
//                            .height(60.2.dp),
//                        textStyle = TextStyle.Default.copy(
//                            fontSize = 25.sp,
//                        ),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color(rgb(0, 105, 52)),
//                            focusedLabelColor = Color(rgb(0, 105, 52)),
//                            cursorColor = Color(rgb(0, 105, 52))
//                        ),
//                        value = expectedMakeNum_ex,
//                        onValueChange = {
//                            if (Pattern.compile(speChat).matcher(it).find()) {
//                                Toast.makeText(
//                                    context,
//                                    "数据不能包含特殊字符！",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            } else {
//                                if (job == null) {
//                                    if (uiFlags is UiFlags.None) {
//                                        if (program != null) {
//                                            expectedMakeNum_ex = it
//                                            val temp = expectedMakeNum_ex.toIntOrNull() ?: 0
//                                            if (temp > 0) {
//                                                if (temp > 99) {
//                                                    Toast.makeText(
//                                                        context,
//                                                        "预计数量不能大于99！",
//                                                        Toast.LENGTH_SHORT
//                                                    ).show()
//                                                } else {
//                                                    expectedMakeNum.value = temp
//                                                    expectedMakeNum_ex = temp.toString()
//                                                    uiEvent(HomeIntent.HigeLowMotherVol)
//                                                }
//                                            } else {
//                                                uiEvent(HomeIntent.MotherVolZero)
//                                            }
//
//                                        } else {
//                                            Toast.makeText(
//                                                context,
//                                                "没有制胶程序！",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    }
//                                }
//                            }
//
//                        },
//                        keyboardOptions = KeyboardOptions(
//                            keyboardType = KeyboardType.Number,
//                            imeAction = ImeAction.Done,
//                        ),
//                        keyboardActions = KeyboardActions(
//                            onDone = {
//                                keyboard?.hide()
//                            }
//                        )
//                    )
//                    Image(
//                        painter = painterResource(id = R.mipmap.add), contentDescription = null,
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clickable {
//                                if (job == null) {
//                                    if (uiFlags is UiFlags.None) {
//                                        if (program.id != 0L) {
//                                            var temp = expectedMakeNum_ex.toIntOrNull() ?: 0
//                                            temp += 1
//                                            if (temp > 99) {
//                                                Toast
//                                                    .makeText(
//                                                        context,
//                                                        "预计数量不能大于99！",
//                                                        Toast.LENGTH_SHORT
//                                                    )
//                                                    .show()
//                                            } else {
//                                                expectedMakeNum.value += 1
//                                                expectedMakeNum_ex =
//                                                    expectedMakeNum.value.toString()
//                                                uiEvent(HomeIntent.HigeLowMotherVol)
//                                            }
//                                        } else {
//                                            Toast
//                                                .makeText(
//                                                    context,
//                                                    "没有制胶程序！",
//                                                    Toast.LENGTH_SHORT
//                                                )
//                                                .show()
//                                        }
//                                    }
//                                }
//
//                            }
//                    )
//                }
//                Text(
//                    modifier = Modifier.padding(top = 14.0.dp),
//                    fontSize = 18.sp,
//                    text = "已制胶数量:$complate"
//                )
//            }
//
//        }
//
//        Row(
//            modifier = Modifier
//                .padding(top = 22.53.dp)
//                .fillMaxWidth()
//        ) {
//            line(Color(0, 105, 5), 48.25f, 528.5f)
//        }
//
//        Text(
//            modifier = Modifier.padding(start = 48.92.dp, top = 21.4.dp),
//            text = "制胶进度",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(
//                0,
//                105,
//                52
//            )
//        )
//        Row(modifier = Modifier.padding(start = 48.92.dp, top = 21.4.dp)) {
//
//            Row(
//                modifier = Modifier
//                    .clickable(onClick = {
//                        if (uiFlags is UiFlags.None) {
//                            wasteDialog.value = true
//                        }
//                    })
//            ) {
//                //废液进度条
//
//                WasteVerticalProgressBar(wasteprogress, wasteprogress.toString())
//            }
//
//            Row(
//                modifier = Modifier
//                    .padding(start = 17.8.dp)
//                    .clickable(onClick = {
//
//                    })
//            ) {
//                //制胶进度的进度条
//                VerticalProgressBar(
//                    if (process > 0.01f) process else 0f,
//                    if (process < 0.01f) "0.0" else (process * 100).toString()
//                )
//            }
//
//        }
//
//        /**
//         * 操作按钮
//         */
//        Row(
//            modifier = Modifier
//                .padding(top = 21.4.dp)
//                .fillMaxWidth()
//                .height(143.98.dp)
//                .background(Color(229, 229, 229))
//        ) {
//
//            Column(
//                modifier = Modifier
//                    .padding(start = 47.55.dp)
//            ) {
//                Row {
//                    Image(
//                        painter = painterResource(id = if (job == null) R.mipmap.start else R.mipmap.stop),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(63.dp, 63.dp)
//                            .clickable {
//                                if (job == null) {
//                                    if (uiFlags is UiFlags.None) {
//                                        if (wasteprogress >= 0.9f) {
//                                            uiEvent(HomeIntent.CleanWaste)
//                                            wasteDialog.value = true
//                                        } else {
//                                            if (expectedMakeNum.value > 0) {
//                                                uiEvent(HomeIntent.Calculate)
//                                                guleDialog.value = true
//                                            } else {
//                                                Toast
//                                                    .makeText(
//                                                        context,
//                                                        "预计制胶数量不能为0!",
//                                                        Toast.LENGTH_SHORT
//                                                    )
//                                                    .show()
//                                            }
//
//                                        }
//                                    } else {
//                                        Toast
//                                            .makeText(
//                                                context,
//                                                "正在运行中,请稍后再操作！",
//                                                Toast.LENGTH_SHORT
//                                            )
//                                            .show()
//                                    }
//                                } else {
//                                    stopGlueDialog.value = true
//                                }
//                            }
//                    )
//
//                    Image(
//                        painter = painterResource(id = R.mipmap.filling),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .padding(start = 82.0.dp)
//                            .size(63.dp, 63.dp)
//                            .clickable {
//                                if (uiFlags is UiFlags.None) {
//                                    if (pipelineDialogOpen) {
//                                        scope.launch {
//                                            if (wasteprogress >= 0.9f) {
//                                                wasteDialog.value = true
//                                            } else {
//                                                if (uiFlags is UiFlags.None) {
//                                                    uiEvent(HomeIntent.Pipeline(1))
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        pipelineDialog.value = true
//                                    }
//                                } else {
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "正在运行中,请稍后再操作！",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
//                                }
//
//                            }
//                    )
//
//                    Image(
//                        painter = painterResource(id = R.mipmap.clean),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .padding(start = 80.4.dp)
//                            .size(63.dp, 63.dp)
//                            .clickable {
//                                if (uiFlags is UiFlags.None) {
//                                    if (cleanDialogOpen) {
//                                        scope.launch {
//                                            if (wasteprogress >= 0.9f) {
//                                                wasteDialog.value = true
//                                            } else {
//                                                if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 2)) {
//                                                    uiEvent(HomeIntent.Clean)
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        cleanDialog.value = true
//                                    }
//                                } else {
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "正在运行中,请稍后再操作！",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
//                                }
//
//                            }
//                    )
//
//                    Image(
//                        painter = painterResource(id = R.mipmap.reset),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .padding(start = 71.5.dp)
//                            .size(63.dp, 63.dp)
//                            .clickable {
//                                if (uiFlags is UiFlags.None) {
//                                    uiEvent(HomeIntent.Reset)
//                                } else {
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "正在运行中,请稍后再操作！",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
//                                }
//                            }
//                    )
//                }
//
//                Row {
//                    Text(
//                        text = if (job == null) "开始制胶" else "停止制胶",
//                        color = if (job == null) Color.Black else Color.Red,
//                        fontSize = 18.sp
//                    )
//                    Text(
//                        modifier = Modifier.padding(start = 70.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) "正在填充" else "管路填充",
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 5) Color.Red else Color.Black,
//                        fontSize = 18.sp
//                    )
//                    Text(
//                        modifier = Modifier.padding(start = 68.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) "正在清洗" else "管路清洗",
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) Color.Red else Color.Black,
//                        fontSize = 18.sp
//                    )
//                    Text(
//                        modifier = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) Modifier.padding(
//                            start = 72.dp
//                        ) else Modifier.padding(start = 78.dp),
//                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) "复位中" else "复 位",
//                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) Color.Red else Color.Black,
//                        fontSize = 18.sp
//                    )
//                }
//
//            }
//
//
//        }
//
//
//    }
//
//    /**
//     * 清洗弹窗
//     */
//    if (cleanDialog.value) {
//        AlertDialog(
//            onDismissRequest = { },
//            title = {
//            },
//            text = {
//                Column {
//                    Text(
//                        fontSize = 16.sp,
//                        text = "清洗请将进液管路置于足量纯水中"
//                    )
//                    Text(
//                        modifier = Modifier.padding(top = 10.dp),
//                        fontSize = 16.sp,
//                        text = "排空请将进液管路置于空气中"
//                    )
//                    Row(
//                        modifier = Modifier.padding(top = 10.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            fontSize = 16.sp,
//                            text = "本次使用不再提醒"
//                        )
//                        Checkbox(
//                            colors = CheckboxDefaults.colors(checkedColor = Color(0, 105, 5)),
//                            checked = cleanDialogOpen,
//                            onCheckedChange = {
//                                uiEvent(HomeIntent.CleanDialog(it))
//                            })
//                    }
//
//
//                }
//
//
//            }, confirmButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        scope.launch {
//                            if (wasteprogress >= 0.9f) {
//                                wasteDialog.value = true
//                            } else {
//                                if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 2)) {
//                                    cleanDialog.value = false
//                                    uiEvent(HomeIntent.Clean)
//                                }
//                            }
//                        }
//                    }) {
//                    Text(fontSize = 18.sp, text = "确认")
//                }
//            }, dismissButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        uiEvent(HomeIntent.CleanDialog(false))
//                        cleanDialog.value = false
//                    }) {
//                    Text(fontSize = 18.sp, text = "取消")
//                }
//            })
//    }
//
//    /**
//     * 填充弹窗
//     */
//    if (pipelineDialog.value) {
//        AlertDialog(
//            onDismissRequest = { },
//            title = {
//            },
//            text = {
//                Column {
//                    Text(
//                        fontSize = 16.sp,
//                        text = "填充前请确认母液量充足!"
//                    )
//
//                    Row(
//                        modifier = Modifier.padding(top = 10.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            fontSize = 16.sp,
//                            text = "本次使用不再提醒！"
//                        )
//                        Checkbox(
//                            colors = CheckboxDefaults.colors(checkedColor = Color(0, 105, 5)),
//                            checked = pipelineDialogOpen,
//                            onCheckedChange = {
//                                uiEvent(HomeIntent.PipelineDialog(it))
//                            })
//                    }
//
//
//                }
//
//            }, confirmButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        scope.launch {
//                            if (wasteprogress >= 0.9f) {
//                                wasteDialog.value = true
//                            } else {
//                                if (uiFlags is UiFlags.None) {
//                                    pipelineDialog.value = false
//                                    uiEvent(HomeIntent.Pipeline(1))
//                                }
//                            }
//                        }
//                    }) {
//                    Text(fontSize = 18.sp, text = "确认")
//                }
//            }, dismissButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        uiEvent(HomeIntent.PipelineDialog(false))
//                        pipelineDialog.value = false
//                    }) {
//                    Text(fontSize = 18.sp, text = "取消")
//                }
//            })
//    }
//
//
//    /**
//     * 停止制胶弹窗
//     */
//    if (stopGlueDialog.value) {
//        AlertDialog(
//            onDismissRequest = { },
//            title = {
//            },
//            text = {
//                Text(
//                    fontSize = 16.sp,
//                    text = "是否停止制胶!"
//                )
//
//            }, confirmButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        stopGlueDialog.value = false
//                    }) {
//                    Text(text = "取消")
//                }
//            }, dismissButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        expectedMakeNum.value = 0
//                        expectedMakeNum_ex = "0"
//                        uiEvent(HomeIntent.MotherVolZero)
//                        experimentRecord.status = EPStatus.ABORT
//                        experimentRecord.detail = "手动停止制胶"
//                        uiEvent(HomeIntent.Update(experimentRecord))
//                        startMake = "开始制胶"
//                        uiEvent(HomeIntent.Stop)
//                        stopGlueDialog.value = false
//                    }) {
//                    Text(text = "停止")
//                }
//            })
//    }
//
//
//    /**
//     * 清空废液槽弹窗
//     */
//    if (wasteDialog.value) {
//
//        if (job == null) {
//            if (uiFlags is UiFlags.None) {
//                AlertDialog(
//                    onDismissRequest = { },
//                    title = {
//                        Text(
//                            modifier = Modifier.padding(start = 80.dp),
//                            fontSize = 16.sp,
//                            text = "清空废液槽！"
//                        )
//                    },
//                    text = {
//                    }, confirmButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                uiEvent(HomeIntent.CleanWasteState)
//                                wasteDialog.value = false
//                            }) {
//                            Text(text = "确定")
//                        }
//                    }, dismissButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                wasteDialog.value = false
//                            }) {
//                            Text(text = "取消")
//                        }
//                    })
//            }
//        }
//
//
//    }
//
//    /**
//     * 继续制胶弹窗
//     */
//    if (continueGlueDialog.value) {
//        AlertDialog(
//            onDismissRequest = { },
//            title = {
//            },
//            text = {
//                Text(
//                    fontSize = 16.sp,
//                    text = "请更换制胶架，继续制胶!"
//                )
//            }, confirmButton = {
//                Button(
//                    enabled = hint,
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        if (uiFlags is UiFlags.Objects && uiFlags.objects == 13) {
//                            Toast.makeText(
//                                context,
//                                "正在冲洗,请稍后再操作!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        } else {
//                            experimentRecord.number = complate
//                            uiEvent(HomeIntent.Update(experimentRecord))
//                            continueGlueDialog.value = false
//                            uiEvent(HomeIntent.Start(complate))
//                        }
//
//                    }) {
//                    Text(text = "继续")
//                }
//            }, dismissButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        if (uiFlags is UiFlags.Objects && uiFlags.objects == 13) {
//                            Toast.makeText(
//                                context,
//                                "正在冲洗,请稍后再操作!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        } else {
//                            expectedMakeNum.value = 0
//                            expectedMakeNum_ex = "0"
//                            experimentRecord.number = complate
//                            experimentRecord.status = EPStatus.ABORT
//                            experimentRecord.detail = "手动停止制胶"
//                            uiEvent(HomeIntent.Update(experimentRecord))
//                            uiEvent(HomeIntent.MotherVolZero)
//                            uiEvent(HomeIntent.MoveCom(complate))
//                            continueGlueDialog.value = false
//                        }
//                    }) {
//                    Text(text = "停止")
//                }
//            })
//    }
//
//    /**
//     * 开始制胶弹窗
//     */
//    if (guleDialog.value) {
//        AlertDialog(
//            onDismissRequest = {},
//            title = {
//            },
//            text = {
//                Text(
//                    fontSize = 16.sp,
//                    text = "请确认母液量不少于推荐量!"
//                )
//
//            }, confirmButton = {
//                Button(
//                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//                        if (calculate >= expectedMakeNum.value) {
//                            waterSweepState = watermother
//                            coagulantSweepState = coagulantmother
//                            lowCoagulantSweepState = lowmother
//                            highCoagulantSweepState = higemother
//
//                            startMake = "停止制胶"
//                            uiEvent(HomeIntent.Start(0))
//                            uiEvent(
//                                HomeIntent.Insert(
//                                    program.startRange,
//                                    program.endRange,
//                                    program.thickness,
//                                    program.coagulant,
//                                    program.volume,
//                                    0,
//                                    EPStatus.RUNNING,
//                                    ""
//                                )
//                            )
//                            guleDialog.value = false
//                        } else {
//                            Toast.makeText(
//                                context,
//                                "剩余液量不足,请补充!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//
//                    }) {
//                    Text(text = "确认")
//                }
//            }, dismissButton = {
//                Button(
//                    modifier = Modifier
//                        .width(100.dp)
//                        .padding(end = 10.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(rgb(0, 105, 52))
//                    ), onClick = {
//
//                        guleDialog.value = false
//                    }) {
//                    Text(text = "取消")
//                }
//            })
//    }
//
//    /**
//     * 纯水弹窗
//     */
//    if (waterDialog.value) {
//        if (job == null) {
//            if (uiFlags is UiFlags.None) {
//                AlertDialog(
//                    onDismissRequest = { },
//                    title = {
//                    },
//                    text = {
//                        Row(
//                            horizontalArrangement = Arrangement.spacedBy(16.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                fontSize = 16.sp,
//                                text = "冲洗液量/mL："
//                            )
//                            OutlinedTextField(
//                                modifier = Modifier.width(100.dp),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(rgb(0, 105, 52)),
//                                    focusedLabelColor = Color(rgb(0, 105, 52)),
//                                    cursorColor = Color(rgb(0, 105, 52))
//                                ),
//                                value = watermother.toString(),
//                                label = { Text(text = "mL") },
//                                enabled = false,
//                                onValueChange = {
//                                },
//                                keyboardOptions = KeyboardOptions(
//                                    keyboardType = KeyboardType.Number,
//                                    imeAction = ImeAction.Done,
//                                ),
//                                keyboardActions = KeyboardActions(
//                                    onDone = {
//                                        keyboard?.hide()
//                                    }
//                                )
//                            )
//
//                        }
//                    }, confirmButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                waterDialog.value = false
//                            }) {
//                            Text(text = "确认")
//                        }
//                    }, dismissButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                waterDialog.value = false
//                            }) {
//                            Text(text = "取消")
//                        }
//                    })
//            }
//        }
//
//
//    }
//
//
//    /**
//     * 促凝剂弹窗
//     */
//    if (coagulantDialog.value) {
//
//        if (job == null) {
//            if (uiFlags is UiFlags.None) {
//                AlertDialog(
//                    onDismissRequest = { },
//                    title = {
//
//                    },
//                    text = {
//
//                        Column {
//
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    fontSize = 16.sp,
//                                    text = "促凝剂浓度/%："
//                                )
//                                OutlinedTextField(
//                                    modifier = Modifier.width(100.dp),
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = Color(rgb(0, 105, 52)),
//                                        focusedLabelColor = Color(rgb(0, 105, 52)),
//                                        cursorColor = Color(rgb(0, 105, 52))
//                                    ),
//                                    value = concentration_ex,
//                                    label = { Text(text = "%") },
//                                    onValueChange = {
//                                        if (Pattern.compile(speChat).matcher(it).find()) {
//                                            Toast.makeText(
//                                                context,
//                                                "数据不能包含特殊字符！",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        } else {
//                                            concentration_ex = it
//                                            concentration.value =
//                                                concentration_ex.toIntOrNull() ?: 0
//                                        }
//                                    },
//                                    keyboardOptions = KeyboardOptions(
//                                        keyboardType = KeyboardType.Number,
//                                        imeAction = ImeAction.Done,
//                                    ),
//                                    keyboardActions = KeyboardActions(
//                                        onDone = {
//                                            keyboard?.hide()
//                                        }
//                                    )
//                                )
//
//                            }
//
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    fontSize = 16.sp,
//                                    text = "促凝剂液量/mL："
//                                )
//                                OutlinedTextField(
//                                    modifier = Modifier.width(100.dp),
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = Color(rgb(0, 105, 52)),
//                                        focusedLabelColor = Color(rgb(0, 105, 52)),
//                                        cursorColor = Color(rgb(0, 105, 52))
//                                    ),
//                                    value = coagulantmother.toString(),
//                                    enabled = false,
//                                    label = { Text(text = "mL") },
//                                    onValueChange = {
//                                    },
//                                    keyboardOptions = KeyboardOptions(
//                                        keyboardType = KeyboardType.Number,
//                                        imeAction = ImeAction.Done,
//                                    ),
//                                    keyboardActions = KeyboardActions(
//                                        onDone = {
//                                            keyboard?.hide()
//                                        }
//                                    )
//                                )
//
//                            }
//                        }
//
//                    }, confirmButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                concentration_ex = concentration.value.toString()
//                                coagulantDialog.value = false
//                            }) {
//                            Text(text = "确认")
//                        }
//                    }, dismissButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                coagulantDialog.value = false
//                            }) {
//                            Text(text = "取消")
//                        }
//                    })
//            }
//        }
//
//
//    }
//
//
//    /**
//     * 低浓度弹窗
//     */
//    if (lowDialog.value) {
//
//        if (job == null) {
//            if (uiFlags is UiFlags.None) {
//                AlertDialog(
//                    onDismissRequest = { },
//                    title = {
//
//                    },
//                    text = {
//                        Column {
//
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    fontSize = 16.sp,
//                                    text = "低浓度浓度/%："
//                                )
//                                OutlinedTextField(
//                                    modifier = Modifier.width(100.dp),
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = Color(rgb(0, 105, 52)),
//                                        focusedLabelColor = Color(rgb(0, 105, 52)),
//                                        cursorColor = Color(rgb(0, 105, 52))
//                                    ),
//                                    value = lowCoagulant_ex,
//                                    label = { Text(text = "%") },
//                                    onValueChange = {
//                                        if (Pattern.compile(speChat).matcher(it).find()) {
//                                            Toast.makeText(
//                                                context,
//                                                "数据不能包含特殊字符！",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        } else {
//                                            lowCoagulant_ex = it
//                                        }
//                                    },
//                                    keyboardOptions = KeyboardOptions(
//                                        keyboardType = KeyboardType.Number,
//                                        imeAction = ImeAction.Done,
//                                    ),
//                                    keyboardActions = KeyboardActions(
//                                        onDone = {
//                                            keyboard?.hide()
//                                        }
//                                    )
//                                )
//
//                            }
//
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    fontSize = 16.sp,
//                                    text = "低浓度液量/mL："
//                                )
//                                OutlinedTextField(
//                                    modifier = Modifier.width(100.dp),
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = Color(rgb(0, 105, 52)),
//                                        focusedLabelColor = Color(rgb(0, 105, 52)),
//                                        cursorColor = Color(rgb(0, 105, 52))
//                                    ),
//                                    value = lowmother.toString(),
//                                    enabled = false,
//                                    label = { Text(text = "mL") },
//                                    onValueChange = {
//                                    },
//                                    keyboardOptions = KeyboardOptions(
//                                        keyboardType = KeyboardType.Number,
//                                        imeAction = ImeAction.Done,
//                                    ),
//                                    keyboardActions = KeyboardActions(
//                                        onDone = {
//                                            keyboard?.hide()
//                                        }
//                                    )
//                                )
//
//                            }
//                        }
//
//                    }, confirmButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                if ((lowCoagulant_ex.toIntOrNull() ?: 0) < 3) {
//                                    Toast.makeText(
//                                        context,
//                                        "母液低浓度不能小于3",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                } else {
//                                    if ((lowCoagulant_ex.toIntOrNull()
//                                            ?: 0) <= program.startRange
//                                    ) {
//                                        if ((lowCoagulant_ex.toIntOrNull() ?: 0) > 0) {
//                                            lowCoagulant.value = lowCoagulant_ex.toIntOrNull() ?: 0
//                                            uiEvent(HomeIntent.HigeLowMotherVol)
//                                            lowDialog.value = false
//                                        } else {
//                                            Toast.makeText(
//                                                context,
//                                                "母液低浓度必须是整数并且大于0!",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    } else {
//                                        Toast.makeText(
//                                            context,
//                                            "母液低浓度不能比制胶程序低浓度高",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//
//
//                                }
//
//
//                            }) {
//                            Text(text = "确认")
//                        }
//                    }, dismissButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                lowCoagulant_ex = lowCoagulant.value.toString()
//                                lowDialog.value = false
//                            }) {
//                            Text(text = "取消")
//                        }
//                    })
//            }
//        }
//
//
//    }
//
//
//    /**
//     * 高浓度弹窗
//     */
//    if (highDialog.value) {
//        if (job == null) {
//            if (uiFlags is UiFlags.None) {
//                AlertDialog(
//                    onDismissRequest = {},
//                    title = {
//                    },
//                    text = {
//                        Column {
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    fontSize = 16.sp,
//                                    text = "高浓度浓度/%："
//                                )
//                                OutlinedTextField(
//                                    modifier = Modifier.width(100.dp),
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = Color(rgb(0, 105, 52)),
//                                        focusedLabelColor = Color(rgb(0, 105, 52)),
//                                        cursorColor = Color(rgb(0, 105, 52))
//                                    ),
//                                    value = highCoagulant_ex,
//                                    label = { Text(text = "%") },
//                                    onValueChange = {
//                                        if (Pattern.compile(speChat).matcher(it).find()) {
//                                            Toast.makeText(
//                                                context,
//                                                "数据不能包含特殊字符！",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        } else {
//                                            highCoagulant_ex = it
//                                        }
//                                    },
//                                    keyboardOptions = KeyboardOptions(
//                                        keyboardType = KeyboardType.Number,
//                                        imeAction = ImeAction.Done,
//                                    ),
//                                    keyboardActions = KeyboardActions(
//                                        onDone = {
//                                            keyboard?.hide()
//                                        }
//                                    )
//                                )
//
//                            }
//
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    fontSize = 16.sp,
//                                    text = "高浓度液量/mL："
//                                )
//                                OutlinedTextField(
//                                    modifier = Modifier.width(100.dp),
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = Color(rgb(0, 105, 52)),
//                                        focusedLabelColor = Color(rgb(0, 105, 52)),
//                                        cursorColor = Color(rgb(0, 105, 52))
//                                    ),
//                                    value = higemother.toString(),
//                                    enabled = false,
//                                    label = { Text(text = "mL") },
//                                    onValueChange = {
//                                    },
//                                    keyboardOptions = KeyboardOptions(
//                                        keyboardType = KeyboardType.Number,
//                                        imeAction = ImeAction.Done,
//                                    ),
//                                    keyboardActions = KeyboardActions(
//                                        onDone = {
//                                            keyboard?.hide()
//                                        }
//                                    )
//                                )
//
//                            }
//                        }
//
//                    }, confirmButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                if ((highCoagulant_ex.toIntOrNull() ?: 0) >= program.endRange) {
//                                    if ((highCoagulant_ex.toIntOrNull() ?: 0) > 0) {
//                                        highCoagulant.value = highCoagulant_ex.toIntOrNull() ?: 0
//                                        uiEvent(HomeIntent.HigeLowMotherVol)
//                                        highDialog.value = false
//                                    } else {
//                                        Toast.makeText(
//                                            context,
//                                            "母液高浓度必须是整数并且大于0!",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                } else {
//                                    Toast.makeText(
//                                        context,
//                                        "母液高浓度不能比制胶程序高浓度低",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }) {
//                            Text(text = "确认")
//                        }
//                    }, dismissButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                highCoagulant_ex = highCoagulant.value.toString()
//                                highDialog.value = false
//                            }) {
//                            Text(text = "取消")
//                        }
//                    })
//            }
//        }
//
//
//    }
//
//    /**
//     * 程序列表弹窗
//     */
//    if (programListDialog.value) {
//        if (job == null) {
//            if (uiFlags is UiFlags.None) {
//                AlertDialog(
//                    onDismissRequest = { },
//                    text = {
//                        //	定义列宽
//                        val cellWidthList = arrayListOf(70, 100, 130, 90, 100, 120)
//                        //	使用lazyColumn来解决大数据量时渲染的性能问题
//                        LazyColumn(
//                            modifier = Modifier
//                                .height(300.dp)
//                        ) {
//                            //	粘性标题
//                            stickyHeader {
//                                LazyRow(
//                                    state = stateRowY,
//                                    modifier = Modifier.background(
//                                        Color(
//                                            android.graphics.Color.rgb(
//                                                0,
//                                                105,
//                                                52
//                                            )
//                                        )
//                                    )
//                                ) {
//                                    item {
//                                        TableTextHead(text = "序号", width = cellWidthList[0])
//                                        TableTextHead(text = "名称", width = cellWidthList[1])
//                                        TableTextHead(text = "浓度", width = cellWidthList[2])
//                                        TableTextHead(text = "厚度", width = cellWidthList[3])
//                                        TableTextHead(text = "促凝剂", width = cellWidthList[3])
//                                        TableTextHead(text = "胶液", width = cellWidthList[3])
//                                        TableTextHead(text = "创建人", width = cellWidthList[3])
//                                    }
//                                }
//                            }
//                            itemsIndexed(entitiesLazy) { index, item ->
//                                val selectedEntity = item == entities[selectedIndex]
//
//                                LazyRow(
//                                    state = stateRowX,
//                                    modifier = Modifier
//                                        .background(
//                                            if (index % 2 == 0) Color(
//                                                android.graphics.Color.rgb(
//                                                    239,
//                                                    239,
//                                                    239
//                                                )
//                                            ) else Color.White
//                                        )
//                                        .fillMaxWidth()
//                                        .clickable {
//                                            selectedIndex = index
//                                        },
//                                    horizontalArrangement = Arrangement.SpaceBetween
//                                ) {
//                                    item {
//                                        TableTextBody(
//                                            text = (index + 1).toString(),
//                                            width = cellWidthList[0],
//                                            selectedEntity
//                                        )
//                                        TableTextBody(
//                                            text = item.displayText,
//                                            width = cellWidthList[1],
//                                            selectedEntity
//                                        )
//                                        TableTextBody(
//                                            text = "" + item.startRange + "%~" + item.endRange + "%",
//                                            width = cellWidthList[2], selectedEntity
//                                        )
//                                        TableTextBody(
//                                            text = item.thickness + "mm",
//                                            width = cellWidthList[3],
//                                            selectedEntity
//                                        )
//
//                                        TableTextBody(
//                                            text = "${item.coagulant}μL",
//                                            width = cellWidthList[3],
//                                            selectedEntity
//                                        )
//
//                                        TableTextBody(
//                                            text = "${item.volume}mL",
//                                            width = cellWidthList[3],
//                                            selectedEntity
//                                        )
//
//                                        TableTextBody(
//                                            text = "${item.founder}",
//                                            width = cellWidthList[3],
//                                            selectedEntity
//                                        )
//
//                                    }
//
//                                }
//
//
//                            }
//
//                        }
//                    }, confirmButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = {
//                                val entity = entities[selectedIndex]
//                                if (entity != null) {
//                                    programId.value = entity.id
//                                    uiEvent(HomeIntent.Selected(entity.id))
//
//                                    if (entity.endRange > 20) {
//                                        highCoagulant.value = 30
//                                        highCoagulant_ex = "30"
//                                    } else {
//                                        highCoagulant.value = 20
//                                        highCoagulant_ex = "20"
//                                    }
//                                    if (entity.startRange < 4) {
//                                        lowCoagulant.value = 3
//                                        lowCoagulant_ex = "3"
//                                    } else {
//                                        lowCoagulant.value = 4
//                                        lowCoagulant_ex = "4"
//                                    }
//
//
//                                    expectedMakeNum.value = 0
//                                    expectedMakeNum_ex = "0"
//
//                                    uiEvent(HomeIntent.MotherVolZero)
//
//                                    programListDialog.value = false
//                                }
//                            }) {
//                            Text(text = "确认")
//                        }
//                    }, dismissButton = {
//                        Button(
//                            modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(rgb(0, 105, 52))
//                            ), onClick = { programListDialog.value = false }) {
//                            Text(text = "取消")
//                        }
//                    })
//            }
//        }
//
//
//    }
//
//
//}



