package com.zktony.android.ui

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.components.TableText
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
import java.io.PrintWriter
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    Column {
        HomeAppBar(page) { navigation() }
//        ProgramAppBar(entities.toList(), selected, page, viewModel::dispatch) { navigation() }
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
    val startRange = rememberDataSaverState(key = "startRange", default = 0f)
    var startRange_ex by remember { mutableStateOf(startRange.value.format(3)) }

    /**
     * 结束浓度
     */
    var endRange = rememberDataSaverState(key = "endRange", default = 0f)
    var endRange_ex by remember { mutableStateOf(endRange.value.format(3)) }


    /**
     * 常用厚度
     * 0.75，1.0，1.5
     */
    val tags = arrayListOf("0.75", "1.0", "1.5")
    var thickness = remember { mutableStateOf(tags[1]) }


    /**
     * 促凝剂体积
     */
    var coagulant = rememberDataSaverState(key = "coagulant", default = 0f)
    var coagulant_ex by remember { mutableStateOf(coagulant.value.format(3)) }

    /**
     * 胶液体积
     */
    var volume = rememberDataSaverState(key = "volume", default = 0f)
    var volume_ex by remember { mutableStateOf(volume.value.format(3)) }

    /**
     * 创建人
     */
    var founder by remember { mutableStateOf("") }


    //	定义列宽
    val cellWidthList = arrayListOf(70, 100, 130, 90, 100, 120)
    //	使用lazyColumn来解决大数据量时渲染的性能问题
    LazyColumn(
        modifier = Modifier
            .height(800.dp)
            .padding(top = 20.dp)
    ) {
        //	粘性标题
        stickyHeader {
            Row(Modifier.background(Color.Gray)) {
                TableText(text = "序号", width = cellWidthList[0])
                TableText(text = "名称", width = cellWidthList[1])
                TableText(text = "浓度", width = cellWidthList[2])
                TableText(text = "厚度", width = cellWidthList[3])
                TableText(text = "创建人", width = cellWidthList[4])
                TableText(text = "日期", width = cellWidthList[5])
            }
        }
        itemsIndexed(entities) { index, item ->
            val selected = item == entities[selectedIndex]
            Row(
                modifier = Modifier
                    .background(if (selected) Color.Gray else Color.White)
                    .clickable(onClick = {
                        selectedIndex = index

                    })
            ) {
                TableText(text = "" + item.id, width = cellWidthList[0])
                TableText(text = item.displayText, width = cellWidthList[1])
                TableText(
                    text = "" + item.startRange + "%~" + item.endRange + "%",
                    width = cellWidthList[2]
                )
                TableText(text = item.thickness + "mm", width = cellWidthList[3])
                TableText(text = item.founder, width = cellWidthList[4])
                TableText(
                    text = "" + item.createTime.dateFormat("yyyy-MM-dd"),
                    width = cellWidthList[5]
                )
            }
        }

    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 850.dp)
    ) {
        Row {
            Button(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .width(100.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {
                    displayText = ""
                    startRange_ex = "0.0"
                    endRange_ex = "0.0"
                    coagulant_ex = "0.0"
                    volume_ex = "0.0"
                    founder = ""
                    showingDialog.value = true
                    open.value = false
                }
            ) {
                Text(text = "新    建", fontSize = 18.sp)
            }

            Button(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .width(100.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {
                    if (entitiesList.size > 0) {
                        val entity = entities[selectedIndex]
                        if (entity != null) {
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
            ) {
                Text(text = "编    辑", fontSize = 18.sp)
            }

            Button(modifier = Modifier
                .padding(start = 20.dp)
                .width(100.dp)
                .height(50.dp),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {
                    deleteDialog.value = true
                }
            ) {
                Text(text = "删    除", fontSize = 18.sp)
            }

            Button(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .width(100.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {
                    importDialog.value = true
                }
            ) {
                Text(text = "导    入", fontSize = 18.sp)
            }

            Button(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .width(100.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {

                    var path = getStoragePath(context, true)
                    if (!"".equals(path)) {
                        if (entitiesList.size > 0) {
                            val entity = entities[selectedIndex]
                            if (entity != null) {
                                File(path + "/zktony/test.txt").writeText(
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
                            }
                        }
                    }


                }
            ) {
                Text(text = "导    出", fontSize = 18.sp)
            }
        }
    }

    //新建和打开弹窗
    if (showingDialog.value) {
        AlertDialog(
            onDismissRequest = { showingDialog.value = false },
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
                                label = { Text(text = "制胶名称") },
                                onValueChange = { displayText = it })

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
                        Row {
                            Text(
                                fontSize = 16.sp,
                                text = "厚度："
                            )
                            tags.forEach {
                                Row {
                                    RadioButton(
                                        selected = it == thickness.value,
                                        onClick = {
                                            thickness.value = it
                                        }
                                    )
                                    Text(text = it)
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
                                onValueChange = { founder = it })

                        }
                    }


                }


            }, confirmButton = {
                TextButton(onClick = {

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

                                if (entity != null) {
                                    entity.displayText = displayText
                                    entity.startRange = startRange_ex.toDouble()
                                    entity.endRange = endRange_ex.toDouble()
                                    entity.thickness = thickness.value
                                    entity.coagulant = coagulant_ex.toDouble()
                                    entity.volume = volume_ex.toDouble()
                                    entity.founder = founder
                                    dispatch(ProgramIntent.Update(entity))
                                    showingDialog.value = false
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
                                dispatch(
                                    ProgramIntent.Insert(
                                        displayText,
                                        startRange_ex.toDouble(),
                                        endRange_ex.toDouble(),
                                        thickness.value,
                                        coagulant_ex.toDouble(),
                                        volume_ex.toDouble(),
                                        founder
                                    )
                                )
                                showingDialog.value = false
                            }

                        }
                    }


                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { showingDialog.value = false }) {
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
                TextButton(onClick = {
                    if (entitiesList.size > 0) {
                        val entity = entities[selectedIndex]
                        if (entity != null) {
                            dispatch(ProgramIntent.Delete(entity.id))
                        }
                        deleteDialog.value = false
                    }
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { deleteDialog.value = false }) {
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
                TextButton(onClick = {

                    //获取usb地址
                    var path = getStoragePath(context, true)
                    println("path========" + path)
                    if (!"".equals(path)) {

                        //获取文件列表
//                        var fileList = File(path + "/zktony")
//                        var files = fileList.listFiles()
//                        files.forEach { file ->
//                            run {
//                                println("files========" + file.name)
//                            }
//                        }
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
                                            textList.get(1).toDouble(),
                                            textList.get(2).toDouble(),
                                            textList.get(3),
                                            textList.get(4).toDouble(),
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


                    }


                }) {
                    Text(text = "开始导入")
                }
            }, dismissButton = {
                TextButton(onClick = { importDialog.value = false }) {
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
                println("usb的path=====" + path)
            } else if (!isUsb == sd) { //sd
                assert(file != null)
                path = file.getAbsolutePath()
                println("sd的path=====" + path)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return path
}


@Composable
fun ProgramDetail(
    entities: List<Program>,
    selected: Long,
    dispatch: (ProgramIntent) -> Unit
) {
}
//) {
//    val scope = rememberCoroutineScope()
//    val program = entities.find { it.id == selected } ?: Program()
//    val maxAbscissa by rememberDataSaverState(key = Constants.ZT_0001, initialValue = 0.0)
//    val maxOrdinate by rememberDataSaverState(key = Constants.ZT_0002, initialValue = 0.0)
//    var colloid by remember { mutableStateOf(program.dosage.colloid.format(1)) }
//    var coagulant by remember { mutableStateOf(program.dosage.coagulant.format(1)) }
//    var preColloid by remember { mutableStateOf(program.dosage.preColloid.format(1)) }
//    var preCoagulant by remember { mutableStateOf(program.dosage.preCoagulant.format(1)) }
//    var glueSpeed by remember { mutableStateOf(program.speed.glue.format(1)) }
//    var preSpeed by remember { mutableStateOf(program.speed.pre.format(1)) }
//
//    LazyColumn(
//        modifier = Modifier
//            .padding(16.dp)
//            .imePadding(),
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//        contentPadding = PaddingValues(16.dp),
//    ) {
//        item {
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                CircleTextField(
//                    modifier = Modifier.weight(1f),
//                    title = "制胶/促凝剂 μL",
//                    value = coagulant,
//                    onValueChange = {
//                        scope.launch {
//                            coagulant = it
//                            val dosage =
//                                program.dosage.copy(coagulant = it.toDoubleOrNull() ?: 0.0)
//                            dispatch(ProgramIntent.Update(program.copy(dosage = dosage)))
//                        }
//                    }
//                )
//                CircleTextField(
//                    modifier = Modifier.weight(1f),
//                    title = "制胶/胶体 μL",
//                    value = colloid,
//                    onValueChange = {
//                        scope.launch {
//                            colloid = it
//                            val dosage =
//                                program.dosage.copy(colloid = it.toDoubleOrNull() ?: 0.0)
//                            dispatch(ProgramIntent.Update(program.copy(dosage = dosage)))
//                        }
//                    }
//                )
//            }
//        }
//        item {
//            CircleTextField(
//                title = "制胶/速度",
//                value = glueSpeed,
//                onValueChange = {
//                    scope.launch {
//                        glueSpeed = it
//                        val speed =
//                            program.speed.copy(glue = it.toDoubleOrNull() ?: 0.0)
//                        dispatch(ProgramIntent.Update(program.copy(speed = speed)))
//                    }
//                }
//            )
//        }
//        item {
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                CircleTextField(
//                    modifier = Modifier.weight(1f),
//                    title = "预排/促凝剂 μL",
//                    value = preCoagulant,
//                    onValueChange = {
//                        scope.launch {
//                            preCoagulant = it
//                            val dosage =
//                                program.dosage.copy(preCoagulant = it.toDoubleOrNull() ?: 0.0)
//                            dispatch(ProgramIntent.Update(program.copy(dosage = dosage)))
//                        }
//                    }
//                )
//                CircleTextField(
//                    modifier = Modifier.weight(1f),
//                    title = "预排/胶体 μL",
//                    value = preColloid,
//                    onValueChange = {
//                        scope.launch {
//                            preColloid = it
//                            val dosage =
//                                program.dosage.copy(preColloid = it.toDoubleOrNull() ?: 0.0)
//                            dispatch(ProgramIntent.Update(program.copy(dosage = dosage)))
//                        }
//                    }
//                )
//            }
//        }
//        item {
//            CircleTextField(
//                title = "预排/速度",
//                value = preSpeed,
//                onValueChange = {
//                    scope.launch {
//                        preSpeed = it
//                        val speed =
//                            program.speed.copy(pre = it.toDoubleOrNull() ?: 0.0)
//                        dispatch(ProgramIntent.Update(program.copy(speed = speed)))
//                    }
//                }
//            )
//        }
//        item {
//            CoordinateInput(
//                modifier = Modifier.fillMaxWidth(),
//                title = "位置",
//                point = program.point,
//                limit = Point(maxAbscissa, maxOrdinate),
//                onCoordinateChange = {
//                    scope.launch {
//                        dispatch(ProgramIntent.Update(program.copy(point = it)))
//                    }
//                }
//            ) {
//                scope.launch {
//                    start {
//                        timeOut = 1000L * 60L
//                        with(index = 1, pdv = 0.0)
//                    }
//                    start {
//                        timeOut = 1000L * 60L
//                        with(index = 0, pdv = program.point.x)
//                    }
//                    start {
//                        timeOut = 1000L * 60L
//                        with(index = 1, pdv = program.point.y)
//                    }
//                }
//            }
//        }
//    }
//}