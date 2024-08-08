package com.zktony.android.ui.components

import android.graphics.Color.rgb
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * @author 刘贺贺
 * @date 2023/8/23 9:03
 */
@Composable
fun HomeAppBar(
    page: Int, start: Boolean, navigation: () -> Unit
) {

    val navigationActions = LocalNavigationActions.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .background(Color.Transparent)
            .height(101.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 13.75.dp),
            horizontalArrangement = Arrangement.spacedBy(7.6.dp)
        ) {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
                if ("首页" != destination.text) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(42.03.dp)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                            .background(
                                if (page == destination.id) Color.White else Color(
                                    0,
                                    105,
                                    52
                                )
                            )
                            .clickable {
                                if (start) {
                                    Toast
                                        .makeText(
                                            context,
                                            "程序运行中,无法前往其他页面！",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                } else {
                                    navigationActions.navigateTo(destination)
                                }
                            }
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            color = if (page == destination.id) Color(
                                0,
                                105,
                                52
                            ) else Color.White,
                            text = destination.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .width(62.dp)
                .height(62.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-13.75).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable {
                    if (start) {
                        Toast
                            .makeText(
                                context,
                                "程序运行中,无法前往其他页面！",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    } else {
                        navigationActions.navigateTo(TOP_LEVEL_DESTINATIONS[4])
                    }
                }
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.mipmap.homebtn),
                contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[4].iconTextId)
            )
        }


    }


    val endTime = System.currentTimeMillis()


}


@Composable
fun DebugModeAppBar(
    page: Int, navigation: () -> Unit
) {

    val navigationActions = LocalNavigationActions.current

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
        Box(
            modifier = Modifier
                .background(Color.Transparent)
                .height(101.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 13.75.dp),
                horizontalArrangement = Arrangement.spacedBy(7.6.dp)
            ) {
                TOP_LEVEL_DESTINATIONS.forEach { destination ->
                    if ("首页" != destination.text) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(42.03.dp)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                .background(
                                    if (page == destination.id) Color.White else Color(
                                        0,
                                        105,
                                        52
                                    )
                                )
                                .clickable {
                                    navigationActions.navigateTo(destination)
                                }
                        ) {

                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                color = if (page == destination.id) Color(
                                    0,
                                    105,
                                    52
                                ) else Color.White,
                                text = destination.text,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(62.dp)
                    .height(62.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-13.75).dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable {
                        navigationActions.navigateTo(TOP_LEVEL_DESTINATIONS[4])
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center),
                    painter = painterResource(id = R.mipmap.homebtn),
                    contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[4].iconTextId)
                )
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    page: Int, dispatch: (SettingIntent) -> Unit, navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()

    TopAppBar(title = {
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
    }, actions = {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape
                )
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(visible = page == PageType.MOTOR_LIST) {
                ElevatedButton(onClick = { scope.launch { dispatch(SettingIntent.Insert) } }) {
                    Icon(
                        imageVector = Icons.Default.Add, contentDescription = null
                    )
                }
            }

            ElevatedButton(onClick = navigation) {
                Icon(
                    imageVector = Icons.Default.Reply, contentDescription = null
                )
            }
        }
    })
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
                        fontWeight = FontWeight.SemiBold, fontSize = 24.sp
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
                        text = program.displayText, style = TextStyle(
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
                        color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = page == PageType.PROGRAM_LIST) {
                    ElevatedButton(onClick = { dialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add, contentDescription = null
                        )
                    }
                }

                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply, contentDescription = null
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