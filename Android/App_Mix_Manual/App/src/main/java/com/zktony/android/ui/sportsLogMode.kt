package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.entities.SportsLog
import com.zktony.android.ui.brogressbar.HorizontalProgressBar
import com.zktony.android.ui.components.TableTextBody
import com.zktony.android.ui.components.TableTextHead
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.ceil

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun sportsLogMode(
    uiEvent: (SettingIntent) -> Unit,
    sportsLogEntitiesDis: LazyPagingItems<SportsLog>,
    entitiesListDis: List<SportsLog>,
    entitiesList: List<SportsLog>,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var selectedIndex by remember { mutableStateOf(0) }
    //	定义列宽
    val cellWidthList = arrayListOf(70, 115, 300)

    /**
     * 导出弹窗
     */
    val exportDialog = remember { mutableStateOf(false) }

    /**
     * 导出进度
     */
    var exportSweepState by remember {
        mutableFloatStateOf(0f)
    }

    var exportSweepStateCount by remember {
        mutableIntStateOf(0)
    }



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
                text = "运行日志",
                fontSize = 20.sp,
                color = Color(rgb(112, 112, 112))
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(800.dp)
                .padding(top = 20.dp, start = 25.dp)
        ) {


            stickyHeader {
                Row(
                    Modifier.background(Color(rgb(0, 105, 52)))
                ) {
                    TableTextHead(text = "序号", width = cellWidthList[0])
                    TableTextHead(text = "时        间", width = cellWidthList[1])
                    TableTextHead(text = "文件名称", width = cellWidthList[2])
                }
            }

            itemsIndexed(sportsLogEntitiesDis) { index, item ->
                val selected = item == sportsLogEntitiesDis[selectedIndex]
                Row(
                    modifier = Modifier
                        .background(
                            if (index % 2 == 0) Color(
                                rgb(
                                    229, 229, 229
                                )
                            ) else Color.White
                        )
                        .clickable(onClick = {
                            selectedIndex = index
                        })
                ) {
                    TableTextBody(text = (index + 1).toString(), width = cellWidthList[0], selected)
                    TableTextBody(
                        text = "" + item.createTime.dateFormat("yyyy-MM-dd"),
                        width = cellWidthList[1],
                        selected
                    )
                    TableTextBody(
                        text = item.logName, width = cellWidthList[2], selected
                    )
                }
            }

        }

        Row(
            modifier = Modifier
                .padding(top = 30.dp)
        ) {
            Button(
                modifier = Modifier
                    .padding(start = 400.dp)
                    .width(100.dp)
                    .height(50.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color(rgb(0, 105, 52))
                ),
                shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
                onClick = {
                    scope.launch {
                        var path = getStoragePath(context, true)
                        if ("" != path) {
                            if (entitiesList.isNotEmpty()) {
                                val entity = sportsLogEntitiesDis[selectedIndex]
                                if (entity != null) {
                                    try {
                                        exportDialog.value = true

                                        val release = Build.VERSION.RELEASE
                                        if (release == "6.0.1") {
                                            //Android6.0.1系统是迈冲
                                            if (path != null) {
                                                path = path.replace("storage", "/mnt/media_rw")
                                            }
                                        }
                                        path += "/${entity.logName}.txt"

                                        val file = File(path)
                                        if (!file.exists()) {
                                            if (file.createNewFile()) {
                                                FileWriter(path, true).use { writer ->
                                                    writer.write(entity.createTime.dateFormat("yyyy-MM-dd") + "\n")
                                                    delay(500)
                                                    exportSweepStateCount = entitiesList.size
                                                    entitiesList.forEach {
                                                        if (entity.logName == it.logName) {
                                                            writer.append("使用模块：${it.startModel}\n")
                                                            writer.append("运行数据：${it.detail}\n")
                                                        }
                                                        delay(500)
                                                        exportSweepState += 1f
                                                    }
                                                }
                                                exportSweepState = 0f
                                                exportDialog.value = false
                                                Toast.makeText(
                                                    context,
                                                    "导出完成",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }else{
                                                Toast.makeText(
                                                    context,
                                                    "创建文件失败,请重试!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }else{
                                            FileWriter(path, true).use { writer ->
                                                writer.write(entity.createTime.dateFormat("yyyy-MM-dd") + "\n")
                                                delay(500)
                                                exportSweepStateCount = entitiesList.size
                                                entitiesList.forEach {
                                                    if (entity.logName == it.logName) {
                                                        writer.append("使用模块：${it.startModel}\n")
                                                        writer.append("运行数据：${it.detail}\n")
                                                    }
                                                    delay(500)
                                                    exportSweepState += 1f
                                                }
                                            }
                                            exportSweepState = 0f
                                            exportDialog.value = false
                                            Toast.makeText(
                                                context,
                                                "导出完成!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
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
                Text(text = "导    出", fontSize = 18.sp)
            }
        }


    }



    if (exportDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = "导出进度", fontSize = 18.sp)
            },
            text = {
                HorizontalProgressBar(exportSweepState / exportSweepStateCount)
            }, confirmButton = {

            }, dismissButton = {

            })
    }


}

private fun <T, K> Iterable<T>.distinctBy(selector: (T) -> K): List<T> {
    val set = HashSet<K>()
    val list = ArrayList<T>()
    for (e in this) {
        val key = selector(e)
        if (set.add(key))
            list.add(e)
    }
    return list
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
            } else if (!isUsb == sd) { //sd
                assert(file != null)
                path = file.getAbsolutePath()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return path
}