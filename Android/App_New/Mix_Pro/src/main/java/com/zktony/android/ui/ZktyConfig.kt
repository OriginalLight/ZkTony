package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.components.CustomTextField
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.core.ext.Ext
import com.zktony.core.ext.format
import com.zktony.core.ext.showShortToast
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
fun ZktyConfig(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyConfigViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler { navController.navigateUp() }

    Column(modifier = modifier) {

        ZktyTopAppBar(
            title = stringResource(id = R.string.system_config),
            navigation = { navController.navigateUp() }
        )

        ConfigList(
            modifier = Modifier,
            uiState = uiState,
            setTravel = viewModel::setTravel,
            setWaste = viewModel::setWaste,
            moveTo = viewModel::moveTo,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ConfigList(
    modifier: Modifier = Modifier,
    uiState: ConfigUiState,
    setTravel: (Int, Float) -> Unit = { _, _ -> },
    setWaste: (Int, Float) -> Unit = { _, _ -> },
    moveTo: (Int, Float) -> Unit = { _, _ -> },
) {
    val softKeyboard = LocalSoftwareKeyboardController.current
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

        val travel = uiState.settings.travelList.ifEmpty { listOf(0f, 0f, 0f) }
        travel.forEachIndexed { index, item ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var text by remember { mutableStateOf(item.format(1)) }

                    ElevatedCard(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(start = 16.dp),
                                painter = painterResource(id = R.drawable.ic_distance),
                                contentDescription = null,
                            )

                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = stringResource(id = R.string.maximum_stroke) + " " +
                                        when (index) {
                                            0 -> stringResource(id = R.string.x_axis)
                                            1 -> stringResource(id = R.string.y_axis)
                                            2 -> stringResource(id = R.string.z_axis)
                                            else -> ""
                                        },
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Column(
                                modifier = Modifier
                                    .width(196.dp)
                                    .padding(end = 16.dp),
                            ) {
                                CustomTextField(
                                    modifier = Modifier.weight(1f),
                                    value = TextFieldValue(text, TextRange(text.length)),
                                    onValueChange = { text = it.text },
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            softKeyboard?.hide()
                                        }
                                    )
                                )
                                Divider()
                            }
                        }
                    }


                    Button(
                        modifier = Modifier.width(128.dp),
                        enabled = !uiState.lock,
                        onClick = { moveTo(index, text.toFloatOrNull() ?: 0f) },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                        )
                    }

                    AnimatedVisibility(visible = (text.toFloatOrNull() ?: 0f) != item) {
                        Button(
                            modifier = Modifier.width(128.dp),
                            onClick = {
                                scope.launch {
                                    val value = text.toFloatOrNull() ?: 0f
                                    text = value.format(1)
                                    setTravel(index, value)
                                    Ext.ctx.getString(R.string.save_success).showShortToast()
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Filled.Save,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }

        val waste = uiState.settings.wasteList.ifEmpty { listOf(0f, 0f, 0f) }
        waste.forEachIndexed { index, item ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var text by remember { mutableStateOf(item.format(1)) }

                    ElevatedCard(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(start = 16.dp),
                                painter = painterResource(id = R.drawable.ic_abscissa),
                                contentDescription = null,
                            )

                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = stringResource(id = R.string.waste_tank) + " " +
                                        when (index) {
                                            0 -> stringResource(id = R.string.x_axis)
                                            1 -> stringResource(id = R.string.y_axis)
                                            2 -> stringResource(id = R.string.z_axis)
                                            else -> ""
                                        },
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Column(
                                modifier = Modifier
                                    .width(196.dp)
                                    .padding(end = 16.dp),
                            ) {
                                CustomTextField(
                                    modifier = Modifier.weight(1f),
                                    value = TextFieldValue(text, TextRange(text.length)),
                                    onValueChange = { text = it.text },
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            softKeyboard?.hide()
                                        }
                                    )
                                )
                                Divider()
                            }
                        }
                    }


                    Button(
                        modifier = Modifier.width(128.dp),
                        enabled = !uiState.lock,
                        onClick = { moveTo(index, text.toFloatOrNull() ?: 0f) },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                        )
                    }

                    AnimatedVisibility(visible = (text.toFloatOrNull() ?: 0f) != item) {
                        Button(
                            modifier = Modifier.width(128.dp),
                            onClick = {
                                scope.launch {
                                    val value = text.toFloatOrNull() ?: 0f
                                    text = value.format(1)
                                    setWaste(index, value)
                                    Ext.ctx.getString(R.string.save_success).showShortToast()
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Filled.Save,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun ConfigListPreview() {
    ConfigList(
        uiState = ConfigUiState()
    )
}