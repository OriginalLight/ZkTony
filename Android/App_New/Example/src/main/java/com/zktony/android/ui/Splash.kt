package com.zktony.android.ui

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationType

/**
 * @author 刘贺贺
 * @date 2023/5/17 10:50
 */

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
    var splash by remember { mutableStateOf(true) }

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
        splash = false
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
        AnimatedVisibility(visible = splash) {
            Image(
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = "Logo",
                modifier = Modifier.scale(scale.value)
            )
        }
        AnimatedVisibility(visible = !splash) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 16.dp),
                    text = stringResource(id = R.string.notice),
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 50.sp,
                    ),
                    fontFamily = FontFamily.Serif,
                )
                Text(
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 32.dp),
                    text = stringResource(id = R.string.notice_content),
                    style = TextStyle(
                        fontSize = 22.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    fontFamily = FontFamily.Serif,
                )
                FloatingActionButton(
                    modifier = Modifier.width(192.dp),
                    onClick = {
                        toggleDrawer(NavigationType.NAVIGATION_RAIL)
                        navController.popBackStack()
                        navController.navigate(Route.HOME)
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