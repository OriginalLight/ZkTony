package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.ProgramAddOrUpdateViewModel
import com.zktony.android.utils.ProductUtils
import com.zktony.room.defaultProgram
import com.zktony.room.entities.Program
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProgramAddOrUpdateView(viewModel: ProgramAddOrUpdateViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val navObj by viewModel.navObj.collectAsStateWithLifecycle()
    var obj by remember(navObj) {
        mutableStateOf(
            navObj ?: defaultProgram(ProductUtils.getProgramType().firstOrNull() ?: 0)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        ProgramAddOrUpdateTopBar(navObj = navObj, obj = obj, viewModel = viewModel)
        // 程序属性列表
        ProgramAttributeList(obj = obj) {
            obj = it
        }
    }
}

// 顶部导航栏
@Composable
fun ProgramAddOrUpdateTopBar(
    modifier: Modifier = Modifier,
    navObj: Program?,
    obj: Program,
    viewModel: ProgramAddOrUpdateViewModel
) {
    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    var loading by remember { mutableStateOf(false) }

    BaseTopBar(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Reply,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = if (navObj == null) "程序添加" else "程序编辑",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Button(
            enabled = obj.canSave(),
            onClick = {
                scope.launch {
                    loading = true
                    val res = if (navObj == null) {
                        withContext(Dispatchers.IO) {
                            viewModel.add(obj)
                        }
                    } else {
                        withContext(Dispatchers.IO) {
                            viewModel.update(obj)
                        }
                    }

                    if (res) {
                        navigationActions.navigateUp()
                    }
                    loading = false
                }
            }
        ) {
            IconLoading(loading = loading) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
            }
            Text(text = "保存", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// 程序属性列表
@Composable
fun ProgramAttributeList(
    modifier: Modifier = Modifier,
    obj: Program,
    onObjChange: (Program) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ProgramAttributeItem(title = "程序名称", required = true) {
                ArgumentsInputField(
                    modifier = Modifier.size(450.dp, 48.dp),
                    value = obj.name,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                ) {
                    onObjChange(obj.copy(name = it))
                }
            }
        }

        if (ProductUtils.getProgramType().size > 1) {
            item {
                ProgramAttributeItem(title = "实验类型", required = true) {
                    SegmentedButtonTabRow(
                        modifier = Modifier.width(200.dp),
                        tabItems = listOf("转膜", "染色"), selected = obj.experimentalType
                    ) {
                        onObjChange(obj.copy(experimentalType = it))
                    }
                }
            }
        }

        item {
            ProgramAttributeItem(title = "工作模式", required = true) {
                SegmentedButtonTabRow(
                    modifier = Modifier.width(300.dp),
                    tabItems = listOf("恒压", "恒流", "恒功率"), selected = obj.experimentalMode
                ) {
                    onObjChange(obj.copy(experimentalMode = it))
                }
            }
        }

        item {
            ProgramAttributeItem(
                title = when (obj.experimentalMode) {
                    0 -> "电压"
                    1 -> "电流"
                    2 -> "功率"
                    else -> ""
                },
                required = true
            ) {
                ArgumentsInputField(
                    modifier = Modifier.size(450.dp, 48.dp),
                    value = obj.value,
                    suffix = when (obj.experimentalMode) {
                        0 -> "V"
                        1 -> "A"
                        2 -> "W"
                        else -> ""
                    }
                ) {
                    onObjChange(obj.copy(value = it))
                }
            }
        }

        if (obj.experimentalType == 0) {
            item {
                ProgramAttributeItem(title = "流速", required = true) {
                    ArgumentsInputField(
                        modifier = Modifier.size(450.dp, 48.dp),
                        value = obj.flowSpeed,
                        suffix = "mL/min"
                    ) {
                        onObjChange(obj.copy(flowSpeed = it))
                    }
                }
            }
        }

        item {
            ProgramAttributeItem(title = "时间", required = true) {
                ArgumentsInputField(
                    modifier = Modifier.size(450.dp, 48.dp),
                    value = obj.time,
                    suffix = "min"
                ) {
                    onObjChange(obj.copy(time = it))
                }
            }
        }

        item {
            ProgramAttributeItem(title = "胶种类") {
                SegmentedButtonTabRow(
                    modifier = Modifier.width(200.dp),
                    tabItems = listOf("普通胶", "梯度胶"), selected = obj.glueType
                ) {
                    onObjChange(obj.copy(glueType = it))
                }
            }
        }

        item {
            ProgramAttributeItem(title = "胶厚度") {
                SegmentedButtonTabRow(
                    modifier = Modifier.width(300.dp),
                    tabItems = listOf("0.75mm", "1.0mm", "1.5mm"), selected = obj.glueThickness
                ) {
                    onObjChange(obj.copy(glueThickness = it))
                }
            }
        }

        item {
            ProgramAttributeItem(title = "胶浓度") {
                if (obj.glueType == 0) {
                    ArgumentsInputField(
                        modifier = Modifier.size(450.dp, 48.dp),
                        value = obj.getGlueConcentrationList().first(),
                        suffix = "%"
                    ) {
                        onObjChange(obj.copy(glueConcentration = it))
                    }
                } else {
                    var min by remember { mutableStateOf(obj.getGlueConcentrationList().first()) }
                    var max by remember { mutableStateOf(obj.getGlueConcentrationList().last()) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ArgumentsInputField(
                            modifier = Modifier.size(200.dp, 48.dp),
                            value = min,
                            suffix = "%"
                        ) {
                            min = it
                            onObjChange(obj.copy(glueConcentration = "$min,$max"))
                        }
                        Text(
                            modifier = Modifier.width(50.dp),
                            text = " - ", style = TextStyle(fontSize = 18.sp),
                            textAlign = TextAlign.Center
                        )
                        ArgumentsInputField(
                            modifier = Modifier.size(200.dp, 48.dp),
                            value = max,
                            suffix = "%"
                        ) {
                            max = it
                            onObjChange(obj.copy(glueConcentration = "$min,$max"))
                        }
                    }
                }
            }
        }

        item {
            ProgramAttributeItem(title = "蛋白大小") {
                ArgumentsInputField(
                    modifier = Modifier.size(450.dp, 48.dp),
                    value = obj.proteinSize,
                    suffix = "kDa"
                ) {
                    onObjChange(obj.copy(proteinSize = it))
                }
            }
        }

        item {
            ProgramAttributeItem(title = "缓冲液类型") {
                SegmentedButtonTabRow(
                    modifier = Modifier.width(200.dp),
                    tabItems = listOf("厂家", "其他"), selected = obj.bufferType
                ) {
                    onObjChange(obj.copy(bufferType = it))
                }
            }
        }
    }
}


@Composable
fun ProgramAttributeItem(
    modifier: Modifier = Modifier,
    title: String,
    required: Boolean = false,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (required) {
                Text(
                    text = "*",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }
            Text(text = title, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal))
        }
        content()
    }
}