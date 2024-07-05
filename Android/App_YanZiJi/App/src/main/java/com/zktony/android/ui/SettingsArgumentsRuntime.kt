package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.data.Arguments
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsArgumentsRuntimeViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils

@Composable
fun SettingsArgumentsRuntimeView(viewModel: SettingsArgumentsRuntimeViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    val arguments by AppStateUtils.argumentsList.collectAsStateWithLifecycle()
    var selected by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsArgumentsRuntimeTopBar(navigationActions = navigationActions)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
                .clip(MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChannelTabRow(selected = selected) {
                selected = it
            }

            ChannelContentRow(
                channel = selected,
                arguments = arguments
            )
        }
    }
}

@Composable
fun ChannelTabRow(
    modifier: Modifier = Modifier,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(ProductUtils.getChannelCount()) { index ->
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.channel) + (index + 1),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ChannelContentRow(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var argument by remember { mutableStateOf(arguments[channel]) }
        var inFillSped by remember(channel) { mutableStateOf(arguments[channel].inFillSpeed.toString()) }
        var experimentalArguments by remember(channel) { mutableStateOf(arguments[channel].toExperimental()) }
        var cleanArguments by remember(channel) { mutableStateOf(arguments[channel].toClean()) }

        SettingsRow(title = stringResource(id = R.string.channel) + (channel + 1)) {
            ArgumentsInputField(
                modifier = Modifier.width(350.dp),
                value = experimentalArguments.inFillSpeed.toString(),
            ) {
                // TODO
            }
        }
    }
}

@Composable
fun SettingsArgumentsRuntimeTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions
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
                text = stringResource(id = R.string.runtime_arguments),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}