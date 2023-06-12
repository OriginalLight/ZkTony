package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.zktony.android.R
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationType
import com.zktony.android.ui.utils.PageType
import com.zktony.core.ext.format
import org.koin.androidx.compose.koinViewModel


@Composable
fun ZktyHome(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    toggleDrawer: (NavigationType) -> Unit = {},
    viewModel: ZktyHomeViewModel = koinViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.LIST -> {}
            else -> viewModel.navTo(PageType.LIST)
        }
    }

    Column(modifier = modifier) {
        AnimatedVisibility(visible = uiState.page == PageType.START) {
            ZktyTopAppBar(
                title = stringResource(id = R.string.tab_program),
                navigation = { viewModel.navTo(PageType.LIST) }
            )
        }
        // menu
        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            HomeMenu(
                modifier = Modifier,
                uiState = uiState,
                navTo = viewModel::navTo,
                navController = navController,
            )
        }
        // start
        AnimatedVisibility(visible = uiState.page == PageType.START) {
            HomeStart(
                modifier = Modifier,
                uiState = uiState,
            )
        }
    }
}


@Composable
fun HomeMenu(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    navTo: (PageType) -> Unit = {},
    navController: NavHostController,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
        verticalArrangement = Arrangement.Center,
    ) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MenuCard(title = "复 位") {
                    Image(
                        modifier = Modifier.size(64.dp),
                        painter = painterResource(id = R.drawable.ic_reset),
                        contentDescription = null,
                    )
                }
            }
            item {
                MenuCard(title = "填充-促凝剂") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_syringe),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                        )
                    }
                }
            }
            item {
                MenuCard(title = "回吸-促凝剂") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_syringe),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            }
            item {
                MenuCard(title = "填充-胶体") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_pipeline),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.TopEnd),
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                        )
                    }
                }
            }
            item {
                MenuCard(title = "回吸-胶体") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_pipeline),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.TopEnd),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            }
        }

        MenuCard(
            modifier = Modifier.padding(horizontal = 196.dp),
            title = "开始程序",
            onClick = {
                if (uiState.entities.isEmpty()) {
                    navController.navigate(Route.PROGRAM)
                } else {
                    navTo(PageType.START)
                }
            }
        ) {
            Image(
                modifier = Modifier.size(64.dp),
                painter = painterResource(id = R.drawable.ic_start),
                contentDescription = null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = { },
    colors: CardColors = CardDefaults.elevatedCardColors(),
    image: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        colors = colors,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            image()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeStart(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.entities.forEach {
                item {
                    Card(
                        onClick = { }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Box {
                                Image(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterStart),
                                    painter = painterResource(id = R.drawable.ic_program),
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = it.text,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Divider()
                            Row {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.volume[0].format(1) + " μL",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.volume[1].format(1) + " μL",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            Divider()
                            Row {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.volume[2].format(1) + " μL",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.volume[3].format(1) + " μL",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun HomeMenuPreview() {
    HomeMenu(
        uiState = HomeUiState(),
        navController = rememberNavController()
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun HomeStartPreview() {
    HomeStart(uiState = HomeUiState())
}