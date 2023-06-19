package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.logic.data.entities.CalibrationEntity
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.android.ui.utils.PageType
import com.zktony.core.ext.format
import com.zktony.core.ext.showShortToast
import com.zktony.core.ext.simpleDateFormat
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Calibration screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel CalibrationViewModel
 * @return Unit
 */
@Composable
fun ZktyCalibration(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyCalibrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.LIST -> navController.navigateUp()
            else -> viewModel.event(CalibrationEvent.NavTo(PageType.LIST))
        }
    }

    Column(modifier = modifier) {
        // app bar with edit page is visible
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            ZktyTopAppBar(
                title = stringResource(id = R.string.edit),
                navigation = {
                    when (uiState.page) {
                        PageType.LIST -> navController.navigateUp()
                        else -> viewModel.event(CalibrationEvent.NavTo(PageType.LIST))
                    }
                }
            )
        }
        // list page
        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            CalibrationList(
                modifier = modifier,
                uiState = uiState,
                event = viewModel::event,
            )
        }
        // edit page
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            CalibrationEdit(
                modifier = modifier,
                uiState = uiState,
                event = viewModel::event,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationList(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    event: (CalibrationEvent) -> Unit = {},
) {

    val scope = rememberCoroutineScope()
    val columnState = rememberLazyListState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        "Name already exists".showShortToast()
                    } else {
                        event(CalibrationEvent.Insert(it))
                        showDialog = false
                    }
                }
            },
            onCancel = { showDialog = false },
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            state = columnState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.entities.forEachIndexed { index, item ->
                item {
                    val background = if (item.id == uiState.selected) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    Card(
                        modifier = Modifier.height(48.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = background,
                        ),
                        onClick = {
                            if (item.id == uiState.selected) {
                                event(CalibrationEvent.ToggleSelected(0L))
                            } else {
                                event(CalibrationEvent.ToggleSelected(item.id))
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(id = R.drawable.ic_water),
                                contentDescription = null,
                            )
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                            )
                            AnimatedVisibility(visible = item.active) {
                                Text(text = "✔️")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = item.createTime.simpleDateFormat("yyyy - MM - dd"),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
        // operation column
        Column(
            modifier = modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Add
            FloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { showDialog = true }) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }
            // Delete
            AnimatedVisibility(visible = uiState.selected != 0L) {
                var count by remember { mutableStateOf(0) }

                FloatingActionButton(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    onClick = {
                        if (count == 1) {
                            event(CalibrationEvent.Delete(uiState.selected))
                            event(CalibrationEvent.ToggleSelected(0L))
                            count = 0
                        } else {
                            count++
                        }
                    }) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (count == 1) Color.Red else Color.Black,
                    )
                }
            }
            // Edit
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { event(CalibrationEvent.NavTo(PageType.EDIT)) },
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
            // Active
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    onClick = { event(CalibrationEvent.Active(uiState.selected)) }) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }
    }
}

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CalibrationEdit(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    event: (CalibrationEvent) -> Unit = {},
) {
    val entity = uiState.entities.find { it.id == uiState.selected } ?: CalibrationEntity()
    var index by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf("") }
    val softKeyboard = LocalSoftwareKeyboardController.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            ElevatedCard {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(9) {
                        item {
                            FilterChip(selected = index == it,
                                shape = MaterialTheme.shapes.small,
                                onClick = {
                                    index = it
                                    showDialog = false
                                },
                                trailingIcon = {
                                    if (index == it) {
                                        Text(text = "✔️")
                                    }
                                },
                                label = {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        text = "V${it + 1}",
                                        style = TextStyle(fontSize = 24.sp),
                                    )
                                })
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.windowInsetsPadding(WindowInsets.imeAnimationSource)
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                ),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Fixed(2)
        ) {
            entity.data.forEach {
                item {
                    Card(
                        modifier = Modifier.height(48.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "V ${it.index + 1}",
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = it.volume.format(2) + " μL",
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { event(CalibrationEvent.DeleteData(it)) },
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red,
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .height(128.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                modifier = Modifier.padding(start = 16.dp),
                onClick = { showDialog = true },
            ) {
                Text(
                    text = "V${index + 1}",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                value = TextFieldValue(volume, TextRange(volume.length)),
                onValueChange = { volume = it.text },
                trailingIcon = {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(id = R.string.volume)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 24.sp,
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
                ),
                shape = MaterialTheme.shapes.medium,
            )
            Button(
                modifier = Modifier
                    .width(156.dp),
                onClick = {
                    softKeyboard?.hide()
                    event(CalibrationEvent.AddLiquid(index))
                }) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                )
            }
            Button(
                modifier = Modifier
                    .width(156.dp)
                    .padding(end = 16.dp),
                enabled = (volume.toDoubleOrNull() ?: 0.0) > 0.0,
                onClick = {
                    softKeyboard?.hide()
                    event(CalibrationEvent.InsertData(index, volume.toDoubleOrNull() ?: 0.0))
                }) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationListPreview() {
    CalibrationList(uiState = CalibrationUiState(entities = listOf(CalibrationEntity())))
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationEditPreview() {
    CalibrationEdit(
        uiState = CalibrationUiState(
            entities = listOf(CalibrationEntity(id = 1L)),
            selected = 1L,
        )
    )
}