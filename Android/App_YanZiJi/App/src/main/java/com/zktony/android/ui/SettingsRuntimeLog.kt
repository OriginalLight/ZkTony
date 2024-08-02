package com.zktony.android.ui

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ImportExport
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.components.TopBarRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsRuntimeLogViewModel
import com.zktony.android.utils.extra.size
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsRuntimeLogView(viewModel: SettingsRuntimeLogViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val fileList by viewModel.fileList.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsRuntimeLogTopBar(
            selected = selected,
            viewModel = viewModel,
            navigationActions = navigationActions
        )

        // 运行日志列表
        SettingsRuntimeLogListView(
            fileList = fileList,
            selected = selected,
            viewModel = viewModel
        )
    }
}

// 顶部导航栏
@Composable
fun SettingsRuntimeLogTopBar(
    modifier: Modifier = Modifier,
    selected: List<File>,
    viewModel: SettingsRuntimeLogViewModel,
    navigationActions: NavigationActions,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    TopBarRow(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Reply,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.app_runtime_log),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Button(
            enabled = !loading && selected.isNotEmpty(),
            onClick = {
                scope.launch {
                    loading = true
                    viewModel.export()
                    loading = false
                }
            }) {
            IconLoading(loading = loading) {
                Icon(imageVector = Icons.Default.ImportExport, contentDescription = "ImportExport")
            }
            Text(
                text = stringResource(id = R.string.app_export),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// 运行日志列表
@Composable
fun SettingsRuntimeLogListView(
    modifier: Modifier = Modifier,
    fileList: List<File>,
    selected: List<File>,
    viewModel: SettingsRuntimeLogViewModel
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
        fileList.forEach { file ->
            item {
                RuntimeLogItem(
                    file = file,
                    selected = selected.contains(file),
                    onClick = {
                        viewModel.select(file)
                    }
                )
            }
        }
    }
}

// 运行日志列表项
@SuppressLint("DefaultLocale")
@Composable
fun RuntimeLogItem(
    modifier: Modifier = Modifier,
    file: File,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.1f
                ),
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = file.name, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal))
        Text(
            text = file.size(),
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
        )
    }
}