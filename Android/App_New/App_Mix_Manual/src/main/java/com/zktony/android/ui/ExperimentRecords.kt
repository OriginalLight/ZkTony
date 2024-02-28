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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.entities.ExperimentRecord
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
import kotlinx.coroutines.launch
import java.io.File
import java.lang.reflect.Method
import kotlin.reflect.KFunction1

/**
 * 实验记录
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExperimentRecords(viewModel: ExperimentRecordsViewModel) {

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
                PageType.EXPERIMENTRECORDS -> navigationActions.navigateUp()
                else -> viewModel.dispatch(ExperimentRecordsIntent.NavTo(PageType.EXPERIMENTRECORDS))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(ExperimentRecordsIntent.Flags(UiFlags.none()))
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
            HomeAppBar(page,false) { navigation() }
            AnimatedContent(targetState = page) {
                when (page) {
                    PageType.EXPERIMENTRECORDS -> experimentList(
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
fun experimentList(
    entities: LazyPagingItems<ExperimentRecord>,
    entitiesList: List<ExperimentRecord>,
    dispatch: KFunction1<ExperimentRecordsIntent, Unit>
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current


    /**
     * 新建和打开的弹窗
     */
    val showingDialog = remember { mutableStateOf(false) }


    /**
     * 删除的弹窗
     */
    val deleteDialog = remember { mutableStateOf(false) }


    /**
     * 导入弹窗
     */
    val importDialog = remember { mutableStateOf(false) }


    var selectedIndex by remember { mutableStateOf(0) }


    //	定义列宽
    val cellWidthList = arrayListOf(70, 100, 70, 130, 120, 120)
    //	使用lazyColumn来解决大数据量时渲染的性能问题
    Column(
        modifier = Modifier
            .padding(start = 13.75.dp)
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
            .background(Color.White)
            .height(904.9.dp)
            .width((572.5).dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .height(800.dp)
                .fillMaxWidth()
                .padding(top = 20.dp, start = 15.dp)
        ) {
            stickyHeader {
                Row(Modifier.background(Color(android.graphics.Color.rgb(0, 105, 52)))) {
                    TableTextHead(text = "序号", width = cellWidthList[0])
                    TableTextHead(text = "日             期", width = cellWidthList[5])
                    TableTextHead(text = "浓               度", width = cellWidthList[3])
//                    TableTextHead(text = "厚度", width = cellWidthList[2])
                    TableTextHead(text = "制胶数量", width = cellWidthList[1])
                    TableTextHead(text = "状             态", width = cellWidthList[4])
                }
            }

            itemsIndexed(entities) { index, item ->
                val selected = item == entities[selectedIndex]
                Row(
                    modifier = Modifier
//                        .background(if (selected) Color.Gray else Color.White)
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
                            Log.d(
                                "Test",
                                "点击选中的=========$selectedIndex"
                            )
                        })
                ) {
                    TableTextBody(text = (index + 1).toString(), width = cellWidthList[0], selected)
                    TableTextBody(
                        text = "" + item.createTime.dateFormat("yyyy-MM-dd"),
                        width = cellWidthList[5], selected
                    )
                    TableTextBody(
                        text = "" + item.startRange + "%~" + item.endRange + "%",
                        width = cellWidthList[3], selected
                    )
//                    TableTextBody(text = item.thickness, width = cellWidthList[3], selected)

                    TableTextBody(text = "" + item.number, width = cellWidthList[1], selected)
                    TableTextBody(text = item.status, width = cellWidthList[4], selected)
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
                        .padding(start = 50.dp)
                        .width(100.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {
                        showingDialog.value = true
                    }
                ) {
                    Text(text = "查    看", fontSize = 18.sp)
                }

                Button(modifier = Modifier
                    .padding(start = 70.dp)
                    .width(100.dp)
                    .height(50.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {
                        deleteDialog.value = true
                    }
                ) {
                    Text(text = "删    除", fontSize = 18.sp)
                }


                Button(
                    modifier = Modifier
                        .padding(start = 70.dp)
                        .width(100.dp)
                        .height(50.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {

                        var path = getStoragePath(context, true)
                        if (!"".equals(path)) {
                            if (entitiesList.isNotEmpty()) {


                                val entity = entities[selectedIndex]
                                if (entity != null) {
                                    val filePath = "$path/zktony/erlog.txt"

                                    val file =
                                        File(filePath)
                                    if (!file.exists()) {
                                        file.createNewFile() // 创建新文件（若不存在）
                                    }

                                    File(filePath).writeText(
                                        "实验记录:" + ",日期：" + entity.createTime.dateFormat("yyyy-MM-dd")
                                                + ",开始浓度:" + entity.startRange.toString()
                                                + ",结束浓度:" + entity.endRange.toString()
                                                + ",常用厚度:" + entity.thickness
                                                + ",促凝剂体积:" + entity.coagulant.toString()
                                                + ",胶液体积:" + entity.volume.toString()
                                                + ",制胶数量:" + entity.number.toString()
                                                + ",状态:" + entity.status
                                                + ",状态详情:" + entity.detail
                                    )
                                    Toast.makeText(
                                        context,
                                        "导出成功！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "未检测到USB！",
                                Toast.LENGTH_SHORT
                            ).show()

                        }


                    }
                ) {
                    Text(text = "导    出", fontSize = 18.sp)
                }

            }
        }


    }

    //新建和打开弹窗
    if (showingDialog.value) {
        if (entitiesList.isNotEmpty()) {
            val entity = entities[selectedIndex]
            if (entity != null) {
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

                                    Text(
                                        fontSize = 16.sp,
                                        text = "日期："
                                    )
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.createTime.dateFormat("yyyy-MM-dd")
                                    )

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
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.startRange.toString() + "%"
                                    )
                                    Text(
                                        fontSize = 16.sp,
                                        text = "~"
                                    )
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.endRange.toString() + "%"
                                    )

                                }
                            }

                            item {
                                Row {
                                    Text(
                                        fontSize = 16.sp,
                                        text = "厚度："
                                    )
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.thickness + "mm"
                                    )

                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    Text(
                                        fontSize = 16.sp,
                                        text = "胶液体积："
                                    )
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.volume.toString() + "mL"
                                    )

                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    Text(
                                        fontSize = 16.sp,
                                        text = "促凝剂体积："
                                    )
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.coagulant.toString() + "μL"
                                    )

                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    Text(
                                        fontSize = 16.sp,
                                        text = "状态："
                                    )
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.status
                                    )

                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        fontSize = 16.sp,
                                        text = entity.detail
                                    )

                                }
                            }

                        }


                    }, confirmButton = {
                    })
            }
        } else {
            showingDialog.value = false
        }


    }


    //删除弹窗
    if (deleteDialog.value) {
        if (entitiesList.isNotEmpty()) {
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
                            val entity = entities[selectedIndex]
                            if (entity != null) {
                                if (entity.status != EPStatus.RUNNING) {
                                    dispatch(ExperimentRecordsIntent.Delete(entity.id))
                                    if (selectedIndex > 0) {
                                        selectedIndex -= 1
                                    }
                                    deleteDialog.value = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "制胶程序还在运动中,不能删除！",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
        } else {
            deleteDialog.value = false
        }


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


