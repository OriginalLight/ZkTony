package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Coordinate
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.CircleTextField
import com.zktony.android.ui.components.CoordinateInput
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.serial
import kotlinx.coroutines.launch

@Composable
fun ProgramRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProgramViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> navController.navigateUp()
                else -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
            }
        }
    }

    BackHandler { navigation() }

    Scaffold(
        topBar = {
            ProgramAppBar(
                uiState = uiState,
                uiEvent = viewModel::uiEvent,
                navigation = navigation,
                snackbarHostState = snackbarHostState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        ProgramScreen(
            modifier = modifier.padding(paddingValues),
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun ProgramScreen(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit
) {

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
        ProgramList(
            modifier = modifier,
            uiState = uiState,
            uiEvent = uiEvent,
        )
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_DETAIL) {
        ProgramDetail(
            modifier = modifier,
            uiState = uiState,
            uiEvent = uiEvent,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    uiEvent: (ProgramUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            ),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(items = uiState.entities) { index, item ->
            val background = if (item.id == uiState.selected) {
                Color.Blue.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            ElevatedCard(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .combinedClickable(
                        onClick = {
                            scope.launch {
                                if (item.id == uiState.selected) {
                                    uiEvent(ProgramUiEvent.ToggleSelected(0L))
                                } else {
                                    uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                                }
                            }
                        },
                        onDoubleClick = {
                            scope.launch {
                                uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                                uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL))
                            }
                        }
                    ),
                colors = CardDefaults.cardColors(containerColor = background)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgramDetail(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    uiEvent: (ProgramUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val selected = uiState.entities.find { it.id == uiState.selected } ?: Program()
    val maxAbscissa by rememberDataSaverState(key = Constants.ZT_0001, initialValue = 0.0)
    val maxOrdinate by rememberDataSaverState(key = Constants.ZT_0002, initialValue = 0.0)
    var colloid by remember { mutableStateOf(selected.dosage.colloid.format(1)) }
    var coagulant by remember { mutableStateOf(selected.dosage.coagulant.format(1)) }
    var preColloid by remember { mutableStateOf(selected.dosage.preColloid.format(1)) }
    var preCoagulant by remember { mutableStateOf(selected.dosage.preCoagulant.format(1)) }
    var glueSpeed by remember { mutableStateOf(selected.speed.glue.format(1)) }
    var preSpeed by remember { mutableStateOf(selected.speed.pre.format(1)) }

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            )
            .windowInsetsPadding(WindowInsets.imeAnimationSource),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "制胶/促凝剂 μL",
                    value = coagulant,
                    onValueChange = {
                        scope.launch {
                            coagulant = it
                            val dosage =
                                selected.dosage.copy(coagulant = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "制胶/胶体 μL",
                    value = colloid,
                    onValueChange = {
                        scope.launch {
                            colloid = it
                            val dosage =
                                selected.dosage.copy(colloid = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
            }
        }
        item {
            CircleTextField(
                title = "制胶/速度",
                value = glueSpeed,
                onValueChange = {
                    scope.launch {
                        glueSpeed = it
                        val speed =
                            selected.speed.copy(glue = it.toDoubleOrNull() ?: 0.0)
                        uiEvent(ProgramUiEvent.Update(selected.copy(speed = speed)))
                    }
                }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "预排/促凝剂 μL",
                    value = preCoagulant,
                    onValueChange = {
                        scope.launch {
                            preCoagulant = it
                            val dosage =
                                selected.dosage.copy(preCoagulant = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "预排/胶体 μL",
                    value = preColloid,
                    onValueChange = {
                        scope.launch {
                            preColloid = it
                            val dosage =
                                selected.dosage.copy(preColloid = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
            }
        }
        item {
            CircleTextField(
                title = "预排/速度",
                value = preSpeed,
                onValueChange = {
                    scope.launch {
                        preSpeed = it
                        val speed =
                            selected.speed.copy(pre = it.toDoubleOrNull() ?: 0.0)
                        uiEvent(ProgramUiEvent.Update(selected.copy(speed = speed)))
                    }
                }
            )
        }
        item {
            CoordinateInput(
                modifier = Modifier.fillMaxWidth(),
                title = "位置",
                coordinate = selected.coordinate,
                limit = Coordinate(maxAbscissa, maxOrdinate),
                onCoordinateChange = {
                    scope.launch {
                        uiEvent(ProgramUiEvent.Update(selected.copy(coordinate = it)))
                    }
                }
            ) {
                scope.launch {
                    serial {
                        timeout = 1000L * 60L
                        start(index = 1, pdv = 0.0)
                    }
                    serial {
                        timeout = 1000L * 60L
                        start(index = 0, pdv = selected.coordinate.abscissa)
                    }
                    serial {
                        timeout = 1000L * 60L
                        start(index = 1, pdv = selected.coordinate.ordinate)
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramListPreview() {
    ProgramList(
        uiState = ProgramUiState(
            entities = listOf(
                Program(text = "test")
            )
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramDetailPreview() {
    ProgramDetail(
        uiState = ProgramUiState(
            entities = listOf(
                Program(text = "test", id = 1L)
            ),
            selected = 1L
        )
    )
}