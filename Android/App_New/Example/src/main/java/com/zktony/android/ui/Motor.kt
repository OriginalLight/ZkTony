package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.model.Motor
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToLong

/**
 * The Motor screen composable function.
 *
 * @param modifier The modifier for the composable.
 * @param navController The NavHostController for the screen.
 * @param viewModel The MotorViewModel for the screen.
 */
@Composable
fun Motor(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: MotorViewModel = koinViewModel(),
) {
    // Observe changes in the UI state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button press
    BackHandler {
        when (uiState.page) {
            PageType.LIST -> navController.navigateUp()
            else -> viewModel.event(MotorEvent.NavTo(PageType.LIST))
        }
    }

    // Render the content of the screen
    ContentWrapper(
        modifier = modifier,
        uiState = uiState,
        event = viewModel::event,
        navController = navController,
    )
}

/**
 * The ContentWrapper composable function for the Motor screen.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The MotorUiState for the screen.
 * @param event The event handler for the screen.
 * @param navController The NavHostController for the screen.
 */
@Composable
fun ContentWrapper(
    modifier: Modifier = Modifier,
    uiState: MotorUiState,
    event: (MotorEvent) -> Unit = {},
    navController: NavHostController,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Display the title
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_engine),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.weight(1f))
            // Display the close button
            FloatingActionButton(
                onClick = {
                    when (uiState.page) {
                        PageType.LIST -> navController.navigateUp()
                        else -> event(MotorEvent.NavTo(PageType.LIST))
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            ListContent(
                modifier = Modifier,
                uiState = uiState,
                event = event,
            )
        }
        // Edit content
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            EditContent(
                modifier = Modifier,
                uiState = uiState,
                event = event,
            )
        }
    }
}

/**
 * The ListContent composable function for the Motor screen.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The MotorUiState for the screen.
 * @param event The event handler for the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    modifier: Modifier = Modifier,
    uiState: MotorUiState = MotorUiState(),
    event: (MotorEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = GridCells.Fixed(3)
    ) {
        items(items = uiState.entities) {
            Card(
                onClick = {
                    scope.launch {
                        event(MotorEvent.ToggleSelected(it.id)) // Step 1: Toggle the selected state of the entity
                        event(MotorEvent.NavTo(PageType.EDIT)) // Step 2: Navigate to the edit page
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

/**
 * The EditContent composable function for the Motor screen.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The MotorUiState for the screen.
 * @param event The event handler for the screen.
 */
@Composable
fun EditContent(
    modifier: Modifier = Modifier,
    uiState: MotorUiState = MotorUiState(),
    event: (MotorEvent) -> Unit = {},
) {
    // Get the selected entity from the UI state
    val entity = uiState.entities.find { it.id == uiState.selected }!!

    // Define the state variables for speed, acceleration, and deceleration
    var speed by remember { mutableStateOf(entity.speed) }
    var acc by remember { mutableStateOf(entity.acc) }
    var dec by remember { mutableStateOf(entity.dec) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            ),
        verticalArrangement = Arrangement.Center,
    ) {
        // Speed slider
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
                text = "S - $speed",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = speed.toFloat(),
                onValueChange = { speed = it.roundToLong() },
                valueRange = 0f..800f,
                steps = 79,
            )
        }

        // Acceleration slider
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
                text = "A - $acc",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = acc.toFloat(),
                onValueChange = { acc = it.roundToLong() },
                valueRange = 0f..800f,
                steps = 79,
            )
        }

        // Deceleration slider
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
                text = "D - $dec",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = dec.toFloat(),
                onValueChange = { dec = it.roundToLong() },
                valueRange = 0f..800f,
                steps = 79,
            )
        }

        // Show the update button if any of the values have changed
        AnimatedVisibility(visible = entity.speed != speed || entity.acc != acc || entity.dec != dec) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                FloatingActionButton(
                    modifier = Modifier.width(192.dp),
                    onClick = {
                        // Update the entity with the new values and navigate back to the list page
                        event(MotorEvent.Update(entity.copy(speed = speed, acc = acc, dec = dec)))
                        event(MotorEvent.NavTo(PageType.LIST))
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Done,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

/**
 * The MotorListContentPreview composable function for the Motor screen.
 * This function is used for previewing the ListContent composable.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The MotorUiState for the screen.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorListContentPreview(
    modifier: Modifier = Modifier,
    uiState: MotorUiState = MotorUiState(
        entities = listOf(
            Motor(text = "M1"), Motor(text = "M2")
        )
    )
) {
    ListContent(
        modifier = modifier,
        uiState = uiState,
    )
}

/**
 * The MotorEditContentPreview composable function for the Motor screen.
 * This function is used for previewing the EditContent composable.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorEditContentPreview() {
    EditContent(
        modifier = Modifier,
        uiState = MotorUiState(
            entities = listOf(
                Motor(text = "M1", id = 1L)
            ),
            selected = 1L
        )
    )
}