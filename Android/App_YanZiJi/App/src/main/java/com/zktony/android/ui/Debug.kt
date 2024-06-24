package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.ui.components.CircularButton
import com.zktony.android.ui.components.DebugAppBar
import com.zktony.android.ui.components.SquareTextField
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.AppStateUtils.hpv
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/9/6 15:10
 */
@Composable
fun DebugRoute(viewModel: DebugViewModel) {

    val navigationActions = LocalNavigationActions.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    BackHandler { navigationActions.navigateUp() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DebugAppBar { navigationActions.navigateUp() }
        PulseForm(loading, viewModel::dispatch)
        ValveGroup(page, loading, viewModel::dispatch)
    }
}

@Composable
fun ValveGroup(
    page: PageType,
    loading: Boolean,
    dispatch : (DebugIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    var valveOne by remember { mutableIntStateOf(0) }
    var valveTwo by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = page) {
        valveOne = (hpv[0] ?: 1) - 1
        valveTwo = (hpv[1] ?: 1) - 1
    }

    Row(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularButton(
                count = 12,
                display = listOf(
                    "A1", "B1", "C1", "D1", "A2", "B2",
                    "C2", "D2", "封闭", "洗涤", "废液", "空气"
                ),
                enabled = !loading,
                selected = valveOne
            ) { index ->
                scope.launch {
                    valveOne = index
                    dispatch(DebugIntent.Valve(index + 1))
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularButton(
                count = 6,
                display = listOf("A", "B", "C", "D", "废液", "空气"),
                enabled = !loading,
                selected = valveTwo
            ) { index ->
                scope.launch {
                    valveTwo = index
                    dispatch(DebugIntent.Valve(index + 1))
                }
            }
        }
    }
}

@Composable
fun PulseForm(
    loading: Boolean,
    dispatch: (DebugIntent) -> Unit
) {
    var turns by remember { mutableStateOf("1") }

    SquareTextField(
        modifier = Modifier.fillMaxWidth(0.5f),
        title = "圈数",
        value = turns,
        trailingIcon = {
            Row {
                ElevatedButton(
                    modifier = Modifier.padding(end = 16.dp),
                    enabled = !loading,
                    onClick = { dispatch(DebugIntent.Transfer((turns.toDoubleOrNull() ?: 0.0) * -1)) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }

                ElevatedButton(
                    modifier = Modifier.padding(end = 16.dp),
                    enabled = !loading,
                    onClick = { dispatch(DebugIntent.Transfer(turns.toDoubleOrNull() ?: 0.0))}
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    ) { turns = it }
}

@Preview(device = "id:Nexus 9")
@Composable
fun ValveGroupPreview() {
    ValveGroup(PageType.DEBUG, false, hiltViewModel())
}

@Preview()
@Composable
fun PulseFormPreview() {
    PulseForm(false, hiltViewModel())
}