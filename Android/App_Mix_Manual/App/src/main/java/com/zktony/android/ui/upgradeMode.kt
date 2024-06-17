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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
import com.zktony.android.ui.utils.getStoragePath
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.line
import com.zktony.android.utils.extra.UpgradeState
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.embeddedUpgrade
import com.zktony.android.utils.extra.embeddedVersion
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
fun upgradeMode(uiEvent: (SettingIntent) -> Unit, uiEventHome: (HomeIntent) -> Unit) {
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


    var text by remember { mutableStateOf("下位机升级") }
    var ver by remember { mutableStateOf("Unknown") }
    var binList by remember { mutableStateOf(emptyList<File>()) }

//    SideEffect {
//        scope.launch {
//            var path = getStoragePath(context, true)
//            binList = File(path).listFiles { _, name -> name.endsWith(".bin") }?.toList() ?: emptyList()
//        }
//    }


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
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.android_update),
                    contentDescription = null,
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
                    .padding(top = 100.dp, start = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                Image(
                    painter = painterResource(id = R.mipmap.master_update),
                    contentDescription = null,
                    modifier = Modifier
                        .size(180.dp)
                        .clickable {
                            scope.launch {
                                var path = getStoragePath(context, true)
                                if (!"".equals(path)) {
                                    path += "/master.bin"
                                    val file = File(path)
                                    if (!file.exists()) {
                                        text = "文件不存在"
                                        return@launch
                                    }
                                    embeddedUpgrade(file).collect {
                                        text = when (it) {
                                            is UpgradeState.Message -> it.message
                                            is UpgradeState.Success -> "升级成功"
                                            is UpgradeState.Err -> "${it.t.message}"
                                            is UpgradeState.Progress -> "升级中 ${
                                                String.format("%.2f", it.progress * 100)
                                            } %"
                                        }
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
                        }
                )
            }

            Column(
                modifier = Modifier
                    .padding(top = 100.dp, start = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.state_update),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {
                            scope.launch {
                                var path = getStoragePath(context, true)
                                if (!"".equals(path)) {
                                    path += "/state.bin"
                                    val file = File(path)
                                    if (!file.exists()) {
                                        Toast
                                            .makeText(
                                                context,
                                                "U盘不存在！",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                        return@launch
                                    }
                                    embeddedUpgrade(file).collect {
                                        text = when (it) {
                                            is UpgradeState.Message -> it.message
                                            is UpgradeState.Success -> "升级成功"
                                            is UpgradeState.Err -> "${it.t.message}"
                                            is UpgradeState.Progress -> "升级中 ${
                                                String.format(
                                                    "%.2f",
                                                    it.progress * 100
                                                )
                                            } %"
                                        }
                                    }
                                }else {
                                    Toast
                                        .makeText(
                                            context,
                                            "U盘不存在！",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }

                            }
                        }
                )
            }

        }

//        Text(text = ver, style = MaterialTheme.typography.displaySmall)
//        Button(onClick = {
//            scope.launch {
//                ver = embeddedVersion()
//            }
//        }) {
//            Text(text = "查询版本")
//        }
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

