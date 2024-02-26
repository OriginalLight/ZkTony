package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.TableTextBody
import com.zktony.android.ui.components.TableTextHead
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.util.regex.Pattern

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgramRoute(viewModel: ProgramViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.PROGRAM -> navigationActions.navigateUp()
                else -> viewModel.dispatch(ProgramIntent.NavTo(PageType.PROGRAM))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(ProgramIntent.Flags(UiFlags.none()))
        }
    }

    Box {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.mipmap.bkimg),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )
        Column {
            HomeAppBar(page) { navigation() }
            AnimatedContent(targetState = page) {
                when (page) {
                    PageType.PROGRAM -> ProgramList(
                        entities,
                        entities.toList(),
                        viewModel::dispatch
                    )

                    else -> {}
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    entitiesList: List<Program>,
    dispatch: (ProgramIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current

    val keyboard = LocalSoftwareKeyboardController.current

    /**
     * 新建和打开的弹窗
     */
    val showingDialog = remember { mutableStateOf(false) }

    /**
     * false 新建
     * true  打开
     */
    var open = remember { mutableStateOf(false) }

    /**
     * 删除的弹窗
     */
    val deleteDialog = remember { mutableStateOf(false) }


    /**
     * 导入弹窗
     */
    val importDialog = remember { mutableStateOf(false) }


    var selectedIndex by remember { mutableStateOf(0) }


    /**
     * 制胶名称
     */
    var displayText by remember { mutableStateOf("") }

    /**
     * 开始浓度
     */
    val startRange = rememberDataSaverState(key = "startRange", default = 0)
    var startRange_ex by remember { mutableStateOf(startRange.value.toString()) }

    /**
     * 结束浓度
     */
    var endRange = rememberDataSaverState(key = "endRange", default = 0)
    var endRange_ex by remember { mutableStateOf(endRange.value.toString()) }


    /**
     * 常用厚度
     * 0.75，1.0，1.5
     */
    val tags = arrayListOf("0.75", "1.0", "1.5")
    var thickness = remember { mutableStateOf(tags[1]) }


    /**
     * 促凝剂体积
     */
    var coagulant = rememberDataSaverState(key = "coagulant", default = 0)
    var coagulant_ex by remember { mutableStateOf(coagulant.value.toString()) }

    /**
     * 胶液体积
     */
    var volume = rememberDataSaverState(key = "volume", default = 0f)
    var volume_ex by remember { mutableStateOf(volume.value.format(3)) }

    /**
     * 创建人
     */
    var founder by remember { mutableStateOf("") }

    /**
     * home选中的制胶程序id
     */
    var programId = rememberDataSaverState(key = "programid", default = 1L)


    //	定义列宽
    val cellWidthList = arrayListOf(70, 100, 120, 70, 90, 120)
    //	使用lazyColumn来解决大数据量时渲染的性能问题
    Column(
        modifier = Modifier
            .padding(start = 13.75.dp)
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
            .background(Color.White)
            .height(904.9.dp)
            .width(572.5.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .height(800.dp)
                .fillMaxWidth()
                .padding(top = 20.dp, start = 2.dp)
        ) {
            stickyHeader {
                Row(Modifier.background(Color(rgb(0, 105, 52)))) {
                    TableTextHead(text = "序号", width = cellWidthList[0])
                    TableTextHead(text = "名        称", width = cellWidthList[1])
                    TableTextHead(text = "浓             度", width = cellWidthList[2])
                    TableTextHead(text = "厚度", width = cellWidthList[3])
                    TableTextHead(text = "创建人", width = cellWidthList[4])
                    TableTextHead(text = "日期", width = cellWidthList[5])
                }
            }

            itemsIndexed(entities) { index, item ->
                val selected = item == entities[selectedIndex]
                Row(
                    modifier = Modifier
//                        .background(if (selected) Color.Gray else Color.White)
                        .background(if (index % 2 == 0) Color(rgb(239, 239, 239)) else Color.White)
                        .clickable(onClick = {
                            selectedIndex = index
                            Log.d(
                                "Test",
                                "点击选中的=========$selectedIndex"
                            )
                        })
                ) {
                    TableTextBody(text = (index + 1).toString(), width = cellWidthList[0], selected)
                    TableTextBody(text = item.displayText, width = cellWidthList[1], selected)
                    TableTextBody(
                        text = "" + item.startRange + "%~" + item.endRange + "%",
                        width = cellWidthList[2], selected
                    )
                    TableTextBody(text = item.thickness, width = cellWidthList[3], selected)
                    TableTextBody(text = item.founder, width = cellWidthList[4], selected)
                    TableTextBody(
                        text = "" + item.createTime.dateFormat("yyyy-MM-dd"),
                        width = cellWidthList[5], selected
                    )
                }
            }

        }

        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(239, 239, 239))
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 30.dp)
            ) {
                Button(
                    modifier = Modifier
                        .padding(start = 25.dp)
                        .width(90.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        displayText = ""
                        startRange_ex = "0"
                        endRange_ex = "0"
                        coagulant_ex = "0"
                        volume_ex = "0.0"
                        founder = ""
                        showingDialog.value = true
                        open.value = false
                    }
                ) {
                    Text(text = "新 建", fontSize = 18.sp)
                }

                Button(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .width(90.dp)
                        .height(50.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {
                        if (entitiesList.size > 0) {
                            val entity = entities[selectedIndex]
                            if (entity != null) {
                                if (programId.value == entity.id) {
                                    Toast.makeText(
                                        context,
                                        "已在制胶操作选中,不能编辑！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    displayText = entity.displayText
                                    startRange_ex = entity.startRange.toString()
                                    endRange_ex = entity.endRange.toString()
                                    coagulant_ex = entity.coagulant.toString()
                                    volume_ex = entity.volume.toString()
                                    thickness.value = entity.thickness
                                    founder = entity.founder
                                    showingDialog.value = true
                                    open.value = true
                                }

                            }
                        }

                    }
                ) {
                    Text(text = "编 辑", fontSize = 18.sp)
                }

                Button(modifier = Modifier
                    .padding(start = 15.dp)
                    .width(90.dp)
                    .height(50.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {
                        deleteDialog.value = true
                    }
                ) {
                    Text(text = "删 除", fontSize = 18.sp)
                }

                Button(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .width(90.dp)
                        .height(50.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {
                        importDialog.value = true
                    }
                ) {
                    Text(text = "导 入", fontSize = 18.sp)
                }

                Button(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .width(90.dp)
                        .height(50.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {
                        var path = getStoragePath(context, true)
                        if ("" != path) {
                            if (entitiesList.isNotEmpty()) {
                                val entity = entities[selectedIndex]
                                if (entity != null) {
                                    try {


                                        File(path + "/zktony/" + entity.displayText + ".txt").writeText(
                                            "制胶程序:" + entity.displayText
                                                    + ",开始浓度:" + entity.startRange.toString()
                                                    + ",结束浓度:" + entity.endRange.toString()
                                                    + ",常用厚度:" + entity.thickness
                                                    + ",促凝剂体积:" + entity.coagulant.toString()
                                                    + ",胶液体积:" + entity.volume.toString()
                                                    + ",创建人:" + entity.founder
                                                    + ",日期：" + entity.createTime.dateFormat("yyyy-MM-dd")
                                        )
                                        Toast.makeText(
                                            context,
                                            "导出成功！",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: IOException) {
                                        Toast.makeText(
                                            context,
                                            "导出异常===${e.printStackTrace()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "U盘不存在！",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    }
                ) {
                    Text(text = "导 出", fontSize = 18.sp)
                }
            }
        }


    }

    //新建和打开弹窗
    if (showingDialog.value) {
        AlertDialog(
            onDismissRequest = {
            },
            text = {

                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(value = displayText,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(text = "制胶名称") },
                                onValueChange = {
                                    if (it.length > 7) {
                                        Toast.makeText(
                                            context,
                                            "名称长度不能过长!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        displayText = it
                                    }
                                })

                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "浓度范围："
                            )
                            OutlinedTextField(
                                modifier = Modifier.width(100.dp),
                                value = startRange_ex,
                                label = { Text(text = "%") },
                                onValueChange = { startRange_ex = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
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
                                text = "~"
                            )
                            OutlinedTextField(
                                modifier = Modifier.width(100.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                value = endRange_ex,
                                label = { Text(text = "%") },
                                onValueChange = { endRange_ex = it },
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
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "厚度："
                            )
                            tags.forEach {
                                Row {
                                    RadioButton(
                                        colors = RadioButtonDefaults.colors(
                                            Color(
                                                android.graphics.Color.rgb(
                                                    0,
                                                    105,
                                                    52
                                                )
                                            )
                                        ),
                                        selected = it == thickness.value,
                                        onClick = {
                                            thickness.value = it
                                        }
                                    )
                                    Text(modifier = Modifier.padding(top = 15.dp), text = it)
                                }

                                Spacer(modifier = Modifier.width(20.dp))
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(value = volume_ex,
                                label = { Text(text = "胶液体积/mL") },
                                onValueChange = { volume_ex = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
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
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(value = coagulant_ex,
                                label = { Text(text = "促凝剂体积/μL") },
                                onValueChange = { coagulant_ex = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
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
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(value = founder,
                                label = { Text(text = "创建人") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                onValueChange = { founder = it })

                        }
                    }


                }


            }, confirmButton = {
                Button(
                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        try {
                            if (open.value) {
                                //打开
                                val entity = entities[selectedIndex]

                                var speChat =
                                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"

                                var nameRepeat = false

                                entitiesList.forEach {
                                    if (displayText == it.displayText) {
                                        if (entity != null) {
                                            if (entity.displayText != displayText) {
                                                nameRepeat = true
                                            }
                                        }
                                    }
                                }

                                if (nameRepeat) {
                                    Toast.makeText(
                                        context,
                                        "文件名不能重复！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (Pattern.compile(speChat).matcher(displayText).find()) {
                                        Toast.makeText(
                                            context,
                                            "文件名不能包含特殊字符！",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        if (startRange_ex.toInt() > endRange_ex.toInt()
                                            || startRange_ex.toInt() < 3 || startRange_ex.toInt() > 30 || endRange_ex.toInt() < 3 || endRange_ex.toInt() > 30 || coagulant_ex.toInt() < 0 || volume_ex.toDouble() < 0
                                            || coagulant_ex.toInt() > 800 || volume_ex.toDouble() > 20
                                        ) {
                                            Toast.makeText(
                                                context,
                                                "数据错误！",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            if (entity != null) {
                                                entity.displayText = displayText
                                                entity.startRange = startRange_ex.toInt()
                                                entity.endRange = endRange_ex.toInt()
                                                entity.thickness = thickness.value
                                                entity.coagulant = coagulant_ex.toInt()
                                                entity.volume = volume_ex.toDouble()
                                                entity.founder = founder
                                                dispatch(ProgramIntent.Update(entity))
                                                showingDialog.value = false
                                            }
                                        }

                                    }
                                }


                            } else {
                                //新建
                                var speChat =
                                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"

                                var nameRepeat = false

                                entitiesList.forEach {
                                    if (displayText == it.displayText) {
                                        nameRepeat = true
                                    }
                                }

                                if (nameRepeat) {
                                    Toast.makeText(
                                        context,
                                        "文件名不能重复！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (Pattern.compile(speChat).matcher(displayText).find()) {
                                        Toast.makeText(
                                            context,
                                            "文件名不能包含特殊字符！",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {
                                        if (startRange_ex.toInt() > endRange_ex.toInt()
                                            || startRange_ex.toInt() < 3 || startRange_ex.toInt() > 30 || endRange_ex.toInt() < 3 || endRange_ex.toInt() > 30 || coagulant_ex.toInt() < 0 || volume_ex.toDouble() < 0
                                            || coagulant_ex.toInt() > 800 || volume_ex.toDouble() > 20
                                        ) {
                                            Toast.makeText(
                                                context,
                                                "数据错误！",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            dispatch(
                                                ProgramIntent.Insert(
                                                    displayText,
                                                    startRange_ex.toInt(),
                                                    endRange_ex.toInt(),
                                                    thickness.value,
                                                    coagulant_ex.toInt(),
                                                    volume_ex.toDouble(),
                                                    founder
                                                )
                                            )
                                            showingDialog.value = false
                                        }

                                    }

                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "数据异常！",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            thickness.value = "1.0"
                        }


                    }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                Button(
                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        thickness.value = "1.0"
                        showingDialog.value = false
                    }) {
                    Text(text = "取消")
                }
            })
    }


    //删除弹窗
    if (deleteDialog.value) {
        AlertDialog(
            onDismissRequest = { deleteDialog.value = false },
            title = {
                Text(text = "是否确认删除！")
            },
            text = {

            }, confirmButton = {
                Button(
                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        Log.d(
                            "Test",
                            "删除选中的selectedIndex===$selectedIndex"
                        )
                        if (entitiesList.size > 0) {
                            val entity = entities[selectedIndex]
                            Log.d(
                                "Test",
                                "删除选中的entity===$entity==="
                            )
                            if (entity != null) {
                                if (programId.value == entity.id) {
                                    Toast.makeText(
                                        context,
                                        "已在制胶操作选中,不能删除！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (selectedIndex > 0) {
                                        selectedIndex -= 1
                                    }
                                    dispatch(ProgramIntent.Delete(entity.id))
                                    deleteDialog.value = false
                                }

                            }

                        }
                    }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                Button(
                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = { deleteDialog.value = false }) {
                    Text(text = "取消")
                }
            })
    }


    //导入弹窗
    if (importDialog.value) {
        AlertDialog(
            onDismissRequest = { importDialog.value = false },
            text = {
                Text(text = "导入格式为(制胶名称:test,开始浓度:20,结束浓度:40....)以此类推,一行是一个制胶程序！")
            }, confirmButton = {
                Button(
                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {

                        //获取usb地址
                        var path = getStoragePath(context, true)
                        println("path========" + path)
                        if (!"".equals(path)) {

                            try {
                                var textList = ArrayList<String>()

                                //读取文件内容
                                val content = File(path + "/zktony/test.txt").readText()
                                println("content========" + content)
                                var contents = content.split(",")
                                contents.forEach {
                                    val entityy = it.split(":").get(1)
                                    textList.add(entityy)
                                }
                                println("textList====" + textList)

                                //新建
                                var speChat =
                                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"

                                var nameRepeat = false

                                entitiesList.forEach {
                                    if (textList.get(0) == it.displayText) {
                                        nameRepeat = true
                                    }
                                }

                                if (nameRepeat) {
                                    Toast.makeText(
                                        context,
                                        "文件名不能重复！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (Pattern.compile(speChat).matcher(textList.get(0)).find()) {
                                        Toast.makeText(
                                            context,
                                            "文件名不能包含特殊字符！",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {
                                        dispatch(
                                            ProgramIntent.Insert(
                                                textList.get(0),
                                                textList.get(1).toInt(),
                                                textList.get(2).toInt(),
                                                textList.get(3),
                                                textList.get(4).toInt(),
                                                textList.get(5).toDouble(),
                                                textList.get(6)
                                            )
                                        )
                                        showingDialog.value = false
                                    }

                                }



                                importDialog.value = false
                            } catch (exception: Exception) {
                                Toast.makeText(
                                    context,
                                    "数据有误！",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } else {
                            Toast.makeText(
                                context,
                                "U盘不存在！",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    }) {
                    Text(text = "开始导入")
                }
            }, dismissButton = {
                Button(
                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = { importDialog.value = false }) {
                    Text(text = "取消")
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
