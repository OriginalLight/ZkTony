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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.ceil

/**
 * 上下位机升级
 */
@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun upgradeMode(uiEvent: (SettingIntent) -> Unit, uiEventHome: (HomeIntent) -> Unit) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()


    /**
     * 主控板升级弹窗
     */
    val masterDialog = remember { mutableStateOf(false) }

    val stateDialog = remember { mutableStateOf(false) }

    val apkDialog = remember { mutableStateOf(false) }


    var text by remember { mutableStateOf("") }

    var showButton by remember { mutableStateOf(true) }

    /**
     * bin文件列表
     */
    var masterList by remember { mutableStateOf(emptyList<File>()) }

    var stateList by remember { mutableStateOf(emptyList<File>()) }

    var apkList by remember { mutableStateOf(emptyList<File>()) }

    LaunchedEffect(text) {
        if (text.contains("成功")) {
            uiEventHome(HomeIntent.WaitTimeRinse)
            uiEventHome(HomeIntent.Heartbeat)
            text = ""
            masterDialog.value = false
            showButton = true
        } else if (text.contains("失败") || text.contains("超时") || text.contains("未知")) {
            showButton = true
            text = ""
        }
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
                            scope.launch {
                                var path = getStoragePath(context, true)
                                if (!"".equals(path)) {
                                    path += "/zktony/apk"
                                    apkList =
                                        File(path)
                                            .listFiles { _, name -> name.endsWith(".apk") }
                                            ?.toList() ?: emptyList()
                                    if (apkList.isNotEmpty()) {
                                        apkDialog.value = true
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "没有更新文件！",
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
                                    path += "/zktony/apk/master"
                                    masterList =
                                        File(path)
                                            .listFiles { _, name -> name.endsWith(".bin") }
                                            ?.toList() ?: emptyList()
                                    if (masterList.isNotEmpty()) {
                                        masterDialog.value = true
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "没有更新文件！",
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
                                    path += "/zktony/apk/state"
                                    stateList =
                                        File(path)
                                            .listFiles { _, name -> name.endsWith(".bin") }
                                            ?.toList() ?: emptyList()
                                    if (stateList.isNotEmpty()) {
                                        stateDialog.value = true
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "没有更新文件！",
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



    if (masterDialog.value) {
        Dialog(onDismissRequest = {
        }) {
            ElevatedCard {
                var selectedFile by remember { mutableStateOf<File?>(null) }
                Column(
                    modifier = Modifier
                        .padding(30.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(masterList) { file ->
                            FileItem(file, selectedFile) {
                                selectedFile = it
                            }
                        }
                        item {
                            if (showButton) {
                                Column {
                                    Row {
                                        Button(
                                            modifier = Modifier
                                                .width(100.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(rgb(0, 105, 52))
                                            ),
                                            onClick = {
                                                scope.launch {
                                                    if (selectedFile != null) {
                                                        showButton = false
                                                        uiEventHome(HomeIntent.StopWaitTimeRinse)
                                                        uiEventHome(HomeIntent.StopHeartbeat)
                                                        delay(200)
                                                        embeddedUpgrade(
                                                            selectedFile!!,
                                                            "master",
                                                            "zkty"
                                                        ).collect {
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
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "未选择更新程序!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                            }) {
                                            Text(fontSize = 18.sp, text = "确认")
                                        }

                                        Button(
                                            modifier = Modifier
                                                .padding(start = 250.dp)
                                                .width(100.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(rgb(0, 105, 52))
                                            ),
                                            onClick = {
                                                masterDialog.value = false
                                            }) {
                                            Text(fontSize = 18.sp, text = "取消")
                                        }
                                    }
                                    Row(modifier = Modifier.padding(top = 10.dp, start = 180.dp)) {
                                        Text(text = "升级中请勿关机!", fontSize = 18.sp)
                                    }
                                }

                            } else {
                                Row {
                                    Text(text = text, style = MaterialTheme.typography.displaySmall)
                                }
                            }

                        }
                    }
                }

            }
        }

    }

    if (stateDialog.value) {
        Dialog(onDismissRequest = {
        }) {
            ElevatedCard {
                var selectedFile by remember { mutableStateOf<File?>(null) }
                Column(
                    modifier = Modifier
                        .padding(30.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stateList) { file ->
                            FileItem(file, selectedFile) {
                                selectedFile = it
                            }
                        }
                        item {
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
                                        scope.launch {
                                            if (selectedFile != null) {
                                                embeddedUpgrade(
                                                    selectedFile!!,
                                                    "state",
                                                    "led"
                                                ).collect {
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
                                                    Toast.makeText(
                                                        context,
                                                        text,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    if (text == "升级成功") {
                                                        stateDialog.value = false
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "未选择更新程序!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                    }) {
                                    Text(fontSize = 18.sp, text = "确认")
                                }

                                Button(
                                    modifier = Modifier
                                        .padding(start = 250.dp)
                                        .width(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    onClick = {
                                        stateDialog.value = false
                                    }) {
                                    Text(fontSize = 18.sp, text = "取消")
                                }
                            }
                        }

                    }

                }
            }
        }

    }


    if (apkDialog.value) {
        Dialog(onDismissRequest = {
        }) {
            ElevatedCard {
                var selectedFile by remember { mutableStateOf<File?>(null) }
                Column(
                    modifier = Modifier
                        .padding(30.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(apkList) { file ->
                            FileItem(file, selectedFile) {
                                selectedFile = it
                            }
                        }
                        item {
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
                                        scope.launch {
                                            if (selectedFile != null) {
                                                uiEvent(
                                                    SettingIntent.UpdateApkU(
                                                        context,
                                                        selectedFile!!.path
                                                    )
                                                )
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "未选择更新程序!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        }

                                    }) {
                                    Text(fontSize = 18.sp, text = "确认")
                                }

                                Button(
                                    modifier = Modifier
                                        .padding(start = 250.dp)
                                        .width(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(rgb(0, 105, 52))
                                    ),
                                    onClick = {
                                        apkDialog.value = false
                                    }) {
                                    Text(fontSize = 18.sp, text = "取消")
                                }
                            }
                        }

                    }

                }
            }
        }

    }

}

@Composable
fun FileItem(
    file: File,
    selectedFile: File?,
    onFileClick: (File) -> Unit
) {
    val isSelected = file == selectedFile
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFileClick(file) }
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Name: ${file.name}",
                color = if (isSelected) Color.Red else Color.Black
            )
            Text(
                text = "Path: ${file.path}",
                color = if (isSelected) Color.Red else Color.Black
            )
        }
    }
}