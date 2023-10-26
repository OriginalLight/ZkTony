package com.zktony.android.ui

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationType
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.getGpio
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.launch

/**
 * Displays a splash screen with an animation and a message, and navigates to the home screen when the user clicks the "Done" button.
 *
 * @param modifier The modifier to apply to the splash screen.
 * @param navController The navigation controller to use for navigating to the home screen.
 * @param toggleDrawer The function to call to toggle the navigation drawer.
 */
@Composable
fun Splash(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    toggleDrawer: (NavigationType) -> Unit = {},
) {

    // Define the animation scale and splash state
    val scale = remember { Animatable(0f) }
    val splash = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()


    var spydjl by rememberDataSaverState(key = "spydjl", default = 0f);
    var xpydjl by rememberDataSaverState(key = "xpydjl", default = 0f);

    var fwgd by rememberDataSaverState(key = "fwgd", default = 0f);
    var fwgd2 by rememberDataSaverState(key = "fwgd2", default = 0f);

    var clickNum = 0;

    val valveOne = rememberDataSaverState(key = "valveOne", default = 0)
    var valveOne_ex by remember { mutableStateOf(0) }


    /**
     * 判断是否复位
     * true=复位完成
     * false=没复位
     */
    val isResetBool = rememberDataSaverState(key = "isResetBool", default = false)
    var isResetBool_ex by remember { mutableStateOf(false) }

    // Animate the splash screen and hide it when the animation is complete
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                })
        )
        splash.value = false
    }

    // Display the splash screen with the logo and message
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = splash.value) {
            Image(
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = "Logo",
                modifier = Modifier.scale(scale.value)
            )
        }

        AnimatedVisibility(visible = !splash.value) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.notice),
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 50.sp,
                    ),
                    fontFamily = FontFamily.Serif,
                )
                Text(
                    text = stringResource(id = R.string.notice_content),
                    style = TextStyle(
                        fontSize = 22.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    fontFamily = FontFamily.Serif,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.width(300.dp),
                        onClick = {
                            if (clickNum == 0) {
                                clickNum = 1;
                                scope.launch {

                                    val ids = listOf(1, 0, 2, 4, 5)
                                    tx {
                                        queryGpio(ids)
                                        delay = 300L
                                    }

                                    // 针对每个电机进行初始化
                                    ids.forEach {
                                        // 如果电机未初始化，则进行初始化
                                        if (!getGpio(it)) {
                                            // 进行电机初始化
                                            tx {
                                                timeout = 1000L * 60
                                                move(MoveType.MOVE_PULSE) {
                                                    index = it
                                                    pulse = 3200L * -30
                                                    ads = Triple(50L, 80L, 100L)
                                                }

                                            }
                                        }

                                        // 进行正向运动
                                        tx {
                                            timeout = 1000L * 10
                                            move(MoveType.MOVE_PULSE) {
                                                index = it
                                                pulse = 800L
                                                ads = Triple(50L, 80L, 100L)
                                            }
                                        }

                                        // 进行反向运动
                                        tx {
                                            timeout = 1000L * 15
                                            move(MoveType.MOVE_PULSE) {
                                                index = it
                                                pulse = 3200L * -3
                                                ads = Triple(50L, 80L, 100L)
                                            }
                                        }
                                    }

                                    //移动上盘到原点距离
                                    tx {
                                        move(MoveType.MOVE_PULSE) {
                                            index = 5
                                            pulse = (3200L * spydjl).toLong();
                                            ads = Triple(50L, 80L, 100L)
                                        }

                                        //移动下盘到原点距离
                                        move(MoveType.MOVE_PULSE) {
                                            index = 4
                                            pulse = (2599L * xpydjl).toLong();
                                            ads = Triple(50L, 80L, 100L)
                                        }

                                    }

                                    tx {
                                        //移动到复位高度
                                        move(MoveType.MOVE_PULSE) {
                                            index = 1
                                            pulse = (3200L * fwgd).toLong();
                                            ads = Triple(50L, 80L, 100L)
                                        }
                                        //移动到复位高度
                                        move(MoveType.MOVE_PULSE) {
                                            index = 0
                                            pulse = (3200L * fwgd2).toLong();
                                            ads = Triple(50L, 80L, 100L)
                                        }
                                    }
                                    valveOne.value = 0
                                    valveOne_ex = 0

                                    isResetBool.value = true
                                    isResetBool_ex = true
                                    toggleDrawer(NavigationType.NAVIGATION_RAIL)
                                    navController.popBackStack()
                                    navController.navigate(Route.HOME)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "复位",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Button(
                        modifier = Modifier.width(300.dp),
                        onClick = {
                            isResetBool.value = false
                            isResetBool_ex = false
                            toggleDrawer(NavigationType.NAVIGATION_RAIL)
                            navController.popBackStack()
                            navController.navigate(Route.CALIBRATION)
                        }
                    ) {
                        Text(
                            text = "进入校准管理",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}