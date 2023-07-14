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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.core.ext.dateFormat
import com.zktony.android.core.ext.format
import com.zktony.android.core.ext.showShortToast
import com.zktony.android.data.entities.CalibrationEntity
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.components.TopAppBar
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Calibration screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel CalibrationViewModel
 */
@Composable
fun Calibration(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: CalibrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.LIST -> navController.navigateUp()
            else -> viewModel.event(CalibrationEvent.NavTo(PageType.LIST))
        }
    }

    ContentWrapper(
        modifier = modifier,
        uiState = uiState,
        event = viewModel::event,
    )
}

/**
 * Composable function that wraps the content of the calibration screen.
 *
 * @param modifier Modifier
 * @param uiState The current UI state of the calibration screen.
 * @param event The event handler for the calibration screen.
 */
@Composable
fun ContentWrapper(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState,
    event: (CalibrationEvent) -> Unit = {},
) {
    Column {
        // App bar with edit page is visible
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            TopAppBar(
                title = stringResource(id = R.string.edit),
                navigation = { event(CalibrationEvent.NavTo(PageType.LIST)) }
            )
        }
        // List page
        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            ListContent(
                modifier = modifier,
                uiState = uiState,
                event = event,
            )
        }
        // Edit page
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            EditContent(
                modifier = modifier,
                uiState = uiState,
                event = event,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    event: (CalibrationEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showDialog by remember { mutableStateOf(false) }

    // Show the input dialog if showDialog is true
    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    // Check if the name already exists
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        "Name already exists".showShortToast()
                    } else {
                        // Insert the new item
                        event(CalibrationEvent.Insert(it))
                        showDialog = false
                    }
                }
            },
            onCancel = { showDialog = false },
        )
    }

    // Row containing the list of items and the operation column
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyVerticalGrid(
            modifier = modifier
                .weight(6f)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            state = gridState,
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(items = uiState.entities) { index, item ->
                val background = if (item.id == uiState.selected) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = background),
                    onClick = {
                        if (item.id == uiState.selected) {
                            event(CalibrationEvent.ToggleSelected(0L))
                        } else {
                            event(CalibrationEvent.ToggleSelected(item.id))
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Display the entity image and title
                        Row(
                            modifier = Modifier.height(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AnimatedVisibility(visible = item.active) {
                                Text(text = "✔")
                            }
                            if (!item.active) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_water),
                                    contentDescription = null,
                                )
                            }
                            Text(
                                modifier = Modifier.weight(1f),
                                text = item.text,
                                style = MaterialTheme.typography.titleMedium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            // Display the entity volume range
                            Text(
                                text = "T - ${item.data.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = item.createTime.dateFormat("yyyy/MM/dd"),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }

        // Column containing the operation buttons
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
            // Button for adding a new item
            FloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { showDialog = true })
            {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }

            // Button for deleting the selected item
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

            // Button for editing the selected item
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

            // Button for activating the selected item
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

/**
 * Composable function that displays the edit content UI.
 *
 * @param modifier Modifier to be applied to the content.
 * @param uiState The current state of the calibration UI.
 * @param event The event to be triggered when the UI state changes.
 */
@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun EditContent(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    event: (CalibrationEvent) -> Unit = {},
) {
    // Get the selected entity or create a new one if none is selected
    val entity = uiState.entities.find { it.id == uiState.selected } ?: CalibrationEntity()

    // Initialize the index and volume variables
    var index by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf("") }

    // Get the software keyboard controller
    val softKeyboard = LocalSoftwareKeyboardController.current

    // Initialize the showDialog variable
    var showDialog by remember { mutableStateOf(false) }

    // Get the coroutine scope
    val scope = rememberCoroutineScope()

    // Show the dialog if showDialog is true
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            // Show the filter chip grid
            ElevatedCard {
                LazyVerticalGrid(
                    modifier = Modifier.padding(8.dp),
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(7) {
                        item {
                            FilterChip(
                                selected = index == it,
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

    // Show the edit content UI
    Column(
        modifier = modifier
            .padding(8.dp)
            .windowInsetsPadding(WindowInsets.imeAnimationSource),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Show the data grid
        LazyVerticalGrid(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                ),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Fixed(3)
        ) {
            items(items = entity.data) {
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
                        // Show the index
                        Text(
                            text = "V ${it.index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Show the volume
                        Text(
                            text = it.volume.format(2) + " μL",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Show the delete icon
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

        // Show the input row
        Row(
            modifier = Modifier
                .height(108.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show the filter chip button
            Button(
                modifier = Modifier.padding(start = 16.dp),
                onClick = { showDialog = true },
            ) {
                Text(
                    text = "V${index + 1}",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            // Show the volume text field
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                value = TextFieldValue(volume, TextRange(volume.length)),
                onValueChange = { volume = it.text },
                label = {
                    Text(
                        text = stringResource(id = R.string.volume)
                    )
                },
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

            // Show the add button
            Button(
                modifier = Modifier
                    .width(156.dp),
                onClick = {
                    scope.launch {
                        softKeyboard?.hide()
                        if (!uiState.loading) {
                            event(CalibrationEvent.AddLiquid(index))
                        }
                    }
                }) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 4.dp,
                        color = Color.White,
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                    )
                }
            }

            // Show the save button
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

/**
 * Composable function that previews the calibration list content.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationListContentPreview() {
    // Create a calibration entity list with a single entity
    val entities = listOf(CalibrationEntity())

    // Create a calibration UI state with the entity list
    val uiState = CalibrationUiState(entities = entities)

    // Show the calibration list content
    ListContent(uiState = uiState)
}

/**
 * Composable function that previews the calibration edit content.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationEditContentPreview() {
    // Create a calibration entity list with a single entity
    val entities = listOf(CalibrationEntity(id = 1L))

    // Create a calibration UI state with the entity list and a selected entity ID
    val uiState = CalibrationUiState(entities = entities, selected = 1L)

    // Show the calibration edit content
    EditContent(uiState = uiState)
}