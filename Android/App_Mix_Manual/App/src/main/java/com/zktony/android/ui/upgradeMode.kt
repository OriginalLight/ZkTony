package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
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
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.line
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

/**
 * 上下位机升级
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun upgradeMode(uiEvent: (SettingIntent) -> Unit) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()


    /**
     * 下位机升级弹窗
     */
    val upgradeLowDialog = remember { mutableStateOf(false) }

    /**
     * 下位机升级进度
     */
    var upgradeStateLow by remember {
        mutableFloatStateOf(0f)
    }

    var upgradeSweepStateCount by remember {
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
                text = "系统更新",
                fontSize = 20.sp,
                color = Color(rgb(112, 112, 112))
            )
        }


        Row(
            modifier = Modifier
                .padding(top = 100.dp, start = 150.dp)
                .background(Color.Blue)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "上位机更新",
                    fontSize = 18.sp,
                    color = Color(rgb(112, 112, 112))
                )
                Image(
                    painter = painterResource(id = R.mipmap.syjl), contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {
                            // 获取usb地址
                            val path = getStoragePath(context, true)
                            if (!"".equals(path)) {
                                try {
                                    val apkPath = "$path/zktony/apk/update.apk"
                                    uiEvent(SettingIntent.UpdateApkU(context, apkPath))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast
                                        .makeText(
                                            context,
                                            "版本信息错误！",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }

                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "U盘不存在！",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                )
            }

        }


        Row(
            modifier = Modifier
                .padding(top = 100.dp)
                .fillMaxWidth()
        ) {
            line(Color(0, 105, 5), 5f, 530f)
        }




        Row {
            Column(
                modifier = Modifier
                    .padding(top = 100.dp, start = 50.dp)
                    .background(Color.Red),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                Text(
                    text = "主控板更新",
                    fontSize = 18.sp,
                    color = Color(rgb(112, 112, 112))
                )
                Image(
                    painter = painterResource(id = R.mipmap.syjl), contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {

                        }
                )
            }

            Column(
                modifier = Modifier
                    .padding(top = 100.dp, start = 80.dp)
                    .background(Color.Red),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "状态检测板更新",
                    fontSize = 18.sp,
                    color = Color(rgb(112, 112, 112))
                )
                Image(
                    painter = painterResource(id = R.mipmap.gulecx), contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {

                        }
                )
            }

        }


    }



    if (upgradeLowDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = "导出进度", fontSize = 18.sp)
            },
            text = {
                HorizontalProgressBar(upgradeStateLow / upgradeSweepStateCount)
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