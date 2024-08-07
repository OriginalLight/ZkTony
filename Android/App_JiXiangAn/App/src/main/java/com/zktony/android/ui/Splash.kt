package com.zktony.android.ui

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.BuildConfig
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.R

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Splash(viewModel: HomeViewModel) {

    val navigationActions = LocalNavigationActions.current

    val initHintDialog by viewModel.initHintDialog.collectAsStateWithLifecycle()

    Box {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.mipmap.bkhome),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )

        Row(modifier = Modifier.padding(top = 200.dp)) {
            Row(modifier = Modifier.padding(start = 100.dp)) {
                Image(
                    painter = painterResource(id = R.mipmap.gulecz), contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {
                            //制胶操作
                            navigationActions.navigate(Route.HOME)
                        }
                )
            }
            Row(modifier = Modifier.padding(start = 20.dp)) {
                Image(
                    painter = painterResource(id = R.mipmap.gulecx), contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {
                            //制胶程序
                            navigationActions.navigate(Route.PROGRAM)
                        }
                )
            }
        }

        Row(modifier = Modifier.padding(top = 420.dp)) {
            Row(modifier = Modifier.padding(start = 100.dp)) {
                Image(
                    painter = painterResource(id = R.mipmap.syjl), contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {
                            //实验记录
                            navigationActions.navigate(Route.EXPERIMENTRECORDS)
                        }
                )
            }
            Row(modifier = Modifier.padding(start = 20.dp)) {
                Image(
                    painter = painterResource(id = R.mipmap.xtsz), contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {
                            //系统设置
                            navigationActions.navigate(Route.SETTING)
                        }
                )
            }
        }

        Row(modifier = Modifier.padding(top = 820.dp)) {
            Text(
                modifier = Modifier.padding(start = 350.dp),
                fontSize = 18.sp,
                color = Color.White,
                text = BuildConfig.VERSION_NAME

            )
        }

    }

    if (initHintDialog) {
        Dialog(onDismissRequest = {}) {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .padding(30.dp)
                            .width(600.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        text = "使  用  须  知",
                        color = Color.Red
                    )
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 10.dp),
                            fontSize = 25.sp,
                            lineHeight = 40.sp,
                            text = "1.实验前请检查混合针内是否有残胶，若有残胶，请先取下混合针清除残胶后再安装使用；",
                        )


                        Text(
                            modifier = Modifier.padding(top = 6.dp),
                            fontSize = 25.sp,
                            lineHeight = 40.sp,
                            text = "2.请正确配制高、低浓度母液，禁止在母液中加入促凝剂；",
                        )

                        Text(
                            modifier = Modifier.padding(top = 6.dp),
                            fontSize = 25.sp,
                            lineHeight = 40.sp,
                            text = "3.请确保冲洗液充足，可外接大容量试剂瓶；",
                        )

                        Text(
                            modifier = Modifier.padding(top = 6.dp),
                            fontSize = 25.sp,
                            lineHeight = 40.sp,
                            text = "4.开机第一次制胶前，务必先填充管路；",
                        )

                        Text(
                            modifier = Modifier.padding(top = 6.dp),
                            fontSize = 25.sp,
                            lineHeight = 40.sp,
                            text = "5.制胶前请确认管路被制胶试剂填充，如若没有需再次填充直至充满；",
                        )

                        Text(
                            modifier = Modifier.padding(top = 6.dp),
                            fontSize = 25.sp,
                            lineHeight = 40.sp,
                            text = "6.小心插入梳子，插入后请勿晃动梳子；",
                        )

                        Text(
                            modifier = Modifier.padding(top = 6.dp),
                            fontSize = 25.sp,
                            lineHeight = 40.sp,
                            text = "7.若发生管路堵塞，请立即停止使用，并联系售后工程师。",
                        )


                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        android.graphics.Color.rgb(
                                            0,
                                            105,
                                            52
                                        )
                                    )
                                ),
                                shape = RoundedCornerShape(0.dp),
                                onClick = {
                                    viewModel.dispatch(HomeIntent.InitHintDialog)
                                }) {
                                Text(fontSize = 25.sp, text = "关   闭")
                            }

                        }
                    }
                }
            }
        }
    }
}

