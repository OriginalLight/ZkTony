package com.zktony.android.ui.screen

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R

/**
 * @author 刘贺贺
 * @date 2023/5/17 10:50
 */

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    val scale = remember { Animatable(0f) }
    var splash by remember { mutableStateOf(true) }
    var notice by remember { mutableStateOf(false) }

    // AnimationEffect
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                })
        )
        notice = true
    }

    // Image
    AnimatedVisibility(visible = splash) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(visible = !notice) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.scale(scale.value)
                )
            }
            AnimatedVisibility(visible = notice) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 16.dp),
                        text = stringResource(id = R.string.notice),
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 50.sp,
                        ),
                    )
                    Text(
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 32.dp),
                        text = stringResource(id = R.string.notice_content),
                        style = TextStyle(
                            fontSize = 24.sp,
                            lineHeight = 36.sp,
                        ),
                    )
                    Button(
                        modifier = Modifier.width(196.dp),
                        onClick = { splash = false },
                        elevation = ButtonDefaults.buttonElevation(4.dp),
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(visible = !splash) {
        content()
    }
}
