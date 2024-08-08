package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.ListEmptyView
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.errorLogHeaderItems
import com.zktony.android.ui.utils.getItemAttributes
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.viewmodel.SettingsErrorLogViewModel
import com.zktony.room.entities.ErrorLog

@Composable
fun SettingsErrorLogView(viewModel: SettingsErrorLogViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val entities = viewModel.entities.collectAsLazyPagingItems()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsErrorLogTopBar(navigationActions = navigationActions)
        // 列表
        SettingsErrorLogList(entities = entities)
    }
}

// 顶部导航栏
@Composable
fun SettingsErrorLogTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions
) {
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
                text = "故障日志",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// 列表
@Composable
fun SettingsErrorLogList(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<ErrorLog>
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
            ErrorLogListHeader()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(entities) { index, item ->
                    ErrorLogItem(index = index, item = item)
                }
            }
        } else {
            ListEmptyView()
        }
    }
}

// 列表项
@Composable
fun ErrorLogItem(
    modifier: Modifier = Modifier,
    index: Int,
    item: ErrorLog,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
fun ErrorLogListHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        errorLogHeaderItems().forEach {
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