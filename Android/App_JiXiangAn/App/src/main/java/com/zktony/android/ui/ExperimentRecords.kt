package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.window.Dialog
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
import com.zktony.android.ui.utils.getStoragePath
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Method
import kotlin.reflect.KFunction1

/**
 * 实验记录
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExperimentRecords(viewModel: ExperimentRecordsViewModel) {
    val startTime = System.currentTimeMillis()
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
            HomeAppBar(page, false) { navigation() }
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

    val endTime = System.currentTimeMillis()

    Log.i("exper", "exper消耗的时间${endTime - startTime}毫秒")
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun experimentList(
    entities: LazyPagingItems<ExperimentRecord>,
    entitiesList: List<ExperimentRecord>,
    dispatch: KFunction1<ExperimentRecordsIntent, Unit>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /**
     * 新建和打开的弹窗
     */
    val showingDialog = remember { mutableStateOf(false) }


    /**
     * 删除的弹窗x
     */
    val deleteDialog = remember { mutableStateOf(false) }


    /**
     *  导出按钮
     */
    var export by remember { mutableStateOf(true) }

    var selectedIndex by remember { mutableStateOf(0) }


    //	定义列宽val cellWidthList = arrayListOf(70, 130, 140, 70, 140)
    val cellWidthList = arrayListOf(80, 100, 70, 145, 90, 145)
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
                .padding(5.dp)
        ) {
            stickyHeader {
                Row(Modifier.background(Color(rgb(0, 105, 52)))) {
                    TableTextHead(text = "序  号", width = cellWidthList[0])
                    TableTextHead(text = "日  期", width = cellWidthList[5])
                    TableTextHead(text = "浓  度", width = cellWidthList[3])
                    TableTextHead(text = "数  量", width = cellWidthList[1])
                    TableTextHead(text = "状  态", width = cellWidthList[4])
                }
            }

            itemsIndexed(entities) { index, item ->
                val selected = item == entities[selectedIndex]
                Row(
                    modifier = Modifier
                        .background(
                            if (index % 2 == 0) Color(
                                rgb(
                                    239,
                                    239,
                                    239
                                )
                            ) else Color.White
                        )
                        .clickable(onClick = {
                            selectedIndex = index
                        })
                ) {
                    TableTextBody(text = (index + 1).toString(), width = cellWidthList[0], selected)
                    TableTextBody(
                        text = item.createTime.dateFormat("yyyy/MM/dd"),
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
                .background(Color(229, 229, 229))
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
//                        for (i in 1..100) {
//                            dispatch(
//                                ExperimentRecordsIntent.Insert(
//                                    0.0,
//                                    0.0,
//                                    "",
//                                    0,
//                                    0.0,
//                                    0,
//                                    "",
//                                    ""
//                                )
//                            )
//                        }
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
                        .height(50.dp),
                    enabled = export, colors = ButtonDefaults.buttonColors(
                        containerColor = Color(rgb(0, 105, 52))
                    ),
                    shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                    onClick = {
                        scope.launch {
                            var path = getStoragePath(context, true)
                            if (!"".equals(path)) {
                                if (entitiesList.isNotEmpty()) {
                                    val entity = entities[selectedIndex]
                                    if (entity != null) {
                                        try {
                                            export = false
                                            path += "/exp${System.currentTimeMillis()}.txt"

                                            val file = File(path)
                                            if (!file.exists()) {
                                                if (file.createNewFile()) {
                                                    FileWriter(path, true).use { writer ->
                                                        writer.write(
                                                            "实验记录:" + ",日期：" + entity.createTime.dateFormat(
                                                                "yyyy/MM/dd"
                                                            )
                                                                    + ",开始浓度:" + entity.startRange.toString()
                                                                    + ",结束浓度:" + entity.endRange.toString()
                                                                    + ",常用厚度:" + entity.thickness
                                                                    + ",促凝剂体积:" + entity.coagulant.toString()
                                                                    + ",胶液体积:" + entity.volume.toString()
                                                                    + ",制胶数量:" + entity.number.toString()
                                                                    + ",状态:" + entity.status
                                                                    + ",状态详情:" + entity.detail
                                                        )
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
                                                    writer.write(
                                                        "实验记录:" + ",日期：" + entity.createTime.dateFormat(
                                                            "yyyy/MM/dd"
                                                        )
                                                                + ",开始浓度:" + entity.startRange.toString()
                                                                + ",结束浓度:" + entity.endRange.toString()
                                                                + ",常用厚度:" + entity.thickness
                                                                + ",促凝剂体积:" + entity.coagulant.toString()
                                                                + ",胶液体积:" + entity.volume.toString()
                                                                + ",制胶数量:" + entity.number.toString()
                                                                + ",状态:" + entity.status
                                                                + ",状态详情:" + entity.detail
                                                    )
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "导出成功!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "导出异常,请重试!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } finally {
                                            export = true
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "未检测到USB!",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
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
                                        fontSize = 20.sp,
                                        text = "日期："
                                    )
                                    Text(
                                        fontSize = 20.sp,
                                        text = entity.createTime.dateFormat("yyyy/MM/dd HH:mm:ss")
                                    )

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
                                    Text(
                                        fontSize = 20.sp,
                                        text = entity.startRange.toString() + "%"
                                    )
                                    Text(
                                        fontSize = 20.sp,
                                        text = "~"
                                    )
                                    Text(
                                        fontSize = 20.sp,
                                        text = entity.endRange.toString() + "%"
                                    )

                                }
                            }

                            item {
                                Row {
                                    Text(
                                        fontSize = 20.sp,
                                        text = "厚度："
                                    )
                                    Text(
                                        fontSize = 20.sp,
                                        text = entity.thickness + "mm"
                                    )

                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    Text(
                                        fontSize = 20.sp,
                                        text = "胶液体积："
                                    )
                                    Text(
                                        fontSize = 20.sp,
                                        text = entity.volume.toString() + "mL"
                                    )

                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    Text(
                                        fontSize = 20.sp,
                                        text = "促凝剂体积："
                                    )
                                    Text(
                                        fontSize = 20.sp,
                                        text = entity.coagulant.toString() + "μL"
                                    )

                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    Text(
                                        fontSize = 20.sp,
                                        text = "状态："
                                    )
                                    Text(
                                        fontSize = 20.sp,
                                        text = entity.status
                                    )

                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        fontSize = 20.sp,
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
        Dialog(onDismissRequest = {}) {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .padding(30.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column {
                        Text(
                            fontSize = 20.sp,
                            text = "是否确认删除!"
                        )

                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            modifier = Modifier
                                .width(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(rgb(0, 105, 52))
                            ),
                            onClick = {
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
                                            "制胶程序还在运动中,无法删除!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            }) {
                            Text(fontSize = 18.sp, text = "确   认")
                        }

                        Button(
                            modifier = Modifier
                                .padding(start = 40.dp)
                                .width(100.dp),
                            border = BorderStroke(1.dp, Color.Gray),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            onClick = {
                                deleteDialog.value = false
                            }) {
                            Text(fontSize = 18.sp, text = "取   消", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

}




