package com.zktony.android.ui

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun Splash(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {

    val scale = remember { Animatable(0f) }
    val splash = remember { mutableStateOf(true) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
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
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = stringResource(id = R.string.notice_content),
                    style = TextStyle(
                        fontSize = 22.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    fontFamily = FontFamily.Serif
                )
                FloatingActionButton(
                    modifier = Modifier.width(192.dp),
                    onClick = {
                        navController.popBackStack()
                        navController.navigate(Route.Home)
                    }
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