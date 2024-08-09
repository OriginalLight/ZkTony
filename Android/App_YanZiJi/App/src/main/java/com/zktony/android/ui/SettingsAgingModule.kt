package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions

@Composable
fun SettingsAgingModuleView() {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsAgingModuleTopBar(navigationActions = navigationActions)
    }
}

// 顶部导航栏
@Composable
fun SettingsAgingModuleTopBar(
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
                text = "模块老化",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
