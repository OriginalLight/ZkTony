package com.zktony.android.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color.rgb
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
import com.zktony.android.ui.utils.Permissions
import com.zktony.android.ui.utils.PermissionsScreen
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.getStoragePath
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
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
    val entityNum by viewModel.entityNum.collectAsStateWithLifecycle()

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
            HomeAppBar(page, false) { navigation() }
            AnimatedContent(targetState = page) {
                when (page) {
                    PageType.PROGRAM -> ProgramList(
                        entities,
                        entities.toList(),
                        viewModel::dispatch,
                        entityNum
                    )

                    else -> {}
                }
            }
        }
    }
}


@OptIn(
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    entitiesList: List<Program>,
    dispatch: (ProgramIntent) -> Unit,
    entityNum: Int
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current

    val keyboard = LocalSoftwareKeyboardController.current

    dispatch(ProgramIntent.count)


    /**
     * 权限的弹窗
     */
    val permissionsDialog = remember { mutableStateOf(false) }



    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            permissionsDialog.value = true
        }
    }

    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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


    val exportDialog = remember { mutableStateOf(false) }


    var selectedIndex by remember { mutableStateOf(0) }

    var selectedId by remember { mutableStateOf(0L) }

    /**
     * 制胶名称
     */
    var displayText by remember { mutableStateOf("") }

    /**
     * 开始浓度
     */
    val startRange = rememberDataSaverState(key = "startRange", default = 0.0)
    var startRange_ex by remember { mutableStateOf(startRange.value.toString()) }

    /**
     * 结束浓度
     */
    var endRange = rememberDataSaverState(key = "endRange", default = 0.0)
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

    val modelsThickness = rememberDataSaverState(key = "modelsThickness", "G1520")


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
                        .background(if (index % 2 == 0) Color(rgb(229, 229, 229)) else Color.White)
                        .clickable(onClick = {
                            selectedIndex = index
                            selectedId = item.id
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
                .background(Color(229, 229, 229))
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
                        scope.launch {
                            displayText = ""
                            startRange_ex = "0"
                            endRange_ex = "0"
                            coagulant_ex = "0"
                            volume_ex = "0.0"
                            founder = ""
                            dispatch(ProgramIntent.count)
                            delay(200)
                            if (modelsThickness.value == "G1500") {
                                if (entityNum < 10) {
                                    showingDialog.value = true
                                    open.value = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "制胶程序数量超过限制,不能新增!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else if (modelsThickness.value == "G1510") {
                                if (entityNum < 100) {
                                    showingDialog.value = true
                                    open.value = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "制胶程序数量超过限制,不能新增!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                if (entityNum < 500) {
                                    showingDialog.value = true
                                    open.value = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "制胶程序数量超过限制,不能新增!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }

                        }


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
                        if (entitiesList.isNotEmpty()) {
                            val entity = entities[selectedIndex]
                            if (entity != null) {
                                if (programId.value == entity.id) {
                                    Toast.makeText(
                                        context,
                                        "已在制胶操作选中,不能编辑!",
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
                    .height(50.dp),
                    enabled = selectedIndex !in 0..2,
                    colors = ButtonDefaults.buttonColors(
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
                        scope.launch {
                            dispatch(ProgramIntent.count)
                            delay(200)
                            importDialog.value = true
                        }

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
                        scope.launch {
                            var path = getStoragePath(context, true)
                            if ("" != path) {
                                if (entitiesList.isNotEmpty()) {
                                    val entity = entities[selectedIndex]
                                    if (entity != null) {
                                        try {
                                            val release = Build.VERSION.RELEASE
                                            if (release == "6.0.1") {
                                                //Android6.0.1系统是迈冲
                                                if (path != null) {
                                                    path = path.replace("storage", "/mnt/media_rw")
                                                }
                                            }
                                            path += "/${entity.displayText}.txt"
                                            val text =
                                                "制胶程序:" + entity.displayText + ",开始浓度:" + entity.startRange.toString() + ",结束浓度:" + entity.endRange.toString() + ",常用厚度:" + entity.thickness + ",促凝剂体积:" + entity.coagulant.toString() + ",胶液体积:" + entity.volume.toString() + ",创建人:" + entity.founder + ",日期：" + entity.createTime.dateFormat(
                                                    "yyyy-MM-dd"
                                                )

                                            val file = File(path)
                                            if (!file.exists()) {
                                                if (file.createNewFile()) {
                                                    FileWriter(path, true).use { writer ->
                                                        writer.write(text)
                                                    }
                                                    Toast.makeText(
                                                        context,
                                                        "导出成功!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "创建文件失败,请重试!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                FileWriter(path, true).use { writer ->
                                                    writer.write(text)
                                                }
                                            }
                                            Toast.makeText(
                                                context,
                                                "导出成功!",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "导出异常,请重试!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "U盘不存在!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
                            OutlinedTextField(
                                value = displayText,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(rgb(0, 105, 52)),
                                    focusedLabelColor = Color(rgb(0, 105, 52)),
                                    cursorColor = Color(rgb(0, 105, 52))
                                ),
                                label = { Text(fontSize = 20.sp, text = "制胶名称") },
                                textStyle = TextStyle(fontSize = 20.sp),
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
                                fontSize = 20.sp,
                                text = "浓度范围："
                            )
                            OutlinedTextField(
                                modifier = Modifier.width(100.dp),
                                value = startRange_ex,
                                label = { Text(fontSize = 20.sp, text = "%") },
                                textStyle = TextStyle(fontSize = 20.sp),
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
                                fontSize = 20.sp,
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
                                label = { Text(fontSize = 20.sp, text = "%") },
                                textStyle = TextStyle(fontSize = 20.sp),
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
                                fontSize = 20.sp,
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
                                    Text(
                                        modifier = Modifier.padding(top = 15.dp),
                                        fontSize = 18.sp,
                                        text = it
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(value = volume_ex,
                                label = { Text(fontSize = 20.sp, text = "胶液体积/mL") },
                                textStyle = TextStyle(fontSize = 20.sp),
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
                                label = { Text(fontSize = 20.sp, text = "促凝剂体积/μL") },
                                textStyle = TextStyle(fontSize = 20.sp),
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
                                label = { Text(fontSize = 20.sp, text = "创建人") },
                                textStyle = TextStyle(fontSize = 20.sp),
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
                    modifier = Modifier.width(100.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    onClick = {
                        thickness.value = "1.0"
                        showingDialog.value = false
                    }) {
                    Text(fontSize = 18.sp, text = "取   消", color = Color.Black)
                }

            }, dismissButton = {

                Button(
                    modifier = Modifier.width(100.dp),
                    enabled = !(open.value && selectedId in 1..3),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    onClick = {
                        try {
                            if (open.value) {
                                //打开
                                val entity = entities[selectedIndex]

                                var speChat =
                                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~!@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s]"

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
                                        "文件名不能重复!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (Pattern.compile(speChat).matcher(displayText).find()) {
                                        Toast.makeText(
                                            context,
                                            "文件名不能包含特殊字符!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        if (startRange_ex.toDouble() > endRange_ex.toDouble()) {
                                            Toast.makeText(
                                                context,
                                                "浓度范围不能由大到小",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (startRange_ex.toDouble() < 1 || startRange_ex.toDouble() > 30) {
                                            Toast.makeText(
                                                context,
                                                "低浓度不能小于1或大于30",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (endRange_ex.toDouble() < 1 || endRange_ex.toDouble() > 30) {
                                            Toast.makeText(
                                                context,
                                                "高浓度不能小于1或大于30",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (coagulant_ex.toInt() <= 0 || coagulant_ex.toInt() > 800) {
                                            Toast.makeText(
                                                context,
                                                "促凝剂体积不能小于1或大于800",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (volume_ex.toDouble() <= 0 || volume_ex.toDouble() > 20) {
                                            Toast.makeText(
                                                context,
                                                "胶液体积不能小于1或大于20",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            if (entity != null) {
                                                entity.displayText = displayText
                                                entity.startRange = startRange_ex.toDouble()
                                                entity.endRange = endRange_ex.toDouble()
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
                                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~!@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s]"

                                var nameRepeat = false

                                entitiesList.forEach {
                                    if (displayText == it.displayText) {
                                        nameRepeat = true
                                    }
                                }

                                if (nameRepeat) {
                                    Toast.makeText(
                                        context,
                                        "文件名不能重复!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (Pattern.compile(speChat).matcher(displayText).find()) {
                                        Toast.makeText(
                                            context,
                                            "文件名不能包含特殊字符!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        if (startRange_ex.toDouble() > endRange_ex.toDouble()) {
                                            Toast.makeText(
                                                context,
                                                "浓度范围不能由大到小",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (startRange_ex.toDouble() < 1 || startRange_ex.toDouble() > 30) {
                                            Toast.makeText(
                                                context,
                                                "低浓度不能小于1或大于30",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (endRange_ex.toDouble() < 1 || endRange_ex.toDouble() > 30) {
                                            Toast.makeText(
                                                context,
                                                "高浓度不能小于1或大于30",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (coagulant_ex.toInt() <= 0 || coagulant_ex.toInt() > 800) {
                                            Toast.makeText(
                                                context,
                                                "促凝剂体积不能小于1或大于800",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (volume_ex.toDouble() <= 0 || volume_ex.toDouble() > 20) {
                                            Toast.makeText(
                                                context,
                                                "胶液体积不能小于1或大于20",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            dispatch(
                                                ProgramIntent.Insert(
                                                    displayText,
                                                    startRange_ex.toDouble(),
                                                    endRange_ex.toDouble(),
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
                                "促凝剂液量必须是整数!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            thickness.value = "1.0"
                        }


                    }) {
                    Text(fontSize = 18.sp, text = "确   认")
                }

            })
    }


    //删除弹窗
    if (deleteDialog.value) {
        AlertDialog(
            onDismissRequest = { deleteDialog.value = false },
            title = {
                Text(text = "是否确认删除!")
            },
            text = {

            }, confirmButton = {

                Button(
                    modifier = Modifier.width(100.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ), onClick = { deleteDialog.value = false }) {
                    Text(text = "取   消")
                }

            }, dismissButton = {
                Button(
                    modifier = Modifier.width(100.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        if (entitiesList.size > 0) {
                            val entity = entities[selectedIndex]
                            if (entity != null) {
                                if (programId.value == entity.id) {
                                    Toast.makeText(
                                        context,
                                        "已在制胶操作选中,无法删除!",
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
                    Text(text = "确   认")
                }

            })
    }


    if (permissionsDialog.value) {
        Dialog(onDismissRequest = {}) {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .padding(30.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PermissionsScreen()
                    permissionsDialog.value = false
                }
            }
        }
    }


    //导入弹窗
    if (importDialog.value) {
        AlertDialog(
            onDismissRequest = { importDialog.value = false },
            text = {
                Column {
                    Text(text = "导入zktony文件夹下的program.txt文件")
                    Text(text = "导入格式为:")
                    Text(text = "(制胶程序名称:test,开始浓度:4,结束浓度:20,厚度:1.5,促凝剂体积:75,胶液体积:9.5,创建人:)")
                    Text(text = "以此类推,一行是一个制胶程序!")
                }
            }, confirmButton = {
                Button(
                    modifier = Modifier.width(100.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ), onClick = { importDialog.value = false }) {
                    Text(text = "取   消")
                }
            }, dismissButton = {

                Button(
                    modifier = Modifier.width(120.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ), onClick = {
                        //获取usb地址
                        var path = getStoragePath(context, true)
                        if (!"".equals(path)) {

                            try {
                                var speChat =
                                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~!@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s]"

                                var nameRepeat = false
                                var programCount = 0
                                File("$path/zktony/program.txt").bufferedReader()
                                    .useLines {
                                        programCount = it.count()
                                    }

                                File("$path/zktony/program.txt").bufferedReader()
                                    .useLines { lines ->
                                        for (line in lines) {
                                            if (line.isNotEmpty()) {
                                                if (modelsThickness.value == "G1500") {
                                                    if (entityNum < 10) {
                                                        if (programCount + entityNum > 10) {
                                                            Toast.makeText(
                                                                context,
                                                                "要导入的程序数量超出限制",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {


                                                            var textList = ArrayList<String>()
                                                            var contents = line.split(",")
                                                            contents.forEach {
                                                                val byte = it.split(":").get(1)
                                                                textList.add(byte)
                                                            }

                                                            entitiesList.forEach {
                                                                if (textList.get(0) == it.displayText) {
                                                                    nameRepeat = true
                                                                }
                                                            }

                                                            if (nameRepeat) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "文件名不能重复!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } else {
                                                                if (Pattern.compile(speChat)
                                                                        .matcher(textList.get(0))
                                                                        .find()
                                                                ) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "文件名不能包含特殊字符!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()

                                                                } else {

                                                                    if (textList.get(1)
                                                                            .toInt() > textList.get(
                                                                            2
                                                                        )
                                                                            .toInt()
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "浓度范围不能由大到小",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(1)
                                                                            .toInt() < 3 || textList.get(
                                                                            1
                                                                        )
                                                                            .toInt() > 30
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "低浓度不能小于3或大于30",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(2)
                                                                            .toInt() < 3 || textList.get(
                                                                            2
                                                                        )
                                                                            .toInt() > 30
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "高浓度不能小于3或大于30",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(4)
                                                                            .toInt() <= 0 || textList.get(
                                                                            4
                                                                        )
                                                                            .toInt() > 800
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "促凝剂体积不能小于1或大于800",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(5)
                                                                            .toDouble() <= 0 || textList.get(
                                                                            5
                                                                        ).toDouble() > 20
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "胶液体积不能小于1或大于20",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else {
                                                                        dispatch(
                                                                            ProgramIntent.Insert(
                                                                                textList.get(0),
                                                                                textList.get(1)
                                                                                    .toDouble(),
                                                                                textList.get(2)
                                                                                    .toDouble(),
                                                                                textList.get(3),
                                                                                textList.get(4)
                                                                                    .toInt(),
                                                                                textList.get(5)
                                                                                    .toDouble(),
                                                                                textList.get(6)
                                                                            )
                                                                        )
                                                                        showingDialog.value = false
                                                                    }
                                                                }

                                                            }


                                                        }
                                                    }
                                                } else if (modelsThickness.value == "G1510") {
                                                    if (entityNum < 100) {
                                                        if (programCount + entityNum > 100) {
                                                            Toast.makeText(
                                                                context,
                                                                "要导入的程序数量超出限制",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            var textList = ArrayList<String>()
                                                            var contents = line.split(",")
                                                            contents.forEach {
                                                                val byte = it.split(":").get(1)
                                                                textList.add(byte)
                                                            }

                                                            entitiesList.forEach {
                                                                if (textList.get(0) == it.displayText) {
                                                                    nameRepeat = true
                                                                }
                                                            }

                                                            if (nameRepeat) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "文件名不能重复!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } else {
                                                                if (Pattern.compile(speChat)
                                                                        .matcher(textList.get(0))
                                                                        .find()
                                                                ) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "文件名不能包含特殊字符!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()

                                                                } else {

                                                                    if (textList.get(1)
                                                                            .toInt() > textList.get(
                                                                            2
                                                                        )
                                                                            .toInt()
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "浓度范围不能由大到小",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(1)
                                                                            .toInt() < 3 || textList.get(
                                                                            1
                                                                        )
                                                                            .toInt() > 30
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "低浓度不能小于3或大于30",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(2)
                                                                            .toInt() < 3 || textList.get(
                                                                            2
                                                                        )
                                                                            .toInt() > 30
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "高浓度不能小于3或大于30",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(4)
                                                                            .toInt() <= 0 || textList.get(
                                                                            4
                                                                        )
                                                                            .toInt() > 800
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "促凝剂体积不能小于1或大于800",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(5)
                                                                            .toDouble() <= 0 || textList.get(
                                                                            5
                                                                        ).toDouble() > 20
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "胶液体积不能小于1或大于20",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else {
                                                                        dispatch(
                                                                            ProgramIntent.Insert(
                                                                                textList.get(0),
                                                                                textList.get(1)
                                                                                    .toDouble(),
                                                                                textList.get(2)
                                                                                    .toDouble(),
                                                                                textList.get(3),
                                                                                textList.get(4)
                                                                                    .toInt(),
                                                                                textList.get(5)
                                                                                    .toDouble(),
                                                                                textList.get(6)
                                                                            )
                                                                        )
                                                                        showingDialog.value = false
                                                                    }
                                                                }

                                                            }


                                                        }
                                                    }
                                                } else {
                                                    if (entityNum < 500) {
                                                        if (programCount + entityNum > 500) {
                                                            Toast.makeText(
                                                                context,
                                                                "要导入的程序数量超出限制",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            var textList = ArrayList<String>()
                                                            var contents = line.split(",")
                                                            contents.forEach {
                                                                val byte = it.split(":").get(1)
                                                                textList.add(byte)
                                                            }

                                                            entitiesList.forEach {
                                                                if (textList.get(0) == it.displayText) {
                                                                    nameRepeat = true
                                                                }
                                                            }

                                                            if (nameRepeat) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "文件名不能重复!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } else {
                                                                if (Pattern.compile(speChat)
                                                                        .matcher(textList.get(0))
                                                                        .find()
                                                                ) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "文件名不能包含特殊字符!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()

                                                                } else {

                                                                    if (textList.get(1)
                                                                            .toInt() > textList.get(
                                                                            2
                                                                        )
                                                                            .toInt()
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "浓度范围不能由大到小",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(1)
                                                                            .toInt() < 3 || textList.get(
                                                                            1
                                                                        )
                                                                            .toInt() > 30
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "低浓度不能小于3或大于30",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(2)
                                                                            .toInt() < 3 || textList.get(
                                                                            2
                                                                        )
                                                                            .toInt() > 30
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "高浓度不能小于3或大于30",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(4)
                                                                            .toInt() <= 0 || textList.get(
                                                                            4
                                                                        )
                                                                            .toInt() > 800
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "促凝剂体积不能小于1或大于800",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else if (textList.get(5)
                                                                            .toDouble() <= 0 || textList.get(
                                                                            5
                                                                        ).toDouble() > 20
                                                                    ) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "胶液体积不能小于1或大于20",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else {
                                                                        dispatch(
                                                                            ProgramIntent.Insert(
                                                                                textList.get(0),
                                                                                textList.get(1)
                                                                                    .toDouble(),
                                                                                textList.get(2)
                                                                                    .toDouble(),
                                                                                textList.get(3),
                                                                                textList.get(4)
                                                                                    .toInt(),
                                                                                textList.get(5)
                                                                                    .toDouble(),
                                                                                textList.get(6)
                                                                            )
                                                                        )
                                                                        showingDialog.value = false
                                                                    }
                                                                }

                                                            }


                                                        }
                                                    }
                                                }


                                            }
                                        }

                                    }

                                importDialog.value = false
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "数据有误===${e.printStackTrace()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } else {
                            Toast.makeText(
                                context,
                                "U盘不存在!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    }) {
                    Text(text = "开始导入")
                }

            })
    }


}




