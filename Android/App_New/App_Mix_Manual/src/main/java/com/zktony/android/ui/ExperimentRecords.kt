package com.zktony.android.ui

import android.content.Context
import android.os.storage.StorageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.ui.components.HomeAppBar
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
import java.lang.reflect.Method
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.inject.Inject
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

    Column {
        HomeAppBar(page) { navigation() }
//        ProgramAppBar(entities.toList(), selected, page, viewModel::dispatch) { navigation() }
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


//    dispatch(
//        ExperimentRecordsIntent.Insert(
//            20.0,
//            40.0,
//            "1.0",
//            95.0,
//            9.5,
//            1,
//            "成功",
//            ""
//        )
//    )

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
    val cellWidthList = arrayListOf(70, 100, 100, 130, 90, 120)
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
                TableText(text = "日期", width = cellWidthList[5])
                TableText(text = "浓度", width = cellWidthList[2])
                TableText(text = "厚度", width = cellWidthList[3])
                TableText(text = "制胶数量", width = cellWidthList[1])
                TableText(text = "状态", width = cellWidthList[4])
            }
        }
        itemsIndexed(entities) { index, item ->
            val selected = item == entities[selectedIndex]



            Row(
                modifier = Modifier
                    .background(if (selected) Color.Black else Color.White)
                    .clickable(onClick = {
                        selectedIndex = index

                    })
            ) {
                TableText(text = "" + item.id, width = cellWidthList[0])
                TableText(
                    text = "" + item.createTime.dateFormat("yyyy-MM-dd"),
                    width = cellWidthList[5]
                )
                TableText(
                    text = "" + item.startRange + "%~" + item.endRange + "%",
                    width = cellWidthList[2]
                )
                TableText(text = item.thickness + "mm", width = cellWidthList[3])

                TableText(text = "" + item.number, width = cellWidthList[1])
                TableText(text = item.detail, width = cellWidthList[4])
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
                    .padding(start = 50.dp)
                    .width(100.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {
                    showingDialog.value = true
                }
            ) {
                Text(text = "查    看", fontSize = 18.sp)
            }

            Button(modifier = Modifier
                .padding(start = 100.dp)
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
                    .padding(start = 100.dp)
                    .width(100.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {

                    var path = getStoragePath(context, true)
                    if (!"".equals(path)) {
                        val entity = entities[selectedIndex]
                        if (entity != null) {
                            File(path + "/zktony/test.txt").writeText(
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
                    }else{
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

    //新建和打开弹窗
    if (showingDialog.value) {
        val entity = entities[selectedIndex]
        if (entity != null)
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
                    val entity = entities[selectedIndex]
                    if (entity != null) {
                        dispatch(ExperimentRecordsIntent.Delete(entity.id))
                    }
                    deleteDialog.value = false
                }) {
                    Text(text = "确认")
                }
            }, dismissButton = {
                TextButton(onClick = { deleteDialog.value = false }) {
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


