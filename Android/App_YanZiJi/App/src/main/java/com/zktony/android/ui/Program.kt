package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.ui.components.FileChoiceDialog
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.components.ListEmptyView
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.ProgramViewModel
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.itemsEqual
import com.zktony.room.entities.Program
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProgramView(viewModel: ProgramViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        ProgramTopBar(viewModel = viewModel, selected = selected)
        // 列表
        ProgramListView(entities = entities, selected = selected, viewModel = viewModel)
    }
}

// 顶部导航栏
@Composable
fun ProgramTopBar(
    modifier: Modifier = Modifier,
    selected: List<Program>,
    viewModel: ProgramViewModel
) {
    val scope = rememberCoroutineScope()
    var showFileChoice by remember { mutableStateOf(false) }
    var fileObjectList by remember { mutableStateOf(listOf<File>()) }
    var loadingImport by remember { mutableStateOf(false) }
    var loadingExport by remember { mutableStateOf(false) }

    if (showFileChoice) {
        FileChoiceDialog(files = fileObjectList, onDismiss = { showFileChoice = false }) { file ->
            scope.launch {
                loadingImport = true
                showFileChoice = false
                viewModel.import(file)
                loadingImport = false
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(brush = zktyBrush, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            Text(text = "搜索", style = MaterialTheme.typography.bodyLarge)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 添加
            Button(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Text(text = "添加", style = MaterialTheme.typography.bodyLarge)
            }
            // 修改
            Button(
                enabled = selected.size == 1,
                onClick = { /*TODO*/ }
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                Text(text = "修改", style = MaterialTheme.typography.bodyLarge)
            }
            // 删除
            Button(
                enabled = selected.isNotEmpty(),
                onClick = { /*TODO*/ }
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                Text(text = "删除", style = MaterialTheme.typography.bodyLarge)
            }
            // 导入
            Button(onClick = {
                scope.launch {
                    viewModel.getProgramFiles()?.let {
                        fileObjectList = it
                        showFileChoice = true
                    }
                }
            }) {
                IconLoading(loading = loadingImport) {
                    Icon(imageVector = Icons.Default.ImportExport, contentDescription = "Export")
                }
                Text(text = "导入", style = MaterialTheme.typography.bodyLarge)
            }
            // 导出
            Button(
                enabled = selected.isNotEmpty(),
                onClick = {
                    scope.launch {
                        loadingExport = true
                        viewModel.export()
                        loadingExport = false
                    }
                }
            ) {
                IconLoading(loading = loadingExport) {
                    Icon(imageVector = Icons.Default.ImportExport, contentDescription = "Export")
                }
                Text(text = "导出", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// 列表
@Composable
fun ProgramListView(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Program>,
    selected: List<Program>,
    viewModel: ProgramViewModel
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
    ) {
        if (entities.itemCount > 0) {
            ProgramListHeader(selected = selected, entities = entities) {
                if (it) {
                    viewModel.selectAll(entities.toList())
                } else {
                    viewModel.selectAll(emptyList())
                }
            }
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(entities) { index, item ->
                    ProgramItem(
                        index = index,
                        item = item,
                        selected = selected,
                        viewModel = viewModel
                    )
                }
            }
        } else {
            ListEmptyView()
        }
    }
}

@Composable
fun ProgramItem(
    modifier: Modifier = Modifier,
    index: Int,
    item: Program,
    selected: List<Program>,
    viewModel: ProgramViewModel
) {
    Row(
        modifier = modifier
            .background(
                color = if (selected.contains(item)) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { viewModel.select(item) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected.contains(item), onCheckedChange = { viewModel.select(item) })
        Text(
            modifier = Modifier.weight(1f),
            text = (index + 1).toString(),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(4f),
            text = item.name,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(3f),
            text = if (item.experimentalType == 0) "转膜" else "染色",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(3f),
            text = when (item.workMode) {
                0 -> "恒压"
                1 -> "恒流"
                2 -> "恒功率"
                else -> "未知"
            },
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(2f),
            text = item.value + when (item.workMode) {
                0 -> "V"
                1 -> "A"
                2 -> "W"
                else -> "/"
            },
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(2f),
            text = item.time,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(3f),
            text = item.createTime.dateFormat("HH:mm\nyyyy-MM-dd"),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProgramListHeader(
    modifier: Modifier = Modifier,
    selected: List<Program>,
    entities: LazyPagingItems<Program>,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected.itemsEqual(entities.toList()), onCheckedChange = {
            onCheckedChange(it)
        })

        Text(
            modifier = Modifier.weight(1f),
            text = "序号",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(4f),
            text = "程序名称",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(3f),
            text = "实验类型",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(3f),
            text = "工作模式",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(2f),
            text = "数值",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(2f),
            text = "时间(min)",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.weight(3f),
            text = "创建时间",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}