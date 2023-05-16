package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.entity.MotorEntity
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.Ext
import kotlinx.coroutines.launch

/**
 * Motor screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel MotorViewModel
 * @return Unit
 */
@Composable
fun MotorScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: MotorViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BackHandler {
        if (uiState.page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(PageEnum.MAIN)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ZkTonyTopAppBar(
                title = if (uiState.page == PageEnum.MAIN) {
                    stringResource(id = R.string.motor_config)
                } else {
                    uiState.entities.find { it.id == uiState.selected }!!.text
                },
                navigation = {
                    if (uiState.page == PageEnum.MAIN) {
                        navController.navigateUp()
                    } else {
                        viewModel.navigationTo(PageEnum.MAIN)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        ),
                ) {
                    AnimatedVisibility(
                        visible = uiState.page == PageEnum.MAIN,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        MotorMainPage(
                            modifier = Modifier,
                            uiState = uiState,
                            navigationTo = viewModel::navigationTo,
                            toggleSelected = viewModel::toggleSelected,
                        )
                    }
                    AnimatedVisibility(
                        visible = uiState.page == PageEnum.EDIT,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        MotorEditPage(
                            modifier = Modifier,
                            entity = uiState.entities.find { it.id == uiState.selected }!!,
                            navigationTo = viewModel::navigationTo,
                            update = viewModel::update,
                            showSnackbar = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = message)
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun MotorMainPage(
    modifier: Modifier = Modifier,
    uiState: MotorUiState = MotorUiState(),
    navigationTo: (PageEnum) -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    LazyVerticalGrid(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        columns = GridCells.Fixed(3)
    ) {
        uiState.entities.forEach {
            item {
                Card(
                    modifier = Modifier
                        .clickable {
                            toggleSelected(it.id)
                            navigationTo(PageEnum.EDIT)
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = it.text,
                            fontSize = 50.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Column(
                            modifier = Modifier.padding(start = 16.dp),
                        ) {
                            Text(
                                text = "S - ${it.speed}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "A - ${it.acc}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "D - ${it.dec}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MotorEditPage(
    modifier: Modifier = Modifier,
    entity: MotorEntity = MotorEntity(),
    navigationTo: (PageEnum) -> Unit = {},
    update: (MotorEntity) -> Unit = {},
    showSnackbar: (String) -> Unit = {},
) {
    var speed by remember { mutableStateOf(entity.speed) }
    var acc by remember { mutableStateOf(entity.acc) }
    var dec by remember { mutableStateOf(entity.dec) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Text(
            text = stringResource(id = R.string.speed),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_speed),
                contentDescription = stringResource(id = R.string.speed)
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = speed.toString(),
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = speed.toFloat(),
                onValueChange = { speed = it.toInt() },
                valueRange = 0f..600f,
            )
        }
        Text(
            text = stringResource(id = R.string.acceleration),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_acceleration),
                contentDescription = stringResource(id = R.string.acceleration)
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = acc.toString(),
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = acc.toFloat(),
                onValueChange = { acc = it.toInt() },
                valueRange = 10f..100f,
            )
        }
        Text(
            text = stringResource(id = R.string.deceleration),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_deceleration),
                contentDescription = stringResource(id = R.string.deceleration)
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = dec.toString(),
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = dec.toFloat(),
                onValueChange = { dec = it.toInt() },
                valueRange = 10f..100f,
            )
        }
        AnimatedVisibility(visible = entity.speed != speed || entity.acc != acc || entity.dec != dec) {
            Button(
                modifier = Modifier
                    .width(128.dp)
                    .padding(vertical = 16.dp),
                onClick = {
                    update(entity.copy(speed = speed, acc = acc, dec = dec))
                    navigationTo(PageEnum.MAIN)
                    showSnackbar(Ext.ctx.getString(R.string.save_success))
                },
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorMainPagePreview() {
    MotorMainPage(
        modifier = Modifier,
        uiState = MotorUiState(
            entities = listOf(
                MotorEntity(text = "M1"),
                MotorEntity(text = "M2")
            )
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorEditPagePreview() {
    MotorEditPage(
        modifier = Modifier,
        entity = MotorEntity(text = "M1")
    )
}