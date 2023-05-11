package com.zktony.android.ui.screen.setting

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.zktony.android.R
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.viewmodel.SettingPage

/**
 * Authentication page
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param navigationTo Function1<SettingPage, Unit>
 */
@Composable
fun AuthenticationPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationTo: (SettingPage) -> Unit = {},
) {
    BackHandler {
        navigationTo(SettingPage.SETTING)
    }

    var show by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.background,
                MaterialTheme.shapes.medium,
            )
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZkTonyTopAppBar(
            title = "",
            onBack = {
                navigationTo(SettingPage.SETTING)
            }
        )
        Spacer(modifier = Modifier.height(128.dp))
        AnimatedVisibility(visible = !show) {
            VerificationCodeField(digits = 6, inputCallback = {
                show = true
            }) { text, focused ->
                VerificationCodeItem(text, focused)
            }
        }
        AnimatedVisibility(visible = show) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    modifier = Modifier.clickable {
                        navigationTo(SettingPage.SETTING)
                        navController.navigate(Route.MOTOR)
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_motor),
                            contentDescription = stringResource(id = R.string.motor_config)
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.motor_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Card(
                    modifier = Modifier.clickable {
                        navigationTo(SettingPage.SETTING)
                        navController.navigate(Route.CONFIG)
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = stringResource(id = R.string.system_config)
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.system_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun AuthenticationPagePreview() {
    AuthenticationPage(navController = rememberNavController())
}