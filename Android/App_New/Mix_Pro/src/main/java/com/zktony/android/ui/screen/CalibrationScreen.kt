package com.zktony.android.ui.screen

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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.logic.data.entities.CalibrationData
import com.zktony.android.logic.data.entities.CalibrationEntity
import com.zktony.android.ui.components.ZkTonyBottomAddAppBar
import com.zktony.android.ui.components.ZkTonyScaffold
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.utils.PageEnum
import com.zktony.core.ext.format
import com.zktony.core.ext.simpleDateFormat

/**
 * Calibration screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel CalibrationViewModel
 * @return Unit
 */
@Composable
fun CalibrationScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: CalibrationViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        if (uiState.page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(PageEnum.MAIN)
        }
    }

    ZkTonyScaffold(
        modifier = modifier,
        topBar = {
            AnimatedVisibility(visible = uiState.page == PageEnum.EDIT) {
                ZkTonyTopAppBar(title = stringResource(id = R.string.edit), navigation = {
                    if (uiState.page == PageEnum.MAIN) {
                        navController.navigateUp()
                    } else {
                        viewModel.navigationTo(PageEnum.MAIN)
                    }
                })
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = uiState.page == PageEnum.ADD) {
                ZkTonyBottomAddAppBar(
                    strings = uiState.entities.map { it.name },
                    insert = viewModel::insert,
                    navigationTo = viewModel::navigationTo,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        AnimatedVisibility(visible = uiState.page in listOf(PageEnum.MAIN, PageEnum.ADD)) {
            CalibrationMainPage(
                modifier = modifier,
                uiState = uiState,
                active = viewModel::active,
                delete = viewModel::delete,
                navigationTo = viewModel::navigationTo,
                toggleSelected = viewModel::toggleSelected,
            )
        }

        AnimatedVisibility(visible = uiState.page == PageEnum.EDIT) {
            CalibrationEditPage(
                modifier = modifier,
                addLiquid = viewModel::addLiquid,
                entity = uiState.entities.find { it.id == uiState.selected }!!,
                insertData = viewModel::insertData,
                deleteData = viewModel::deleteData,
            )
        }
    }
}

@Composable
fun CalibrationMainPage(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    active: (Long) -> Unit = {},
    delete: (Long) -> Unit = {},
    navigationTo: (PageEnum) -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            val columnState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier
                    .weight(6f)
                    .padding(end = 8.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    ),
                state = columnState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.entities.forEach {
                    item {
                        val background = if (it.id == uiState.selected) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier
                                .height(48.dp)
                                .clickable {
                                    if (it.id == uiState.selected) {
                                        toggleSelected(0L)
                                    } else {
                                        toggleSelected(it.id)
                                    }
                                }, colors = CardDefaults.cardColors(
                                containerColor = background,
                            )
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
                                    painter = painterResource(id = R.drawable.ic_scale),
                                    contentDescription = null,
                                )
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                )
                                AnimatedVisibility(visible = it.active) {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = it.createTime.simpleDateFormat("yyyy - MM - dd"),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    ), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingActionButton(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    onClick = { navigationTo(PageEnum.ADD) }) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
                AnimatedVisibility(visible = uiState.selected != 0L) {
                    var count by remember { mutableStateOf(0) }

                    FloatingActionButton(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                        onClick = {
                            if (count == 1) {
                                delete(uiState.selected)
                                toggleSelected(0L)
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
                AnimatedVisibility(visible = uiState.selected != 0L) {
                    FloatingActionButton(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                        onClick = { navigationTo(PageEnum.EDIT) }) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.Black,
                        )
                    }
                }
                AnimatedVisibility(visible = uiState.selected != 0L) {
                    FloatingActionButton(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                        onClick = { active(uiState.selected) }) {
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
}

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CalibrationEditPage(
    modifier: Modifier = Modifier,
    addLiquid: (Int) -> Unit = { },
    entity: CalibrationEntity = CalibrationEntity(),
    insertData: (Int, Double) -> Unit = { _, _ -> },
    deleteData: (CalibrationData) -> Unit = { },
) {
    var index by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf("") }
    val softKeyboard = LocalSoftwareKeyboardController.current

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
                            Image(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(id = R.drawable.ic_note),
                                contentDescription = null,
                            )
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
                                    .clickable { deleteData(it) },
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red,
                            )
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            contentPadding = PaddingValues(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(9) {
                item {
                    FilterChip(selected = index == it,
                        shape = MaterialTheme.shapes.small,
                        onClick = { index = it },
                        trailingIcon = {
                            if (index == it) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                )
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
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                value = TextFieldValue(volume, TextRange(volume.length)),
                onValueChange = { volume = it.text },
                leadingIcon = {
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
                    addLiquid(index)
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
                    insertData(index, volume.toDoubleOrNull() ?: 0.0)
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
fun CalibrationMainPagePreview() {
    CalibrationMainPage(uiState = CalibrationUiState(entities = listOf(CalibrationEntity())))
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationEditPagePreview() {
    CalibrationEditPage()
}