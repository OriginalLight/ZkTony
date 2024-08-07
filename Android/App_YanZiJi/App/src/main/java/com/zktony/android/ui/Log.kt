package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.DeleteDialog
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.components.ListEmptyView
import com.zktony.android.ui.components.NameTimeRangeDialog
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.getItemAttributes
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.logHeaderItems
import com.zktony.android.ui.utils.toList
import com.zktony.android.ui.viewmodel.LogViewModel
import com.zktony.android.utils.extra.itemsEqual
import com.zktony.room.entities.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:57
 */
@Composable
fun LogView(viewModel: LogViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        LogTopBar(
            viewModel = viewModel,
            selected = selected,
            entities = entities,
            navigationActions = navigationActions
        )
        // 列表
        LogListView(
            entities = entities,
            selected = selected,
            viewModel = viewModel
        )
    }
}

// 顶部导航栏
@Composable
fun LogTopBar(
    modifier: Modifier = Modifier,
    selected: List<Long>,
    entities: LazyPagingItems<Log>,
    viewModel: LogViewModel,
    navigationActions: NavigationActions
) {
    val scope = rememberCoroutineScope()
    var showDelete by remember { mutableStateOf(false) }
    var showQuery by remember { mutableStateOf(false) }
    var loadingExport by remember { mutableStateOf(false) }
    var loadingDelete by remember { mutableStateOf(false) }

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
        NameTimeRangeDialog(onDismiss = { showQuery = false }) {
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
            // 详情
            Button(
                enabled = selected.size == 1,
                onClick = {
                    scope.launch {
                        selected.firstOrNull()?.let {
                            navigationActions.navigate(Route.LOG_DETAIL + "/${it}")
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "MoreVert")
                Text(text = "详情", style = MaterialTheme.typography.bodyLarge)
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

            // 导出
            Button(
                enabled = selected.isNotEmpty() && !loadingExport,
                onClick = {}
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
fun LogListView(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Log>,
    selected: List<Long>,
    viewModel: LogViewModel
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
            LogListHeader(selected = selected, entities = entities) {
                if (it) {
                    viewModel.selectAll(entities.toList().map { p -> p.id })
                } else {
                    viewModel.selectAll(emptyList())
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(entities) { index, item ->
                    LogItem(
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
fun LogItem(
    modifier: Modifier = Modifier,
    index: Int,
    item: Log,
    selected: List<Long>,
    onSelect: (Long) -> Unit
) {
    Row(
        modifier = modifier
            .background(
                color = if (selected.contains(item.id)) MaterialTheme.colorScheme.inversePrimary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { onSelect(item.id) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected.contains(item.id),
            onCheckedChange = { onSelect(item.id) })

        item.getItemAttributes(index).forEach { it ->
            it?.let {
                Text(
                    modifier = Modifier.weight(it.second),
                    text = it.first,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// 列表头
@Composable
fun LogListHeader(
    modifier: Modifier = Modifier,
    selected: List<Long>,
    entities: LazyPagingItems<Log>,
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
            }
        )

        logHeaderItems().forEach {
            it?.let {
                Text(
                    modifier = Modifier.weight(it.second),
                    text = it.first,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}