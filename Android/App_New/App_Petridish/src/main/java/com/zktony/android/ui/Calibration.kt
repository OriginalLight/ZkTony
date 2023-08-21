package com.zktony.android.ui

import android.text.style.BackgroundColorSpan
import android.widget.Button
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ext.dateFormat
import com.zktony.android.utils.ext.format
import com.zktony.android.utils.ext.showShortToast
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Calibration screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel CalibrationViewModel
 */
@Composable
fun Calibration(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: CalibrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.CALIBRATION_LIST -> navController.navigateUp()
            else -> viewModel.event(CalibrationEvent.NavTo(PageType.CALIBRATION_LIST))
        }
    }

    // List page
    AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_LIST) {
        CalibrationList(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CalibrationList(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    event: (CalibrationEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    val keyboard = LocalSoftwareKeyboardController.current

    /**
     * 复位高度
     */
    val fwgd = rememberDataSaverState(key = "fwgd", default = 0f)
    var fwgd_ex by remember { mutableStateOf(fwgd.value.format(1)) }


    /**
     * 盘子距离
     */
    val pzjl = rememberDataSaverState(key = "pzjl", default = 0f)
    var pzjl_ex by remember { mutableStateOf(pzjl.value.format(1)) }


    /**
     * 夹爪高度
     */
    val jzgd = rememberDataSaverState(key = "jzgd", default = 0f)
    var jzgd_ex by remember { mutableStateOf(jzgd.value.format(1)) }

    /**
     * 分离距离
     */
    val fljl = rememberDataSaverState(key = "fljl", default = 0f)
    var fljl_ex by remember { mutableStateOf(fljl.value.format(1)) }

    /**
     * 矫正高度
     */
    val jiaozgd = rememberDataSaverState(key = "jiaozgd", default = 0f)
    var jiaozgd_ex by remember { mutableStateOf(jiaozgd.value.format(1)) }

    /**
     * 复位高度
     */
    val fwgd2 = rememberDataSaverState(key = "fwgd2", default = 0f)
    var fwgd2_ex by remember { mutableStateOf(fwgd2.value.format(1)) }

    /**
     * 盘子距离
     */
    val pzjl2 = rememberDataSaverState(key = "pzjl2", default = 0f)
    var pzjl2_ex by remember { mutableStateOf(pzjl2.value.format(1)) }

    /**
     * 夹紧距离
     */
    val jjjl = rememberDataSaverState(key = "jjjl", default = 0f)
    var jjjl_ex by remember { mutableStateOf(jjjl.value.format(1)) }

    /**
     * 松开距离
     */
    val skjl = rememberDataSaverState(key = "skjl", default = 0f)
    var skjl_ex by remember { mutableStateOf(skjl.value.format(1)) }

    /**
     * 上盘距离
     */
    val spjl = rememberDataSaverState(key = "spjl", default = 0f)
    var spjl_ex by remember { mutableStateOf(spjl.value.format(1)) }

    /**
     * 下盘距离
     */
    val xpjl = rememberDataSaverState(key = "xpjl", default = 0f)
    var xpjl_ex by remember { mutableStateOf(xpjl.value.format(1)) }

    /**
     * 原点距离
     */
    val ydjl = rememberDataSaverState(key = "ydjl", default = 0f)
    var ydjl_ex by remember { mutableStateOf(ydjl.value.format(1)) }

    /**
     * 加液前
     */
    val jyq = rememberDataSaverState(key = "jyq", default = 0f)
    var jyq_ex by remember { mutableStateOf(jyq.value.format(1)) }

    /**
     * 加液后
     */
    val jyh = rememberDataSaverState(key = "jyh", default = 0f)
    var jyh_ex by remember { mutableStateOf(jyh.value.format(1)) }

    // Show the input dialog if showDialog is true
    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    // Check if the name already exists
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        "Name already exists".showShortToast()
                    } else {
                        // Insert the new item
                        event(CalibrationEvent.Insert(it))
                        showDialog = false
                    }
                }
            },
            onCancel = { showDialog = false },
        )
    }
    Row {
        Text(
            text = "举升1",
            fontSize = 30.sp
        )
        Button(
            onClick = {

            },
            modifier = Modifier.padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("复    位")
        }

        Button(
            onClick = {

            },
            modifier = Modifier.padding(start = 500.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("全部复位")
        }

        Button(
            onClick = {
//                event(CalibrationEvent.AddLiquid())
            },
            modifier = Modifier.padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("全部保存")
        }
    }



    Row(
        modifier = Modifier.padding(top = 40.dp),
    ) {

        OutlinedTextField(
            modifier = Modifier.width(100.dp),
            value = fwgd_ex,
            onValueChange = {
                scope.launch {
                    fwgd_ex = it
                    fwgd.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "复位高度") },
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

        Button(
            onClick = {
                scope.launch {
                    tx {
                        move{
                            index = 0
                            pulse = 3200L;
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }




        OutlinedTextField(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp),
            value = pzjl_ex,
            onValueChange = {
                scope.launch {
                    pzjl_ex = it
                    pzjl.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "盘子距离") },
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

        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * pzjl.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }


        OutlinedTextField(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp),
            value = jzgd_ex,
            onValueChange = {
                scope.launch {
                    jzgd_ex = it
                    jzgd.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "夹爪高度") },
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

        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * jzgd.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }

    }

    Row(
        modifier = Modifier.padding(top = 120.dp),
    ) {

        OutlinedTextField(
            modifier = Modifier.width(100.dp),
            value = fljl_ex,
            onValueChange = {
                scope.launch {
                    fljl_ex = it
                    fljl.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "分离距离") },
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

        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * fljl.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }

        OutlinedTextField(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp),
            value = jiaozgd_ex,
            onValueChange = {
                scope.launch {
                    jiaozgd_ex = it
                    jiaozgd.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "矫正高度") },
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

        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * jzgd.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }

    }

    Row(
        modifier = Modifier.padding(top = 190.dp),
    ) {
        Text(
            text = "举升2",
            fontSize = 30.sp
        )
        Button(
            onClick = {

            },
            modifier = Modifier.padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("复    位")
        }

    }

    Row(
        modifier = Modifier.padding(top = 230.dp),
    ) {


        OutlinedTextField(
            modifier = Modifier.width(100.dp),
            value = fwgd2_ex,
            onValueChange = {
                scope.launch {
                    fwgd2_ex = it
                    fwgd2.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "复位距离") },
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

        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * fwgd2.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }


        OutlinedTextField(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp),
            value = pzjl2_ex,
            onValueChange = {
                scope.launch {
                    pzjl2_ex = it
                    pzjl2.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "盘子距离") },
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
        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * pzjl2.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }

    }

    Row(
        modifier = Modifier.padding(top = 300.dp),
    ) {
        Text(
            text = "夹爪",
            fontSize = 30.sp
        )
        Button(
            onClick = {

            },
            modifier = Modifier.padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("复    位")
        }

    }

    Row(
        modifier = Modifier.padding(top = 340.dp),
    ) {

        OutlinedTextField(
            modifier = Modifier.width(100.dp),
            value = jjjl_ex,
            onValueChange = {
                scope.launch {
                    jjjl_ex = it
                    jjjl.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "夹紧距离") },
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
        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * jjjl.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }


        OutlinedTextField(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp),
            value = skjl_ex,
            onValueChange = {
                scope.launch {
                    skjl_ex = it
                    skjl.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "松开距离") },
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
        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * skjl.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }

    }


    Row(
        modifier = Modifier.padding(top = 410.dp),
    ) {
        Text(
            text = "上盘",
            fontSize = 30.sp
        )
        Button(
            onClick = {

            },
            modifier = Modifier.padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("复    位")
        }

    }

    Row(
        modifier = Modifier.padding(top = 450.dp),
    ) {

        OutlinedTextField(
            modifier = Modifier.width(100.dp),
            value = spjl_ex,
            onValueChange = {
                scope.launch {
                    spjl_ex = it
                    spjl.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "上盘距离") },
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
        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * spjl.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }


        OutlinedTextField(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 20.dp),
            value = xpjl_ex,
            onValueChange = {
                scope.launch {
                    xpjl_ex = it
                    xpjl.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "下盘距离") },
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
        Button(
            onClick = {
                scope.launch {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 3200L * xpjl.value.toLong();
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }

    }


    Row(
        modifier = Modifier.padding(top = 520.dp),
    ) {
        Text(
            text = "下盘",
            fontSize = 30.sp
        )
        Button(
            onClick = {

            },
            modifier = Modifier.padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("复    位")
        }

        Text(
            text = "蠕动泵",
            fontSize = 30.sp,
            modifier = Modifier.padding(start = 60.dp),
        )
        Button(
            onClick = {

            },
            modifier = Modifier.padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("加    液")
        }

    }

    Row(
        modifier = Modifier.padding(top = 570.dp),
    ) {

        OutlinedTextField(
            modifier = Modifier.width(100.dp),
            value = ydjl_ex,
            onValueChange = {
                scope.launch {
                    ydjl_ex = it
                    ydjl.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "原点距离") },
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
        Button(
            onClick = {

            },
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("移    动")
        }


        OutlinedTextField(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 30.dp),
            value = jyq_ex,
            onValueChange = {
                scope.launch {
                    jyq_ex = it
                    jyq.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "加液前") },
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
                .padding(start = 20.dp),
            value = jyh_ex,
            onValueChange = {
                scope.launch {
                    jyh_ex = it
                    jyh.value = it.toFloatOrNull() ?: 0f
                }
            },
            label = { Text(text = "加液后") },
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


    }


}


/**
 * Composable function that previews the calibration list content.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationListPreview() {
    // Create a calibration entity list with a single entity
    val entities = listOf(Calibration())

    // Create a calibration UI state with the entity list
    val uiState = CalibrationUiState(entities = entities)

    // Show the calibration list content
    CalibrationList(uiState = uiState)
}

/**
 * Composable function that previews the calibration edit content.
 */
//@Composable
//@Preview(showBackground = true, widthDp = 960, heightDp = 640)
//fun CalibrationDetailPreview() {
//    // Create a calibration entity list with a single entity
//    val entities = listOf(Calibration(id = 1L))
//
//    // Create a calibration UI state with the entity list and a selected entity ID
//    val uiState = CalibrationUiState(entities = entities, selected = 1L)
//
//    // Show the calibration edit content
//    CalibrationDetail(uiState = uiState)
//}