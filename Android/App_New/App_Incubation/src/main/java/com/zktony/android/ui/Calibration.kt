package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.components.CalibrationAppBar
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import kotlinx.coroutines.launch

@Composable
fun CalibrationRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: CalibrationViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.CALIBRATION_LIST -> navController.navigateUp()
                else -> viewModel.uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_LIST))
            }
        }
    }

    BackHandler { navigation() }

    Scaffold(
        topBar = {
            CalibrationAppBar(
                uiState = uiState,
                uiEvent = viewModel::uiEvent,
                navigation = navigation,
                snackbarHostState = snackbarHostState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        CalibrationScreen(
            modifier = modifier.padding(paddingValues),
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun CalibrationScreen(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState,
    uiEvent: (CalibrationUiEvent) -> Unit,
) {
    AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_LIST) {
        CalibrationList(modifier, uiState, uiEvent)
    }

    AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_DETAIL) {
        CalibrationDetail(modifier, uiState, uiEvent)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalibrationList(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    uiEvent: (CalibrationUiEvent) -> Unit = {},
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
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
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
                                    uiEvent(CalibrationUiEvent.ToggleSelected(0L))
                                } else {
                                    uiEvent(CalibrationUiEvent.ToggleSelected(item.id))
                                }
                            }
                        },
                        onDoubleClick = {
                            scope.launch {
                                uiEvent(CalibrationUiEvent.ToggleSelected(item.id))
                                uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_DETAIL))
                            }
                        }
                    ),
                colors = CardDefaults.cardColors(containerColor = background)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

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

@Composable
fun CalibrationDetail(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    uiEvent: (CalibrationUiEvent) -> Unit = {},
) {

    val entity = uiState.entities.find { it.id == uiState.selected } ?: Calibration()
    val number by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
    val list = remember {
        mutableStateListOf<String>().apply {
            repeat(number / 4) { add("M$it") }
        }
    }

    LazyVerticalGrid(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            ),
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items = entity.data) { index, it ->
            Row(
                modifier = Modifier
                    .background(
                        color = Color.Transparent,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${index + 1}、",
                    style = MaterialTheme.typography.titleLarge,
                    fontStyle = FontStyle.Italic
                )

                Column {
                    Text(
                        text = list[it.first],
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = it.second.format(2) + " μL",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { uiEvent(CalibrationUiEvent.DeleteData(it)) },
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationListPreview() {
    val entities = listOf(Calibration())
    val uiState = CalibrationUiState(entities = entities)
    CalibrationList(uiState = uiState)
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationDetailPreview() {
    val entities = listOf(Calibration(id = 1L))
    val uiState = CalibrationUiState(entities = entities, selected = 1L)
    CalibrationDetail(uiState = uiState)
}