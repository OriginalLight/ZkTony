package com.zktony.android.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.utils.PageType
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

    val keyboard = LocalSoftwareKeyboardController.current

    /**
     * 复位高度
     */
    val fwgd = rememberDataSaverState(key = "fwgd", default = 0f)
    var fwgd_ex by remember { mutableStateOf(fwgd.value.format(4)) }


    /**
     * 盘子距离
     */
    val pzjl = rememberDataSaverState(key = "pzjl", default = 0f)
    var pzjl_ex by remember { mutableStateOf(pzjl.value.format(4)) }


    /**
     * 夹爪高度
     */
    val jzgd = rememberDataSaverState(key = "jzgd", default = 0f)
    var jzgd_ex by remember { mutableStateOf(jzgd.value.format(4)) }

    /**
     * 分离距离
     */
    val fljl = rememberDataSaverState(key = "fljl", default = 0f)
    var fljl_ex by remember { mutableStateOf(fljl.value.format(4)) }

    /**
     * 矫正高度
     */
    val jiaozgd = rememberDataSaverState(key = "jiaozgd", default = 0f)
    var jiaozgd_ex by remember { mutableStateOf(jiaozgd.value.format(4)) }

    /**
     * 上盘高度
     */
    val spgd = rememberDataSaverState(key = "spgd", default = 0f)
    var spgd_ex by remember { mutableStateOf(spgd.value.format(4)) }


    /**
     * 复位高度
     */
    val fwgd2 = rememberDataSaverState(key = "fwgd2", default = 0f)
    var fwgd2_ex by remember { mutableStateOf(fwgd2.value.format(4)) }

    /**
     * 盘子距离
     */
    val pzjl2 = rememberDataSaverState(key = "pzjl2", default = 0f)
    var pzjl2_ex by remember { mutableStateOf(pzjl2.value.format(4)) }

    /**
     * 上盘高度
     */
    val spgd2 = rememberDataSaverState(key = "spgd2", default = 0f)
    var spgd2_ex by remember { mutableStateOf(spgd2.value.format(4)) }

    /**
     * 夹紧距离
     */
    val jjjl = rememberDataSaverState(key = "jjjl", default = 0f)
    var jjjl_ex by remember { mutableStateOf(jjjl.value.format(4)) }

    /**
     * 松开距离
     */
    val skjl = rememberDataSaverState(key = "skjl", default = 0f)
    var skjl_ex by remember { mutableStateOf(skjl.value.format(4)) }

    /**
     * 上盘原点距离
     */
    val spydjl = rememberDataSaverState(key = "spydjl", default = 0f)
    var spydjl_ex by remember { mutableStateOf(spydjl.value.format(4)) }

    /**
     * 上盘孔位距离1
     */
    val spkwjl1 = rememberDataSaverState(key = "spkwjl1", default = 0f)
    var spkwjl1_ex by remember { mutableStateOf(spkwjl1.value.format(4)) }

    /**
     * 上盘孔位距离2
     */
    val spkwjl2 = rememberDataSaverState(key = "spkwjl2", default = 0f)
    var spkwjl2_ex by remember { mutableStateOf(spkwjl2.value.format(4)) }

    /**
     * 上盘孔位距离3
     */
    val spkwjl3 = rememberDataSaverState(key = "spkwjl3", default = 0f)
    var spkwjl3_ex by remember { mutableStateOf(spkwjl3.value.format(4)) }

    /**
     * 上盘孔位距离4
     */
    val spkwjl4 = rememberDataSaverState(key = "spkwjl4", default = 0f)
    var spkwjl4_ex by remember { mutableStateOf(spkwjl4.value.format(4)) }

    /**
     * 上盘孔位距离5
     */
    val spkwjl5 = rememberDataSaverState(key = "spkwjl5", default = 0f)
    var spkwjl5_ex by remember { mutableStateOf(spkwjl5.value.format(4)) }

    /**
     * 上盘孔位距离6
     */
    val spkwjl6 = rememberDataSaverState(key = "spkwjl6", default = 0f)
    var spkwjl6_ex by remember { mutableStateOf(spkwjl6.value.format(4)) }

    /**
     * 上盘孔位距离7
     */
    val spkwjl7 = rememberDataSaverState(key = "spkwjl7", default = 0f)
    var spkwjl7_ex by remember { mutableStateOf(spkwjl7.value.format(4)) }

    /**
     * 上盘孔位距离8
     */
    val spkwjl8 = rememberDataSaverState(key = "spkwjl8", default = 0f)
    var spkwjl8_ex by remember { mutableStateOf(spkwjl8.value.format(4)) }


    /**
     * 下盘原点距离
     */
    val xpydjl = rememberDataSaverState(key = "xpydjl", default = 0f)
    var xpydjl_ex by remember { mutableStateOf(xpydjl.value.format(4)) }

    /**
     * 下盘孔位距离1
     */
    val xpkwjl1 = rememberDataSaverState(key = "xpkwjl1", default = 0f)
    var xpkwjl1_ex by remember { mutableStateOf(xpkwjl1.value.format(4)) }

    /**
     * 下盘孔位距离2
     */
    val xpkwjl2 = rememberDataSaverState(key = "xpkwjl2", default = 0f)
    var xpkwjl2_ex by remember { mutableStateOf(xpkwjl2.value.format(4)) }

    /**
     * 下盘孔位距离3
     */
    val xpkwjl3 = rememberDataSaverState(key = "xpkwjl3", default = 0f)
    var xpkwjl3_ex by remember { mutableStateOf(xpkwjl3.value.format(4)) }


    /**
     * 加液前
     */
    val jyq = rememberDataSaverState(key = "jyq", default = 0f)
    var jyq_ex by remember { mutableStateOf(jyq.value.format(4)) }

    /**
     * 加液后
     */
    val jyh = rememberDataSaverState(key = "jyh", default = 0f)
    var jyh_ex by remember { mutableStateOf(jyh.value.format(4)) }

    /**
     * 加液前2
     */
    val jyq2 = rememberDataSaverState(key = "jyq2", default = 0f)
    var jyq_ex2 by remember { mutableStateOf(jyq2.value.format(4)) }

    /**
     * 加液后2
     */
    val jyh2 = rememberDataSaverState(key = "jyh2", default = 0f)
    var jyh_ex2 by remember { mutableStateOf(jyh2.value.format(4)) }

    /**
     * ui的坐标
     */
    val valveOne = rememberDataSaverState(key = "valveOne", default = 0)
    var valveOne_ex by remember { mutableStateOf(0) }


    /**
     * 判断是否复位
     * true=复位完成
     * false=没复位
     */
    val isResetBool = rememberDataSaverState(key = "isResetBool", default = false)
    var isResetBool_ex by remember { mutableStateOf(false) }

    isResetBool.value = false
    isResetBool_ex = false

    val context = LocalContext.current;

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

    LazyColumn {
        item {
            Row {
                Text(
                    text = "举升1",
                    fontSize = 30.sp
                )
                Button(
                    onClick = {
                        event(
                            CalibrationEvent.Reset(
                                listOf(1),
                                spydjl.value.toDouble(),
                                xpydjl.value.toDouble(),
                                fwgd.value.toDouble(),
                                fwgd2.value.toDouble()
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 20.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("复    位")
                }

                Button(
                    onClick = {
                        valveOne.value = 0
                        valveOne_ex = 0

                        isResetBool.value = true
                        isResetBool_ex = true
                        event(
                            CalibrationEvent.Reset(
                                listOf(1, 0, 2, 4, 3),
                                spydjl.value.toDouble(),
                                xpydjl.value.toDouble(),
                                fwgd.value.toDouble(),
                                fwgd2.value.toDouble()
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 500.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("全部复位")
                }
            }

            Row {

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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 1
                                        pulse = (3200L * fwgd.value.toDouble()).toLong()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 1
                                        pulse = (3200L * pzjl.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 1
                                        pulse = (3200L * jzgd.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {

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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 1
                                        pulse = (3200L * fljl.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 1
                                        pulse = (3200L * jiaozgd.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spgd_ex,
                    onValueChange = {
                        scope.launch {
                            spgd_ex = it
                            spgd.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "上盘高度") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 1
                                        pulse = (3200L * spgd.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {
                Text(
                    text = "举升2",
                    fontSize = 30.sp
                )
                Button(
                    onClick = {
                        event(
                            CalibrationEvent.Reset(
                                listOf(0),
                                spydjl.value.toDouble(),
                                xpydjl.value.toDouble(),
                                fwgd.value.toDouble(),
                                fwgd2.value.toDouble()
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 20.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("复    位")
                }

            }

            Row {


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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 0
                                        pulse = (3200L * fwgd2.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 0
                                        pulse = (3200L * pzjl2.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spgd2_ex,
                    onValueChange = {
                        scope.launch {
                            spgd2_ex = it
                            spgd2.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "上盘高度") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 0
                                        pulse = (3200L * spgd2.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {
                Text(
                    text = "夹爪",
                    fontSize = 30.sp
                )
                Button(
                    onClick = {
                        event(
                            CalibrationEvent.Reset(
                                listOf(2),
                                spydjl.value.toDouble(),
                                xpydjl.value.toDouble(),
                                fwgd.value.toDouble(),
                                fwgd2.value.toDouble()
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 20.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("复    位")
                }

            }

            Row {

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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 2
                                        pulse = (3200L * jjjl.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 2
                                        pulse = (3200L * skjl.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {
                Text(
                    text = "上盘",
                    fontSize = 30.sp
                )
                Button(
                    onClick = {
                        valveOne.value = 0
                        valveOne_ex = 0
                        event(
                            CalibrationEvent.Reset(
                                listOf(3),
                                spydjl.value.toDouble(),
                                xpydjl.value.toDouble(),
                                fwgd.value.toDouble(),
                                fwgd2.value.toDouble()
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 20.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("复    位")
                }

            }

            Row {

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = spydjl_ex,
                    onValueChange = {
                        scope.launch {
                            spydjl_ex = it
                            spydjl.value = it.toFloatOrNull() ?: 0f
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
                        scope.launch {

                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spydjl.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spkwjl1_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl1_ex = it
                            spkwjl1.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离1") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl1.value.toDouble()).toLong()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spkwjl2_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl2_ex = it
                            spkwjl2.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离2") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl2.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = spkwjl3_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl3_ex = it
                            spkwjl3.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离3") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl3.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spkwjl4_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl4_ex = it
                            spkwjl4.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离4") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl4.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spkwjl5_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl5_ex = it
                            spkwjl5.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离5") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl5.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = spkwjl6_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl6_ex = it
                            spkwjl6.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离6") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl6.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spkwjl7_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl7_ex = it
                            spkwjl7.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离7") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl7.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = spkwjl8_ex,
                    onValueChange = {
                        scope.launch {
                            spkwjl8_ex = it
                            spkwjl8.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离8") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 3
                                        pulse = (3200L * spkwjl8.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {
                Text(
                    text = "下盘",
                    fontSize = 30.sp
                )
                Button(
                    onClick = {
                        event(
                            CalibrationEvent.Reset(
                                listOf(4),
                                spydjl.value.toDouble(),
                                xpydjl.value.toDouble(),
                                fwgd.value.toDouble(),
                                fwgd2.value.toDouble()
                            )
                        )
                    },
                    modifier = Modifier.padding(start = 20.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("复    位")
                }

            }

            Row {

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = xpydjl_ex,
                    onValueChange = {
                        scope.launch {
                            xpydjl_ex = it
                            xpydjl.value = it.toFloatOrNull() ?: 0f
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
                        scope.launch {
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 4
                                        pulse = (1300 * xpydjl.value.toDouble()).toLong()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = xpkwjl1_ex,
                    onValueChange = {
                        scope.launch {
                            xpkwjl1_ex = it
                            xpkwjl1.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离1") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 4
                                        pulse = (3255L * xpkwjl1.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = xpkwjl2_ex,
                    onValueChange = {
                        scope.launch {
                            xpkwjl2_ex = it
                            xpkwjl2.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离2") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 4
                                        pulse = (3255L * xpkwjl2.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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

            Row {

                OutlinedTextField(
                    modifier = Modifier.width(100.dp),
                    value = xpkwjl3_ex,
                    onValueChange = {
                        scope.launch {
                            xpkwjl3_ex = it
                            xpkwjl3.value = it.toFloatOrNull() ?: 0f
                        }
                    },
                    label = { Text(text = "孔位距离3") },
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
                            try {
                                tx {
                                    move(MoveType.MOVE_PULSE) {
                                        index = 4
                                        pulse = (3255L * xpkwjl3.value.toDouble()).toLong();
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "请先复位再移动！",
                                    Toast.LENGTH_SHORT
                                ).show();
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



            Row {
                Text(
                    text = "蠕动泵1",
                    fontSize = 30.sp
                )
                Button(
                    onClick = {
                        scope.launch {
                            tx {
                                move(MoveType.MOVE_PULSE) {
                                    index = 5
                                    pulse = 32000L
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(start = 20.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("加    液")
                }


                Text(
                    text = "蠕动泵2",
                    fontSize = 30.sp,
                    modifier = Modifier.padding(start = 20.dp),
                )
                Button(
                    onClick = {
                        scope.launch {
                            tx {
                                move(MoveType.MOVE_PULSE) {
                                    index = 6
                                    pulse = 32000L
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(start = 20.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("加    液")
                }

            }

            Row {
                OutlinedTextField(
                    modifier = Modifier
                        .width(100.dp),
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
                        .width(120.dp)
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


                OutlinedTextField(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(start = 20.dp),
                    value = jyq_ex2,
                    onValueChange = {
                        scope.launch {
                            jyq_ex2 = it
                            jyq2.value = it.toFloatOrNull() ?: 0f
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
                        .width(120.dp)
                        .padding(start = 20.dp),
                    value = jyh_ex2,
                    onValueChange = {
                        scope.launch {
                            jyh_ex2 = it
                            jyh2.value = it.toFloatOrNull() ?: 0f
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