package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.DEBUG -> navigationActions.navigateUp()
                else -> viewModel.uiEvent(DebugUiEvent.NavTo(PageType.DEBUG))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.uiEvent(DebugUiEvent.Message(null))
        }
    }

    DebugWrapper(
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}

@Composable
fun DebugWrapper(
    uiState: DebugUiState,
    uiEvent: (DebugUiEvent) -> Unit,
    navigation: () -> Unit
) {

    var group by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DebugAppBar { navigation() }
        PulseForm(group, uiState, uiEvent) { group = it }
        ValveGroup(group, uiState, uiEvent)
    }
}

@Composable
fun ValveGroup(
    group: Int,
    uiState: DebugUiState,
    uiEvent: (DebugUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    var valveOne by remember { mutableIntStateOf(0) }
    var valveTwo by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = uiState.page) {
        valveOne = (hpv[0 + 2 * group] ?: 1) - 1
        valveTwo = (hpv[1 + 2 * group] ?: 1) - 1
    }

    Row(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularButtons(
                enabled = uiState.uiFlags != UiFlags.VALVE,
                selected = valveOne
            ) { index ->
                scope.launch {
                    valveOne = index
                    uiEvent(DebugUiEvent.Valve(0 + 2 * group + group, index + 1))
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularButtons(
                count = 6,
                enabled = uiState.uiFlags != UiFlags.VALVE,
                selected = valveTwo
            ) { index ->
                scope.launch {
                    valveTwo = index
                    uiEvent(DebugUiEvent.Valve(1 + 2 * group, index + 1))
                }
            }
        }
    }
}

@Composable
fun PulseForm(
    group: Int,
    uiState: DebugUiState,
    uiEvent: (DebugUiEvent) -> Unit,
    onGroupChanged: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val number by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
    var pulse by remember { mutableStateOf("6400") }

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
            title = "步数",
            value = pulse,
            trailingIcon = {
                ElevatedButton(
                    modifier = Modifier.padding(end = 16.dp),
                    enabled = uiState.uiFlags != UiFlags.PUMP,
                    onClick = {
                        scope.launch {
                            uiEvent(DebugUiEvent.Pulse(1 + group, pulse.toLongOrNull() ?: 0L))
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        ) { pulse = it }
    }
}