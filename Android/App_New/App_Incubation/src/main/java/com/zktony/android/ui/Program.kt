package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.zktony.android.data.entities.IncubationStageStatus
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.*
import com.zktony.android.ui.components.timeline.LazyTimeline
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.dateFormat
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
    uiEvent: (ProgramUiEvent) -> Unit,
) {
    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
        ProgramList(modifier, uiState, uiEvent)
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_DETAIL) {
        ProgramDetail(modifier, uiState, uiEvent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                shape = MaterialTheme.shapes.small
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
                colors = CardDefaults.cardColors(containerColor = background),
                onClick = {
                    scope.launch {
                        if (item.id == uiState.selected) {
                            uiEvent(ProgramUiEvent.ToggleSelected(0L))
                        } else {
                            uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                        }
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

@Composable
fun ProgramDetail(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    uiEvent: (ProgramUiEvent) -> Unit = {},
) {

    val scope = rememberCoroutineScope()
    val selected = uiState.entities.find { it.id == uiState.selected } ?: Program()

    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        LazyTimeline(
            modifier = Modifier.fillMaxWidth(0.5f),
            stages = selected.stages,
        ) {
            scope.launch {
                val list = selected.stages.toMutableList()
                list.forEachIndexed { index, item ->
                    if (index == it) {
                        list[index] = item.copy(status = IncubationStageStatus.CURRENT)
                    } else {
                        list[index] = item.copy(status = IncubationStageStatus.FINISHED)
                    }
                }
                uiEvent(ProgramUiEvent.Update(selected.copy(stages = list)))
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
            entities = listOf(Program(text = "test", id = 1L)),
            selected = 1L
        )
    )
}