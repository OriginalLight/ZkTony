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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.data.Role
import com.zktony.android.ui.components.RequirePermission
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsVersionInfoViewModel

@Composable
fun SettingsVersionInfoView(viewModel: SettingsVersionInfoViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigate(Route.SETTINGS)
    }

    val versionList by viewModel.versionList.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsVersionInfoTopBar(navigationActions = navigationActions)
        // 版本信息列表
        SettingsVersionInfoList(versionList = versionList)
    }
}

// 顶部导航栏
@Composable
fun SettingsVersionInfoTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions,
) {
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
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
            Text(
                text = stringResource(id = R.string.version_info),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

// 版本信息列表
@Composable
fun SettingsVersionInfoList(
    modifier: Modifier = Modifier,
    versionList: List<String>
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
            SettingsItem(title = "上位机版本") {
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
                            onClick = { /*TODO*/ }) {
                            Text(text = "升级")
                        }
                    }
                }
            }
        }

        item {
            SettingsItem(title = "灯板固件版本") {
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
                            onClick = { /*TODO*/ }) {
                            Text(text = "升级")
                        }
                    }
                }
            }
        }

        repeat(4) {
            item {
                SettingsItem(title = "下位机${it + 1}固件版本") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = versionList.getOrNull(it + 2) ?: "Unknown",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        RequirePermission(role = Role.CUSTOMER_SERVICE) {
                            Button(

                                modifier = Modifier.width(120.dp),
                                onClick = { /*TODO*/ }) {
                                Text(text = "升级")
                            }
                        }
                    }
                }
            }
        }
    }
}