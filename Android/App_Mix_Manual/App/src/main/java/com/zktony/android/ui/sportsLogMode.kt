package com.zktony.android.ui

import android.content.Context
import android.graphics.Color.rgb
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.zktony.android.ui.utils.getStoragePath
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.utils.extra.DownloadState
import com.zktony.android.utils.extra.copyTo
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.ceil

@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun sportsLogMode(
    uiEvent: (SettingIntent) -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var txtList =
        File("sdcard/Download").listFiles { _, name -> name.endsWith(".txt") }?.toList()
            ?: emptyList()


    var selectedName by remember { mutableStateOf("") }
    //	定义列宽
    val cellWidthList = arrayListOf(100, 400)


    var selectedFile by remember { mutableStateOf<File?>(null) }

    var index = 0
    println("index===$index")
//    var index by remember { mutableStateOf(0) }

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
                    TableTextHead(text = "文件名称", width = cellWidthList[1])
                }
            }

            itemsIndexed(txtList) { index, file ->

                val selected = file.name == selectedName

                Row(
                    modifier = Modifier
                        .background(
                            if (txtList.size % 2 == 0) Color(
                                rgb(
                                    229, 229, 229
                                )
                            ) else Color.White
                        )
                        .clickable(onClick = {
                            selectedFile = file
                            selectedName = file.name
                        })
                ) {
                    TableTextBody(
                        text = (index + 1).toString(), width = cellWidthList[0], selected
                    )
                    TableTextBody(
                        text = file.name, width = cellWidthList[1], selected
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
                        try {
                            var path = getStoragePath(context, true)
                            if ("" != path) {
                                if (selectedFile != null) {
                                    val targetDir = File(path + "/${selectedFile!!.name}")
                                    if (!targetDir.exists()) {
                                        if (targetDir.createNewFile()) {

                                            FileInputStream(selectedFile).use { input ->
                                                input.copyTo(FileOutputStream(targetDir)) {
                                                }
                                            }
                                            Toast.makeText(
                                                context,
                                                "导出完成",
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
                                        FileInputStream(selectedFile).use { input ->
                                            input.copyTo(FileOutputStream(targetDir)) {
                                            }
                                        }
                                        Toast.makeText(
                                            context,
                                            "导出完成",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "未选择文件!",
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
                        } catch (e: IOException) {
                            Toast.makeText(
                                context,
                                "日志导出异常:${e.printStackTrace()}!",
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
