package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverListState
import com.zktony.android.ext.format
import com.zktony.android.ext.utils.Constants
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Displays the system configuration screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param navController The NavHostController used for navigation.
 * @param viewModel The ConfigViewModel used to manage the UI state.
 */
@Composable
fun Config(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ConfigViewModel = koinViewModel(),
) {
    // Observe the UI state from the view model
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle the back button press
    BackHandler { navController.navigateUp() }

    // Display the screen content
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Display the title
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_config),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.weight(1f))
            // Display the close button
            FloatingActionButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }

        // Display the screen content wrapper
        ContentWrapper(
            modifier = Modifier,
            uiState = uiState,
            event = viewModel::event
        )
    }
}

/**
 * Wrapper composable for the system configuration screen content.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The ConfigUiState used to manage the UI state.
 * @param event The event handler for the ConfigEvent.
 */
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
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            )
            .windowInsetsPadding(WindowInsets.imeAnimationSource),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Display the maximum travel settings
        item {
            var stroke by rememberDataSaverListState(Constants.MAXIMUM_STROKE, listOf(0f, 0f))
            var strokeEdit by remember {
                mutableStateOf(
                    listOf(
                        stroke[0].format(1),
                        stroke[1].format(1)
                    )
                )
            }

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
                            value = strokeEdit[0],
                            onValueChange = {
                                scope.launch {
                                    stroke = listOf(it.toFloatOrNull() ?: 0f, stroke[1])
                                    strokeEdit = listOf(it, strokeEdit[1])
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
                                        event(ConfigEvent.MoveTo(0, stroke[0]))
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
                            value = strokeEdit[1],
                            onValueChange = {
                                scope.launch {
                                    stroke = listOf(stroke[0], it.toFloatOrNull() ?: 0f)
                                    strokeEdit = listOf(strokeEdit[0], it)
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
                                        event(ConfigEvent.MoveTo(1, stroke[1]))
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

        // Display the waste tank position settings
        item {
            var location by rememberDataSaverListState(
                Constants.WASTE_TANK_LOCATION,
                listOf(0f, 0f)
            )
            var locationEdit by remember {
                mutableStateOf(
                    listOf(
                        location[0].format(1),
                        location[1].format(1)
                    )
                )
            }

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
                            value = locationEdit[0],
                            onValueChange = {
                                scope.launch {
                                    location = listOf(it.toFloatOrNull() ?: 0f, location[1])
                                    locationEdit = listOf(it, locationEdit[1])
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
                                        event(ConfigEvent.MoveTo(0, location[0]))
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
                            value = locationEdit[1],
                            onValueChange = {
                                scope.launch {
                                    location = listOf(location[0], it.toFloatOrNull() ?: 0f)
                                    locationEdit = listOf(locationEdit[0], it)
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
                                        event(ConfigEvent.MoveTo(1, location[1]))
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

/**
 * Preview function for the [ContentWrapper] composable.
 */
@Composable
@Preview(showBackground = true, widthDp = 960)
fun ConfigContentWrapperPreview() {
    ContentWrapper(
        uiState = ConfigUiState()
    )
}