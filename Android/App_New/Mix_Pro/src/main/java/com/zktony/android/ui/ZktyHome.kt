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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.utils.PageType
import org.koin.androidx.compose.koinViewModel


@Composable
fun ZktyHome(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyHomeViewModel = koinViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val page = remember { mutableStateOf(PageType.MENU) }
    val content = LocalContext.current

    BackHandler {
        when (page.value) {
            PageType.MENU -> {}
            else -> page.value = PageType.MENU
        }
    }

    Column(modifier = modifier) {
        // menu
        AnimatedVisibility(visible = page.value == PageType.MENU) {
            HomeMenu(
                modifier = Modifier
            )
        }
        // select
        AnimatedVisibility(visible = page.value == PageType.SELECT) {

        }
    }
}


@Composable
fun HomeMenu(
    modifier: Modifier = Modifier,
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
            columns = GridCells.Fixed(3),
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
            title = "开始程序"
        ) {
            Image(
                modifier = Modifier.size(64.dp),
                painter = painterResource(id = R.drawable.ic_start),
                contentDescription = null,
            )
        }

    }
}

@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = { },
    image: @Composable () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            image()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun HomeMenuPreview() {
    HomeMenu()
}