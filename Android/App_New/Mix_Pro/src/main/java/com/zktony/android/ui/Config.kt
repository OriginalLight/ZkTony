package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.core.ext.format
import com.zktony.android.ui.components.ZktyTopAppBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * 系统配置
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel ConfigViewModel
 * @return Unit
 */
@Composable
fun Config(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ConfigViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler { navController.navigateUp() }

    Column(modifier = modifier) {

        ZktyTopAppBar(
            title = stringResource(id = R.string.system_config),
            navigation = { navController.navigateUp() }
        )

        ContentWrapper(
            modifier = Modifier,
            uiState = uiState,
            event = viewModel::event
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ContentWrapper(
    modifier: Modifier = Modifier,
    uiState: ConfigUiState,
    event: (ConfigEvent) -> Unit = { },
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.imeAnimationSource)
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            val travel = uiState.settings.travelList.ifEmpty { listOf(0f, 0f) }
            var y by remember { mutableStateOf(travel[0].format(1)) }
            var z by remember { mutableStateOf(travel[1].format(1)) }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "最大行程", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "托盘",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = y,
                            onValueChange = {
                                scope.launch {
                                    y = it
                                    val value = it.toFloatOrNull() ?: 0f
                                    event(ConfigEvent.SetTravel(0, value))
                                }
                            },
                            label = { Text(text = "坐标") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ),
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                modifier = Modifier.width(96.dp),
                                enabled = !uiState.loading,
                                onClick = {
                                    scope.launch {
                                        keyboard?.hide()
                                        event(ConfigEvent.MoveTo(0, y.toFloatOrNull() ?: 0f))
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "针头",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = z,
                            onValueChange = {
                                scope.launch {
                                    z = it
                                    val value = it.toFloatOrNull() ?: 0f
                                    event(ConfigEvent.SetTravel(1, value))
                                }
                            },
                            label = { Text(text = "坐标") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ),
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                modifier = Modifier.width(96.dp),
                                enabled = !uiState.loading,
                                onClick = {
                                    scope.launch {
                                        keyboard?.hide()
                                        event(ConfigEvent.MoveTo(1, z.toFloatOrNull() ?: 0f))
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
                }
            }

        }

        item {
            val travel = uiState.settings.wasteList.ifEmpty { listOf(0f, 0f) }
            var y by remember { mutableStateOf(travel[0].format(1)) }
            var z by remember { mutableStateOf(travel[1].format(1)) }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "废液槽位置", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "托盘",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = y,
                            onValueChange = {
                                scope.launch {
                                    y = it
                                    val value = it.toFloatOrNull() ?: 0f
                                    event(ConfigEvent.SetWaste(0, value))
                                }
                            },
                            label = { Text(text = "坐标") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ),
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                modifier = Modifier.width(96.dp),
                                enabled = !uiState.loading,
                                onClick = {
                                    scope.launch {
                                        keyboard?.hide()
                                        event(ConfigEvent.MoveTo(0, y.toFloatOrNull() ?: 0f))
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "针头",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = z,
                            onValueChange = {
                                scope.launch {
                                    z = it
                                    val value = it.toFloatOrNull() ?: 0f
                                    event(ConfigEvent.SetWaste(1, value))
                                }
                            },
                            label = { Text(text = "坐标") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            ),
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                modifier = Modifier.width(96.dp),
                                enabled = !uiState.loading,
                                onClick = {
                                    scope.launch {
                                        keyboard?.hide()
                                        event(ConfigEvent.MoveTo(1, z.toFloatOrNull() ?: 0f))
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
                }
            }

        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun ConfigContentWrapperPreview() {
    ContentWrapper(
        uiState = ConfigUiState()
    )
}