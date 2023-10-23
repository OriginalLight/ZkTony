package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.HOME))
                else -> {}
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.uiEvent(HomeUiEvent.Message(null))
        }
    }

    HomeWrapper(
        entities = entities,
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeWrapper(
    entities: LazyPagingItems<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit,
    navigation: () -> Unit
) {

    Column {
        HomeAppBar(uiState = uiState) { navigation() }
        AnimatedContent(targetState = uiState.page) {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> ProgramList(entities, uiEvent)
                PageType.HOME -> JobContent(entities.toList(), uiState, uiEvent)
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    uiEvent: (HomeUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier.padding(16.dp),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            ListItem(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        scope.launch {
                            uiEvent(HomeUiEvent.ToggleSelected(item.id))
                            uiEvent(HomeUiEvent.NavTo(PageType.HOME))
                        }
                    },
                headlineContent = {
                    Text(
                        text = item.displayText,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = item.createTime.dateFormat("yyyy/MM/dd"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                },
                leadingContent = {
                    Text(
                        text = "${index + 1}、",
                        style = MaterialTheme.typography.headlineSmall,
                        fontStyle = FontStyle.Italic
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobContent(
    entities: List<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    var time by remember { mutableLongStateOf(0L) }
    val item = entities.find { it.id == uiState.selected } ?: Program()

    LaunchedEffect(key1 = uiState.selected) {
        if (uiState.selected == 0L && entities.isNotEmpty()) {
            uiEvent(HomeUiEvent.ToggleSelected(entities[0].id))
        }
    }

    LaunchedEffect(key1 = uiState.job) {
        while (true) {
            if (uiState.job != null) {
                time += 1
            } else {
                time = 0
            }
            delay(1000L)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card(
                onClick = {
                    scope.launch {
                        if (uiState.job == null) {
                            uiEvent(HomeUiEvent.NavTo(PageType.PROGRAM_LIST))
                        }
                    }
                },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = item.displayText,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontStyle = FontStyle.Italic,
                            )
                        )
                        Text(
                            text = item.createTime.dateFormat("yyyy/MM/dd"),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                            ),
                            color = Color.Gray,
                        )
                    }
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }
            }

            Card {
                Box(
                    modifier = Modifier.padding(8.dp),
                ) {
                    if (uiState.job != null) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopStart),
                            strokeWidth = 4.dp,
                        )
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = time.timeFormat(),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Card {
                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = stringResource(id = R.string.glue_making),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                Card(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = item.dosage.coagulant.format(1),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                    )
                }


                Card(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = item.dosage.colloid.format(1),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Card {
                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = stringResource(id = R.string.pre_drain),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                Card(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = item.dosage.preCoagulant.format(1),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                    )
                }

                Card(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = item.dosage.preColloid.format(1),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(196.dp)
                    .clip(CircleShape)
                    .clickable {
                        scope.launch {
                            if (uiState.uiFlags == UiFlags.NONE) {
                                if (uiState.job == null) {
                                    if (uiState.selected == 0L) {
                                        navigationActions.navigate(Route.PROGRAM)
                                    } else {
                                        uiEvent(HomeUiEvent.Start)
                                    }
                                } else {
                                    uiEvent(HomeUiEvent.Stop)
                                }
                            } else {
                                snackbarHostState.showSnackbar("请先完成当前操作")
                            }
                        }
                    },
                imageVector = if (uiState.job == null) Icons.Default.PlayArrow else Icons.Default.Close,
                contentDescription = null,
                tint = if (uiState.job == null) MaterialTheme.colorScheme.primary else Color.Red
            )

            if (uiState.job == null) {
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE) {
                                        uiEvent(HomeUiEvent.Reset)
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.RESET) "复位中" else "复位",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.RESET) Color.Red else Color.Unspecified
                    )
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.CLEAN) {
                                        uiEvent(HomeUiEvent.Clean)
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.CLEAN) "清洗中" else "清洗",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.CLEAN) Color.Red else Color.Unspecified
                    )
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_IN) {
                                        if (uiState.uiFlags == UiFlags.NONE) {
                                            uiEvent(HomeUiEvent.Pipeline(1))
                                        } else {
                                            uiEvent(HomeUiEvent.Pipeline(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.PIPELINE_IN) "填充胶体中" else "填充胶体",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.PIPELINE_IN) Color.Red else Color.Unspecified
                    )
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_OUT) {
                                        if (uiState.uiFlags == UiFlags.NONE) {
                                            uiEvent(HomeUiEvent.Pipeline(2))
                                        } else {
                                            uiEvent(HomeUiEvent.Pipeline(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.PIPELINE_OUT) "回吸胶体中" else "回吸胶体",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.PIPELINE_OUT) Color.Red else Color.Unspecified
                    )
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.SYRINGE_IN) {
                                        if (uiState.uiFlags == UiFlags.NONE) {
                                            uiEvent(HomeUiEvent.Syringe(1))
                                        } else {
                                            uiEvent(HomeUiEvent.Syringe(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.SYRINGE_IN) "填充促凝剂中" else "填充促凝剂",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.SYRINGE_IN) Color.Red else Color.Unspecified
                    )
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.SYRINGE_OUT) {
                                        if (uiState.uiFlags == UiFlags.NONE) {
                                            uiEvent(HomeUiEvent.Pipeline(2))
                                        } else {
                                            uiEvent(HomeUiEvent.Pipeline(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.SYRINGE_OUT) "回吸促凝剂中" else "回吸促凝剂",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.SYRINGE_OUT) Color.Red else Color.Unspecified
                    )
                }
            }
        }
    }
}