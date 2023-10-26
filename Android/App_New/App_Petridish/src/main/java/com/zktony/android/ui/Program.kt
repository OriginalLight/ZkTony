package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.ext.format
import com.zktony.android.utils.ext.showShortToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


/**
 * The Program composable function for the app.
 *
 * @param modifier The modifier for the composable.
 * @param navController The NavHostController for the app.
 * @param viewModel The ProgramViewModel for the app.
 */
@Composable
fun Program(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProgramViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle the back button press
    BackHandler {
        when (uiState.page) {
            PageType.PROGRAM_LIST -> navController.navigateUp() // Step 1: Navigate up if on the list page
            else -> viewModel.event(ProgramEvent.NavTo(PageType.PROGRAM_LIST)) // Step 2: Navigate to the list page if on any other page
        }
    }

    // Display the list page
    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
        ProgramList(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
        )
    }
}

/**
 * The ListContent composable function for the app.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The ProgramUiState for the app.
 * @param event The event handler for the app.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    /**
     * 体积/mL
     */
    val tj = rememberDataSaverState(key = "tj", default = 0f)
    var tj_ex by remember { mutableStateOf(tj.value.format(1)) }
    val keyboard = LocalSoftwareKeyboardController.current

    // Show the input dialog if showDialog is true
    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        "Name already exists".showShortToast()
                    } else {
                        event(ProgramEvent.Insert(it))
                        showDialog = false
                    }
                }
            },
            onCancel = { showDialog = false },
        )
    }

    Row {
        Column(
            modifier
                .height(700.dp)
                .width(800.dp)
                //设置边框的宽度为10dp,颜色为Yellow,设置圆角为20dp
                .border(1.dp, Color.Black)

        ) {
            Box(modifier = Modifier.height(700.dp), contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 120.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    text = "0"
                )
            }


        }
        Column {
            OutlinedTextField(
                modifier = Modifier
                    .width(100.dp)
                    .padding(start = 20.dp, top = 20.dp),
                value = tj_ex,
                onValueChange = {
                    scope.launch {
                        tj_ex = it
                        tj.value = it.toFloatOrNull() ?: 0f
                    }
                },
                label = { Text(text = "体积") },
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboard?.hide()
                    }
                ),
            )

            OutlinedTextField(
                modifier = Modifier
                    .width(100.dp)
                    .padding(start = 20.dp, top = 20.dp),
                enabled = false,
                value = tj_ex,
                onValueChange = {
                    scope.launch {
                        tj_ex = it
                        tj.value = it.toFloatOrNull() ?: 0f
                    }
                },
                label = { Text(text = "计数") },
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboard?.hide()
                    }
                ),
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                text = "当前停止不是即停,需要等当前举升1上方所有培养血清空后停止，如需即停，请关闭电源"
            )

            Button(
                onClick = {

                },
                modifier = Modifier
                    .padding(start = 10.dp, top = 10.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("运    动")
            }


        }
    }


}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramListPreview() {
    // Call the ListContent function and pass in a ProgramUiState object as a parameter
    ProgramList(
        uiState = ProgramUiState(
            entities = listOf(
                Program(text = "test")
            )
        )
    )
}