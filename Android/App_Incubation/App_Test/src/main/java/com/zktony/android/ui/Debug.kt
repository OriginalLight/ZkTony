package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.components.CircularButtons
import com.zktony.android.ui.components.DebugAppBar
import com.zktony.android.ui.components.SquareTextField
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.AppStateUtils.hpv
import com.zktony.android.utils.Constants
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/9/6 15:10
 */
@Composable
fun DebugRoute(viewModel: DebugViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()

    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.DEBUG -> navigationActions.navigateUp()
                else -> viewModel.dispatch(DebugIntent.NavTo(PageType.DEBUG))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(DebugIntent.Flags(UiFlags.none()))
        }
    }

    var group by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DebugAppBar { navigation() }
        PulseForm(group, uiFlags, viewModel::dispatch) { group = it }
        ValveGroup(group, page, uiFlags, viewModel::dispatch)
    }
}

@Composable
fun ValveGroup(
    group: Int,
    page: Int,
    uiFlags: UiFlags,
    dispatch: (DebugIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    var valveOne by remember { mutableIntStateOf(0) }
    var valveTwo by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = page) {
        valveOne = (hpv[0 + 2 * group] ?: 1) - 1
        valveTwo = (hpv[1 + 2 * group] ?: 1) - 1
    }

    Row(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularButtons(
                enabled = uiFlags is UiFlags.None,
                selected = valveOne
            ) { index ->
                scope.launch {
                    valveOne = index
                    dispatch(DebugIntent.Valve(0 + 2 * group + group, index + 1))
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularButtons(
                count = 6,
                enabled = uiFlags is UiFlags.None,
                selected = valveTwo
            ) { index ->
                scope.launch {
                    valveTwo = index
                    dispatch(DebugIntent.Valve(1 + 2 * group, index + 1))
                }
            }
        }
    }
}

@Composable
fun PulseForm(
    group: Int,
    uiFlags: UiFlags,
    dispatch: (DebugIntent) -> Unit,
    onGroupChanged: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val number by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
    var turns by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (number > 4) {
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "模组",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                ElevatedButton(
                    onClick = {
                        scope.launch {
                            if (group < (number / 4) - 1) {
                                onGroupChanged(group + 1)
                            } else {
                                onGroupChanged(0)
                            }
                        }
                    }
                ) {
                    Text(text = "${'A' + group}")
                }
            }
        }

        SquareTextField(
            title = "圈数",
            value = turns,
            trailingIcon = {
                Row {
                    ElevatedButton(
                        modifier = Modifier.padding(end = 16.dp),
                        enabled = uiFlags is UiFlags.None,
                        onClick = {
                            scope.launch {
                                dispatch(
                                    DebugIntent.Transfer(
                                        1 + group,
                                        (turns.toDoubleOrNull() ?: 0.0) * -1
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }

                    ElevatedButton(
                        modifier = Modifier.padding(end = 16.dp),
                        enabled = uiFlags is UiFlags.None,
                        onClick = {
                            scope.launch {
                                dispatch(
                                    DebugIntent.Transfer(
                                        1 + group,
                                        turns.toDoubleOrNull() ?: 0.0
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        ) { turns = it }
    }
}