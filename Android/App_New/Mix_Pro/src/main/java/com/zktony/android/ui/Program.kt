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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.core.ext.dateFormat
import com.zktony.android.core.ext.format
import com.zktony.android.core.ext.showShortToast
import com.zktony.android.data.entities.ProgramEntity
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.components.TopAppBar
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


/**
 * The Program composable function for the app.
 *
 * @param modifier The modifier for the composable.
 * @param navController The NavHostController for the app.
 * @param viewModel The ProgramViewModel for the app.
 */
@Composable
fun Program(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProgramViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle the back button press
    BackHandler {
        when (uiState.page) {
            PageType.LIST -> navController.navigateUp() // Step 1: Navigate up if on the list page
            else -> viewModel.event(ProgramEvent.NavTo(PageType.LIST)) // Step 2: Navigate to the list page if on any other page
        }
    }

    // Display the content wrapper
    ContentWrapper(
        modifier = modifier,
        uiState = uiState,
        event = viewModel::event,
    )
}

/**
 * The ContentWrapper composable function for the app.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The ProgramUiState for the app.
 * @param event The event handler for the app.
 */
@Composable
fun ContentWrapper(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState,
    event: (ProgramEvent) -> Unit = {},
) {
    Column(modifier = modifier) {
        // Display the app bar for the edit page
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            TopAppBar(
                title = stringResource(id = R.string.edit),
                navigation = { event(ProgramEvent.NavTo(PageType.LIST)) } // Step 1: Navigate to the list page when the back button is pressed
            )
        }
        // Display the list page
        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            ListContent(
                modifier = Modifier,
                uiState = uiState,
                event = event,
            )
        }
        // Display the edit page
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            EditContent(
                modifier = Modifier,
                uiState = uiState,
                event = event,
            )
        }
    }
}

/**
 * The ListContent composable function for the app.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The ProgramUiState for the app.
 * @param event The event handler for the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showDialog by remember { mutableStateOf(false) }

    // Show the input dialog if showDialog is true
    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        "Name already exists".showShortToast()
                    } else {
                        event(ProgramEvent.Insert(it))
                        showDialog = false
                    }
                }
            },
            onCancel = { showDialog = false },
        )
    }

    // Display the list and operation columns
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            event(ProgramEvent.ToggleSelected(0L))
                        } else {
                            event(ProgramEvent.ToggleSelected(item.id))
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Display the entity image and title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.ic_program),
                                contentDescription = null,
                            )
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
                            Column {
                                Text(
                                    text = "G - ${item.volume[0].format(1)}/${
                                        item.volume[1].format(
                                            1
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                )
                                Text(
                                    text = "P - ${item.volume[2].format(1)}/${
                                        item.volume[3].format(
                                            1
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                )
                                Text(
                                    text = "A - ${item.axis[0].format(1)}/${
                                        item.axis[1].format(
                                            1
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
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

        // Display the operation column
        Column(
            modifier = modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add button
            FloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { showDialog = true }
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }
            // Delete button
            AnimatedVisibility(visible = uiState.selected != 0L) {
                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            if (count == 1) {
                                event(ProgramEvent.Delete(uiState.selected))
                                event(ProgramEvent.ToggleSelected(0L))
                                count = 0
                            } else {
                                count++
                            }
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (count == 1) Color.Red else Color.Black,
                    )
                }
            }
            // Edit button
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { event(ProgramEvent.NavTo(PageType.EDIT)) },
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }
    }
}

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun EditContent(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val entity = uiState.entities.find { it.id == uiState.selected } ?: ProgramEntity()
    val travel = uiState.settings.travelList.ifEmpty { listOf(100f, 100f) }
    var v1 by remember { mutableStateOf(entity.volume[0].format(1)) }
    var v2 by remember { mutableStateOf(entity.volume[1].format(1)) }
    var v3 by remember { mutableStateOf(entity.volume[2].format(1)) }
    var v4 by remember { mutableStateOf(entity.volume[3].format(1)) }
    var y by remember { mutableStateOf(entity.axis[0].format(1)) }
    var z by remember { mutableStateOf(entity.axis[1].format(1)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.imeAnimationSource)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                // volume
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.glue_making),
                                style = MaterialTheme.typography.titleMedium,
                            )

                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(v1, TextRange(v1.length)),
                                onValueChange = {
                                    scope.launch {
                                        v1 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[0] = v1.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.colloid)) },
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
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(v2, TextRange(v2.length)),
                                onValueChange = {
                                    scope.launch {
                                        v2 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[1] = v2.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.coagulant)) },
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
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.pre_drain),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(v3, TextRange(v3.length)),
                                onValueChange = {
                                    scope.launch {
                                        v3 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[2] = v3.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.colloid)) },
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
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(v4, TextRange(v4.length)),
                                onValueChange = {
                                    scope.launch {
                                        v4 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[3] = v4.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.coagulant)) },
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
                        }
                    }
                }
            }
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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
                                value = TextFieldValue(y, TextRange(y.length)),
                                onValueChange = {
                                    scope.launch {
                                        val num = it.text.toFloatOrNull() ?: 0f
                                        y = if (num > travel[0]) {
                                            travel[0].format(1)
                                        } else if (num < 0) {
                                            "0"
                                        } else {
                                            it.text
                                        }
                                        val axis = entity.axis.toMutableList()
                                        axis[0] = y.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(axis = axis)))
                                    }
                                },
                                label = { Text(text = "坐标(0 ~ ${travel[0].format(1)})") },
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
                                            event(ProgramEvent.MoveTo(0, y.toFloatOrNull() ?: 0f))
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
                                value = TextFieldValue(z, TextRange(z.length)),
                                onValueChange = {
                                    scope.launch {
                                        val num = it.text.toFloatOrNull() ?: 0f
                                        z = if (num > travel[1]) {
                                            travel[1].format(1)
                                        } else if (num < 0) {
                                            "0"
                                        } else {
                                            it.text
                                        }
                                        val axis = entity.axis.toMutableList()
                                        axis[1] = z.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(axis = axis)))
                                    }
                                },
                                label = { Text(text = "坐标(0 ~ ${travel[1].format(1)})") },
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
                                            event(ProgramEvent.MoveTo(1, z.toFloatOrNull() ?: 0f))
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
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramListContentPreview() {
    // Call the ListContent function and pass in a ProgramUiState object as a parameter
    ListContent(
        uiState = ProgramUiState(
            entities = listOf(
                ProgramEntity(text = "test")
            )
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramEditContentPreview() {
    // Call the EditContent function and pass in a ProgramUiState object as a parameter
    EditContent(
        uiState = ProgramUiState(
            entities = listOf(
                ProgramEntity(text = "test", id = 1L)
            ),
            selected = 1L
        )
    )
}