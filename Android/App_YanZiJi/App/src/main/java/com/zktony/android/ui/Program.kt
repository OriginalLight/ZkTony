package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.DeleteDialog
import com.zktony.android.ui.components.FileChoiceDialog
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.components.ListEmptyView
import com.zktony.android.ui.components.ProgramQueryDialog
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.ui.viewmodel.ProgramViewModel
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.itemsEqual
import com.zktony.room.entities.Program
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        ProgramTopBar(viewModel = viewModel, selected = selected, entities = entities)
        // 列表
        ProgramListView(entities = entities, selected = selected, viewModel = viewModel)
    }
}

// 顶部导航栏
@Composable
fun ProgramTopBar(
    modifier: Modifier = Modifier,
    selected: List<Long>,
    entities: LazyPagingItems<Program>,
    viewModel: ProgramViewModel
) {
    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    var showFileChoice by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showQuery by remember { mutableStateOf(false) }
    var fileObjectList by remember { mutableStateOf(listOf<File>()) }
    var loadingImport by remember { mutableStateOf(false) }
    var loadingExport by remember { mutableStateOf(false) }
    var loadingDelete by remember { mutableStateOf(false) }

    if (showFileChoice) {
        FileChoiceDialog(files = fileObjectList, onDismiss = { showFileChoice = false }) { file ->
            scope.launch {
                loadingImport = true
                showFileChoice = false
                withContext(Dispatchers.IO) {
                    viewModel.import(file)
                }
                loadingImport = false
            }
        }
    }

    if (showDelete) {
        DeleteDialog(onDismiss = { showDelete = false }) {
            scope.launch {
                loadingDelete = true
                withContext(Dispatchers.IO) {
                    viewModel.delete()
                }
                loadingDelete = false
            }
        }
    }

    if (showQuery) {
        ProgramQueryDialog(onDismiss = { showQuery = false }) {
            scope.launch {
                showQuery = false
                viewModel.search(it)
            }
        }
    }

    BaseTopBar(modifier = modifier) {
        Button(onClick = { showQuery = true }) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            Text(text = "搜索", style = MaterialTheme.typography.bodyLarge)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 添加
            Button(onClick = { navigationActions.navigate(Route.PROGRAM_ADD_OR_UPDATE + "/-1") }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Text(text = "添加", style = MaterialTheme.typography.bodyLarge)
            }
            // 修改
            Button(
                enabled = selected.size == 1,
                onClick = {
                    scope.launch {
                        selected.firstOrNull()?.let {
                            navigationActions.navigate(Route.PROGRAM_ADD_OR_UPDATE + "/${it}")
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                Text(text = "编辑", style = MaterialTheme.typography.bodyLarge)
            }
            // 删除
            Button(
                enabled = selected.isNotEmpty(),
                onClick = { showDelete = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                IconLoading(loading = loadingDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                }
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
                        viewModel.export(entities.toList().filter { p -> selected.contains(p.id) })
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
    selected: List<Long>,
    viewModel: ProgramViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (entities.itemCount > 0) {
            ProgramListHeader(selected = selected, entities = entities) {
                if (it) {
                    viewModel.selectAll(entities.toList().map { p -> p.id })
                } else {
                    viewModel.selectAll(emptyList())
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(entities) { index, item ->
                    ProgramItem(
                        index = index,
                        item = item,
                        selected = selected
                    ) {
                        viewModel.select(it)
                    }
                }
            }
        } else {
            ListEmptyView()
        }
    }
}

// 列表项
@Composable
fun ProgramItem(
    modifier: Modifier = Modifier,
    index: Int,
    item: Program,
    selected: List<Long>,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(
                color = if (selected.contains(item.id)) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { onSelect(item.id) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected.contains(item.id),
                onCheckedChange = { onSelect(item.id) })

            listOf(
                Pair((index + 1).toString(), 1f),
                Pair(item.name, 4f),
                Pair(if (item.experimentalType == 0) "转膜" else "染色", 2f),
                Pair(
                    when (item.experimentalMode) {
                        0 -> "恒压"
                        1 -> "恒流"
                        2 -> "恒功率"
                        else -> "未知"
                    }, 2f
                ),
                Pair(
                    item.value + when (item.experimentalMode) {
                        0 -> "V"
                        1 -> "A"
                        2 -> "W"
                        else -> "/"
                    }, 2f
                ),
                Pair(item.time, 2f),
                Pair(item.createTime.dateFormat("HH:mm\nyyyy-MM-dd"), 3f),
            ).forEach {
                Text(
                    modifier = Modifier.weight(it.second),
                    text = it.first,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (expanded) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.experimentalType == 0) {
                    Text(
                        text = item.flowSpeed + "mL/min",
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = if (item.glueType == 0) "普通胶" else "梯度胶",
                    fontSize = 18.sp
                )

                Text(
                    text = item.getGlueConcentrationStr(),
                    fontSize = 18.sp
                )

                Text(
                    text = when (item.glueThickness) {
                        0 -> "0.75mm"
                        1 -> "1.0mm"
                        2 -> "1.5mm"
                        else -> "/"
                    },
                    fontSize = 18.sp
                )

                Text(
                    text = item.proteinSize + "kDa",
                    fontSize = 18.sp
                )

                Text(
                    text = when (item.bufferType) {
                        0 -> "厂家缓冲液"
                        1 -> "其他缓冲液"
                        else -> "/"
                    },
                    fontSize = 18.sp
                )
            }
        }
    }
}

// 列表头
@Composable
fun ProgramListHeader(
    modifier: Modifier = Modifier,
    selected: List<Long>,
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
        Checkbox(
            checked = selected.itemsEqual(entities.toList().map { it.id }),
            onCheckedChange = {
                onCheckedChange(it)
            })

        listOf(
            Pair("序号", 1f),
            Pair("程序名称", 4f),
            Pair("实验类型", 2f),
            Pair("工作模式", 2f),
            Pair("数值", 2f),
            Pair("时间(min)", 2f),
            Pair("创建时间", 3f),
            Pair("操作", 1f)
        ).forEach {
            Text(
                modifier = Modifier.weight(it.second),
                text = it.first,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}