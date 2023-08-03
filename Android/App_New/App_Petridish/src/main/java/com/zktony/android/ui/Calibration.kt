package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.ext.dateFormat
import com.zktony.android.utils.ext.format
import com.zktony.android.utils.ext.showShortToast
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

    // List page
    AnimatedVisibility(visible = uiState.page == PageType.LIST) {
        CalibrationList(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
        )
    }
    // Edit page
    AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
        CalibrationDetail(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
        )
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
    val gridState = rememberLazyGridState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }


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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Column containing the operation buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("搜索") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val items =
                        uiState.entities.filter { query.isNotEmpty() && it.text.contains(query) }
                    items(items.size) {
                        val item = items[it]
                        ListItem(
                            headlineContent = { Text(item.text) },
                            supportingContent = { Text(item.createTime.dateFormat("yyyy/MM/dd")) },
                            leadingContent = {
                                if (item.text == query) Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                query = item.text
                                active = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            // Button for adding a new item
            FloatingActionButton(
                modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                onClick = { showDialog = true })
            {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }

            // Button for deleting the selected item
            AnimatedVisibility(visible = uiState.selected != 0L) {
                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
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
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (count == 1) Color.Red else Color.Black,
                    )
                }
            }

            // Button for editing the selected item
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                    onClick = { event(CalibrationEvent.NavTo(PageType.EDIT)) }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }

            // Button for activating the selected item
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                    onClick = { event(CalibrationEvent.Active(uiState.selected)) }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                ),
            state = gridState,
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val items = uiState.entities.filter { it.text.contains(query) }

            itemsIndexed(items = items) { index, item ->
                val background = if (item.id == uiState.selected) {
                    Color.Blue.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                ElevatedCard(
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
                            Text(
                                text = "${index + 1}、",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            AnimatedVisibility(visible = item.active) {
                                Text(text = "✔")
                            }
                        }
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = item.text,
                            fontSize = 20.sp,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = item.createTime.dateFormat("yyyy/MM/dd"),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                        )
                    }
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
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalibrationDetail(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    event: (CalibrationEvent) -> Unit = {},
) {
    // Get the selected entity or create a new one if none is selected
    val entity = uiState.entities.find { it.id == uiState.selected } ?: Calibration()

    val list = remember {
        mutableStateListOf(
            "注射泵",
            "一号重液泵",
            "一号轻液泵",
            "二号重液泵",
            "二号轻液泵",
            "三号重液泵",
            "三号轻液泵"
        )
    }

    var index by remember { mutableStateOf(0) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var volume by remember { mutableStateOf("0") }
    val softKeyboard = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Display the title
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.Edit,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.weight(1f))
            // Display the close button
            FloatingActionButton(
                onClick = {
                    event(CalibrationEvent.NavTo(PageType.LIST))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }
        // Show the edit content UI
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Show the data grid
            LazyVerticalGrid(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .shadow(
                        elevation = 2.dp,
                        shape = MaterialTheme.shapes.medium,
                    ),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(items = entity.data) { index, it ->
                    Row(
                        modifier = Modifier
                            .background(
                                color = if (index % 2 == 0) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    Color.Transparent
                                },
                                shape = MaterialTheme.shapes.medium,
                            )
                            .shadow(
                                elevation = 2.dp,
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${index + 1}、",
                            style = MaterialTheme.typography.titleLarge,
                            fontStyle = FontStyle.Italic,
                        )

                        Column {
                            Text(
                                text = list[it.first],
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Serif,
                            )
                            Text(
                                text = it.second.format(2) + " μL",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                            )
                        }
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

            // Show the input row
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shadow(
                        elevation = 2.dp,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Show the filter chip button
                Column {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                textFieldSize = it.size.toSize()
                            },
                        onClick = { expanded = !expanded },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = list[index],
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    ) {
                        list.forEachIndexed { ix, label ->
                            DropdownMenuItem(
                                text = { Text(text = label) },
                                onClick = {
                                    index = ix
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Show the volume text field
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
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


}

/**
 * Composable function that previews the calibration list content.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationListPreview() {
    // Create a calibration entity list with a single entity
    val entities = listOf(Calibration())

    // Create a calibration UI state with the entity list
    val uiState = CalibrationUiState(entities = entities)

    // Show the calibration list content
    CalibrationList(uiState = uiState)
}

/**
 * Composable function that previews the calibration edit content.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationDetailPreview() {
    // Create a calibration entity list with a single entity
    val entities = listOf(Calibration(id = 1L))

    // Create a calibration UI state with the entity list and a selected entity ID
    val uiState = CalibrationUiState(entities = entities, selected = 1L)

    // Show the calibration edit content
    CalibrationDetail(uiState = uiState)
}