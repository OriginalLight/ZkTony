package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.Role
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.DeleteDialog
import com.zktony.android.ui.components.DropDownBox
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.components.ListEmptyView
import com.zktony.android.ui.components.PasswordClearDialog
import com.zktony.android.ui.components.UserAddDialog
import com.zktony.android.ui.components.NameQueryDialog
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.ui.viewmodel.SettingsUserManagementViewModel
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.itemsEqual
import com.zktony.room.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsUserManagementView(viewModel: SettingsUserManagementViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsUserManagementTopBar(
            selected = selected,
            entities = entities.toList(),
            navigationActions = navigationActions,
            viewModel = viewModel
        )

        // 用户列表
        SettingsUserListView(
            entities = entities,
            selected = selected,
            viewModel = viewModel
        )
    }
}

// 顶部导航栏
@Composable
fun SettingsUserManagementTopBar(
    modifier: Modifier = Modifier,
    selected: List<Long>,
    entities: List<User>,
    navigationActions: NavigationActions,
    viewModel: SettingsUserManagementViewModel
) {
    val scope = rememberCoroutineScope()
    var showQuery by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showClear by remember { mutableStateOf(false) }
    var loadingDelete by remember { mutableStateOf(false) }
    var loadingClear by remember { mutableStateOf(false) }

    if (showQuery) {
        NameQueryDialog(onDismiss = { showQuery = false }) {
            showQuery = false
            viewModel.search(it)
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

    if (showAdd) {
        UserAddDialog(onDismiss = { showAdd = false }) {
            viewModel.add(it)
        }
    }

    if (showClear) {
        PasswordClearDialog(onDismiss = { showClear = false }) {
            scope.launch {
                loadingClear = true
                withContext(Dispatchers.IO) {
                    viewModel.clearPassword(entities.firstOrNull { p ->
                        p.id == (selected.firstOrNull() ?: 0)
                    } ?: User())
                }
                loadingClear = false
            }
        }
    }

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
                text = stringResource(id = R.string.app_user_management),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(onClick = { showQuery = true }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                Text(text = "搜索", style = MaterialTheme.typography.bodyLarge)
            }

            // 添加
            Button(onClick = { showAdd = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Text(text = "添加", style = MaterialTheme.typography.bodyLarge)
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

            // 删除
            Button(
                enabled = selected.size == 1,
                onClick = { showClear = true }
            ) {
                IconLoading(loading = loadingClear) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
                Text(text = "清除密码", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// 用户列表
@Composable
fun SettingsUserListView(
    modifier: Modifier = Modifier,
    selected: List<Long>,
    entities: LazyPagingItems<User>,
    viewModel: SettingsUserManagementViewModel
) {
    val scope = rememberCoroutineScope()

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
            UserListHeader(selected = selected, entities = entities) {
                if (it) {
                    viewModel.selectAll(entities.toList().map { p -> p.id })
                } else {
                    viewModel.selectAll(emptyList())
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(entities) { index, item ->
                    UserItem(index = index, item = item, selected = selected,
                        onSelect = { viewModel.select(it) },
                        onUpdate = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    viewModel.update(it)
                                }
                            }
                        }
                    )
                }
            }
        } else {
            ListEmptyView()
        }
    }
}

// 列表头
@Composable
fun UserListHeader(
    modifier: Modifier = Modifier,
    selected: List<Long>,
    entities: LazyPagingItems<User>,
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
        Checkbox(checked = selected.itemsEqual(entities.toList().map { it.id }), onCheckedChange = {
            onCheckedChange(it)
        })

        listOf(
            Pair("序号", 1f),
            Pair("用户名", 4f),
            Pair("用户角色", 3f),
            Pair("是否启用", 3f),
            Pair("上次登录时间", 3f)
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

// 列表项
@Composable
fun UserItem(
    modifier: Modifier = Modifier,
    index: Int,
    item: User,
    selected: List<Long>,
    onSelect: (Long) -> Unit,
    onUpdate: (User) -> Unit
) {
    Row(
        modifier = modifier
            .background(
                color = if (selected.contains(item.id)) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surfaceVariant,
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

        listOf(
            Pair((index + 1).toString(), 1f),
            Pair(item.name, 4f),
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
            modifier = Modifier.weight(3f),
            contentAlignment = Alignment.Center,
        ) {
            val roles = Role.getLowerRole(AuthUtils.getRole())
            val roleIndex = roles.indexOfFirst { p -> p.name == item.role }

            DropDownBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp),
                selected = roleIndex,
                options = roles.map { stringResource(id = it.resId) },
            ) {
                val role = roles.getOrNull(it) ?: Role.USER
                onUpdate(item.copy(role = role.name))
            }
        }

        Box(
            modifier = Modifier.weight(3f),
            contentAlignment = Alignment.Center
        ) {
            Switch(checked = item.enable, onCheckedChange = {
                onUpdate(item.copy(enable = it))
            })
        }

        Text(
            modifier = Modifier.weight(3f),
            text = item.lastLoginTime.dateFormat("HH:mm\nyyyy-MM-dd"),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}