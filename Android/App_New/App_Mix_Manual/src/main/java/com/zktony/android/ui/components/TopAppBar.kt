package com.zktony.android.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.ProgramIntent
import com.zktony.android.ui.SettingIntent
import com.zktony.android.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/23 9:03
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    page: Int,
    navigation: () -> Unit
) {

    val navigationActions = LocalNavigationActions.current

    TopAppBar(
        title = {

        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 40.dp, vertical = 4.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(50.dp)
            ) {
                AnimatedVisibility(visible = page != PageType.HOME && page != PageType.PROGRAM && page != PageType.EXPERIMENTRECORDS && page != PageType.SETTINGS) {
                    ElevatedButton(onClick = navigation) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = null
                        )
                    }
                }
                TOP_LEVEL_DESTINATIONS.forEach { destination ->

                    if ("首页".equals(destination.text)) {
                        Icon(
                            modifier = Modifier.clickable {
                                navigationActions.navigateTo(destination)
                            },
                            imageVector = destination.icon,
                            contentDescription = stringResource(id = destination.iconTextId)
                        )
                    } else {
                        Text(
                            modifier = Modifier
                                .clickable {
                                    navigationActions.navigateTo(destination)
                                },
                            color = if (page == destination.id) Color.Red else Color.Black,
                            text = destination.text
                        )
                    }

                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugModeAppBar(
    page: Int,
    navigation: () -> Unit
) {

    val navigationActions = LocalNavigationActions.current
    TopAppBar(
        title = {

        },
        actions = {
            if (page == PageType.DEBUGMODE) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 40.dp, vertical = 4.dp)
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(420.dp)
                ) {

                    Icon(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                navigation()
                            },
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )

                    Icon(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                navigationActions.navigateTo(TOP_LEVEL_DESTINATIONS[4])
                            },
                        imageVector = Icons.Default.Home,
                        contentDescription = null
                    )


                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 40.dp, vertical = 4.dp)
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(50.dp)
                ) {
                    AnimatedVisibility(visible = page != PageType.HOME && page != PageType.PROGRAM && page != PageType.EXPERIMENTRECORDS && page != PageType.SETTINGS) {
                        ElevatedButton(onClick = navigation) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null
                            )
                        }
                    }
                    TOP_LEVEL_DESTINATIONS.forEach { destination ->

                        if ("首页".equals(destination.text)) {
                            Icon(
                                modifier = Modifier.clickable {
                                    navigationActions.navigateTo(destination)
                                },
                                imageVector = destination.icon,
                                contentDescription = stringResource(id = destination.iconTextId)
                            )
                        } else {
                            Text(
                                modifier = Modifier
                                    .clickable {
                                        navigationActions.navigateTo(destination)
                                    },
                                color = if (page == destination.id) Color.Red else Color.Black,
                                text = destination.text
                            )
                        }

                    }
                }
            }


        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    page: Int,
    dispatch: (SettingIntent) -> Unit,
    navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                text = stringResource(id = R.string.setting),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = page == PageType.MOTOR_LIST) {
                    ElevatedButton(onClick = { scope.launch { dispatch(SettingIntent.Insert) } }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }

                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramAppBar(
    entities: List<Program>,
    selected: Long,
    page: Int,
    dispatch: (ProgramIntent) -> Unit,
    navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dialog by rememberSaveable { mutableStateOf(false) }

//    if (dialog) {
//        InputDialog(
//            onConfirm = {
//                scope.launch {
//                    dialog = false
//                    dispatch(ProgramIntent.Insert(it))
//                }
//            }
//        ) { dialog = false }
//    }

    TopAppBar(
        title = {
            if (page == PageType.PROGRAM_LIST) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 32.dp, vertical = 4.dp),
                    text = stringResource(id = R.string.program),
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                )
            } else {
                Column(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 32.dp, vertical = 4.dp)
                ) {
                    val program = entities.find { it.id == selected } ?: Program()
                    Text(
                        text = program.displayText,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                        )
                    )
                    Text(
                        text = program.createTime.dateFormat("yyyy/MM/dd"),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        ),
                        color = Color.Gray,
                    )
                }
            }
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = page == PageType.PROGRAM_LIST) {
                    ElevatedButton(onClick = { dialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }

                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        },
    )
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CalibrationAppBar(
//    entities: List<Calibration>,
//    selected: Long,
//    page: Int,
//    dispatch: (CalibrationIntent) -> Unit,
//    navigation: () -> Unit
//) {
//    val scope = rememberCoroutineScope()
//    var dialog by remember { mutableStateOf(false) }
//
//    if (dialog) {
//        InputDialog(
//            onConfirm = {
//                dispatch(CalibrationIntent.Insert(it))
//                dialog = false
//            },
//            onCancel = { dialog = false }
//        )
//    }
//
//    TopAppBar(
//        title = {
//            if (page == PageType.CALIBRATION_LIST) {
//                Text(
//                    modifier = Modifier
//                        .background(
//                            color = MaterialTheme.colorScheme.surfaceVariant,
//                            shape = MaterialTheme.shapes.small,
//                        )
//                        .padding(horizontal = 32.dp, vertical = 4.dp),
//                    text = stringResource(id = R.string.calibration),
//                    style = MaterialTheme.typography.headlineSmall
//                )
//            } else {
//                Column(
//                    modifier = Modifier
//                        .background(
//                            color = MaterialTheme.colorScheme.surfaceVariant,
//                            shape = MaterialTheme.shapes.small,
//                        )
//                        .padding(horizontal = 32.dp, vertical = 4.dp)
//                ) {
//                    val calibration = entities.find { it.id == selected } ?: Calibration(
//                        displayText = "None"
//                    )
//                    Text(
//                        text = calibration.displayText,
//                        style = TextStyle(
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 20.sp,
//                            fontStyle = FontStyle.Italic,
//                        )
//                    )
//                    Text(
//                        text = calibration.createTime.dateFormat("yyyy/MM/dd"),
//                        style = TextStyle(
//                            fontFamily = FontFamily.Monospace,
//                            fontSize = 12.sp,
//                        ),
//                        color = Color.Gray,
//                    )
//                }
//            }
//        },
//        actions = {
//            Row(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp, vertical = 4.dp)
//                    .background(
//                        color = MaterialTheme.colorScheme.surfaceVariant,
//                        shape = CircleShape
//                    )
//                    .padding(horizontal = 4.dp),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                ElevatedButton(onClick = {
//                    scope.launch {
//                        if (page == PageType.CALIBRATION_LIST) {
//                            dialog = true
//                        } else {
//                            val calibration = entities.find { it.id == selected } ?: Calibration(
//                                displayText = "None"
//                            )
//                            val points = calibration.points.toMutableList()
//                            points.add(Point(0.0, 0.0))
//                            dispatch(CalibrationIntent.Update(calibration.copy(points = points)))
//                        }
//                    }
//                }) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = null
//                    )
//                }
//
//                ElevatedButton(onClick = navigation) {
//                    Icon(
//                        imageVector = Icons.Default.Reply,
//                        contentDescription = null
//                    )
//                }
//            }
//        }
//    )
//}