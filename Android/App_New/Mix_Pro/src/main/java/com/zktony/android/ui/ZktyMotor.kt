package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.zktony.android.logic.data.entities.MotorEntity
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.android.ui.utils.PageType

/**
 * Motor screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel MotorViewModel
 * @return Unit
 */
@Composable
fun ZktyMotor(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyMotorViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val page = remember { mutableStateOf(PageType.LIST) }

    BackHandler {
        when (page.value) {
            PageType.LIST -> navController.navigateUp()
            else -> page.value = PageType.LIST
        }
    }

    Column(modifier = modifier) {
        // app bar
        ZktyTopAppBar(
            title = if (page.value == PageType.LIST) {
                stringResource(id = R.string.motor_config)
            } else {
                uiState.entities.find { it.id == uiState.selected }!!.text
            },
            navigation = {
                when (page.value) {
                    PageType.LIST -> navController.navigateUp()
                    else -> page.value = PageType.LIST
                }
            }
        )
        // motor list
        AnimatedVisibility(visible = page.value == PageType.LIST) {
            MotorList(
                modifier = Modifier,
                uiState = uiState,
                navigationToEdit = { page.value = PageType.EDIT },
                toggleSelected = viewModel::toggleSelected,
            )
        }
        // motor edit
        AnimatedVisibility(visible = page.value == PageType.EDIT) {
            MotorEdit(
                modifier = Modifier,
                entity = uiState.entities.find { it.id == uiState.selected }!!,
                navigationToList = { page.value = PageType.LIST },
                update = viewModel::update,
            )
        }
    }
}

@Composable
fun MotorList(
    modifier: Modifier = Modifier,
    uiState: MotorUiState = MotorUiState(),
    navigationToEdit: () -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        columns = GridCells.Fixed(3)
    ) {
        uiState.entities.forEach {
            item {
                Card(
                    modifier = Modifier.clickable {
                        toggleSelected(it.id)
                        navigationToEdit()
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
                                text = "S - ${it.speed}", style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "A - ${it.acc}", style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "D - ${it.dec}", style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MotorEdit(
    modifier: Modifier = Modifier,
    entity: MotorEntity = MotorEntity(),
    navigationToList: () -> Unit = {},
    update: (MotorEntity) -> Unit = {},
) {
    var speed by remember { mutableStateOf(entity.speed) }
    var acc by remember { mutableStateOf(entity.acc) }
    var dec by remember { mutableStateOf(entity.dec) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium
            ),
        verticalArrangement = Arrangement.Center,
    ) {

        Text(
            modifier = Modifier.padding(start = 32.dp),
            text = stringResource(id = R.string.speed),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
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
            modifier = Modifier.padding(start = 32.dp),
            text = stringResource(id = R.string.acceleration),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_rocket),
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
            modifier = Modifier.padding(start = 32.dp),
            text = stringResource(id = R.string.deceleration),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_turtle),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    modifier = Modifier
                        .width(192.dp)
                        .padding(top = 16.dp),
                    onClick = {
                        update(entity.copy(speed = speed, acc = acc, dec = dec))
                        navigationToList()
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorListPreview() {
    MotorList(
        modifier = Modifier, uiState = MotorUiState(
            entities = listOf(
                MotorEntity(text = "M1"), MotorEntity(text = "M2")
            )
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorEditPreview() {
    MotorEdit(
        modifier = Modifier, entity = MotorEntity(text = "M1")
    )
}