package com.zktony.android.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Splash() {
    val navigationActions = LocalNavigationActions.current
    val scale = remember { Animatable(0f) }
    val splash = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()


    Image(
        painter = painterResource(id = R.mipmap.logo), contentDescription = null,
        modifier = Modifier
            .height(150.dp)
            .width(300.dp)
    )

    Row(modifier = Modifier.padding(top = 200.dp)) {
        Row(modifier = Modifier.padding(start = 100.dp)) {
            Image(
                painter = painterResource(id = R.mipmap.gulecz), contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        //制胶操作
                        navigationActions.navigate(Route.HOME)
                    }
            )
        }
        Row(modifier = Modifier.padding(start = 100.dp)) {
            Image(
                painter = painterResource(id = R.mipmap.gulecx), contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        //制胶程序
                        navigationActions.navigate(Route.PROGRAM)
                    }
            )
        }
    }


    Row(modifier = Modifier.padding(top = 350.dp)) {
        Text(
            text = "制胶操作",
            modifier = Modifier
                .padding(start = 140.dp),
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 32.sp,
            )
        )

        Text(
            text = "制胶程序",
            modifier = Modifier
                .padding(start = 150.dp),
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 32.sp,
            )
        )
    }

    Row(modifier = Modifier.padding(top = 500.dp)) {
        Row(modifier = Modifier.padding(start = 100.dp)) {
            Image(
                painter = painterResource(id = R.mipmap.syjl), contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        //实验记录
                        navigationActions.navigate(Route.EXPERIMENTRECORDS)
                    }
            )
        }
        Row(modifier = Modifier.padding(start = 100.dp)) {
            Image(
                painter = painterResource(id = R.mipmap.xtsz), contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        //系统设置
                        navigationActions.navigate(Route.SETTING)
                    }
            )
        }
    }


    Row(modifier = Modifier.padding(top = 650.dp)) {
        Text(
            text = "实验记录",
            modifier = Modifier
                .padding(start = 140.dp),
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 32.sp,
            )
        )

        Text(
            text = "系统设置",
            modifier = Modifier
                .padding(start = 150.dp),
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 32.sp,
            )
        )
    }

    Row(modifier = Modifier.padding(top = 800.dp)) {
        Row(modifier = Modifier.padding(start = 50.dp)) {
            Image(
                painter = painterResource(id = R.mipmap.qrcode), contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
            )
        }
        Row(modifier = Modifier.padding(start = 20.dp)) {
            Image(
                painter = painterResource(id = R.mipmap.sph), contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
            )
        }
        Row(modifier = Modifier.padding(start = 50.dp, top = 100.dp)) {
                Text(text = "Android:V1.0.0")
                Text(text = "C:V1.0.0")
        }
    }

}


