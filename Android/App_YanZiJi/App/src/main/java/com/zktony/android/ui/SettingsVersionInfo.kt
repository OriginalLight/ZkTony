package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.data.Role
import com.zktony.android.ui.components.FileChoiceDialog
import com.zktony.android.ui.components.RequirePermission
import com.zktony.android.ui.components.TopBarRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsVersionInfoViewModel
import com.zktony.android.utils.extra.installApp
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsVersionInfoView(viewModel: SettingsVersionInfoViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val versionList by viewModel.versionList.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsVersionInfoTopBar(navigationActions = navigationActions)
        // 版本信息列表
        SettingsVersionInfoList(versionList = versionList, viewModel = viewModel)
    }
}

// 顶部导航栏
@Composable
fun SettingsVersionInfoTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions,
) {
    TopBarRow(modifier = modifier) {
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
                text = stringResource(id = R.string.app_version_info),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// 版本信息列表
@Composable
fun SettingsVersionInfoList(
    modifier: Modifier = Modifier,
    versionList: List<String>,
    viewModel: SettingsVersionInfoViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var flag by remember { mutableIntStateOf(-1) }
    var fileObjectList by remember { mutableStateOf(listOf<File>()) }
    var showFileChoice by remember { mutableStateOf(false) }

    if (showFileChoice) {
        FileChoiceDialog(
            files = fileObjectList,
            onDismiss = {
                showFileChoice = false
                flag = -1
            }) { file ->
            scope.launch {
                when (flag) {
                    0 -> {
                        flag = -1
                        showFileChoice = false
                        context.installApp(file)
                    }

                    1 -> {
                        showFileChoice = false
                        viewModel.upgrade(file, "B")
                        flag = -1
                    }

                    else -> {
                        showFileChoice = false
                        viewModel.upgrade(file, channel = flag - 2)
                        flag = -1
                    }
                }
            }
        }
    }

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
            SettingsItem(title = "上位机") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = versionList.getOrNull(0) ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    RequirePermission(role = Role.CUSTOMER_SERVICE) {
                        Button(
                            modifier = Modifier.width(120.dp),
                            enabled = flag == -1,
                            onClick = {
                                scope.launch {
                                    viewModel.getApks()?.let {
                                        flag = 0
                                        fileObjectList = it
                                        showFileChoice = true
                                    }
                                }
                            }) {
                            Text(text = "升级")
                        }
                    }
                }
            }
        }

        item {
            SettingsItem(title = "灯板") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = versionList.getOrNull(1) ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    RequirePermission(role = Role.CUSTOMER_SERVICE) {
                        Button(
                            modifier = Modifier.width(120.dp),
                            enabled = flag == -1,
                            onClick = {
                                scope.launch {
                                    viewModel.getBins()?.let {
                                        flag = 1
                                        fileObjectList = it
                                        showFileChoice = true
                                    }
                                }
                            }) {
                            Text(text = "升级")
                        }
                    }
                }
            }
        }

        repeat(4) { index ->
            item {
                SettingsItem(title = "通道 ${index + 1}") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = versionList.getOrNull(index + 2) ?: "Unknown",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        RequirePermission(role = Role.CUSTOMER_SERVICE) {
                            Button(
                                modifier = Modifier.width(120.dp),
                                enabled = flag == -1,
                                onClick = {
                                    scope.launch {
                                        viewModel.getBins()?.let {
                                            flag = index + 2
                                            fileObjectList = it
                                            showFileChoice = true
                                        }
                                    }
                                }) {
                                Text(text = "升级")
                            }
                        }
                    }
                }
            }
        }
    }
}